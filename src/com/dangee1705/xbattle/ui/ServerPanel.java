package com.dangee1705.xbattle.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.dangee1705.xbattle.model.Server;
import com.dangee1705.xbattle.model.ClientHandler;

public class ServerPanel extends JPanel implements ListCellRenderer<ClientHandler> {

	private static final long serialVersionUID = -8262626154827675947L;
	private DefaultListModel<ClientHandler> clientListModel;
	private JList<ClientHandler> clientList;

	private Server server;
	private JPanel lobbyPanel;

	public ServerPanel() {
		server = new Server();

		setLayout(new BorderLayout());

		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS));
		add(wrapperPanel, BorderLayout.PAGE_START);

		JPanel settingsPanel = new JPanel(new BorderLayout());
		JPanel settingsGridPanel = new JPanel(new GridLayout(3, 2, 10, 10));
		settingsGridPanel.add(new JLabel("Board Width"));
		JSpinner boardWidthSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
		settingsGridPanel.add(boardWidthSpinner);
		settingsGridPanel.add(new JLabel("Board Height"));
		JSpinner boardHeightSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
		settingsGridPanel.add(boardHeightSpinner);
		settingsGridPanel.add(new JLabel("Ticks Per Second"));
		JSpinner ticksPerSecondSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 500, 1));
		settingsGridPanel.add(ticksPerSecondSpinner);
		settingsPanel.add(settingsGridPanel, BorderLayout.CENTER);

		JButton startServerButton = new JButton("Start Server");
		settingsPanel.add(startServerButton, BorderLayout.PAGE_END);

		wrapperPanel.add(settingsPanel);

		startServerButton.addActionListener(event -> {
			server.setBoardWidth((int) boardWidthSpinner.getValue());
			server.setBoardHeight((int) boardHeightSpinner.getValue());
			server.setTicksPerSecond((int) ticksPerSecondSpinner.getValue());
			startServerButton.setEnabled(false);
			startServerButton.setText("Starting...");
			server.start();
		});

		lobbyPanel = new JPanel();
		lobbyPanel.setLayout(new BorderLayout());
		clientListModel = new DefaultListModel<>();
		clientList = new JList<>(clientListModel);
		clientList.setCellRenderer(this);
		JScrollPane clientListScrollPane = new JScrollPane(clientList);
		lobbyPanel.add(clientListScrollPane, BorderLayout.CENTER);
		lobbyPanel.setVisible(false);
		wrapperPanel.add(lobbyPanel);

		JButton startGameButton = new JButton("Start Game");
		startGameButton.setAlignmentX(CENTER_ALIGNMENT);
		lobbyPanel.add(startGameButton, BorderLayout.PAGE_END);

		startGameButton.addActionListener(event -> {
			if(server.getPlayers().size() == 0) {
				JOptionPane.showMessageDialog(this, "No Players Connected!", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				startGameButton.setEnabled(false);
				startGameButton.setText("Game Starting...");
				server.startGame();
			}
		});
		server.addOnStartListener(() -> SwingUtilities.invokeLater(() -> {
			startServerButton.setEnabled(false);
			startServerButton.setText("Running");
			lobbyPanel.setVisible(true);
		}));
		server.addOnStartErrorListener(() -> SwingUtilities.invokeLater(() -> {
			startServerButton.setText("Start Server");
			startServerButton.setEnabled(true);
			lobbyPanel.setVisible(false);
			JOptionPane.showMessageDialog(this, "Could not start the server. Is it already running?", "Error", JOptionPane.ERROR_MESSAGE);
		}));
		server.addOnClientConnectListener(() -> SwingUtilities.invokeLater(() -> updateClientList()));
		server.addOnClientDisconnectListener(() -> SwingUtilities.invokeLater(() -> updateClientList()));
		server.addOnStopListener(() -> SwingUtilities.invokeLater(() -> {
			startServerButton.setText("Start Server");
			startServerButton.setEnabled(true);
			lobbyPanel.setVisible(false);
		}));

		server.addOnGameStartListener(() -> SwingUtilities.invokeLater(() -> {
			startGameButton.setText("Game Started");
		}));
	}

	public void updateClientList() {
		clientListModel.clear();
		for(ClientHandler clientHandler : server.getClientHandlers()) {
			if(clientHandler != null) {
				clientListModel.addElement(clientHandler);
			}
		}
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends ClientHandler> list, ClientHandler value, int index, boolean isSelected, boolean cellHasFocus) {
		ClientHandler clientHandler = (ClientHandler) value;
		JLabel label = new JLabel(clientHandler.getSocket().getInetAddress().getHostName() + " - " + clientHandler.getPlayer().getName());
		label.setForeground(clientHandler.getPlayer().getColorId() == -1 ? Color.BLACK : XBattle.DEFAULT_NAMED_COLORS[clientHandler.getPlayer().getColorId()].getColor());
		label.setBackground(new Color(192, 192, 192));
		return label;
	}
}
