# Sudokusolver 
### by LESIEUX Benjamin - LIU Senhua -  MARIOTTE Thomas - PHAM Van Alenn

We coded together with JetBrains tools hosted by Benjamin.

# ZIO 

### **Features** 

- `scala
  import zio.Console.{printLine, readLine}`
  
  **'zio.console'** module provides functions for interacting with the console. For our project of Sudoku Resolver he can printing our Grid in the console.

- `scala impor zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}`
  
  When working with ZIO in Scala, you often need to import various dependencies and types to leverage the power of the library. The zio.Scope import allows you to control the scope and   lifecycle of resources, while the zio.ZIO import provides the main ZIO type for building functional programs. The zio.ZIOAppArgs and zio.ZIOAppDefault imports are related to creating ZIO applications with or without command-line arguments.



# Data Strucutre 

### Grid 

We decided to stay with the JSON format for our Grid. Moreover we figured that the **double ARRAY** is much more simple to represent the grid. 

This is what our grid looks like :

```JSON
{
  "grid": [
[5, 3, null, null, 7, null, null, null, null],
   [6, null, null, 1, 9, 5, null, null, null],
[null, 9, 8, null, null, null, null, 6, null],
[8, null, null, null, 6, null, null, null, 3],
   [4, null, null, 8, null, 3, null, null, 1],
[7, null, null, null, 2, null, null, null, 6],
[null, 6, null, null, null, null, 2, 8, null],
   [null, null, null, 4, 1, 9, null, null, 5],
 [null, null, null, null, 8, null, null, 7, 9]
]

```
▶️ We'll use the optional type to find out when our cell will be empty. The **null** cells will become None and the others **Some(value)**. 

Now let's explain how our code works. 
We will first explicit `SudukoGrid.scala`

# SudokuGrid.scala 

## How are we solving our grid ?

We define a few datatypes to help us in our algorithm. We define a cell a something that can or can not contain an integer in it.
We then define a SolvedCell as something that **must** be an int. 
We define a trait **SudokuGrid** that defines the basic interface of a grid. It only provides a grid object, which is a list of list of either a Cell or a SolvedCell. 

```Scala
type Cell = Option[Int] // We must a value
type SolvedCell = Int

sealed trait SudokuGrid[A <: Cell | SolvedCell] { 
  def grid: List[List[A]] A list of a list of something
}

```

**That is how we retrieve the information from our grid.**

We created a case class **UnsolvedSudokuGrid** that takes into parameters a **List[List[Cell]]** and extends the trait **SudokuGrid[Cell]**. The instance of this class is only created if the grid is of 9x9 format and if the Cell either contains no values or only values between 1 and 9.  

```Scala
  require(grid.length == 9 && grid.forall(e => e.length == 9 && e.forall(i => i match {
    case Some(value) => value >= 1 && value <= 9
    case None => true
  })), "Invalid grid")
```

We override the **toString** method only to show the grid in a prettier format like this : 

```text 
+-------+-------+-------+
| x x 3 | x 2 x | 6 x x |
| 9 x x | 3 x 5 | x x 1 |
| x x 1 | 8 x 6 | 4 x x |
+-------+-------+-------+
| x x 8 | 1 x 2 | 9 x x |
| 7 x x | x x x | x x 8 |
| x x 6 | 7 x 8 | 2 x x |
+-------+-------+-------+
| x x 2 | 6 x 9 | 5 x x |
| 8 x x | 2 x 3 | x x 9 |
| x x 5 | x 1 x | 3 x x |
+-------+-------+-------+ 
```

```Scala
  override def toString: String = { // Creating separator for build our grid
    val horizontalSeparator = "+-------+-------+-------+\n" 
    val rowSeparator = "|"
    val emptyCell = "x"

    val gridString = grid.grouped(3).map { bigGroup => // Construction of our grid with separator
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

    // Thanks to that we can clearly display the grid on the console.

    f"$horizontalSeparator$gridString\n$horizontalSeparator"
  }
```

## How to validate a cell? 

The function validate will need to take an **Array[Array[Cell]]**, a position x and y as well a the value to validate. It will check if the value can be put at said position following the rules of the sudoku. First by checking if the row already contains the value. Then checking for the column and finally for the 3x3 box. 

```Scala
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
```

### And how are we solving a grid ? 

For that we are using a Backtracking algorithm. The solve function first transform our grid (List[List[Cell]]) to an Array[Array[Cell]] in order to be able to modify and build a solution without modifying our unsolved grid directly. The object is juste to produce a result (could be an exception) and let our UnsolvedSudokuGrid immutable and in its unsolved state. 

We define a variable **solvedGrid** that can either be a SolvedSudokuGrid or None. If None, that means no solutions were found by our algorithm. 

The **solveHelper** implements the backtracking algorithm with recursion by trying to put every value in each cell and validate it or not. If it manages to place numbers in every cell and then returns true and assigns to **solvedGrid** Some(solvedSudokuGrid). Otherwise, solvedGrid will be None. 

We then call the solvedHelper method and match the solvedGrid which either contains the solution or not.

```Scala
  def solve(): Option[SolvedSudokuGrid] = {
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
    solvedGrid
  }
```

Here is the whole code for the UnsolvedSudokuGrid class: 

```Scala
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

  def solve(): Option[SolvedSudokuGrid] = {
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
    solvedGrid
  }
}
```

## AND then, we check if the solving is correct.

We created another case class **SolvedSudokuGrid** which takes a List[List[SolvedCell]] as a parameter. It extends SudokuGrid[SolvedCell]. As we want this class to only represent solved sudoku grid, we apply the same constraint as the UnsolvedSudokuGrid but we also check if the grid passed as parameter is indeed solved with the parameter **isSolved**. 

```Scala
  require(grid.length == 9 && grid.forall(e => e.length == 9 && e.forall(i => i >= 1 && i <= 9)) && isSolved, "Invalid grid (is not solved, check for duplicates)") 
```

The validate method then checks for duplicates in the rows, columns and boxes. If none are found then the grid is considered solved and correct.

```Scala
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
```

Here is the full code for the class. 

```Scala
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
```


### With the example of our previous grid, here is the final result :

![image](https://github.com/BenjaminLesieux/sudokusolver/assets/73226823/49c39c2b-12de-4282-b3aa-054381609d64)

The grid is correctly solved.

# Main.Scala
The Main file is an extension of ZIOAppDefault. It is a trait that is provided by the ZIO Library which simplifies the writing of ZIO Applications.

```Scala
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
```  
The Main file is going to :
- Print a welcoming message using "printLine".
- It's going to prompt the user to enter the path of a JSON file using "readLine".
- It calls the "parseFile" function using "jsonFilePath" and it is going to be interpreted as a "fileContent".
- It will also call "buildGrid" function, and pass over the "fileContent", hence it is going to be interpreted as "sudokuGrid".
- As a final step, we will check the value of "sudokuGrid" using a pattern match, if it's Right(grid), it means he grid was successfully built and it will then proceed to allow the solver to solve it using "grid.solve()". The result is then written to a file specified by "jsonFilePath" using the "writeFile" function or fails if no solution are found. If sudokuGrid is "Left(Error)", that means that there was an error while building the grid and in this case "printLine" will display an appropriate error message.

# FileReader.Scala

This function allows us to read our grid from a file.


### Parse 

```Scala
def parseFile(filePath: String) = {
  val source = Try { Source.fromFile("src/main/files/" + filePath + ".json") }
  source match {
    case Success(value) => ZIO.succeed(value.mkString)
    case Failure(exception) => ZIO.fail(exception, "File not found")
  }
}
```

▶️ The `parseFile` function searches for a file whose name matches the filename we provide as input.



### Write 

```Scala 
def writeFile(solvedGrid: SolvedSudokuGrid, gridName: String): Unit = {
  val pw = new PrintWriter(new File("src/main/files/solutions/" + gridName + ".json"))
  println(solvedGrid)
  println("Your solution has been saved in src/main/files/solutions/" + gridName + ".json")
  pw.write(solvedGrid.toJson)
  pw.close()
}
```

▶️ This function allows us to easily write to a file.   


### Build 

```Scala
def buildGrid(jsonContent: String) = {
  val grid = Try { jsonContent.fromJson[UnsolvedSudokuGrid] }
  grid match {
    case Success(value) => ZIO.succeed(value)
    case Failure(exception) => ZIO.fail(exception, "Error while parsing the file")
  }
}
```

▶️ Finally, this function checks if the grid is valid for construction.

