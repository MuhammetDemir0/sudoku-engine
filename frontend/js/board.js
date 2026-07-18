const SIZE = 9;
const EMPTY = 0;

export class SudokuBoardView {
    constructor(container, onChange = () => {}) {
        this.container = container;
        this.onChange = onChange;
        this.initialBoard = emptyBoard();
        this.notes = createNotes();
        this.pencilMode = false;
        this.paused = false;
        this.render(this.initialBoard);
    }

    render(board, options = {}) {
        this.container.innerHTML = "";
        const givens = options.givens || board;

        for (let row = 0; row < SIZE; row++) {
            for (let col = 0; col < SIZE; col++) {
                const wrapper = document.createElement("div");
                wrapper.className = "cell-wrap";
                wrapper.dataset.row = String(row);
                wrapper.dataset.col = String(col);

                const input = document.createElement("input");
                input.className = "cell";
                input.type = "text";
                input.inputMode = "numeric";
                input.autocomplete = "off";
                input.maxLength = 1;
                input.pattern = "[1-9]";
                input.placeholder = " ";
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

                const notes = document.createElement("div");
                notes.className = "notes";
                notes.ariaHidden = "true";
                notes.dataset.row = String(row);
                notes.dataset.col = String(col);
                renderNotes(notes, this.notes[row][col]);

                input.addEventListener("input", () => {
                    if (this.paused) {
                        input.value = "";
                        return;
                    }
                    if (this.pencilMode && !input.readOnly) {
                        const match = input.value.match(/[1-9]/);
                        input.value = "";
                        if (match) {
                            this.toggleNote(row, col, Number(match[0]));
                        }
                        return;
                    }
                    normalizeCell(input);
                    if (input.value) {
                        this.clearNotes(row, col);
                    }
                    this.notifyChange();
                });
                input.addEventListener("keydown", event => handleCellKeyDown(event, this));
                input.addEventListener("focus", () => highlightPeers(input, this.container));
                input.addEventListener("blur", () => clearSelection(this.container));

                wrapper.appendChild(input);
                wrapper.appendChild(notes);
                this.container.appendChild(wrapper);
            }
        }

        this.updateConflicts();
        this.setPaused(this.paused);
    }

    loadPuzzle(board) {
        this.initialBoard = cloneBoard(board);
        this.notes = createNotes();
        this.render(board, { givens: board });
    }

    reset() {
        this.notes = createNotes();
        this.render(this.initialBoard, { givens: this.initialBoard });
    }

    clear() {
        this.initialBoard = emptyBoard();
        this.notes = createNotes();
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
                if (value !== EMPTY) {
                    this.clearNotes(row, col);
                }
                if (className) {
                    cell.classList.add(className);
                }
            }
        });
        this.updateConflicts();
    }

    markHint(row, col, value) {
        this.clearMarks();
        const cell = this.cellAt(row, col);
        if (cell) {
            cell.value = String(value);
            this.clearNotes(row, col);
            cell.classList.add("hint");
            cell.focus();
            this.updateConflicts();
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

    notifyChange() {
        this.clearMarks();
        const conflicts = this.updateConflicts();
        this.onChange({
            board: this.read(),
            conflicts,
            complete: this.isComplete()
        });
    }

    setPencilMode(enabled) {
        this.pencilMode = enabled;
        this.container.classList.toggle("pencil-mode", enabled);
    }

    setPaused(paused) {
        this.paused = paused;
        this.container.classList.toggle("is-paused", paused);
        this.cells().forEach(cell => {
            cell.tabIndex = paused ? -1 : 0;
        });
        if (paused && this.container.contains(document.activeElement)) {
            document.activeElement.blur();
        }
    }

    toggleNote(row, col, value) {
        const cell = this.cellAt(row, col);
        if (!cell || cell.value || cell.classList.contains("given")) {
            return;
        }

        const noteSet = this.notes[row][col];
        if (noteSet.has(value)) {
            noteSet.delete(value);
        } else {
            noteSet.add(value);
        }
        renderNotes(this.notesAt(row, col), noteSet);
    }

    clearNotes(row, col) {
        this.notes[row][col].clear();
        renderNotes(this.notesAt(row, col), this.notes[row][col]);
    }

    updateConflicts() {
        const conflicts = detectConflicts(this.read());
        this.clearConflicts();

        conflicts.forEach(conflict => {
            conflict.cells.forEach(([row, col]) => {
                const cell = this.cellAt(row, col);
                if (cell) {
                    cell.classList.add("conflict", `conflict-${conflict.type}`);
                    cell.setAttribute("aria-invalid", "true");
                    cell.title = addConflictTitle(cell.title, conflict.type);
                }
            });
        });

        return conflicts;
    }

    clearConflicts() {
        this.cells().forEach(cell => {
            cell.classList.remove("conflict", "conflict-row", "conflict-column", "conflict-box");
            cell.removeAttribute("aria-invalid");
            cell.removeAttribute("title");
        });
    }

    isComplete() {
        return this.read().every(row => row.every(value => value !== EMPTY));
    }

    cellAt(row, col) {
        return this.container.querySelector(`.cell[data-row="${row}"][data-col="${col}"]`);
    }

    notesAt(row, col) {
        return this.container.querySelector(`.notes[data-row="${row}"][data-col="${col}"]`);
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

function createNotes() {
    return Array.from({ length: SIZE }, () => Array.from({ length: SIZE }, () => new Set()));
}

function renderNotes(container, noteSet) {
    if (!container) {
        return;
    }
    container.innerHTML = "";
    for (let value = 1; value <= SIZE; value++) {
        const note = document.createElement("span");
        note.textContent = noteSet.has(value) ? String(value) : "";
        container.appendChild(note);
    }
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

export function detectConflicts(board) {
    const conflicts = [];

    for (let row = 0; row < SIZE; row++) {
        addGroupConflicts(conflicts, "row", board[row].map((value, col) => ({ row, col, value })));
    }

    for (let col = 0; col < SIZE; col++) {
        addGroupConflicts(conflicts, "column", board.map((row, rowIndex) => ({
            row: rowIndex,
            col,
            value: row[col]
        })));
    }

    for (let boxRow = 0; boxRow < SIZE; boxRow += 3) {
        for (let boxCol = 0; boxCol < SIZE; boxCol += 3) {
            const cells = [];
            for (let row = boxRow; row < boxRow + 3; row++) {
                for (let col = boxCol; col < boxCol + 3; col++) {
                    cells.push({ row, col, value: board[row][col] });
                }
            }
            addGroupConflicts(conflicts, "box", cells);
        }
    }

    return conflicts;
}

function addGroupConflicts(conflicts, type, cells) {
    const positionsByValue = new Map();
    cells
        .filter(cell => cell.value !== EMPTY)
        .forEach(cell => {
            if (!positionsByValue.has(cell.value)) {
                positionsByValue.set(cell.value, []);
            }
            positionsByValue.get(cell.value).push([cell.row, cell.col]);
        });

    positionsByValue.forEach((positions, value) => {
        if (positions.length > 1) {
            conflicts.push({ type, value, cells: positions });
        }
    });
}

function handleCellKeyDown(event, boardView) {
    if (boardView.paused) {
        event.preventDefault();
        return;
    }

    if (event.key.startsWith("Arrow")) {
        moveFocus(event, boardView.container);
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
        boardView.clearNotes(Number(cell.dataset.row), Number(cell.dataset.col));
        cell.classList.remove("hint", "invalid", "solved");
        boardView.notifyChange();
        return;
    }

    if (/^[1-9]$/.test(event.key)) {
        event.preventDefault();
        const row = Number(cell.dataset.row);
        const col = Number(cell.dataset.col);
        if (boardView.pencilMode) {
            boardView.toggleNote(row, col, Number(event.key));
            return;
        }
        cell.value = event.key;
        boardView.clearNotes(row, col);
        cell.classList.remove("hint", "invalid", "solved");
        boardView.notifyChange();
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

    const target = container.querySelector(`.cell[data-row="${next[0]}"][data-col="${next[1]}"]`);
    if (target) {
        target.focus();
    }
}

function highlightPeers(cell, container) {
    clearSelection(container);

    const row = cell.dataset.row;
    const col = cell.dataset.col;

    container.querySelectorAll(`.cell[data-row="${row}"], .cell[data-col="${col}"]`).forEach(peer => {
        peer.classList.add("peer");
    });
    cell.classList.add("selected");
}

function clearSelection(container) {
    container.querySelectorAll(".cell.peer, .cell.selected").forEach(cell => {
        cell.classList.remove("peer", "selected");
    });
}

function addConflictTitle(existingTitle, type) {
    const label = `${capitalize(type)} conflict`;
    if (!existingTitle) {
        return label;
    }
    return existingTitle.includes(label) ? existingTitle : `${existingTitle}; ${label}`;
}

function capitalize(value) {
    return `${value.charAt(0).toUpperCase()}${value.slice(1)}`;
}
