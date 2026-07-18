import { generatePuzzle, requestHint, solvePuzzle, validateBoard } from "./api.js";
import { SudokuBoardView } from "./board.js";
import { LoadingIndicator, ToastCenter } from "./feedback.js";
import { Timer } from "./timer.js";
import { SolverVisualizer } from "./visualizer.js";

const boardView = new SudokuBoardView(document.getElementById("board"), onBoardChange);
const timer = new Timer(document.getElementById("timerDisplay"));
let visualizer;
let completionValidationRequest = 0;
let hasActivePuzzle = false;
let gameCompleted = false;
let hintCount = 0;
let gamePaused = false;
let pencilMode = false;

const elements = {
    difficulty: document.getElementById("difficulty"),
    generate: document.getElementById("generateBtn"),
    solve: document.getElementById("solveBtn"),
    hint: document.getElementById("hintBtn"),
    pauseGame: document.getElementById("pauseGameBtn"),
    pencil: document.getElementById("pencilBtn"),
    reset: document.getElementById("resetBtn"),
    clear: document.getElementById("clearBtn"),
    playVisualizer: document.getElementById("playVizBtn"),
    pauseVisualizer: document.getElementById("pauseVizBtn"),
    resetVisualizer: document.getElementById("resetVizBtn"),
    speed: document.getElementById("speedControl"),
    stepProgress: document.getElementById("stepProgress"),
    loadingOverlay: document.getElementById("loadingOverlay"),
    loadingText: document.getElementById("loadingText"),
    toastRegion: document.getElementById("toastRegion"),
    status: document.getElementById("statusText"),
    message: document.getElementById("messageText"),
    visitedNodes: document.getElementById("visitedNodes"),
    backtracks: document.getElementById("backtracks"),
    maxDepth: document.getElementById("maxDepth"),
    runtime: document.getElementById("runtime"),
    hintCount: document.getElementById("hintCount")
};

const loading = new LoadingIndicator(elements.loadingOverlay, elements.loadingText);
const toasts = new ToastCenter(elements.toastRegion);
visualizer = new SolverVisualizer(boardView, updateVisualizerState);
visualizer.setSpeed(elements.speed.value);

elements.generate.addEventListener("click", onGenerate);
elements.solve.addEventListener("click", onSolve);
elements.hint.addEventListener("click", onHint);
elements.pauseGame.addEventListener("click", onPauseGame);
elements.pencil.addEventListener("click", onTogglePencil);
elements.reset.addEventListener("click", onReset);
elements.clear.addEventListener("click", onClear);
elements.playVisualizer.addEventListener("click", onPlayVisualization);
elements.pauseVisualizer.addEventListener("click", onPauseVisualization);
elements.resetVisualizer.addEventListener("click", onResetVisualization);
elements.speed.addEventListener("input", () => visualizer.setSpeed(elements.speed.value));

async function onGenerate() {
    await run("Loading new game", async () => {
        invalidateCompletionValidation();
        visualizer.clear();
        boardView.clear();
        resetMetrics();
        resetHintCount();
        setGamePaused(false);
        setGameCompleted(false);
        timer.reset();
        setMessage("Loading a new puzzle.", "loading");

        const response = await generatePuzzle(elements.difficulty.value);
        boardView.loadPuzzle(response.puzzle);
        hasActivePuzzle = true;
        timer.start();
        setMessage(`${response.difficulty} puzzle ready.`, "success");
    });
}

async function onSolve() {
    await run("Solving", async () => {
        const startBoard = boardView.read();
        const response = await solvePuzzle(startBoard, true, "MRV");
        updateMetrics(response.metrics);
        if (!response.solved) {
            setMessage("This board could not be solved.", "error");
            return;
        }

        visualizer.load(response.steps, response.board, startBoard);
        setGameCompleted(false);
        setMessage("Solver steps loaded. Press Play to animate.", "success");
    });
}

async function onPlayVisualization() {
    const completed = await visualizer.play();
    if (completed) {
        timer.stop();
        setGameCompleted(true);
        setMessage("Solved.", "success");
    }
}

function onPauseVisualization() {
    visualizer.pause();
    setMessage("Visualization paused.");
}

function onResetVisualization() {
    visualizer.reset();
    setGameCompleted(false);
    setMessage("Visualization reset.");
}

async function onHint() {
    await run("Finding hint", async () => {
        const response = await requestHint(boardView.read());
        if (response === null) {
            timer.stop();
            setGameCompleted(true);
            setMessage("The board is already complete.", "success");
            return;
        }
        boardView.markHint(response.row, response.col, response.value);
        incrementHintCount();
        setMessage(`${response.reason} Hint ${hintCount} used.`);
    });
}

async function onBoardChange(state) {
    if (gamePaused) {
        return;
    }

    completionValidationRequest++;

    if (state.conflicts.length > 0) {
        setStatus("Check conflicts");
        setMessage(formatConflictSummary(state.conflicts), "error");
        return;
    }

    setStatus("Ready");
    if (!state.complete) {
        setMessage("No conflicts found.");
        return;
    }

    const requestId = completionValidationRequest;
    setStatus("Verifying");
    setMessage("Board is complete. Verifying with the server.", "loading");
    loading.show("Verifying board");

    try {
        const response = await validateBoard(state.board);
        if (requestId !== completionValidationRequest) {
            return;
        }

        if (response.valid) {
            timer.stop();
            setStatus("Ready");
            setGameCompleted(true);
            setMessage("Completed board verified by the server.", "success");
            return;
        }

        boardView.markInvalid(response.violations);
        setStatus("Check conflicts");
        setMessage(`${response.violations.length} server violation(s) found.`, "error");
    } catch (error) {
        if (requestId === completionValidationRequest) {
            setStatus("Error");
            setMessage(error.message, "error");
        }
    } finally {
        if (requestId === completionValidationRequest) {
            loading.hide();
        }
    }
}

function onReset() {
    invalidateCompletionValidation();
    visualizer.clear();
    setGamePaused(false);
    if (!hasActivePuzzle) {
        boardView.clear();
        resetMetrics();
        timer.reset();
        setStatus("Ready");
        setMessage("No puzzle to reset.");
        return;
    }

    boardView.reset();
    resetMetrics();
    resetHintCount();
    setGameCompleted(false);
    timer.reset();
    timer.start();
    setStatus("Ready");
    setMessage("Puzzle reset.");
}

function onClear() {
    invalidateCompletionValidation();
    visualizer.clear();
    setGamePaused(false);
    boardView.clear();
    hasActivePuzzle = false;
    resetMetrics();
    resetHintCount();
    setGameCompleted(false);
    timer.reset();
    setStatus("Ready");
    setMessage("Board cleared.");
}

function onPauseGame() {
    if (!hasActivePuzzle || gameCompleted) {
        return;
    }
    setGamePaused(!gamePaused);
}

function onTogglePencil() {
    pencilMode = !pencilMode;
    boardView.setPencilMode(pencilMode);
    elements.pencil.setAttribute("aria-pressed", String(pencilMode));
    elements.pencil.classList.toggle("active", pencilMode);
    setMessage(pencilMode ? "Pencil mode enabled." : "Pencil mode disabled.");
}

function invalidateCompletionValidation() {
    completionValidationRequest++;
}

async function run(status, action) {
    setBusy(true);
    setStatus(status);
    loading.show(status);
    boardView.clearMarks();
    try {
        await action();
        setStatus("Ready");
    } catch (error) {
        setStatus("Error");
        setMessage(error.message, "error");
    } finally {
        loading.hide();
        setBusy(false);
    }
}

function setBusy(isBusy) {
    Object.values(elements)
        .filter(element => element instanceof HTMLButtonElement || element instanceof HTMLSelectElement)
        .forEach(element => {
            element.disabled = isBusy;
        });
    updateControlAvailability(isBusy);
}

function setStatus(value) {
    elements.status.textContent = value;
}

function setMessage(value, state = "info") {
    elements.message.textContent = value;
    elements.message.dataset.state = state;
    if (state === "error" || state === "success") {
        toasts.show(value, state);
    }
}

function setGamePaused(isPaused) {
    gamePaused = isPaused;
    boardView.setPaused(isPaused);
    if (isPaused) {
        visualizer.pause();
        timer.pause();
        elements.pauseGame.textContent = "Resume Game";
        setStatus("Paused");
        setMessage("Game paused.");
    } else {
        if (hasActivePuzzle && !gameCompleted && !timer.isRunning()) {
            timer.resume();
        }
        elements.pauseGame.textContent = "Pause Game";
        setStatus("Ready");
    }
    updateControlAvailability(false);
}

function updateControlAvailability(isBusy = false) {
    elements.hint.disabled = isBusy || gameCompleted || gamePaused;
    elements.solve.disabled = isBusy || gamePaused;
    elements.pencil.disabled = isBusy || gameCompleted || gamePaused;
    elements.pauseGame.disabled = isBusy || !hasActivePuzzle || gameCompleted;
    elements.playVisualizer.disabled = isBusy || !visualizer.hasSteps() || gameCompleted || gamePaused;
    elements.pauseVisualizer.disabled = isBusy || !visualizer.hasSteps();
    elements.resetVisualizer.disabled = isBusy || !visualizer.hasSteps() || gamePaused;
}

function updateMetrics(metrics) {
    if (!metrics) {
        resetMetrics();
        return;
    }
    elements.visitedNodes.textContent = String(metrics.visitedNodes);
    elements.backtracks.textContent = String(metrics.backtracks);
    elements.maxDepth.textContent = String(metrics.maxRecursionDepth);
    elements.runtime.textContent = formatNanos(metrics.elapsedNanos);
}

function resetMetrics() {
    elements.visitedNodes.textContent = "-";
    elements.backtracks.textContent = "-";
    elements.maxDepth.textContent = "-";
    elements.runtime.textContent = "-";
}

function incrementHintCount() {
    hintCount++;
    elements.hintCount.textContent = String(hintCount);
}

function resetHintCount() {
    hintCount = 0;
    elements.hintCount.textContent = "0";
}

function setGameCompleted(isCompleted) {
    gameCompleted = isCompleted;
    if (isCompleted) {
        setGamePaused(false);
    }
    elements.hint.disabled = isCompleted;
    elements.playVisualizer.disabled = isCompleted || !visualizer.hasSteps();
    updateControlAvailability(false);
}

function formatNanos(nanos) {
    if (typeof nanos !== "number") {
        return "-";
    }
    return `${(nanos / 1_000_000).toFixed(2)} ms`;
}

function formatConflictSummary(conflicts) {
    const counts = conflicts.reduce((summary, conflict) => {
        summary[conflict.type] = (summary[conflict.type] || 0) + 1;
        return summary;
    }, {});
    return Object.entries(counts)
        .map(([type, count]) => `${count} ${type}`)
        .join(", ") + " conflict(s) found.";
}

function updateVisualizerState(state) {
    elements.stepProgress.textContent = state.loaded
        ? `Step ${state.index} of ${state.total}`
        : "No steps loaded.";
    elements.playVisualizer.disabled = !state.loaded || state.playing || gameCompleted || gamePaused;
    elements.pauseVisualizer.disabled = !state.loaded || !state.playing;
    elements.resetVisualizer.disabled = !state.loaded || state.playing || gamePaused;
}
