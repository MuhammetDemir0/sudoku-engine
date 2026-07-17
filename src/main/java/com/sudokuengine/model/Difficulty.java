package com.sudokuengine.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Supported puzzle difficulty levels ordered from easiest to hardest.
 */
@Schema(description = "Supported puzzle difficulty levels ordered from easiest to hardest.")
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}
