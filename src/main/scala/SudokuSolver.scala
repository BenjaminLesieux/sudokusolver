object SudokuSolver {
  
    def solveSudoku(grid: List[List[Option[Int]]]) : Option[List[List[Option[Int]]]] = {
        def solve(grid: List[List[Option[Int]]], row: Int, col: Int): Option[List[List[Option[Int]]]] = {
            if (row == 9) {
                Some(grid)
            } else if (col == 9) {
                solve(grid, row + 1, 0) //next row
            } else {
                grid(row)(col) match {
                    case Some(value) => solve(grid, row, col + 1)
                    case None => {
                        val possibleValues = getPossibleValues(grid, row, col)
                        val solutions = possibleValues.map { value =>
                            solve(grid.updated(row, grid(row).updated(col, Some(value))), row, col + 1)
                        }
                        solutions.find(_.isDefined).flatten
                    }
                }
            }
        }
    }
    
}
