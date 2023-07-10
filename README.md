## sudokusolver 
### by LESIEUX Benjamin - LIU Senhua -  MARIOTTE Thomas - PHAM Van Alenn

We are coding together with IntelliJ.

# ZIO 

### **Features** 

- `scala
  import zio.Console.{printLine, readLine}`
  
  **'zio.console'** module provides functions for interacting with the console. For our project of Sudoku Resolver he can printing our Grid in the console.

- `scala impor zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}`
  
  When working with ZIO in Scala, you often need to import various dependencies and types to leverage the power of the library. The zio.Scope import allows you to control the scope and   lifecycle of resources, while the zio.ZIO import provides the main ZIO type for building functional programs. The zio.ZIOAppArgs and zio.ZIOAppDefault imports are related to creating ZIO applications with or without command-line arguments.



# Data Strucutre 

### Grid 

We decided to stay with the JSON format for our Grid. Moreover we thinking that the **double ARRAY** is simplier for represente the grid. 

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

## How we are displaying our grid 
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
```



