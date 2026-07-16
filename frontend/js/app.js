/**
 * Sudoku Engine - Frontend Application
 * Vanilla JavaScript implementation
 */

// ===========================
// Global State
// ===========================
const APP_STATE = {
  currentPuzzle: null,
  currentSolution: null,
  selectedCell: null,
  isLoading: false
};

// API Configuration
const API_BASE = 'http://localhost:8080/api';

// ===========================
// DOM Elements
// ===========================
const sudokuGrid = document.getElementById('sudokuGrid');
const newPuzzleBtn = document.getElementById('newPuzzleBtn');
const solvePuzzleBtn = document.getElementById('solvePuzzleBtn');
const resetPuzzleBtn = document.getElementById('resetPuzzleBtn');
const clearPuzzleBtn = document.getElementById('clearPuzzleBtn');
const difficultySelect = document.getElementById('difficultySelect');
const solvingTimeElement = document.getElementById('solvingTime');
const puzzleStatusElement = document.getElementById('puzzleStatus');

// ===========================
// Initialization
// ===========================
document.addEventListener('DOMContentLoaded', () => {
  initializeEventListeners();
  createEmptyGrid();
});

// ===========================
// Event Listeners
// ===========================
function initializeEventListeners() {
  newPuzzleBtn.addEventListener('click', generateNewPuzzle);
  solvePuzzleBtn.addEventListener('click', solvePuzzle);
  resetPuzzleBtn.addEventListener('click', resetPuzzle);
  clearPuzzleBtn.addEventListener('click', clearPuzzle);
}

// ===========================
// Grid Creation & Rendering
// ===========================
function createEmptyGrid() {
  sudokuGrid.innerHTML = '';
  const cells = new Array(81).fill(null);

  cells.forEach((_, index) => {
    const cell = document.createElement('div');
    cell.className = 'sudoku-cell';
    cell.dataset.index = index;
    cell.dataset.value = '';

    // Make cells editable
    cell.contentEditable = 'true';
    cell.addEventListener('input', (e) => handleCellInput(e, index));
    cell.addEventListener('click', (e) => handleCellClick(e, index));
    cell.addEventListener('keydown', (e) => handleKeyPress(e, index));

    sudokuGrid.appendChild(cell);
  });
}

function createGridFromString(puzzleString) {
  sudokuGrid.innerHTML = '';

  for (let i = 0; i < 81; i++) {
    const cell = document.createElement('div');
    cell.className = 'sudoku-cell';
    cell.dataset.index = i;

    const value = puzzleString[i];
    if (value !== '0' && value !== '') {
      cell.textContent = value;
      cell.classList.add('filled');
      cell.contentEditable = 'false';
      cell.dataset.value = value;
    } else {
      cell.contentEditable = 'true';
      cell.data Value = '';
    }

    cell.addEventListener('input', (e) => handleCellInput(e, i));
    cell.addEventListener('click', (e) => handleCellClick(e, i));
    cell.addEventListener('keydown', (e) => handleKeyPress(e, i));

    sudokuGrid.appendChild(cell);
  }
}

// ===========================
// Cell Input Handling
// ===========================
function handleCellInput(event, index) {
  const cell = event.target;
  let value = cell.textContent.trim();

  // Only allow digits 1-9
  if (value === '') {
    cell.dataset.value = '';
    return;
  }

  if (!/^[1-9]$/.test(value)) {
    cell.textContent = '';
    cell.dataset.value = '';
    return;
  }

  cell.dataset.value = value;
}

function handleCellClick(event, index) {
  // Remove previous selection
  document.querySelectorAll('.sudoku-cell.selected').forEach(cell => {
    cell.classList.remove('selected');
  });

  // Add selection to current cell
  event.target.classList.add('selected');
  APP_STATE.selectedCell = index;
}

function handleKeyPress(event, index) {
  const cell = event.target;

  // Allow navigation with arrow keys
  if (event.key === 'ArrowUp') {
    event.preventDefault();
    const newIndex = Math.max(0, index - 9);
    navigateToCell(newIndex);
  } else if (event.key === 'ArrowDown') {
    event.preventDefault();
    const newIndex = Math.min(80, index + 9);
    navigateToCell(newIndex);
  } else if (event.key === 'ArrowLeft') {
    event.preventDefault();
    const newIndex = index % 9 === 0 ? index : index - 1;
    navigateToCell(newIndex);
  } else if (event.key === 'ArrowRight') {
    event.preventDefault();
    const newIndex = index % 9 === 8 ? index : index + 1;
    navigateToCell(newIndex);
  } else if (event.key === 'Delete' || event.key === 'Backspace') {
    event.preventDefault();
    cell.textContent = '';
    cell.dataset.value = '';
  }
}

function navigateToCell(index) {
  const cells = document.querySelectorAll('.sudoku-cell');
  cells[index].focus();
  handleCellClick({ target: cells[index] }, index);
}

// ===========================
// Puzzle Operations
// ===========================
async function generateNewPuzzle() {
  APP_STATE.isLoading = true;
  updateStatus('Generating puzzle...');
  newPuzzleBtn.disabled = true;

  try {
    const difficulty = difficultySelect.value;
    // TODO: Call backend API
    // const response = await fetch(`${API_BASE}/puzzles/generate?difficulty=${difficulty}`);
    // const data = await response.json();
    // APP_STATE.currentPuzzle = data.puzzleData;
    // createGridFromString(data.puzzleData);

    // Temporary: Create empty grid for now
    createEmptyGrid();
    updateStatus('Ready');
  } catch (error) {
    console.error('Error generating puzzle:', error);
    updateStatus('Error generating puzzle');
  } finally {
    APP_STATE.isLoading = false;
    newPuzzleBtn.disabled = false;
  }
}

async function solvePuzzle() {
  APP_STATE.isLoading = true;
  updateStatus('Solving...');
  solvePuzzleBtn.disabled = true;

  try {
    const puzzleString = getPuzzleString();

    // TODO: Call backend API
    // const response = await fetch(`${API_BASE}/puzzles/solve`, {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ puzzle: puzzleString })
    // });
    // const data = await response.json();
    // APP_STATE.currentSolution = data.solution;
    // displaySolution(data.solution);
    // updateSolvingTime(data.solvingTimeMs);

    updateStatus('Solution ready');
  } catch (error) {
    console.error('Error solving puzzle:', error);
    updateStatus('Error solving puzzle');
  } finally {
    APP_STATE.isLoading = false;
    solvePuzzleBtn.disabled = false;
  }
}

function resetPuzzle() {
  // Reset to original puzzle (keep filled cells)
  const cells = document.querySelectorAll('.sudoku-cell');
  cells.forEach(cell => {
    if (!cell.classList.contains('filled')) {
      cell.textContent = '';
      cell.dataset.value = '';
    }
  });
  updateStatus('Puzzle reset');
}

function clearPuzzle() {
  // Clear all cells
  createEmptyGrid();
  updateStatus('Puzzle cleared');
}

// ===========================
// Helper Functions
// ===========================
function getPuzzleString() {
  const cells = document.querySelectorAll('.sudoku-cell');
  let puzzleString = '';

  cells.forEach(cell => {
    const value = cell.dataset.value || cell.textContent.trim();
    puzzleString += value || '0';
  });

  return puzzleString;
}

function displaySolution(solutionString) {
  const cells = document.querySelectorAll('.sudoku-cell');

  cells.forEach((cell, index) => {
    if (!cell.classList.contains('filled')) {
      cell.textContent = solutionString[index];
      cell.classList.add('solved');
    }
  });
}

function updateSolvingTime(timeMs) {
  const seconds = (timeMs / 1000).toFixed(2);
  solvingTimeElement.textContent = `Solving Time: ${seconds}s`;
}

function updateStatus(message) {
  const statusSpan = puzzleStatusElement.querySelector('span');
  statusSpan.textContent = message;
}

// ===========================
// Utilities
// ===========================
function log(message, level = 'info') {
  console.log(`[${level.toUpperCase()}] ${message}`);
}
