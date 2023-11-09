/** 
 * Class representing an efficient implementation of a 2-dimensional table 
 * when lots of repeated entries as a doubly linked list. Idea is to record entry only when a 
 * value changes in the table as scan from left to right through 
 * successive rows.
 * 
 * @author cs62
 * @param <E> type of value stored in the table
 */
package compression;

class CompressedTable<E> implements TwoDTable<E> {
	// List holding table entries - do not change
	// We've made the variables protected to facilitate testing (grading)
	protected CurDoublyLinkedList<Association<RowOrderedPosn, E>> tableInfo;
	protected int numRows, numCols; // Number of rows and cols in table

	/**
	 * Constructor for table of size rows x cols, all of whose values are initially
	 * set to defaultValue
	 * 
	 * @param rows
	 *            # of rows in table
	 * @param cols
	 *            # of columns in table
	 * @param defaultValue
	 *            initial value of all entries in table
	 */
	public CompressedTable(int rows, int cols, E defaultValue) {
		tableInfo = new CurDoublyLinkedList<Association<RowOrderedPosn, E>>();
		this.numRows = rows;
		this.numCols = cols;
		tableInfo.addFirst(new Association<RowOrderedPosn, E>(new RowOrderedPosn(0, 0, rows, cols), defaultValue));
	}

	/**
	 * Given a (x, y, rows, cols) RowOrderedPosn object, it searches for it in the
	 * table which is represented as a doubly linked list with a current pointer. If
	 * the table contains the (x,y) cell, it sets the current pointer to it.
	 * Otherwise it sets it to the closest cell in the table which comes before that
	 * entry.
	 * 
	 * e.g., if the table only contains a cell at (0,0) and you pass the cell (3,3)
	 * it will set the current to (0,0).
	 */
	private void find(RowOrderedPosn findPos) {
		tableInfo.first();
		Association<RowOrderedPosn, E> entry = tableInfo.currentValue();
		RowOrderedPosn pos = entry.getKey();
		while (!findPos.less(pos)) {
			// search through list until pass elt looking for
			tableInfo.next();
			if (tableInfo.isOff()) {
				break;
			}
			entry = tableInfo.currentValue();
			pos = entry.getKey();
		}
		tableInfo.back(); // Since passed desired entry, go back to it.
	}

	/**
	 * Given a legal (row, col) cell in the table, update its value to newInfo. 
	 * 
	 * @param row
	 *            row of cell to be updated
	 * @param col
	 *            column of cell to be update
	 * @param newInfo
	 *            new value to place in cell (row, col)
	 */
	public void updateInfo(int row, int col, E newInfo) {

		if (row < 0 || row > numRows - 1 || col < 0 || col > numCols - 1) {
			return;
		}

		find(new RowOrderedPosn(row, col, numRows, numCols));

		if (!tableInfo.currentValue().getValue().equals(newInfo)) {
			Association<RowOrderedPosn, E> originalCell = tableInfo.currentValue();
			RowOrderedPosn updatePosn = new RowOrderedPosn(row, col, numRows, numCols);
			RowOrderedPosn nextPosn = updatePosn.next();
			tableInfo.addAfterCurrent(new Association<RowOrderedPosn, E>(updatePosn, newInfo));

			if (originalCell.getKey().equals(updatePosn)) {
				tableInfo.remove(new Association<RowOrderedPosn, E>(updatePosn, newInfo));
			}

			DoublyLinkedList<Association<RowOrderedPosn, E> >.Node nextEntry = tableInfo.current.next;

			if (nextPosn != null) {

				if (nextEntry == null) {
					tableInfo.addAfterCurrent(new Association<RowOrderedPosn, E>(nextPosn, originalCell.getValue()));
				} else if (!nextEntry.item.getKey().equals(nextPosn)) {
					tableInfo.addAfterCurrent(new Association<RowOrderedPosn, E>(nextPosn, originalCell.getValue()));
				}

			}

			compress();
		}
	}

	/**
	 * Removes any consecutive repeated value entries in the table
	 * @post There are no entries in the table with the same element
	 * as the one before or after
	 */
	public void compress() {
		tableInfo.first();

		while (!tableInfo.isOff()) {
			tableInfo.next();

			while(!tableInfo.isOff()) {
				if (tableInfo.current.prev == null) {
					tableInfo.next();
				} else if (tableInfo.currentValue().equals(tableInfo.current.prev.item)) {
					tableInfo.remove(tableInfo.current.prev.item);
				} else if (tableInfo.currentValue().getValue().equals(tableInfo.current.prev.item.getValue())) {
					tableInfo.removeCurrent();
				} else {
					break;
				}
			}

		}

	}

	/**
	 * Returns contents of specified cell
	 * 
	 * @pre: (row,col) is legal cell in table
	 * 
	 * @param row
	 *            row of cell to be queried
	 * @param col
	 *            column of cell to be queried
	 * @return value stored in (row, col) cell of table
	 */
	public E getInfo(int row, int col) {
		find(new RowOrderedPosn(row, col, numRows, numCols));
		E cellInfo = tableInfo.currentValue().getValue();
		return cellInfo;  
	}

	/**
	 *  @return
	 *  		 succinct description of contents of table
	 */
	public String toString() { // do not change
	    return tableInfo.otherString();
	}

	public String entireTable() { //do not change
		StringBuilder ans = new StringBuilder("");
		for (int r = 0; r<numRows; r++) {
			for (int c = 0; c< numCols; c++) {
				ans.append(this.getInfo(r, c));
			}
			ans.append("\n");
		}
		return ans.toString();

	}

	/**
	 * program to test implementation of CompressedTable
	 * @param args
	 * 			ignored, as not used in main
	 */
	public static void main(String[] args) {
		
		// add your own tests to make sure your implementation is correct!!
		CompressedTable<String> table = new CompressedTable<String>(5, 6, "x");
		System.out.println(table);
		table.updateInfo(0, 1, "a");
		System.out.println("table is " + table);
		table.updateInfo(0, 1, "x");
		System.out.println("table is " + table);
		table.updateInfo(2, 0, "b");
		System.out.println("table is " + table);
		table.updateInfo(2, 1, "a");
		System.out.println("table is " + table);	
		table.updateInfo(2, 2, "b");
		System.out.println("table is " + table);
		table.updateInfo(2, 3, "r");
		System.out.println("table is " + table);
		table.updateInfo(2, 4, "r");
		System.out.println("table is " + table);
		table.updateInfo(2, 3, "b");
		System.out.println("table is " + table);
		table.updateInfo(4, 3, "b");
		System.out.println("table is " + table);
		table.updateInfo(4, 4, "a");
		System.out.println("table is " + table);
		table.updateInfo(4, 4, "g");
		System.out.println("table is " + table);
		table.updateInfo(2, 1, "b");
		System.out.println("table is " + table);
		table.updateInfo(2, 0, "x");
		System.out.println("table is " + table);
		table.updateInfo(4, 5, "g");
		System.out.println("table is " + table);
		table.updateInfo(21, 5, "b");
		System.out.println("table is " + table);
		//System.out.println(table.getInfo(0, 0));
		//System.out.println(table.getInfo(0, 1));
		
	}

}