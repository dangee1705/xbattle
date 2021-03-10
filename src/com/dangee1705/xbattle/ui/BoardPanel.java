package com.dangee1705.xbattle.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import com.dangee1705.xbattle.model.Board;

public class BoardPanel extends JPanel {
	private static final long serialVersionUID = -8094612097000197130L;
	private static final int TILE_SIZE = 20;
	private Board board;
	private Dimension SIZE;

	public BoardPanel(Board board) {
		this.board = board;
		this.SIZE = new Dimension(TILE_SIZE * board.getWidth() + board.getWidth() + 1, TILE_SIZE * board.getHeight() + board.getHeight() + 1);

		setMinimumSize(SIZE);
		setPreferredSize(SIZE);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		for(int y = 0; y < board.getHeight(); y++) {
			for(int x = 0; x < board.getWidth(); x++) {
				int elevation = board.getCell(x, y).getElevation();
				// g.setColor(elevation < 0 ? new Color(0, 0, 255) : new Color(0, 255, 0));
				g.setColor(
					elevation < 0 ?
					colorLerp(new Color(0, 0, 100), new Color(0, 0, 255), (elevation + 4) / 3f) :
					colorLerp(new Color(0, 255, 0), new Color(127, 127, 0), elevation / 4f)
				);

				g.fillRect((TILE_SIZE + 1) * x + 1, (TILE_SIZE + 1) * y + 1, TILE_SIZE, TILE_SIZE);
			}
		}
	}

	private int clamp(int value, int min, int max) {
		return value < min ? min : value > max ? max : value;
	}

	private Color colorLerp(Color a, Color b, float t) {
		int red = clamp((int) (a.getRed() + (b.getRed() - a.getRed()) * t), 0, 255);
		int green = clamp((int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t), 0, 255);
		int blue = clamp((int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t), 0, 255);
		return new Color(red, green, blue);
	}
}
