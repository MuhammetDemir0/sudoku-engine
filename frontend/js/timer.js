export class Timer {
    constructor(display) {
        this.display = display;
        this.startedAt = 0;
        this.elapsedBeforePause = 0;
        this.intervalId = null;
        this.reset();
    }

    start() {
        this.stop();
        this.elapsedBeforePause = 0;
        this.startedAt = Date.now();
        this.intervalId = window.setInterval(() => this.render(), 250);
        this.render();
    }

    resume() {
        if (this.intervalId !== null) {
            return;
        }
        this.startedAt = Date.now() - this.elapsedBeforePause;
        this.intervalId = window.setInterval(() => this.render(), 250);
        this.render();
    }

    pause() {
        if (this.intervalId === null) {
            return;
        }
        this.elapsedBeforePause = Date.now() - this.startedAt;
        this.stop();
        this.renderElapsed(this.elapsedBeforePause);
    }

    stop() {
        if (this.intervalId !== null) {
            window.clearInterval(this.intervalId);
            this.intervalId = null;
        }
    }

    reset() {
        this.stop();
        this.startedAt = 0;
        this.elapsedBeforePause = 0;
        this.display.textContent = "00:00";
    }

    isRunning() {
        return this.intervalId !== null;
    }

    render() {
        this.renderElapsed(Date.now() - this.startedAt);
    }

    renderElapsed(elapsedMillis) {
        const elapsedSeconds = Math.floor(elapsedMillis / 1000);
        const minutes = String(Math.floor(elapsedSeconds / 60)).padStart(2, "0");
        const seconds = String(elapsedSeconds % 60).padStart(2, "0");
        this.display.textContent = `${minutes}:${seconds}`;
    }
}
