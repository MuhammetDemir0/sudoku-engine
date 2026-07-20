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
                .andExpect(content().string(containsString("difficulty-tab")))
                .andExpect(content().string(containsString("Yeni Oyun")))
                .andExpect(content().string(containsString("id=\"hintBtn\"")))
                .andExpect(content().string(containsString("id=\"hintCount\"")))
                .andExpect(content().string(containsString("id=\"mistakeCount\"")))
                .andExpect(content().string(containsString("id=\"pauseGameBtn\"")))
                .andExpect(content().string(containsString("id=\"pencilBtn\"")))
                .andExpect(content().string(containsString("id=\"pencilState\"")))
                .andExpect(content().string(containsString("id=\"numberPad\"")))
                .andExpect(content().string(containsString("id=\"undoBtn\"")))
                .andExpect(content().string(containsString("aria-pressed=\"false\"")))
                .andExpect(content().string(containsString("id=\"loadingOverlay\"")))
                .andExpect(content().string(containsString("id=\"toastRegion\"")))
                .andExpect(content().string(containsString("type=\"module\" src=\"/js/game.js?v=")));
    }

    @Test
    void staticAssetsAreServedWithoutFrontendServer() throws Exception {
        assertStaticFile("/css/app.css");
        assertStaticFile("/js/api.js");
        assertStaticFile("/js/board.js");
        assertStaticFile("/js/feedback.js");
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
                .andExpect(content().string(containsString("resetMistakes();")))
                .andExpect(content().string(containsString("resetHistory();")))
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
                .andExpect(content().string(containsString("elements.hint.disabled = isBusy || !hasActivePuzzle || gameCompleted || gamePaused;")))
                .andExpect(content().string(containsString("setGameCompleted(true);")));
    }

    @Test
    void gameplayCountersReplaceSolverControls() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hatalar")))
                .andExpect(content().string(containsString("hintCount")));

        mockMvc.perform(get("/js/game.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("const MAX_MISTAKES = 3;")))
                .andExpect(content().string(containsString("recordMistake(state.move);")))
                .andExpect(content().string(containsString("endGameAfterMistakes();")))
                .andExpect(content().string(containsString("onNumberPadClick")))
                .andExpect(content().string(containsString("selectDifficulty(button)")))
                .andExpect(content().string(containsString("elements.mistakeCount.textContent = `0/${MAX_MISTAKES}`;")));

        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".sudoku-board.is-locked")))
                .andExpect(content().string(containsString(".round-tool")));
    }

    @Test
    void gameplayControlsSupportPauseAndPencilNotes() throws Exception {
        mockMvc.perform(get("/js/timer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("pause()")))
                .andExpect(content().string(containsString("resume()")))
                .andExpect(content().string(containsString("isRunning()")));

        mockMvc.perform(get("/js/board.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("setPencilMode(enabled)")))
                .andExpect(content().string(containsString("toggleNote(row, col, value)")))
                .andExpect(content().string(containsString("input.value = \"\";")));

        mockMvc.perform(get("/js/game.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("async function onBoardChange(state)")))
                .andExpect(content().string(containsString("function onPauseGame()")))
                .andExpect(content().string(containsString("function onTogglePencil()")))
                .andExpect(content().string(containsString("timer.pause();")))
                .andExpect(content().string(containsString("timer.resume();")));

        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("caret-color: transparent")))
                .andExpect(content().string(containsString(".cell.selected")))
                .andExpect(content().string(containsString(".notes")))
                .andExpect(content().string(containsString(".sudoku-board.is-paused")))
                .andExpect(content().string(containsString("-webkit-text-fill-color: transparent")))
                .andExpect(content().string(containsString("visibility: hidden")))
                .andExpect(content().string(containsString("#pauseGameBtn.is-resume::before")))
                .andExpect(content().string(containsString(".number-pad.is-paused")));
    }

    @Test
    void feedbackComponentsProvideLoadingToastsAndMobileStyles() throws Exception {
        mockMvc.perform(get("/js/feedback.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("export class LoadingIndicator")))
                .andExpect(content().string(containsString("show(message)")))
                .andExpect(content().string(containsString("hide()")))
                .andExpect(content().string(containsString("export class ToastCenter")))
                .andExpect(content().string(containsString("toast.dataset.state = state")));

        mockMvc.perform(get("/js/game.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("new LoadingIndicator(elements.loadingOverlay, elements.loadingText)")))
                .andExpect(content().string(containsString("new ToastCenter(elements.toastRegion)")))
                .andExpect(content().string(containsString("loading.show(status)")))
                .andExpect(content().string(containsString("loading.hide()")))
                .andExpect(content().string(containsString("toasts.show(value, state)")));

        mockMvc.perform(get("/js/api.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("friendlyHttpMessage")))
                .andExpect(content().string(containsString("Something went wrong. Please try again.")));

        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".loading-overlay")))
                .andExpect(content().string(containsString(".loading-spinner")))
                .andExpect(content().string(containsString(".toast-region")))
                .andExpect(content().string(containsString(".toast[data-state=\"success\"]")))
                .andExpect(content().string(containsString(".toast[data-state=\"error\"]")))
                .andExpect(content().string(containsString("@media (max-width: 560px)")));
    }

    private void assertStaticFile(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk());
    }
}
