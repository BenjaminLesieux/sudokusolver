package com.efrei.team

import sun.security.provider.NativePRNG.Blocking
import zio.Console.{printLine, readLine}
import zio.ZIO.blocking
import zio.nio.file.Path
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

import scala.io.Source

object Main extends ZIOAppDefault {
  def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = appLogic

  private val appLogic = for {
    _ <- printLine("Welcome to sudoku solver!!")
    jsonFilePath <- readLine("Please enter the path to the JSON file: ")
    fileContent <- parseFile(jsonFilePath)
    sudokuGrid <- buildGrid(fileContent)
    _ <- printLine(sudokuGrid.toString)
    _ <- sudokuGrid match {
      case Right(grid) =>
        val result = grid.solve()
        result match
          case Some(solvedGrid) => ZIO.succeed(writeFile(solvedGrid, jsonFilePath))
          case None => ZIO.fail("No solution found")
      case Left(error) => printLine("Invalid grid")
    }
  } yield ()
}
