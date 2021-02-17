package com.dangee1705.xbattle;

public class Player {
	private int id;
	private String name;
	private int colorId;

	public Player(int id, String name, int colorId) {
		this.id = id;
		this.name = name;
		this.colorId = colorId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColorId() {
		return colorId;
	}

	public void setColorId(int colorId) {
		this.colorId = colorId;
	}
}
