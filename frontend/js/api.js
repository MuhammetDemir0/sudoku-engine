const API_ROOT = "/api/v1/puzzles";

async function postJson(path, payload) {
    let response;
    try {
        response = await fetch(`${API_ROOT}${path}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(payload)
        });
    } catch (error) {
        throw new Error("Unable to reach the Sudoku API. Please try again.");
    }

    if (response.status === 204) {
        return null;
    }

    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        const message = body.message || friendlyHttpMessage(response.status);
        throw new Error(message);
    }
    return body;
}

function friendlyHttpMessage(status) {
    if (status === 400) {
        return "Please check the board and try again.";
    }
    if (status === 422) {
        return "This puzzle cannot be completed from the current board.";
    }
    return "Something went wrong. Please try again.";
}

export function generatePuzzle(difficulty) {
    return postJson("/generate", { difficulty });
}

export function solvePuzzle(board, includeSteps = false, solver = "MRV") {
    return postJson("/solve", { board, includeSteps, solver });
}

export function validateBoard(board) {
    return postJson("/validate", { board });
}

export function requestHint(board) {
    return postJson("/hint", { board });
}
