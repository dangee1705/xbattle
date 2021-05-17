package com.dangee1705.xbattle.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

public class Board implements Iterable<Cell> {
	private ArrayList<Player> players;
	private int width;
	private int height;
	private Cell[][] cells;
	private Random random = new Random();

	public Board(ArrayList<Player> players, int width, int height) {
		this.players = players;
		this.width = width;
		this.height = height;
		this.cells = new Cell[height][width];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				this.cells[y][x] = new Cell(this, x, y);
			}
		}
		this.clearHasUpdates();
	}

	public Board(ArrayList<Player> players, int width, int height, float threshold, int steps) {
		this(players, width, height);

		int landCells;
		do {
			// create a completely random set of cells
			boolean[][] automataSourceCells = new boolean[height][width];
			for(int y = 0; y < height; y++)
				for(int x = 0; x < width; x++)
					automataSourceCells[y][x] = random.nextFloat() > threshold; 
			
			// run the cellular automata
			boolean[][] automataDestinationCells = new boolean[height][width];
			for(int step = 0; step < steps; step++) {
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

			landCells = 0;
			for(int y = 0; y < height; y++)
				for(int x = 0; x < width; x++)
					if(cells[y][x].isLand())
						landCells++;
		} while(landCells < players.size() * 10);

		int[] indices = new int[width * height];
		for(int i = 0; i < width * height; i++)
			indices[i] = i;
		for(int i = 0; i < width * height; i++) {
			int r = random.nextInt(width * height);
			int temp = indices[i];
			indices[i] = indices[r];
			indices[r] = temp;
		}

		int playerI = 0;
		for(int i = 0; i < width * height; i++) {
			Cell cell = getCell(indices[i] % width, indices[i] / width);
			if(cell.isLand()) {
				cell.setOwner(this.players.get(playerI++));
				cell.setTroops(100);
				cell.setBase(8);
				if(playerI >= players.size())
					break;
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

	public void clearInvalidPaths() {
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				Cell cell = getCell(x, y);

				// clear edge paths
				if(y == 0)
					cell.setPath(Cell.NORTH, false);
				else if(y == height - 1)
					cell.setPath(Cell.SOUTH, false);
				if(x == width - 1)
					cell.setPath(Cell.EAST, false);
				else if(x == 0)
					cell.setPath(Cell.WEST, false);

				// clear paths which are in water or unowned
				if(cell.isWater() || cell.getOwner() == null)
					for(int direction = 0; direction < 4; direction++)
						cell.setPath(direction, false);
				if(cell.isWater()) {
					cell.setOwner(null);
					cell.setTroops(0);
					cell.setBase(0);
				}
			}
		}
	}

	private void moveTroops(Cell from, Cell to, int amountToMove) {
		if(from.getOwner() == to.getOwner()) {
			while(to.getTroops() + amountToMove > 100)
				amountToMove--;
			to.setTroops(to.getTroops() + amountToMove);
			from.setTroops(from.getTroops() - amountToMove);
		} else {
			to.setTroops(to.getTroops() - amountToMove);
			from.setTroops(from.getTroops() - amountToMove);

			if(to.getTroops() < 0) {
				to.setTroops(-to.getTroops());
				to.setOwner(from.getOwner());
				to.clearPaths();
			}
		}
	}

	private int elevationChange(Cell one, Cell two) {
		return Math.abs(two.getElevation() - one.getElevation());
	}

	public Player winner() {
		Player player = null;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				Cell cell = getCell(x, y);
				if(player == null && cell.getOwner() != null)
					player = cell.getOwner();
				else if(player != null && player != cell.getOwner() && cell.getOwner() != null)
					return null;
			}
		}
		return player;
	}

	public void update() {
		synchronized(this) {
			clearInvalidPaths();

			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					// get the cell
					Cell cell = getCell(x, y);

					// add troops to bases
					if(cell.getBase() == 8 && cell.getTroops() < 100)
						cell.setTroops(cell.getTroops() + 1);
				}
			}

			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					// get the cell
					Cell cell = getCell(x, y);

					// get neighbours
					Cell northCell = cell.getNorthNeighbour();
					Cell eastCell = cell.getEastNeighbour();
					Cell southCell = cell.getSouthNeighbour();
					Cell westCell = cell.getWestNeighbour();

					int activePathCount = cell.getActivePathCount();
					if(activePathCount > 0 && cell.getTroops() > activePathCount) {
						int toMove = cell.getTroops() / activePathCount;

						if(northCell != null && northCell.isLand() && cell.getPath(Cell.NORTH))
							moveTroops(cell, northCell, Math.min(toMove, 5) / (1 + elevationChange(cell, northCell) / 2));
						if(eastCell != null && eastCell.isLand() && cell.getPath(Cell.EAST))
							moveTroops(cell, eastCell, Math.min(toMove, 5) / (1 + elevationChange(cell, eastCell) / 2));
						if(southCell != null && southCell.isLand() && cell.getPath(Cell.SOUTH))
							moveTroops(cell, southCell, Math.min(toMove, 5) / (1 + elevationChange(cell, southCell) / 2));
						if(westCell != null && westCell.isLand() && cell.getPath(Cell.WEST))
							moveTroops(cell, westCell, Math.min(toMove, 5) / (1 + elevationChange(cell, westCell) / 2));
					}
				}
			}
		}
	}

	public void clearHasUpdates() {
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				getCell(x, y).setHasUpdate(false);
	}

	@Override
	public Iterator<Cell> iterator() {
		return new Iterator<Cell>(){
			private int x = 0;
			private int y = 0;

			@Override
			public boolean hasNext() {
				return y < getHeight();
			}

			@Override
			public Cell next() {
				if(!hasNext())
					throw new NoSuchElementException("No more cells!");

				Cell cell = getCell(x, y);
				x++;
				if(x == getWidth()) {
					x = 0;
					y++;
				}
				return cell;
			}
		};
	}
}
