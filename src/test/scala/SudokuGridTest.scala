package com.efrei.team

import org.scalatest.flatspec.AnyFlatSpec

class SudokuGridTest extends AnyFlatSpec {

  "A correct grid" should "have a solution" in {
    val grid = UnsolvedSudokuGrid(
      List(
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, Some(1), Some(2), Some(3), None, None, None),
        List(None, None, None, Some(4), Some(5), Some(6), None, None, None),
        List(None, None, None, Some(7), Some(8), Some(9), None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None)
      )
    )

    val solution = SolvedSudokuGrid(
      List(
        List(4, 7, 8, 2, 9, 1, 6, 3, 5),
        List(1, 2, 3, 5, 6, 8, 7, 4, 9),
        List(9, 5, 6, 3, 4, 7, 2, 8, 1),
        List(6, 4, 5, 1, 2, 3, 8, 9, 7),
        List(7, 8, 9, 4, 5, 6, 1, 2, 3),
        List(2, 3, 1, 7, 8, 9, 4, 5, 6),
        List(5, 1, 7, 8, 3, 4, 9, 6, 2),
        List(8, 9, 2, 6, 1, 5, 3, 7, 4),
        List(3, 6, 4, 9, 7, 2, 5, 1, 8)
      )
    )

    assert(grid.solve() == solution)
  }

  "An incorrect grid" should "not have a solution" in {
    assertThrows[IllegalArgumentException](UnsolvedSudokuGrid(
      List(
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, Some(1), Some(2), Some(3), None, None, None),
        List(None, None, None, Some(4), Some(5), Some(6), None, None, None),
        List(None, None, None, Some(7), Some(8), Some(9), None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, Some(1), None)
      )
    ))
  }

  "An unsolvable grid" should "not have a solution" in {
    val unsolvableSudokuGrid = UnsolvedSudokuGrid(
      List(
        List(Some(1), Some(2), Some(3), None, None, None, None, None, None),
        List(None, None, None, None, Some(4), None, None, None, None),
        List(None, None, None, None, None, None, None, None, Some(4)),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None),
        List(None, None, None, None, None, None, None, None, None)
      )
    )

    assertThrows[Exception](unsolvableSudokuGrid.solve())
  }
}
