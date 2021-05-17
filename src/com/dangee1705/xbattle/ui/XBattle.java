package com.dangee1705.xbattle.ui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.dangee1705.xbattle.model.NamedColor;

public class XBattle {
	public static final int DEFAULT_PORT = 20000;
	public static final NamedColor[] DEFAULT_NAMED_COLORS = {
		new NamedColor(new Color(255, 0, 0), "Red"),
		new NamedColor(new Color(0, 255, 0), "Green"),
		new NamedColor(new Color(0, 0, 255), "Blue"),
		new NamedColor(new Color(255, 255, 0), "Yellow"),
		new NamedColor(new Color(0, 255, 255), "Cyan"),
		new NamedColor(new Color(255, 0, 255), "Magenta"),
		new NamedColor(new Color(0, 0, 0), "Black"),
		new NamedColor(new Color(255, 255, 255), "White")
	};

	public XBattle() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}

		JFrame jFrame = new JFrame("XBattle by Daniel Gee");

		JTabbedPane jTabbedPane = new JTabbedPane();
		jTabbedPane.addTab("Intro", new IntroPanel());
		jFrame.setContentPane(jTabbedPane);

		jFrame.setSize(600, 800);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
	}

	public static void main(String[] args) {
		new XBattle();
	}
}