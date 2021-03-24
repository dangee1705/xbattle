package com.dangee1705.xbattle.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import com.dangee1705.xbattle.model.Board;

public class BoardPanel extends JPanel implements Runnable, MouseWheelListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -8094612097000197130L;

	private static final int TILE_SIZE = 50;

	private Board board;

	private int lastMousePressX = 0;
	private int lastMousePressY = 0;
	private int offsetX = 0;
	private int offsetY = 0;
	private int mouseX = 0;
	private int mouseY = 0;

	public BoardPanel(Board board) {
		this.board = board;

		addMouseWheelListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		(new Thread(this)).start();
	}

	@Override
	public void run() {
		while(true) {
			repaint();
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if(board.getWidth() * TILE_SIZE < getWidth()) {
			offsetX = (getWidth() - (board.getWidth() * TILE_SIZE)) / -2;
		} else {
			if(offsetX < 0)
				offsetX = 0;
			if((board.getWidth() * TILE_SIZE) - offsetX < getWidth())
				offsetX = board.getWidth() * TILE_SIZE - getWidth();
		}
		if(board.getHeight() * TILE_SIZE < getHeight()) {
			offsetY = (getHeight() - (board.getHeight() * TILE_SIZE)) / -2;
		} else {
			if(offsetY < 0)
				offsetY = 0;
			if((board.getHeight() * TILE_SIZE) - offsetY < getHeight())
				offsetY = board.getHeight() * TILE_SIZE - getHeight();
		}

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		for(int y = 0; y < board.getHeight(); y++) {
			for(int x = 0; x < board.getWidth(); x++) {
				int elevation = board.getCell(x, y).getElevation();
				g.setColor(
					elevation < 0 ?
					colorLerp(new Color(0, 0, 100), new Color(0, 0, 255), (elevation + 4) / 3f) :
					colorLerp(new Color(0, 255, 0), new Color(127, 127, 0), elevation / 4f)
				);

				g.fillRect(TILE_SIZE * x - offsetX, TILE_SIZE * y - offsetY, TILE_SIZE, TILE_SIZE);
			}
		}

		g.setColor(Color.WHITE);
		int cursorThickness = 5;
		g.fillRect((mouseX / TILE_SIZE) * TILE_SIZE - offsetX, (mouseY / TILE_SIZE) * TILE_SIZE - offsetY, TILE_SIZE, cursorThickness);
		g.fillRect((mouseX / TILE_SIZE) * TILE_SIZE - offsetX, (mouseY / TILE_SIZE) * TILE_SIZE - offsetY, cursorThickness, TILE_SIZE);
		g.fillRect((mouseX / TILE_SIZE) * TILE_SIZE + TILE_SIZE - cursorThickness - offsetX, (mouseY / TILE_SIZE) * TILE_SIZE - offsetY, cursorThickness, TILE_SIZE);
		g.fillRect((mouseX / TILE_SIZE) * TILE_SIZE - offsetX, (mouseY / TILE_SIZE) * TILE_SIZE + TILE_SIZE - cursorThickness - offsetY, TILE_SIZE, cursorThickness);
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

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// if(e.getWheelRotation() == -1) {
		// 	ZOOM++;
		// 	offsetX = (int) (offsetX * ((ZOOM - 1) / ZOOM));
		// 	offsetY = (int) (offsetY * ((ZOOM - 1) / ZOOM));
		// } else if(e.getWheelRotation() == 1) {
		// 	ZOOM--;
		// 	if(ZOOM < 1)
		// 		ZOOM = 1;
		// 	offsetX = (int) (offsetX * ((ZOOM + 1) / ZOOM));
		// 	offsetY = (int) (offsetY * ((ZOOM + 1) / ZOOM));
		// }
		// System.out.println(ZOOM + ", " + offsetX + ", " + offsetY);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lastMousePressX = e.getX() + offsetX;
		lastMousePressY = e.getY() + offsetY;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		offsetX = lastMousePressX - e.getX();
		offsetY = lastMousePressY - e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX() + offsetX;
		mouseY = e.getY() + offsetY;
	}
}
