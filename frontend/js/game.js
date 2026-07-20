import { generatePuzzle, requestHint, validateBoard } from "./api.js";
import { SudokuBoardView } from "./board.js";
import { LoadingIndicator, ToastCenter } from "./feedback.js";
import { Timer } from "./timer.js";

const boardView = new SudokuBoardView(document.getElementById("board"), onBoardChange);
const timer = new Timer(document.getElementById("timerDisplay"));
let completionValidationRequest = 0;
let hasActivePuzzle = false;
let gameCompleted = false;
let hintCount = 0;
let mistakeCount = 0;
let gamePaused = false;
let pencilMode = false;
let moveHistory = [];
const MAX_MISTAKES = 3;

const elements = {
    difficulty: document.getElementById("difficulty"),
    difficultyButtons: Array.from(document.querySelectorAll(".difficulty-tab")),
    generate: document.getElementById("generateBtn"),
    hint: document.getElementById("hintBtn"),
    pauseGame: document.getElementById("pauseGameBtn"),
    pencil: document.getElementById("pencilBtn"),
    pencilState: document.getElementById("pencilState"),
    undo: document.getElementById("undoBtn"),
    reset: document.getElementById("resetBtn"),
    clear: document.getElementById("clearBtn"),
    numberPad: document.getElementById("numberPad"),
    loadingOverlay: document.getElementById("loadingOverlay"),
    loadingText: document.getElementById("loadingText"),
    toastRegion: document.getElementById("toastRegion"),
    status: document.getElementById("statusText"),
    message: document.getElementById("messageText"),
    mistakeCount: document.getElementById("mistakeCount"),
    hintCount: document.getElementById("hintCount")
};

const loading = new LoadingIndicator(elements.loadingOverlay, elements.loadingText);
const toasts = new ToastCenter(elements.toastRegion);

elements.generate.addEventListener("click", onGenerate);
elements.hint.addEventListener("click", onHint);
elements.pauseGame.addEventListener("click", onPauseGame);
elements.pencil.addEventListener("click", onTogglePencil);
elements.undo.addEventListener("click", onUndo);
elements.reset.addEventListener("click", onReset);
elements.clear.addEventListener("click", onClearSelected);
elements.difficultyButtons.forEach(button => button.addEventListener("click", () => selectDifficulty(button)));
elements.numberPad.addEventListener("click", onNumberPadClick);
updateNumberCounts();

async function onGenerate() {
    await run("Loading new game", async () => {
        invalidateCompletionValidation();
        boardView.clear();
        updateNumberCounts();
        resetMistakes();
        resetHintCount();
        resetHistory();
        setPencilMode(false);
        setGamePaused(false);
        setGameCompleted(false);
        timer.reset();
        setMessage("Loading a new puzzle.", "loading");

        const response = await generatePuzzle(elements.difficulty.value);
        boardView.loadPuzzle(response.puzzle);
        updateNumberCounts();
        hasActivePuzzle = true;
        timer.start();
        setMessage(`${response.difficulty} puzzle ready.`, "success");
    });
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
        updateNumberCounts();
        incrementHintCount();
        setMessage(`${response.reason} Hint ${hintCount} used.`);
    });
}

async function onBoardChange(state) {
    if (gamePaused) {
        return;
    }

    completionValidationRequest++;
    updateNumberCounts(state.board);

    if (state.conflicts.length > 0) {
        rememberMove(state.move);
        recordMistake(state.move);
        setStatus("Check conflicts");
        if (mistakeCount >= MAX_MISTAKES) {
            endGameAfterMistakes();
            return;
        }
        setMessage(`${formatConflictSummary(state.conflicts)} Mistakes ${mistakeCount}/${MAX_MISTAKES}.`, "error");
        return;
    }

    setStatus("Ready");
    rememberMove(state.move);
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
    setGamePaused(false);
    if (!hasActivePuzzle) {
        boardView.clear();
        updateNumberCounts();
        resetMistakes();
        timer.reset();
        setStatus("Ready");
        setMessage("No puzzle to reset.");
        return;
    }

    boardView.reset();
    updateNumberCounts();
    resetMistakes();
    resetHintCount();
    resetHistory();
    setPencilMode(false);
    setGameCompleted(false);
    timer.reset();
    timer.start();
    setStatus("Ready");
    setMessage("Puzzle reset.");
}

function onClear() {
    invalidateCompletionValidation();
    setGamePaused(false);
    boardView.clear();
    updateNumberCounts();
    hasActivePuzzle = false;
    resetMistakes();
    resetHintCount();
    resetHistory();
    setPencilMode(false);
    setGameCompleted(false);
    timer.reset();
    setStatus("Ready");
    setMessage("Board cleared.");
}

function onClearSelected() {
    boardView.clearSelectedValue();
}

function onUndo() {
    const lastMove = moveHistory.pop();
    if (!lastMove) {
        setMessage("No move to undo.");
        return;
    }
    const cell = boardView.cellAt(lastMove.row, lastMove.col);
    if (!cell) {
        return;
    }
    boardView.setCellValue(cell, lastMove.row, lastMove.col, lastMove.previous, "undo");
    updateNumberCounts();
    setMessage("Last move undone.");
}

function onNumberPadClick(event) {
    const button = event.target.closest("button[data-value]");
    if (!button) {
        return;
    }
    boardView.inputSelectedValue(Number(button.dataset.value));
}

function selectDifficulty(button) {
    elements.difficulty.value = button.dataset.difficulty;
    elements.difficultyButtons.forEach(tab => {
        const selected = tab === button;
        tab.classList.toggle("active", selected);
        tab.setAttribute("aria-pressed", String(selected));
    });
}

function onPauseGame() {
    if (!hasActivePuzzle || gameCompleted) {
        return;
    }
    setGamePaused(!gamePaused);
}

function onTogglePencil() {
    setPencilMode(!pencilMode);
    setMessage(pencilMode ? "Pencil notes on." : "Pencil notes off.");
}

function setPencilMode(enabled) {
    pencilMode = enabled;
    boardView.setPencilMode(pencilMode);
    elements.pencil.setAttribute("aria-pressed", String(pencilMode));
    elements.pencil.classList.toggle("active", pencilMode);
    elements.pencilState.textContent = pencilMode ? "On" : "Off";
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
    elements.numberPad.classList.toggle("is-paused", isPaused);
    elements.pauseGame.classList.toggle("is-resume", isPaused);
    if (isPaused) {
        timer.pause();
        elements.pauseGame.textContent = "";
        elements.pauseGame.setAttribute("aria-label", "Resume game");
        setStatus("Paused");
        setMessage("Game paused.");
    } else {
        if (hasActivePuzzle && !gameCompleted && !timer.isRunning()) {
            timer.resume();
        }
        elements.pauseGame.textContent = "";
        elements.pauseGame.setAttribute("aria-label", "Pause game");
        setStatus("Ready");
    }
    updateControlAvailability(false);
}

function updateControlAvailability(isBusy = false) {
    elements.hint.disabled = isBusy || gameCompleted || gamePaused;
    elements.pencil.disabled = isBusy || gameCompleted || gamePaused;
    elements.pauseGame.disabled = isBusy || !hasActivePuzzle || gameCompleted;
    elements.undo.disabled = isBusy || gameCompleted || gamePaused || moveHistory.length === 0;
    elements.clear.disabled = isBusy || gameCompleted || gamePaused;
    elements.numberPad.querySelectorAll("button").forEach(button => {
        button.disabled = isBusy || gameCompleted || gamePaused;
    });
}

function updateNumberCounts(board = boardView.read()) {
    const usedCounts = Array(10).fill(0);
    board.flat().forEach(value => {
        if (value >= 1 && value <= 9) {
            usedCounts[value]++;
        }
    });

    elements.numberPad.querySelectorAll("button[data-value]").forEach(button => {
        const value = Number(button.dataset.value);
        const remaining = Math.max(0, 9 - usedCounts[value]);
        const count = button.querySelector(".remaining-count");
        if (count) {
            count.textContent = String(remaining);
        }
        button.classList.toggle("is-complete", remaining === 0);
    });
}

function rememberMove(move) {
    if (!move || move.source !== "value" || move.previous === move.value) {
        updateControlAvailability(false);
        return;
    }
    moveHistory.push(move);
    updateControlAvailability(false);
}

function resetHistory() {
    moveHistory = [];
    updateControlAvailability(false);
}

function recordMistake(move) {
    if (!move || move.source !== "value" || move.value === 0) {
        return;
    }
    mistakeCount = Math.min(MAX_MISTAKES, mistakeCount + 1);
    elements.mistakeCount.textContent = `${mistakeCount}/${MAX_MISTAKES}`;
}

function resetMistakes() {
    mistakeCount = 0;
    elements.mistakeCount.textContent = `0/${MAX_MISTAKES}`;
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
    boardView.setLocked(isCompleted);
    elements.hint.disabled = isCompleted;
    updateControlAvailability(false);
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

function endGameAfterMistakes() {
    timer.stop();
    setGameCompleted(true);
    setStatus("Game over");
    setMessage(`Three mistakes used. Start a new game or reset this puzzle.`, "error");
}
