-- Sudoku Engine Database Schema
-- PostgreSQL DDL script

-- Drop existing tables if they exist (for development)
-- DROP TABLE IF EXISTS sudoku_puzzles CASCADE;
-- DROP TABLE IF EXISTS sudoku_solutions CASCADE;

-- Sudoku Puzzles Table
CREATE TABLE IF NOT EXISTS sudoku_puzzles (
    id SERIAL PRIMARY KEY,
    puzzle_data VARCHAR(81) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sudoku Solutions Table
CREATE TABLE IF NOT EXISTS sudoku_solutions (
    id SERIAL PRIMARY KEY,
    puzzle_id INTEGER NOT NULL REFERENCES sudoku_puzzles(id) ON DELETE CASCADE,
    solution_data VARCHAR(81) NOT NULL,
    solving_time_ms BIGINT,
    solved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_puzzles_difficulty ON sudoku_puzzles(difficulty);
CREATE INDEX IF NOT EXISTS idx_solutions_puzzle_id ON sudoku_solutions(puzzle_id);
