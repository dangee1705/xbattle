package com.dangee1705.xbattle.model;

public class Cell {
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;

	private int x;
	private int y;
	private int troops = 0;
	private Player owner = null;
	private int elevation = 0;
	private boolean[] paths = {false, false, false, false};
	private int base = 0;
	private boolean hasUpdate = true;

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getTroops() {
		return troops;
	}

	public void setTroops(int troops) {
		this.troops = troops;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public boolean[] getPaths() {
		return paths;
	}

	public void setPath(int direction, boolean active) {
		this.paths[direction] = active;
	}

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		this.base = base;
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
}
