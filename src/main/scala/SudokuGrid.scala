package com.efrei.team

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class SudokuGrid(grid: List[List[Option[Int]]]) {
  require(grid.length == 9 && grid.forall(e => e.length == 9 && e.forall(i => i match {
    case Some(value) => value >= 1 && value <= 9
    case None => true
  })), "Invalid grid")

  override def toString: String = {
    val horizontalSeparator = "+-------+-------+-------+\n"
    val rowSeparator = " | "
    val emptyCell = "x"

    val formattedRows = grid.map { row =>
      row.map {
        case Some(value) => value.toString
        case None => emptyCell
      }.grouped(3).map(_.mkString("  ")).grouped(3).map(_.mkString(rowSeparator)).mkString("\n")
    }

    val formattedGrid = formattedRows.grouped(3).map(_.mkString(s"\n$horizontalSeparator")).mkString("\n")

    s"$horizontalSeparator$formattedGrid\n$horizontalSeparator"
  }
}
object SudokuGrid {
  implicit val decoder: JsonDecoder[SudokuGrid] = DeriveJsonDecoder.gen[SudokuGrid]
}