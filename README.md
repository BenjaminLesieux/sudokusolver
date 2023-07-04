## sudokusolver LIU Senhua - LESIEUX Benjamin -  MARIOTTE Thomas - PHAM Van Alenn

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
    [5, 3, 0, 0, 7, 0, 0, 0, 0],
    [6, 0, 0, 1, 9, 5, 0, 0, 0],
    [0, 9, 8, 0, 0, 0, 0, 6, 0],
    [8, 0, 0, 0, 6, 0, 0, 0, 3],
    [4, 0, 0, 8, 0, 3, 0, 0, 1],
    [7, 0, 0, 0, 2, 0, 0, 0, 6],
    [0, 6, 0, 0, 0, 0, 2, 8, 0],
    [0, 0, 0, 4, 1, 9, 0, 0, 5],
    [0, 0, 0, 0, 8, 0, 0, 7, 9]
  ]
}

```
We'll use Nil to find out when our cell will be empty
