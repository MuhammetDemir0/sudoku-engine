package com.sudokuengine.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class StaticWebApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexIsServedFromSpringBootStaticResources() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sudoku Engine")))
                .andExpect(content().string(containsString("id=\"difficulty\"")))
                .andExpect(content().string(containsString("New Game")))
                .andExpect(content().string(containsString("id=\"hintBtn\"")))
                .andExpect(content().string(containsString("id=\"hintCount\"")))
                .andExpect(content().string(containsString("type=\"module\" src=\"/js/game.js\"")));
    }

    @Test
    void staticAssetsAreServedWithoutFrontendServer() throws Exception {
        assertStaticFile("/css/app.css");
        assertStaticFile("/js/api.js");
        assertStaticFile("/js/board.js");
        assertStaticFile("/js/game.js");
        assertStaticFile("/js/timer.js");
        assertStaticFile("/js/visualizer.js");
    }

    @Test
    void newGameFrontendCallsGenerateApiAndHandlesUserStates() throws Exception {
        mockMvc.perform(get("/js/api.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fetch(`${API_ROOT}${path}`")))
                .andExpect(content().string(containsString("postJson(\"/generate\", { difficulty })")))
                .andExpect(content().string(containsString("Unable to reach the Sudoku API")));

        mockMvc.perform(get("/js/game.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Loading a new puzzle.")))
                .andExpect(content().string(containsString("boardView.clear();")))
                .andExpect(content().string(containsString("resetMetrics();")))
                .andExpect(content().string(containsString("timer.reset();")))
                .andExpect(content().string(containsString("element.disabled = isBusy")))
                .andExpect(content().string(containsString("dataset.state = state")));
    }

    @Test
    void boardFrontendDetectsConflictsAndVerifiesCompletionWithBackend() throws Exception {
        mockMvc.perform(get("/js/board.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("export function detectConflicts(board)")))
                .andExpect(content().string(containsString("addGroupConflicts(conflicts, \"row\"")))
                .andExpect(content().string(containsString("addGroupConflicts(conflicts, \"column\"")))
                .andExpect(content().string(containsString("addGroupConflicts(conflicts, \"box\"")))
                .andExpect(content().string(containsString("cell.classList.add(\"conflict\", `conflict-${conflict.type}`)")))
                .andExpect(content().string(containsString("clearConflicts()")));

        mockMvc.perform(get("/js/game.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("async function onBoardChange(state)")))
                .andExpect(content().string(containsString("validateBoard(state.board)")))
                .andExpect(content().string(containsString("Completed board verified by the server.")));

        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".cell.conflict-row")))
                .andExpect(content().string(containsString(".cell.conflict-column")))
                .andExpect(content().string(containsString(".cell.conflict-box")));
    }

    @Test
    void gameplayFlowUsesTimerResetAndBackendCompletionChecks() throws Exception {
        mockMvc.perform(get("/js/timer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("window.setInterval")))
                .andExpect(content().string(containsString("window.clearInterval")))
                .andExpect(content().string(containsString("this.display.textContent = \"00:00\"")));

        mockMvc.perform(get("/js/board.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("this.initialBoard = cloneBoard(board);")))
                .andExpect(content().string(containsString("this.render(this.initialBoard, { givens: this.initialBoard });")));

        mockMvc.perform(get("/js/game.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("let hasActivePuzzle = false;")))
                .andExpect(content().string(containsString("hasActivePuzzle = true;")))
                .andExpect(content().string(containsString("timer.start();")))
                .andExpect(content().string(containsString("timer.stop();")))
                .andExpect(content().string(containsString("No puzzle to reset.")))
                .andExpect(content().string(containsString("validateBoard(state.board)")))
                .andExpect(content().string(containsString("Completed board verified by the server.")))
                .andExpect(content().string(containsString("server violation(s) found.")));
    }

    @Test
    void hintFrontendSendsBoardTracksUsageAndDisablesAfterCompletion() throws Exception {
        mockMvc.perform(get("/js/api.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("postJson(\"/hint\", { board })")));

        mockMvc.perform(get("/js/board.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("markHint(row, col, value)")))
                .andExpect(content().string(containsString("cell.classList.add(\"hint\")")))
                .andExpect(content().string(containsString("cell.focus()")));

        mockMvc.perform(get("/js/game.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("requestHint(boardView.read())")))
                .andExpect(content().string(containsString("let hintCount = 0;")))
                .andExpect(content().string(containsString("incrementHintCount();")))
                .andExpect(content().string(containsString("elements.hintCount.textContent = String(hintCount);")))
                .andExpect(content().string(containsString("elements.hint.disabled = isBusy || gameCompleted;")))
                .andExpect(content().string(containsString("setGameCompleted(true);")));
    }

    private void assertStaticFile(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk());
    }
}
