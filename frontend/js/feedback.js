export class LoadingIndicator {
    constructor(overlay, label) {
        this.overlay = overlay;
        this.label = label;
    }

    show(message) {
        this.label.textContent = message;
        this.overlay.hidden = false;
    }

    hide() {
        this.overlay.hidden = true;
    }
}

export class ToastCenter {
    constructor(region) {
        this.region = region;
    }

    show(message, state = "info") {
        const toast = document.createElement("div");
        toast.className = "toast";
        toast.dataset.state = state;
        toast.textContent = message;
        this.region.appendChild(toast);

        window.setTimeout(() => {
            toast.remove();
        }, 4200);
    }
}
