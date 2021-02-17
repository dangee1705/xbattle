package com.dangee1705.xbattle;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class XBattle {
	public static final int DEFAULT_PORT = 20000;

	public XBattle() {
		JFrame jFrame = new JFrame("XBattle by Daniel Gee");

		JTabbedPane jTabbedPane = new JTabbedPane();
		jTabbedPane.addTab("Server", new ServerPanel());
		jTabbedPane.addTab("Client", new ClientPanel());
		jTabbedPane.addTab("Client", new ClientPanel());
		jFrame.setContentPane(jTabbedPane);

		jFrame.setSize(600, 600);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
	}

	public static void main(String[] args) {
		new XBattle();
	}
}