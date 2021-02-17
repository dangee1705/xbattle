package com.dangee1705.xbattle;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ClientPanel extends JPanel {

	private static final long serialVersionUID = -4319791624197181177L;
	private Client client;
	private JComboBox<String> serverAddressComboBox;
	private JComboBox<Integer> serverPortComboBox;

	public ClientPanel() {
		client = new Client();

		setLayout(new BorderLayout());

		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS));
		add(wrapperPanel, BorderLayout.PAGE_START);

		JPanel settingsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
		settingsPanel.add(new JLabel("Server Address"));
		serverAddressComboBox = new JComboBox<>( );
		serverAddressComboBox.setEditable(true);
		serverAddressComboBox.addItem("localhost");
		settingsPanel.add(serverAddressComboBox);
		settingsPanel.add(new JLabel("Server Port"));
		serverPortComboBox = new JComboBox<>();
		serverPortComboBox.setEditable(true);
		serverPortComboBox.addItem(XBattle.DEFAULT_PORT);
		settingsPanel.add(serverPortComboBox);
		wrapperPanel.add(settingsPanel);

		JButton connectButton = new JButton("Connect");
		connectButton.setAlignmentX(CENTER_ALIGNMENT);
		wrapperPanel.add(connectButton);

		connectButton.addActionListener(event -> {
			// set the server address and port specified by the user
			client.setServerAddress((String) serverAddressComboBox.getSelectedItem());
			client.setServerPort((int) serverPortComboBox.getSelectedItem());

			// tell the client to try to connect to the server
			client.connect();

			// stop them clicking the button again until either the connection succeeds or fails
			connectButton.setEnabled(false);
			connectButton.setText("Connecting...");
		});
		client.addOnConnectListener(() -> SwingUtilities.invokeLater(() -> connectButton.setText("Connected")));
		client.addOnConnetErrorListener(() -> SwingUtilities.invokeLater(() -> {
			connectButton.setEnabled(true);
			connectButton.setText("Connect");
		}));

		// JPanel lobbyPanel = new JPanel();
		// wrapperPanel.add(lobbyPanel);

		// JButton startGameButton = new JButton("Start Game");
		// startGameButton.setAlignmentX(CENTER_ALIGNMENT);
		// wrapperPanel.add(startGameButton);
	}
}
