package com.efrei.team

import zio.ZIO
import zio.json.*

import java.io.{BufferedWriter, File, FileNotFoundException, FileWriter, PrintWriter}
import scala.io.Source
import scala.util.{Failure, Success, Try}

def parseFile(filePath: String) = {
  val source = Try { Source.fromFile("src/main/files/" + filePath + ".json") }
  source match {
    case Success(value) => ZIO.succeed(value.mkString)
    case Failure(exception) => ZIO.fail(exception, "File not found")
  }
}

def writeFile(solvedGrid: SolvedSudokuGrid, gridName: String): Unit = {
  val pw = new PrintWriter(new File("src/main/files/solutions/" + gridName + ".json"))
  println(solvedGrid)
  println("Your solution has been saved in src/main/files/solutions/" + gridName + ".json")
  pw.write(solvedGrid.toJson)
  pw.close()
}

def buildGrid(jsonContent: String) = {
  val grid = Try { jsonContent.fromJson[UnsolvedSudokuGrid] }
  grid match {
    case Success(value) => ZIO.succeed(value)
    case Failure(exception) => ZIO.fail(exception, "Error while parsing the file")
  }
}
