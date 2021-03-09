package com.dangee1705.xbattle.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {
	
	private String serverAddress;
	private int serverPort;
	private Socket socket;
	private boolean running = false;
	private Thread thread;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;

	private Player player = new Player(-1, "Player", -1);
	private ArrayList<Player> players = new ArrayList<>();
	private Board board;

	private Listeners onConnectListeners = new Listeners();
	private Listeners onConnectErrorListeners = new Listeners();

	public Client() {

	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public void connect() {
		if(!running) {
			running = true;
			thread = new Thread(this, "Client-Thread");
			thread.start();
		}
	}

	public void addOnConnectListener(Listener listener) {
		onConnectListeners.add(listener);
	}


	public void addOnConnetErrorListener(Listener listener) {
		onConnectErrorListeners.add(listener);
	}

	@Override
	public void run() {
		try {
			socket = new Socket(serverAddress, serverPort);
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
		} catch(IOException e) {
			running = false;
			onConnectErrorListeners.on();
			e.printStackTrace();
			return;
		}

		onConnectListeners.on();

		while(running){
			// TODO: make sure lobby commands cannot be run during game phase
			try {
				byte b = dataInputStream.readByte();
				switch(b) {
					case 0: {
						int playerId = dataInputStream.readInt();
						player.setId(playerId);
						break;
					}
					case 1: {
						int playerId = dataInputStream.readByte();
						int playerNameLength = dataInputStream.readInt();
						byte[] playerNameBytes = dataInputStream.readNBytes(playerNameLength);
						String playerName = new String(playerNameBytes);
						int playerColorId = dataInputStream.readInt();
						// TODO: add or update the player
						break;
					}
					case 2: {
						int playerId = dataInputStream.readByte();
						// TODO: remove player from lobby
						break;
					}
					case 3: {
						int boardWidth = dataInputStream.readInt();
						int boardHeight = dataInputStream.readInt();
						board = new Board(boardWidth, boardHeight);
						break;
					}
					case 4: {
						int x = dataInputStream.readInt();
						int y = dataInputStream.readInt();
						int troops = dataInputStream.readInt();
						int ownerId = dataInputStream.readInt();
						int elevation = dataInputStream.readInt();
						boolean[] paths = new boolean[4];
						for(int i = 0; i < 4; i++)
							paths[i] = dataInputStream.readBoolean();
						int base = dataInputStream.readInt();
						// TODO: update cell on board
						break;
					}
					case 5: {
						int winnerId = dataInputStream.readInt();
						// TODO: announce winner
						break;
					}

					default:
						// TODO: handle message of wrong type
						System.out.println("wrong message type");
						break;
				}
			} catch(IOException e) {
				e.printStackTrace();
				return;
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPlayerUpdate() throws IOException {
		dataOutputStream.writeByte(0);
		dataOutputStream.writeInt(player.getName().length());
		dataOutputStream.writeBytes(player.getName());
		dataOutputStream.writeInt(player.getColorId());
	}

	public void sendCellUpdate(Cell cell) throws IOException {
		dataOutputStream.writeByte(1);
		dataOutputStream.writeInt(cell.getX());
		dataOutputStream.writeInt(cell.getY());
		dataOutputStream.writeInt(cell.getElevation());
		for(boolean path : cell.getPaths())
			dataOutputStream.writeBoolean(path);
		dataOutputStream.writeInt(cell.getBase());
	}
}
