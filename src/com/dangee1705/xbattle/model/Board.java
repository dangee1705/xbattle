package com.dangee1705.xbattle.model;

import java.util.Random;

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

	public Board(int width, int height, boolean gen) {
		this(width, height);

		// create a completely random set of cells
		boolean[][] automataSourceCells = new boolean[height][width];
		Random random = new Random();
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				// TODO: make this threshold modifiable
				automataSourceCells[y][x] = random.nextFloat() > 0.5; 
		
		// run the cellular automata
		boolean[][] automataDestinationCells = new boolean[height][width];
		for(int step = 0; step < 10; step++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					int neighbours = 0;
					if(x > 0 && automataSourceCells[y][x - 1])
						neighbours++;
					if(x < width - 1 && automataSourceCells[y][x + 1])
						neighbours++;
					if(y > 0 && automataSourceCells[y - 1][x])
						neighbours++;
					if(y < height - 1 && automataSourceCells[y + 1][x])
						neighbours++;
					
					if(neighbours == 0 || neighbours == 1)
						automataDestinationCells[y][x] = false;
					else if(neighbours == 3 || neighbours == 4)
						automataDestinationCells[y][x] = true;
					else
						automataDestinationCells[y][x] = automataSourceCells[y][x];
				}
			}

			boolean[][] temp = automataSourceCells;
			automataSourceCells = automataDestinationCells;
			automataDestinationCells = temp;
		}

		// convert cellular automata cells from true false into mountains and deep sea
		int[][] sourceElevations = new int[height][width];
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				sourceElevations[y][x] = automataSourceCells[y][x] ? 4 : -4;

		// smooth out the map
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int total = 0;
				int spread = 3;
				for(int dy = -spread; dy <= spread; dy++) {
					for(int dx = -spread; dx <= spread; dx++) {
						if(x + dx >= 0 && x + dx < width && y + dy >= 0 && y + dy < height) {
							total += sourceElevations[y + dy][x + dx];
						} else {
							total--;
						}
					}
				}
				
				// take the average height in this square
				float elevation = total / (float) ((spread * 2 + 1) * (spread * 2 + 1));

				cells[y][x].setElevation(elevation < -4 ? -4 : elevation > 4 ? 4 : Math.round(elevation));
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
