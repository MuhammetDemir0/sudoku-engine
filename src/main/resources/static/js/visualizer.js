const BASE_DELAY_MS = 360;

export class SolverVisualizer {
    constructor(boardView, onStateChange = () => {}) {
        this.boardView = boardView;
        this.onStateChange = onStateChange;
        this.timeoutId = null;
        this.steps = [];
        this.finalBoard = null;
        this.startBoard = null;
        this.index = 0;
        this.delayMs = BASE_DELAY_MS / 3;
        this.playing = false;
        this.resolvePlayback = null;
    }

    load(steps, finalBoard, startBoard) {
        this.stop();
        this.steps = Array.isArray(steps) ? steps : [];
        this.finalBoard = finalBoard;
        this.startBoard = startBoard;
        this.index = 0;
        this.resetBoardToStart();
        this.emitState();
    }

    play() {
        if (this.playing) {
            return Promise.resolve();
        }

        if (this.steps.length === 0) {
            this.finish();
            return Promise.resolve();
        }

        this.playing = true;
        this.emitState();

        return new Promise(resolve => {
            this.resolvePlayback = resolve;
            this.scheduleNextStep();
        });
    }

    pause() {
        this.clearTimer();
        this.playing = false;
        this.emitState();
    }

    reset() {
        this.stop();
        this.index = 0;
        this.resetBoardToStart();
        this.emitState();
    }

    clear() {
        this.stop();
        this.steps = [];
        this.finalBoard = null;
        this.startBoard = null;
        this.index = 0;
        clearVisualClasses(this.boardView);
        this.emitState();
    }

    stop() {
        this.clearTimer();
        this.playing = false;
        if (this.resolvePlayback) {
            this.resolvePlayback(false);
            this.resolvePlayback = null;
        }
        this.emitState();
    }

    setSpeed(value) {
        const speed = Number(value) || 3;
        this.delayMs = BASE_DELAY_MS / Math.max(1, speed);
    }

    hasSteps() {
        return this.steps.length > 0;
    }

    scheduleNextStep() {
        this.timeoutId = window.setTimeout(() => {
            this.timeoutId = null;
            this.applyNextStep();
        }, this.delayMs);
    }

    applyNextStep() {
        if (!this.playing) {
            return;
        }

        const step = this.steps[this.index];
        applyStep(this.boardView, step);
        this.index += 1;
        this.emitState();

        if (this.index >= this.steps.length) {
            this.finish();
            return;
        }

        this.scheduleNextStep();
    }

    finish() {
        this.clearTimer();
        this.playing = false;
        if (this.finalBoard) {
            this.boardView.write(this.finalBoard, "solved");
        }
        if (this.resolvePlayback) {
            this.resolvePlayback(true);
            this.resolvePlayback = null;
        }
        this.emitState();
    }

    resetBoardToStart() {
        if (this.startBoard) {
            this.boardView.clearMarks();
            this.boardView.write(this.startBoard);
        }
        clearVisualClasses(this.boardView);
    }

    clearTimer() {
        if (this.timeoutId !== null) {
            window.clearTimeout(this.timeoutId);
            this.timeoutId = null;
        }
    }

    emitState() {
        this.onStateChange({
            index: this.index,
            total: this.steps.length,
            playing: this.playing,
            loaded: this.finalBoard !== null
        });
    }
}

function applyStep(boardView, step) {
    clearVisualClasses(boardView);
    if (!step || step.row < 0 || step.col < 0) {
        return;
    }

    const cell = boardView.cellAt(step.row, step.col);
    if (!cell || cell.classList.contains("given")) {
        return;
    }

    if (step.type === "REMOVE_VALUE") {
        cell.value = "";
        cell.classList.remove("solved");
        cell.classList.add("visual-remove");
        return;
    }

    if (step.type === "PLACE_VALUE") {
        cell.value = String(step.value);
        cell.classList.add("visual-place", "solved");
    }
}

function clearVisualClasses(boardView) {
    boardView.cells().forEach(cell => {
        cell.classList.remove("visual-place", "visual-remove");
    });
}
