package com.efrei.team

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import scala.annotation.tailrec
import scala.util.boundary
import scala.util.boundary.break

type Cell = Option[Int]
type SolvedCell = Int

sealed trait SudokuGrid[A <: Cell | SolvedCell] {
  def grid: List[List[A]]
}

case class UnsolvedSudokuGrid(grid: List[List[Cell]]) extends SudokuGrid[Cell] {
  require(grid.length == 9 && grid.forall(e => e.length == 9 && e.forall(i => i match {
    case Some(value) => value >= 1 && value <= 9
    case None => true
  })), "Invalid grid")

  override def toString: String = {
    val horizontalSeparator = "+-------+-------+-------+\n"
    val rowSeparator = "|"
    val emptyCell = "x"

    val gridString = grid.grouped(3).map { bigGroup =>
      bigGroup.map { row =>
        row.map {
          case Some(value) => value.toString
          case None => emptyCell
        }
          .grouped(3).map { smallGroup =>
          smallGroup.mkString(" ", " ", " ")
        }.mkString(rowSeparator, rowSeparator, rowSeparator)
      }.mkString("\n")
    }.mkString("\n" + horizontalSeparator)

    f"$horizontalSeparator$gridString\n$horizontalSeparator"
  }

  private def validate(grid: Array[Array[Cell]], x: Int, y: Int, value: Int): Boolean = {
    val rowValid = grid(y).forall {
      case Some(cellValue) => cellValue != value
      case None => true
    }

    val colValid = grid.map(_(x)).forall {
      case Some(cellValue) => cellValue != value
      case None => true
    }

    val boxRow = y / 3
    val boxCol = x / 3

    val boxValid = grid.slice(boxRow * 3, boxRow * 3 + 3).forall { row =>
      row.slice(boxCol * 3, boxCol * 3 + 3).forall {
        case Some(cellValue) => cellValue != value
        case None => true
      }
    }

    rowValid && colValid && boxValid
  }

  def solve(): SolvedSudokuGrid = {
    val arrayGrid = grid.map(_.toArray).toArray
    var solvedGrid: Option[SolvedSudokuGrid] = None

    def solveHelper(sudoku: Array[Array[Cell]], x: Int = 0, y: Int = 0): Boolean = {
      if (y >= 9) {
        solvedGrid = Some(SolvedSudokuGrid(arrayGrid.map(_.map(_.getOrElse(0)).toList).toList))
        return true
      } else if (x >= 9) {
        return solveHelper(sudoku, 0, y + 1)
      } else if (sudoku(y)(x).isDefined) {
        return solveHelper(sudoku, x + 1, y)
      } else (1 to 9).filter(value => validate(sudoku, x, y, value)).foreach { value =>
        sudoku(y)(x) = Some(value)
        if (!solveHelper(sudoku, x + 1, y)) {
          sudoku(y)(x) = None
        }
      }

      false
    }

    solveHelper(arrayGrid)
    solvedGrid match
      case Some(grid) => grid
      case None => throw new Exception("No solution found")
  }
}

case class SolvedSudokuGrid(grid: List[List[SolvedCell]]) extends SudokuGrid[SolvedCell] {
  require(grid.length == 9 && grid.forall(e => e.length == 9 && e.forall(i => i >= 1 && i <= 9)) && isSolved, "Invalid grid (is not solved, check for duplicates)")

  private def isSolved: Boolean = {
    def hasDuplicates(values: List[Int]): Boolean = {
      values.distinct.length != values.length
    }

    def checkRows(grid: List[List[Int]]): Boolean = {
      grid.forall(row => !row.contains(0) && !hasDuplicates(row))
    }

    def checkColumns(grid: List[List[Int]]): Boolean = {
      val transposedGrid = grid.transpose
      transposedGrid.forall(col => !col.contains(0) && !hasDuplicates(col))
    }

    def checkBlocks(grid: List[List[Int]]): Boolean = {
      def extractBlock(startRow: Int, startCol: Int): List[Int] = {
        val block = for {
          i <- startRow until startRow + 3
          j <- startCol until startCol + 3
        } yield grid(i)(j)
        block.toList
      }

      val blocks = for {
        i <- 0 until 9 by 3
        j <- 0 until 9 by 3
      } yield extractBlock(i, j)

      blocks.forall(block => !block.contains(0) && !hasDuplicates(block))
    }

    checkRows(grid) && checkColumns(grid) && checkBlocks(grid)
  }


  override def toString: String = {
    val horizontalSeparator = "+-------+-------+-------+\n"
    val rowSeparator = "|"

    val gridString = grid.grouped(3).map { bigGroup =>
      bigGroup.map { row =>
        row.grouped(3).map { smallGroup =>
          smallGroup.mkString(" ", " ", " ")
        }.mkString(rowSeparator, rowSeparator, rowSeparator)
      }.mkString("\n")
    }.mkString("\n" + horizontalSeparator)

    f"$horizontalSeparator$gridString\n$horizontalSeparator"
  }
}

object UnsolvedSudokuGrid {
  implicit val decoder: JsonDecoder[UnsolvedSudokuGrid] = DeriveJsonDecoder.gen[UnsolvedSudokuGrid]
}

object SolvedSudokuGrid {
  implicit val decoder: JsonDecoder[SolvedSudokuGrid] = DeriveJsonDecoder.gen[SolvedSudokuGrid]
  implicit val encoder: JsonEncoder[SolvedSudokuGrid] = DeriveJsonEncoder.gen[SolvedSudokuGrid]
}