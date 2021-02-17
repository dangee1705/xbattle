package com.dangee1705.xbattle;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class ServerPanel extends JPanel {

	private static final long serialVersionUID = -8262626154827675947L;
	private DefaultListModel<String> clientListModel;
	private JList<String> clientList;

	private Server server;

	public ServerPanel() {
		server = new Server();

		setLayout(new BorderLayout());

		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS));
		add(wrapperPanel, BorderLayout.PAGE_START);

		JPanel settingsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
		settingsPanel.add(new JLabel("Board Width"));
		JSpinner boardWidthSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
		settingsPanel.add(boardWidthSpinner);
		settingsPanel.add(new JLabel("Board Height"));
		JSpinner boardHeightSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
		settingsPanel.add(boardHeightSpinner);
		settingsPanel.add(new JLabel("Ticks Per Second"));
		JSpinner ticksPerSecondSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));
		settingsPanel.add(ticksPerSecondSpinner);
		wrapperPanel.add(settingsPanel);

		JButton startServerButton = new JButton("Start Server");
		startServerButton.setAlignmentX(CENTER_ALIGNMENT);
		wrapperPanel.add(startServerButton);

		JPanel lobbyPanel = new JPanel();
		lobbyPanel.setLayout(new BorderLayout());
		clientListModel = new DefaultListModel<>();
		clientList = new JList<>(clientListModel);
		JScrollPane clientListScrollPane = new JScrollPane(clientList);
		lobbyPanel.add(clientListScrollPane, BorderLayout.CENTER);
		wrapperPanel.add(lobbyPanel);

		JButton startGameButton = new JButton("Start Game");
		startGameButton.setAlignmentX(CENTER_ALIGNMENT);
		wrapperPanel.add(startGameButton);

		startServerButton.addActionListener(event -> {
			startServerButton.setEnabled(false);
			startServerButton.setText("Starting...");
			server.start();
		});
		server.addOnStartListener(() -> SwingUtilities.invokeLater(() -> {
			startServerButton.setEnabled(false);
			startServerButton.setText("Running");
		}));
		server.addOnStartErrorListener(() -> SwingUtilities.invokeLater(() -> {
			startServerButton.setText("Start Server");
			startServerButton.setEnabled(true);
		}));
		server.addOnClientConnectListener(() -> SwingUtilities.invokeLater(() -> updateClientList()));
		server.addOnClientDisconnectListener(() -> SwingUtilities.invokeLater(() -> updateClientList()));
		server.addOnStopListener(() -> SwingUtilities.invokeLater(() -> {
			startServerButton.setText("Start Server");
			startServerButton.setEnabled(true);
		}));
		startGameButton.addActionListener(event -> {
			server.sendGameStart();
		});
	}

	public void updateClientList() {
		clientListModel.clear();
		for(Server.ClientHandler clientHandler : server.getClientHandlers()) {
			clientListModel.addElement(clientHandler.getSocket() + ", " + clientHandler.getPlayer());
		}
	}
}
