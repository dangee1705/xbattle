package com.dangee1705.xbattle.model;

import java.awt.Color;

public class NamedColor {
	private Color color;
	private String name;

	public NamedColor(Color color, String name) {
		this.color = color;
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public String getName() {
		return name;
	}
}
