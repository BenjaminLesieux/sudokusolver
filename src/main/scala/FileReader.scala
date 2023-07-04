package com.efrei.team

import zio.ZIO
import zio.json._

import java.io.FileNotFoundException
import scala.io.Source
import scala.util.{Failure, Success, Try}

def parseFile(filePath: String) = {
  val source = Try { Source.fromFile("src/main/files/" + filePath + ".json") }
  source match {
    case Success(value) => ZIO.succeed(value.mkString)
    case Failure(exception) => ZIO.fail(exception, "File not found")
  }
}

def buildGrid(jsonContent: String) = {
  val grid = Try { jsonContent.fromJson[SudokuGrid] }
  grid match {
    case Success(value) => ZIO.succeed(value)
    case Failure(exception) => ZIO.fail(exception, "Error while parsing the file")
  }
}
