const STEP_DELAY_MS = 20;

export class SolverVisualizer {
    constructor(boardView) {
        this.boardView = boardView;
        this.timeoutId = null;
    }

    stop() {
        if (this.timeoutId !== null) {
            window.clearTimeout(this.timeoutId);
            this.timeoutId = null;
        }
    }

    play(steps, finalBoard) {
        this.stop();
        if (!Array.isArray(steps) || steps.length === 0) {
            this.boardView.write(finalBoard, "solved");
            return Promise.resolve();
        }

        return new Promise(resolve => {
            let index = 0;
            const tick = () => {
                const step = steps[index];
                applyStep(this.boardView, step);
                index += 1;

                if (index >= steps.length) {
                    this.boardView.write(finalBoard, "solved");
                    this.timeoutId = null;
                    resolve();
                    return;
                }
                this.timeoutId = window.setTimeout(tick, STEP_DELAY_MS);
            };
            tick();
        });
    }
}

function applyStep(boardView, step) {
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
        return;
    }

    if (step.type === "PLACE_VALUE") {
        cell.value = String(step.value);
        cell.classList.add("solved");
    }
}
