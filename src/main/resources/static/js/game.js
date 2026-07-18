import { generatePuzzle, requestHint, solvePuzzle, validateBoard } from "./api.js";
import { SudokuBoardView } from "./board.js";
import { Timer } from "./timer.js";
import { SolverVisualizer } from "./visualizer.js";

const boardView = new SudokuBoardView(document.getElementById("board"));
const timer = new Timer(document.getElementById("timerDisplay"));
const visualizer = new SolverVisualizer(boardView);

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
    await run("Generating", async () => {
        const response = await generatePuzzle(elements.difficulty.value);
        boardView.loadPuzzle(response.puzzle);
        resetMetrics();
        timer.reset();
        timer.start();
        setMessage(`${response.difficulty} puzzle ready.`);
    });
}

async function onSolve() {
    await run("Solving", async () => {
        const response = await solvePuzzle(boardView.read(), true, "MRV");
        updateMetrics(response.metrics);
        if (!response.solved) {
            setMessage("This board could not be solved.");
            return;
        }

        await visualizer.play(response.steps, response.board);
        timer.stop();
        setMessage("Solved.");
    });
}

async function onHint() {
    await run("Finding hint", async () => {
        const response = await requestHint(boardView.read());
        if (response === null) {
            setMessage("The board is already complete.");
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
            setMessage("Board is valid.");
            return;
        }
        boardView.markInvalid(response.violations);
        setMessage(`${response.violations.length} violation(s) found.`);
    });
}

function onReset() {
    visualizer.stop();
    boardView.reset();
    resetMetrics();
    timer.reset();
    timer.start();
    setStatus("Ready");
    setMessage("Puzzle reset.");
}

function onClear() {
    visualizer.stop();
    boardView.clear();
    resetMetrics();
    timer.reset();
    setStatus("Ready");
    setMessage("Board cleared.");
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
        setMessage(error.message);
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

function setMessage(value) {
    elements.message.textContent = value;
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
