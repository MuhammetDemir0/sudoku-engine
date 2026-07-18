import { generatePuzzle, requestHint, solvePuzzle, validateBoard } from "./api.js";
import { SudokuBoardView } from "./board.js";
import { Timer } from "./timer.js";
import { SolverVisualizer } from "./visualizer.js";

const boardView = new SudokuBoardView(document.getElementById("board"), onBoardChange);
const timer = new Timer(document.getElementById("timerDisplay"));
const visualizer = new SolverVisualizer(boardView);
let completionValidationRequest = 0;
let hasActivePuzzle = false;

const elements = {
    difficulty: document.getElementById("difficulty"),
    generate: document.getElementById("generateBtn"),
    solve: document.getElementById("solveBtn"),
    hint: document.getElementById("hintBtn"),
    validate: document.getElementById("validateBtn"),
    reset: document.getElementById("resetBtn"),
    clear: document.getElementById("clearBtn"),
    status: document.getElementById("statusText"),
    message: document.getElementById("messageText"),
    visitedNodes: document.getElementById("visitedNodes"),
    backtracks: document.getElementById("backtracks"),
    maxDepth: document.getElementById("maxDepth"),
    runtime: document.getElementById("runtime")
};

elements.generate.addEventListener("click", onGenerate);
elements.solve.addEventListener("click", onSolve);
elements.hint.addEventListener("click", onHint);
elements.validate.addEventListener("click", onValidate);
elements.reset.addEventListener("click", onReset);
elements.clear.addEventListener("click", onClear);

async function onGenerate() {
    await run("Loading new game", async () => {
        invalidateCompletionValidation();
        visualizer.stop();
        boardView.clear();
        resetMetrics();
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
        const response = await solvePuzzle(boardView.read(), true, "MRV");
        updateMetrics(response.metrics);
        if (!response.solved) {
            setMessage("This board could not be solved.", "error");
            return;
        }

        await visualizer.play(response.steps, response.board);
        timer.stop();
        setMessage("Solved.", "success");
    });
}

async function onHint() {
    await run("Finding hint", async () => {
        const response = await requestHint(boardView.read());
        if (response === null) {
            setMessage("The board is already complete.", "success");
            return;
        }
        boardView.markHint(response.row, response.col, response.value);
        setMessage(response.reason);
    });
}

async function onValidate() {
    await run("Validating", async () => {
        const response = await validateBoard(boardView.read());
        if (response.valid) {
            boardView.clearMarks();
            setMessage("Board is valid.", "success");
            return;
        }
        boardView.markInvalid(response.violations);
        setMessage(`${response.violations.length} violation(s) found.`, "error");
    });
}

async function onBoardChange(state) {
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

    try {
        const response = await validateBoard(state.board);
        if (requestId !== completionValidationRequest) {
            return;
        }

        if (response.valid) {
            timer.stop();
            setStatus("Ready");
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
    }
}

function onReset() {
    invalidateCompletionValidation();
    visualizer.stop();
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
    timer.reset();
    timer.start();
    setStatus("Ready");
    setMessage("Puzzle reset.");
}

function onClear() {
    invalidateCompletionValidation();
    visualizer.stop();
    boardView.clear();
    hasActivePuzzle = false;
    resetMetrics();
    timer.reset();
    setStatus("Ready");
    setMessage("Board cleared.");
}

function invalidateCompletionValidation() {
    completionValidationRequest++;
}

async function run(status, action) {
    setBusy(true);
    setStatus(status);
    boardView.clearMarks();
    try {
        await action();
        setStatus("Ready");
    } catch (error) {
        setStatus("Error");
        setMessage(error.message, "error");
    } finally {
        setBusy(false);
    }
}

function setBusy(isBusy) {
    Object.values(elements)
        .filter(element => element instanceof HTMLButtonElement || element instanceof HTMLSelectElement)
        .forEach(element => {
            element.disabled = isBusy;
        });
}

function setStatus(value) {
    elements.status.textContent = value;
}

function setMessage(value, state = "info") {
    elements.message.textContent = value;
    elements.message.dataset.state = state;
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
