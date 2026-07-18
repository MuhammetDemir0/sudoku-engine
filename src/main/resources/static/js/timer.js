export class Timer {
    constructor(display) {
        this.display = display;
        this.startedAt = 0;
        this.intervalId = null;
        this.reset();
    }

    start() {
        this.stop();
        this.startedAt = Date.now();
        this.intervalId = window.setInterval(() => this.render(), 250);
        this.render();
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
        this.display.textContent = "00:00";
    }

    render() {
        const elapsedSeconds = Math.floor((Date.now() - this.startedAt) / 1000);
        const minutes = String(Math.floor(elapsedSeconds / 60)).padStart(2, "0");
        const seconds = String(elapsedSeconds % 60).padStart(2, "0");
        this.display.textContent = `${minutes}:${seconds}`;
    }
}
