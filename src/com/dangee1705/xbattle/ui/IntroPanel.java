package com.dangee1705.xbattle.ui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class IntroPanel extends JPanel {
	public IntroPanel() {
		JButton addServerTabButton = new JButton("Add Server");
		addServerTabButton.addActionListener(e -> {
			JTabbedPane parent = (JTabbedPane) getParent();
			ServerPanel serverPanel = new ServerPanel();
			parent.addTab("Server", serverPanel);
			parent.setSelectedComponent(serverPanel);
		});

		JButton addClientTabButton = new JButton("Add Client");
		addClientTabButton.addActionListener(e -> {
			JTabbedPane parent = (JTabbedPane) getParent();
			ClientPanel clientPanel = new ClientPanel();
			parent.addTab("Client", clientPanel);
			parent.setSelectedComponent(clientPanel);
		});

		JPanel wrapperPanel = new JPanel(new BorderLayout());
		wrapperPanel.add(addServerTabButton, BorderLayout.PAGE_START);
		wrapperPanel.add(addClientTabButton, BorderLayout.PAGE_END);

		setLayout(new BorderLayout());
		add(wrapperPanel, BorderLayout.PAGE_START);
	}
}
