package edu.lockhaven.bloom_frogsandtoads;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Frogs and Toads
 * @version 1.1
 * @author Michael Bloom
 */
public class FrogsAndToads implements Serializable {

	private static final long serialVersionUID = 6125612625697345892L;

	private Stack<int[]> previousMoves = new Stack<>();
	private int[] emptyCords = new int[2];
	private char[][] grid;
	
	private final static char EMPTY_SPACE = '-';
	private final static char FROG_SPACE = 'F';
	private final static char TOAD_SPACE = 'T';
	
	/**
	 * Creates a game of default size.
	 */
	public FrogsAndToads() {
		this(5);
	};

	/**
	 * Creates a new game on a square grid.
	 * @param size is the length of the square on each side.
	 */
	public FrogsAndToads(int size) {
		this(size, size);
	}
	
	/**
	 * Creates a new game on a rectangular grid.
	 * @param rows is the vertical length.
	 * @param columns is the horizontal length.
	 */
	public FrogsAndToads(int rows, int columns) {
		
		//Checks if the rows are even. If so, increment by 1.
		//NOTE: Rows need to be odd for the game to work.
		if(rows % 2 == 0) {
			++rows;
		}
		
		//Checks if the columns are even. If so, increment by 1.
		//NOTE: Columns need to be odd for the game to work.
		if(columns % 2 == 0) {
			++columns;
		}
		
		//Specify the dimensions of the grid.
		this.grid = new char[rows][columns];
		
		//Divide the grid in two.
		final int HALF_ROWS = rows / 2;
		final int HALF_COLUMNS = columns / 2;
		
		//Generate the grid.
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < columns; c++) {
				
				if(r < HALF_ROWS || (c < HALF_COLUMNS && !(r > HALF_ROWS))) {
					//Creates the first half of the rows with frogs.
					this.grid[r][c] = FrogsAndToads.FROG_SPACE;
					
				} else if(r > HALF_ROWS || (c > HALF_COLUMNS && !(r < HALF_ROWS))) {
					//Creates the second half of the rows with toads.
					this.grid[r][c] = FrogsAndToads.TOAD_SPACE;
					
				} else {
					//Sets what is assumed to be the middle space as empty.
					//Sets the coordinates of where the empty space is.
					this.grid[r][c] = FrogsAndToads.EMPTY_SPACE;
					this.setEmptyCords(r, c);
				}
				
			}
		}
		
	}
	
	/**
	 * Retrieves the number of rows within the game space.
	 * @return rows length
	 */
	private int getRowLength() {
		return this.grid.length;
	}
	
	/**
	 * Retrieves the number of columns within the game space.
	 * @return column length
	 */
	private int getColumnLength(){
		return this.grid[0].length;
	}
	
	/**
	 * Retrieves the row of the empty space.
	 * @return empty space row
	 */
	private int getEmptySpaceRow() {
		return this.emptyCords[0];
	}
	
	/**
	 * Retrieves the column of the empty space.
	 * @return empty space column
	 */
	private int getEmptySpaceColumn() {
		return this.emptyCords[1];
	}
	
	/**
	 * Sets the empty coordinates.
	 * @param row of the cell with the empty space.
	 * @param column of the cell with the empty space.
	 */
	private void setEmptyCords(int row, int column) {
		this.emptyCords[0] = row;
		this.emptyCords[1] = column;
	}
	
	/**
	 * Returns true if the there is at least one legal move.
	 * @return if there is a legal move.
	 */
	public boolean canMove() {
		return !this.getLegalMoves().isEmpty();
	}
	
	/**
	 * Returns true if the empty space is at (row, column).
	 * @param row of the cell that is checked for an empty space.
	 * @param column of the cell that is check for an empty space.
	 * @return if the cell contains an empty space.
	 */
	public boolean emptyAt(int row, int column) {
		return ((row >= 0 && row < this.grid.length) 
				&& (column >= 0 && column < this.grid[0].length) 
				&& this.grid[row][column] == FrogsAndToads.EMPTY_SPACE);
	}
	
	/**
	 * Returns true there is a frog at (row, column).
	 * @param row of the cell that is check for a frog.
	 * @param column of the cell that is check for a frog.
	 * @return if the cell contains a frog.
	 */
	public boolean frogAt(int row, int column) {
		return ((row >= 0 && row < this.grid.length) 
				&& (column >= 0 && column < this.grid[0].length) 
				&& this.grid[row][column] == FrogsAndToads.FROG_SPACE);
	}
	
	/**
	 * Returns true if there is a toad at (row, column).
	 * @param row of the cell that is checked for a toad.
	 * @param column of the cell that is checked for a toad.
	 * @return if the cell contains a toad.
	 */
	public boolean toadAt(int row, int column) {
		return ((row >= 0 && row < this.grid.length)
				&& (column >= 0 && column < this.grid[0].length)
				&& this.grid[row][column] == FrogsAndToads.TOAD_SPACE);
	}
	
	/**
	 * Returns true if the positions of the frogs
	 *  and toads in the starting configuration
	 *  have been interchanged (game over).
	 * @return if the user won the game.
	 */
	public boolean over() {
		
		//Over function is similar to the constructor.
		//	The only difference is that the over function
		//	checks if the grid opposite to the starting grid.
		
		//Divide the grid in two.
		final int HALF_ROWS = this.getRowLength() / 2;
		final int HALF_COLUMNS = this.getColumnLength() / 2;
		
		//Iterate through the grid.
		for(int r = 0; r < this.getRowLength(); r++) {
			for(int c = 0; c < this.getColumnLength(); c++) {
				
				if(r < HALF_ROWS || (c < HALF_COLUMNS && !(r > HALF_ROWS))) {
					//Checks if the first half of rows are toads.
					if(this.grid[r][c] != FrogsAndToads.TOAD_SPACE) {
						return false;
					}
					
				} else if(r > HALF_ROWS || (c > HALF_COLUMNS && !(r < HALF_ROWS))) {
					//Checks if the second half of the rows are frogs.
					if(this.grid[r][c] != FrogsAndToads.FROG_SPACE) {
						return false;
					}
					
				} else {
					//Checks if the empty space occupies what should be the middle.
					if(this.grid[r][c] != FrogsAndToads.EMPTY_SPACE) {
						return false;
					}
					
				}
				
			}
		}
		
		return true;
	}
	
	/**
	 * Makes a move at cell (row, column).
	 * @param row of the cell to be swapped with the empty space.
	 * @param column of the cell to be swapped with the empty space.
	 * @return true if a move was successful.
	 */
	public boolean move(int row, int column) {
		
		//Checks all possible legal moves.
		//Was not able to get contains function to work properly
		for(int[] move : this.getLegalMoves()) {
			
			//Checks if input is a valid move.
			if(move[0] == row && move[1] == column) {
				
				//Swaps the empty space with the other item.
				char swap = this.grid[row][column];
				this.grid[this.getEmptySpaceRow()][this.getEmptySpaceColumn()] = swap;
		
				int[] cords = {this.getEmptySpaceRow(), this.getEmptySpaceColumn()};
				this.previousMoves.push(cords);
				
				this.grid[row][column] = FrogsAndToads.EMPTY_SPACE;
				this.setEmptyCords(row, column);
				return true;
			}
			
		}

		return false;
	}
	
	/**
	 * Undoes the most recent move.
	 * @return true if an undo was successful.
	 */
	public boolean undo() {
		
		//Checks if stack is empty.
		if(this.previousMoves.isEmpty()) {
			return false;
		}
		
		//Pops the previous move off the stack and swaps the empty space 
		//	with the space that was moved.
		int[] cords = this.previousMoves.pop();
		char swap = this.grid[cords[0]][cords[1]];
		this.grid[this.getEmptySpaceRow()][this.getEmptySpaceColumn()] = swap;
		
		this.grid[cords[0]][cords[1]] = FrogsAndToads.EMPTY_SPACE;
		this.setEmptyCords(cords[0], cords[1]);

		return true;
	}
	
	/**
	 * Returns a list of legal moves from the current configuration.
	 * @return list of legal moves.
	 */
	public List<int[]> getLegalMoves(){
		
		//Most of the code below involves checking one to two spaces from the empty space.
		//	An example, if the first space down has a toad, then it counts as a valid
		//	move; other conditions will be checked. If the first space down has 
		//	no toad, then an assumption can be made that a frog inhabits that space; 
		//	the next space below would need to be checked before checking other directions.
		
		//Legal Moves
		//A toad is moving right or up into the empty space.
		//A frog is moving left or down into the empty space.
		//A frog can hop over a toad into the empty space.
		//A toad can hop over a frog into the empty space.
		
		//Create a list for valid moves.
		ArrayList<int[]> legalMoves = new ArrayList<>();
		
		//Temporary variable for adding valid moves to the list.
		int[] cords = new int[2];
			
		//Checks below the empty space for a valid toad move.
		if(this.toadAt(this.getEmptySpaceRow() + 1, this.getEmptySpaceColumn())) {
			cords[0] = this.getEmptySpaceRow() + 1;
			cords[1] = this.getEmptySpaceColumn();
			legalMoves.add(cords.clone());
			
		} else if(this.toadAt(this.getEmptySpaceRow() + 2, this.getEmptySpaceColumn())) {
			cords[0] = this.getEmptySpaceRow() + 2;
			cords[1] = this.getEmptySpaceColumn();
			legalMoves.add(cords.clone());
			
		}
		
		//Checks right of the empty space for a valid toad move.
		if(this.toadAt(this.getEmptySpaceRow(), this.getEmptySpaceColumn() + 1)) {
			cords[0] = this.getEmptySpaceRow();
			cords[1] = this.getEmptySpaceColumn() + 1;
			legalMoves.add(cords.clone());
			
		} else if(this.toadAt(this.getEmptySpaceRow(), this.getEmptySpaceColumn() + 2)) {
			cords[0] = this.getEmptySpaceRow();
			cords[1] = this.getEmptySpaceColumn() + 2;
			legalMoves.add(cords.clone());
			
		}
	
		//Checks above the empty space for a valid frog move.
		if(this.frogAt(this.getEmptySpaceRow() - 1, this.getEmptySpaceColumn())) {
			cords[0] = this.getEmptySpaceRow() - 1;
			cords[1] = this.getEmptySpaceColumn();
			legalMoves.add(cords.clone());
			
		} else if(this.frogAt(getEmptySpaceRow() - 2, this.getEmptySpaceColumn())) {
			cords[0] = this.getEmptySpaceRow() - 2;
			cords[1] = this.getEmptySpaceColumn();
			legalMoves.add(cords.clone());
			
		}
		
		//Checks left of the empty space for a valid frog move.
		if(this.frogAt(this.getEmptySpaceRow(), this.getEmptySpaceColumn() - 1)) {
			cords[0] = this.getEmptySpaceRow();
			cords[1] = this.getEmptySpaceColumn() - 1;
			legalMoves.add(cords.clone());
			
		} else if(this.frogAt(this.getEmptySpaceRow(), this.getEmptySpaceColumn() - 2)) {
			cords[0] = this.getEmptySpaceRow();
			cords[1] = this.getEmptySpaceColumn() - 2;
			legalMoves.add(cords.clone());
			
		}
		
		return legalMoves;
		
	}
	
	@Override
	public String toString() {
		
		//For constructing strings.
		StringBuilder builder = new StringBuilder("   ");
		
		for(int c = 0; c < this.getColumnLength(); c++) {
			builder.append(c).append(" ");
		}
		
		//Iterates through all the columns and rows within the array.
		for(int r = 0; r < this.getRowLength(); r++) {
			builder.append('\n');
			builder.append(r).append(" ");
			for(int c = 0; c < this.getColumnLength(); c++) {
				builder.append(" ").append(this.grid[r][c]);
			}
		}
		
		//Returns the newly constructed string.
		return builder.toString();
	}
	
}
