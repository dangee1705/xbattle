package com.dangee1705.xbattle;

public class Board {
	private int width;
	private int height;
	private Cell[][] cells;

	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		this.cells = new Cell[height][width];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				this.cells[y][x] = new Cell(x, y);
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Cell getCell(int x, int y) {
		return cells[y][x];
	}

	private void clearEdgePaths() {
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(y == 0)
					getCell(x, y).setPath(0, false);
				else if(y == height - 1)
					getCell(x, y).setPath(2, false);
				if(x == width - 1)
					getCell(x, y).setPath(1, false);
				else if(x == 0)
					getCell(x, y).setPath(3, false);
			}
		}
	}

	private void moveTroops(Cell from, Cell to, int amountToMove) {
		to.setTroops(to.getTroops() - amountToMove);
		from.setTroops(from.getTroops() - amountToMove);
		if(to.getTroops() < 0) {
			to.setTroops(-to.getTroops());
			to.setOwner(from.getOwner());
		}
	}

	public void update() {
		clearEdgePaths();
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				Cell cell = getCell(x, y);
				int activePathCount = cell.getActivePathCount();
				if(activePathCount > 0) {
					int amountToMove = cell.getTroops() / activePathCount;
					if(cell.getPaths()[1])
						moveTroops(cell, getCell(x + 1, y), amountToMove);
					if(cell.getPaths()[3])
						moveTroops(cell, getCell(x - 1, y), amountToMove);
					if(cell.getPaths()[0])
						moveTroops(cell, getCell(x, y + 1), amountToMove);
					if(cell.getPaths()[2])
						moveTroops(cell, getCell(x, y - 1), amountToMove);
				}
			}
		}
	}
}
