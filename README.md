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
▶️ We'll use Nil to find out when our cell will be empty.    

Now let's explain how our code works. 
We will first explicit `SudukoGrid.scala`

## How are we displaying our grid ?

This is our version of a `prettyPrint`  

```Scala
type Cell = Option[Int] // We must a value
type SolvedCell = Int

sealed trait SudokuGrid[A <: Cell | SolvedCell] {// A list of cell. The type have to be a unsolve cell or a solve cell.
  def grid: List[List[A]] A list of a list of something
}

```

**That is how we retrieve the information from our grid.**

```Scala
/** 
* We always have a sudoku grid 
* In this grid we have require OR a solved grid
**/

case class UnsolvedSudokuGrid(grid: List[List[Cell]]) extends SudokuGrid[Cell] { 
  require(grid.length == 9 && grid.forall(e => e.length == 9 && e.forall(i => i match {
    case Some(value) => value >= 1 && value <= 9
    case None => true
  })), "Invalid grid")

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

## How to validate a cell ? 

```Scala
    
    // Solving a cell with a row verification 
    val rowValid = grid(y).forall {
      case Some(cellValue) => cellValue != value
      case None => true
    }

    // Solving a cell with a col verification 
    val colValid = grid.map(_(x)).forall {
      case Some(cellValue) => cellValue != value
      case None => true
    }

    val boxRow = y / 3
    val boxCol = x / 3

    // Solving a cell with the box verification, it's important to do that after the row and col validation
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

For that we are using a Backtracking algorithm.

```Scala
def solve(): SolvedSudokuGrid = {
    val arrayGrid = grid.map(_.toArray).toArray
    var solvedGrid: Option[SolvedSudokuGrid] = None

    def solveHelper(sudoku: Array[Array[Cell]], x: Int = 0, y: Int = 0): Unit = {
      if (y >= 9) {
        solvedGrid = Some(SolvedSudokuGrid(arrayGrid.map(_.map(_.getOrElse(0)).toList).toList))
      } else if (x >= 9) {
        solveHelper(sudoku, 0, y + 1)
      } else if (sudoku(y)(x).isDefined) {
        solveHelper(sudoku, x + 1, y)
      } else (1 to 9).filter(value => validate(sudoku, x, y, value)).foreach { value =>
        sudoku(y)(x) = Some(value)
        solveHelper(sudoku, x + 1, y)
        sudoku(y)(x) = None
      }
    }

    solveHelper(arrayGrid)
    solvedGrid match
      case Some(grid) => grid
      case None => throw new Exception("No solution found")
  }
}
```

## AND then, we check if the solving is correct.

```Scala
case class SolvedSudokuGrid(grid: List[List[SolvedCell]]) extends SudokuGrid[SolvedCell] {
  require(grid.length == 9 && grid.forall(e => e.length == 9 && e.forall(i => i >= 1 && i <= 9)) && isSolved, "Invalid grid (is not solved, check for duplicates)") 
// => Throws error in the case we put a wrong grid or a invalid grid. We are using a [require].

 // We check if the grid is correctly solved.
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
### With the example of our previous grid, here is the final result :

![image](https://github.com/BenjaminLesieux/sudokusolver/assets/73226823/49c39c2b-12de-4282-b3aa-054381609d64)


The grid is correctly solved.


