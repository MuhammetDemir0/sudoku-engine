const API_ROOT = "/api/v1/puzzles";

async function postJson(path, payload) {
    const response = await fetch(`${API_ROOT}${path}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        },
        body: JSON.stringify(payload)
    });

    if (response.status === 204) {
        return null;
    }

    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        const message = body.message || `Request failed with HTTP ${response.status}.`;
        throw new Error(message);
    }
    return body;
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
