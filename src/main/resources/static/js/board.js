const SIZE = 9;
const EMPTY = 0;

export class SudokuBoardView {
    constructor(container) {
        this.container = container;
        this.initialBoard = emptyBoard();
        this.render(this.initialBoard);
    }

    render(board, options = {}) {
        this.container.innerHTML = "";
        const givens = options.givens || board;

        for (let row = 0; row < SIZE; row++) {
            for (let col = 0; col < SIZE; col++) {
                const input = document.createElement("input");
                input.className = "cell";
                input.type = "text";
                input.inputMode = "numeric";
                input.autocomplete = "off";
                input.maxLength = 1;
                input.pattern = "[1-9]";
                input.ariaLabel = `Row ${row + 1}, column ${col + 1}`;
                input.dataset.row = String(row);
                input.dataset.col = String(col);

                const value = board[row][col];
                if (value !== EMPTY) {
                    input.value = String(value);
                }

                if (givens[row][col] !== EMPTY) {
                    input.classList.add("given");
                    input.readOnly = true;
                }

                input.addEventListener("input", () => normalizeCell(input));
                input.addEventListener("keydown", event => handleCellKeyDown(event, this.container));
                input.addEventListener("focus", () => highlightPeers(input, this.container));
                input.addEventListener("blur", () => clearSelection(this.container));
                this.container.appendChild(input);
            }
        }
    }

    loadPuzzle(board) {
        this.initialBoard = cloneBoard(board);
        this.render(board, { givens: board });
    }

    reset() {
        this.render(this.initialBoard, { givens: this.initialBoard });
    }

    clear() {
        this.initialBoard = emptyBoard();
        this.render(this.initialBoard);
    }

    read() {
        const board = emptyBoard();
        this.cells().forEach(cell => {
            const row = Number(cell.dataset.row);
            const col = Number(cell.dataset.col);
            board[row][col] = toCellValue(cell.value);
        });
        return board;
    }

    write(board, className = "") {
        this.cells().forEach(cell => {
            const row = Number(cell.dataset.row);
            const col = Number(cell.dataset.col);
            const value = board[row][col];
            if (!cell.classList.contains("given")) {
                cell.value = value === EMPTY ? "" : String(value);
                if (className) {
                    cell.classList.add(className);
                }
            }
        });
    }

    markHint(row, col, value) {
        this.clearMarks();
        const cell = this.cellAt(row, col);
        if (cell) {
            cell.value = String(value);
            cell.classList.add("hint");
            cell.focus();
        }
    }

    markInvalid(violations) {
        this.clearMarks();
        violations.forEach(violation => {
            const cell = this.cellAt(violation.row, violation.col);
            if (cell) {
                cell.classList.add("invalid");
            }
        });
    }

    clearMarks() {
        this.cells().forEach(cell => {
            cell.classList.remove("hint", "invalid", "solved");
        });
    }

    cellAt(row, col) {
        return this.container.querySelector(`[data-row="${row}"][data-col="${col}"]`);
    }

    cells() {
        return Array.from(this.container.querySelectorAll(".cell"));
    }
}

export function emptyBoard() {
    return Array.from({ length: SIZE }, () => Array(SIZE).fill(EMPTY));
}

export function cloneBoard(board) {
    return board.map(row => row.slice());
}

function normalizeCell(input) {
    if (input.readOnly) {
        return;
    }

    const match = input.value.match(/[1-9]/);
    input.value = match ? match[0] : "";
    input.classList.remove("hint", "invalid", "solved");
}

function toCellValue(value) {
    return /^[1-9]$/.test(value) ? Number(value) : EMPTY;
}

function handleCellKeyDown(event, container) {
    if (event.key.startsWith("Arrow")) {
        moveFocus(event, container);
        return;
    }

    const cell = event.currentTarget;
    const isEditingKey = event.key.length === 1 || event.key === "Backspace" || event.key === "Delete";
    if (cell.readOnly && isEditingKey) {
        event.preventDefault();
        return;
    }

    if (event.key === "Backspace" || event.key === "Delete") {
        event.preventDefault();
        cell.value = "";
        cell.classList.remove("hint", "invalid", "solved");
        return;
    }

    if (/^[1-9]$/.test(event.key)) {
        event.preventDefault();
        cell.value = event.key;
        cell.classList.remove("hint", "invalid", "solved");
        return;
    }

    if (event.key.length === 1 && !event.ctrlKey && !event.metaKey && !event.altKey) {
        event.preventDefault();
    }
}

function moveFocus(event, container) {
    const keys = new Set(["ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight"]);
    if (!keys.has(event.key)) {
        return;
    }

    event.preventDefault();
    const cell = event.currentTarget;
    const row = Number(cell.dataset.row);
    const col = Number(cell.dataset.col);
    const next = {
        ArrowUp: [Math.max(0, row - 1), col],
        ArrowDown: [Math.min(SIZE - 1, row + 1), col],
        ArrowLeft: [row, Math.max(0, col - 1)],
        ArrowRight: [row, Math.min(SIZE - 1, col + 1)]
    }[event.key];

    const target = container.querySelector(`[data-row="${next[0]}"][data-col="${next[1]}"]`);
    if (target) {
        target.focus();
    }
}

function highlightPeers(cell, container) {
    clearSelection(container);

    const row = cell.dataset.row;
    const col = cell.dataset.col;

    container.querySelectorAll(`[data-row="${row}"], [data-col="${col}"]`).forEach(peer => {
        peer.classList.add("peer");
    });
    cell.classList.add("selected");
}

function clearSelection(container) {
    container.querySelectorAll(".cell.peer, .cell.selected").forEach(cell => {
        cell.classList.remove("peer", "selected");
    });
}
