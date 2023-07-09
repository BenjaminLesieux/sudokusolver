package com.efrei.team

import zio.json.{DeriveJsonDecoder, JsonDecoder}

import scala.util.boundary.break

type Cell = Option[Int]
type SolvedCell = Int

sealed trait SudokuGrid[A] {
  def grid: List[List[A]]

  def validate(x: Int, y: Int, value: Int): Boolean = {
    val row = grid(y)
    val column = grid.map(row => row(x))
    val bigGroupX = x / 3
    val bigGroupY = y / 3
    val bigGroup = grid.slice(bigGroupY * 3, bigGroupY * 3 + 3).map(row => row.slice(bigGroupX * 3, bigGroupX * 3 + 3))

    !row.contains(Some(value)) && !column.contains(Some(value)) && !bigGroup.flatten.contains(Some(value))
  }
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

  private def findUnassignedLocation(): Option[(Int, Int)] = {
    grid.zipWithIndex.find { case (row, rowIndex) =>
      row.zipWithIndex.exists { case (cell, columnIndex) =>
        cell.isEmpty
      }
    }.map { case (row, rowIndex) =>
      (rowIndex, row.zipWithIndex.find { case (cell, columnIndex) =>
        cell.isEmpty
      }.get._2)
    }
  }
  def solve(): SolvedSudokuGrid = {
    val arrayGrid = grid.map(_.toArray).toArray

    println(arrayGrid.mkString("Array(", ", ", ")"))

    def solveHelper(currentGrid: Array[Array[Cell]]): Boolean = {
      val location: Option[(Int, Int)] = findUnassignedLocation()

      println(location)

      if (location.isEmpty) return true

      val (x, y) = location.get

      val possibleValues = (1 to 9).filter(validate(x, y, _))

      possibleValues.foreach { value =>
        currentGrid(y)(x) = Some(value)

        if (!solveHelper(currentGrid)) {
          currentGrid(y)(x) = None
        }
      }

      false
    }

    if (solveHelper(arrayGrid))
      SolvedSudokuGrid(arrayGrid.map(_.map(_.get).toList).toList)
    else
      throw new Exception("No solution found")
  }
}

case class SolvedSudokuGrid(grid: List[List[SolvedCell]]) extends SudokuGrid[SolvedCell] {
  require(
    grid.length == 9 &&
      grid.forall((e => e.length == 9 &&
        e.forall(i => i >= 1 && i <= 9))) &&
        grid.zipWithIndex.forall((row, rowIndex) =>
          row.zipWithIndex.forall((cell, columnIndex) =>
            validate(rowIndex, columnIndex, cell)
          )
        ),
        "Invalid grid")

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
}