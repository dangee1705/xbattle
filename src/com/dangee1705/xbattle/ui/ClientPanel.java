package com.dangee1705.xbattle.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.dangee1705.xbattle.model.Client;
import com.dangee1705.xbattle.model.NamedColor;

public class ClientPanel extends JPanel {

	private static final long serialVersionUID = -4319791624197181177L;
	private Client client;
	private JComboBox<String> serverAddressComboBox;
	private JComboBox<Integer> serverPortComboBox;
	private JPanel playerSettings;

	public ClientPanel() {
		client = new Client();

		setLayout(new BorderLayout());

		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS));
		add(wrapperPanel, BorderLayout.PAGE_START);

		JPanel settingsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
		settingsPanel.add(new JLabel("Server Address"));
		serverAddressComboBox = new JComboBox<>();
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
		client.addOnConnectListener(() -> SwingUtilities.invokeLater(() -> {
			connectButton.setText("Connected");
			playerSettings.setVisible(true);
		}));
		client.addOnConnetErrorListener(() -> SwingUtilities.invokeLater(() -> {
			connectButton.setEnabled(true);
			connectButton.setText("Connect");
		}));

		playerSettings = new JPanel(new GridLayout(2, 2, 10, 10));
		playerSettings.add(new JLabel("Name"));
		JTextField nameTextField = new JTextField();
		nameTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO: dont send after every keypress only on certain ones
				try {
					client.getPlayer().setName(nameTextField.getText());
					client.sendPlayerUpdate();
				} catch (IOException e1) {
					
				}
			}
			
		});
		playerSettings.add(nameTextField);
		playerSettings.add(new JLabel("Colour"));
		JComboBox<String> colorComboBox = new JComboBox<>();
		colorComboBox.addItem("(Please Select)");
		for(NamedColor namedColor : XBattle.DEFAULT_NAMED_COLORS) {
			colorComboBox.addItem(namedColor.getName());
		}
		colorComboBox.setSelectedIndex(0);
		colorComboBox.addActionListener(event -> {
			int selected = colorComboBox.getSelectedIndex() - 1;
			// make sure there was actually a change
			if(selected != client.getPlayer().getColorId()) {
				client.getPlayer().setColorId(selected);
				try {
					client.sendPlayerUpdate();
				} catch (IOException e) {
					
				}
			}
		});
		playerSettings.add(colorComboBox);
		playerSettings.setVisible(false);
		wrapperPanel.add(playerSettings);

		client.addOnPlayerUpdateListener(() -> SwingUtilities.invokeLater(() -> {
			nameTextField.setText(client.getPlayer().getName());
			colorComboBox.setSelectedIndex(client.getPlayer().getColorId() + 1);
		}));

		
		// JPanel gamePanel = new JPanel(new BorderLayout());
		// wrapperPanel.add(gamePanel);

		client.addOnGameStartListener(() -> SwingUtilities.invokeLater(() -> {
			add(new BoardPanel(client.getBoard()), BorderLayout.CENTER);
		}));
	}
}
