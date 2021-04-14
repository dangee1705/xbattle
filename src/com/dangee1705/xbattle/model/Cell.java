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

	public int getY() {
		return y;
	}

	public int getTroops() {
		return troops;
	}

	public void setTroops(int troops) {
		if(troops != this.troops) {
			this.troops = troops;
			hasUpdate = true;
		}
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		if(owner != this.owner) {
			this.owner = owner;
			hasUpdate = true;
		}
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		if(elevation != this.elevation) {
			this.elevation = elevation;
			hasUpdate = true;
		}
	}

	public boolean[] getPaths() {
		return paths;
	}

	public boolean getPath(int direction) {
		return paths[direction];
	}

	public void setPath(int direction, boolean active) {
		if(active != paths[direction]) {
			paths[direction] = active;
			hasUpdate = true;
		}
	}

	public void togglePath(int direction) {
		setPath(direction, !getPath(direction));
		hasUpdate = true;
	}

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		if(base != this.base) {
			this.base = base;
			hasUpdate = true;
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
}
