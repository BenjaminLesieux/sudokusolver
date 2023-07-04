package com.efrei.team

import sun.security.provider.NativePRNG.Blocking
import zio.Console.{printLine, readLine}
import zio.ZIO.blocking
import zio.nio.file.Path
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

import scala.io.Source

object Main extends ZIOAppDefault {
  def run = appLogic

  private val appLogic = for {
    _ <- printLine("Welcome to sudoku solver!!")
    jsonFilePath <- readLine("Please enter the path to the JSON file: ")
    fileContent <- parseFile(jsonFilePath)
    sudokuGrid <- buildGrid(fileContent)
    _ <- sudokuGrid match {
      case Right(grid) => printLine(grid.toString)
      case Left(error) => printLine("Invalid grid")
    }
  } yield ()
}
