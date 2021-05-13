package com.dangee1705.xbattle.model;

public class Cell {
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;

	private Board board;
	private int x;
	private int y;
	private int troops = 0;
	private Player owner = null;
	private int elevation = 0;
	private boolean[] paths = {false, false, false, false};
	private int base = 0;
	private boolean hasUpdate = true;
	
	public Cell(Board board, int x, int y) {
		this.board = board;
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getTroops() {
		return troops;
	}

	public void setTroops(int troops) {
		if(troops != this.troops) {
			this.troops = troops;
			setHasUpdate(true);
		}
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		if(owner != this.owner) {
			this.owner = owner;
			setHasUpdate(true);
		}
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		if(elevation != this.elevation) {
			this.elevation = elevation;
			setHasUpdate(true);
			if(isWater())
				for(int i = 0; i < 4; i++)
					paths[i] = false;
		}
	}

	public boolean getPath(int direction) {
		return paths[direction];
	}

	public void setPath(int direction, boolean active) {
		if(active != paths[direction]) {
			paths[direction] = active;
			setHasUpdate(true);
		}
	}

	public boolean[] getPaths() {
		return paths;
	}

	public void togglePath(int direction) {
		setPath(direction, !getPath(direction));
	}

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		if(base != this.base) {
			this.base = base;
			setHasUpdate(true);
		}
	}

	public boolean getHasUpdate() {
		return hasUpdate;
	}

	public void setHasUpdate(boolean hasUpdate) {
		this.hasUpdate = hasUpdate;
	}

	public int getActivePathCount() {
		int count = 0;
		for(int i = 0; i < 4; i++)
			if(paths[i])
				count++;
		return count;
	}

	public boolean isLand() {
		return getElevation() >= 0;
	}

	public boolean isWater() {
		return getElevation() < 0;
	}

	public void increaseElevation() {
		setElevation(elevation < 4 ? elevation + 1 : 4);
	}

	public void decreaseElevation() {
		setElevation(elevation > -4 ? elevation - 1 : -4);
	}

	public Cell getNorthNeighbour() {
		return y == 0 ? null : board.getCell(x, y - 1);
	}

	public Cell getEastNeighbour() {
		return x == board.getWidth() - 1 ? null : board.getCell(x + 1, y);
	}

	public Cell getSouthNeighbour() {
		return y == board.getHeight() - 1 ? null : board.getCell(x, y + 1);
	}

	public Cell getWestNeighbour() {
		return x == 0 ? null : board.getCell(x - 1, y);
	}

	// public void clearInvalidPaths() {
	// 	if(isWater()) {
	// 		for(int i = 0; i < 4; i++) {
	// 			if(paths[i]) {
	// 				paths[i] = false;
	// 				setHasUpdate(true);
	// 			}
	// 		}
	// 	}
	// 	if((getNorthNeighbour() == null || getNorthNeighbour().isWater()) && paths[NORTH]) {
	// 		paths[NORTH] = false;
	// 		setHasUpdate(true);
	// 	}
	// 	if((getEastNeighbour() == null || getEastNeighbour().isWater()) && paths[EAST]) {
	// 		paths[EAST] = false;
	// 		setHasUpdate(true);
	// 	}
	// 	if((getSouthNeighbour() == null || getSouthNeighbour().isWater()) && paths[SOUTH]) {
	// 		paths[SOUTH] = false;
	// 		setHasUpdate(true);
	// 	}
	// 	if((getWestNeighbour() == null || getWestNeighbour().isWater()) && paths[WEST]) {
	// 		paths[WEST] = false;
	// 		setHasUpdate(true);
	// 	}
	// }

	public void clearPaths() {
		for(int i = 0; i < 4; i++)
			setPath(i, false);
	}

	public void increaseBase() {
		setBase(getBase() < 8 ? getBase() + 1 : 8);
	}

	public void decreaseBase() {
		setBase(getBase() > 0 ? getBase() - 1 : 0);
	}

	@Override
	public String toString() {
		return "Cell<" + x + ", " + y + ">";
	}
}
