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
import com.dangee1705.xbattle.model.Server.ClientHandler;

public class ServerPanel extends JPanel implements ListCellRenderer<ClientHandler> {

	private static final long serialVersionUID = -8262626154827675947L;
	private DefaultListModel<ClientHandler> clientListModel;
	private JList<ClientHandler> clientList;

	private Server server;
	// TODO: hide lobby panel when server is not on
	private JPanel lobbyPanel;

	public ServerPanel() {
		server = new Server();

		setLayout(new BorderLayout());

		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS));
		add(wrapperPanel, BorderLayout.PAGE_START);

		JPanel settingsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
		settingsPanel.add(new JLabel("Board Width"));
		JSpinner boardWidthSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 100, 1));
		settingsPanel.add(boardWidthSpinner);
		settingsPanel.add(new JLabel("Board Height"));
		JSpinner boardHeightSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 100, 1));
		settingsPanel.add(boardHeightSpinner);
		settingsPanel.add(new JLabel("Ticks Per Second"));
		JSpinner ticksPerSecondSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));
		settingsPanel.add(ticksPerSecondSpinner);
		wrapperPanel.add(settingsPanel);

		// TODO: stop server too
		JButton startServerButton = new JButton("Start Server");
		startServerButton.setAlignmentX(CENTER_ALIGNMENT);
		wrapperPanel.add(startServerButton);

		startServerButton.addActionListener(event -> {
			server.setBoardWidth((int) boardWidthSpinner.getValue());
			server.setBoardHeight((int) boardHeightSpinner.getValue());
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
		wrapperPanel.add(lobbyPanel);

		JButton startGameButton = new JButton("Start Game");
		startGameButton.setAlignmentX(CENTER_ALIGNMENT);
		wrapperPanel.add(startGameButton);

		startGameButton.addActionListener(event -> {
			if(server.getPlayers().size() == 0) {
				JOptionPane.showMessageDialog(this, "No Players Connected!", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				server.startGame();
			}
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
	}

	public void updateClientList() {
		clientListModel.clear();
		for(Server.ClientHandler clientHandler : server.getClientHandlers()) {
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
