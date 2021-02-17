package com.dangee1705.xbattle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {
	
	private String serverAddress;
	private int serverPort;
	private Socket socket;
	private Thread thread;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;

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
		thread = new Thread(this, "Client-Thread");
		thread.start();
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
			onConnectErrorListeners.on();
			return;
		}

		onConnectListeners.on();

		boolean running = true;
		while(running){
			try {
				byte b = dataInputStream.readByte();
				switch(b) {
					case 0: {
						int playerId = dataInputStream.readByte();
						int numOfPlayers = dataInputStream.readByte();
						break;
					}
					case 1: {
						int playerId = dataInputStream.readByte();
						int playerNameLength = dataInputStream.readInt();
						break;
					}
					case 2: {
						int playerId = dataInputStream.readByte();
						int colorId = dataInputStream.readByte();
						break;
					}
					case 3: {
						System.out.println("Game do be starting doe");
						break;
					}
					case 4: {
						int boardWidth = dataInputStream.readInt();
						int boardHeight = dataInputStream.readInt();
						int tickRate = dataInputStream.readInt();
						board = new Board(boardWidth, boardHeight);
						break;
					}
					case 5: {
						int cellX = dataInputStream.readInt();
						int cellY = dataInputStream.readInt();
						int cellTroops = dataInputStream.readInt();
						int cellOwnerId = dataInputStream.readByte();
						int cellElevation = dataInputStream.readByte();
						boolean[] cellPaths = new boolean[4];
						for(int i = 0; i < 4; i++)
							cellPaths[i] = dataInputStream.readBoolean();
						int cellBase = dataInputStream.readInt();

						Cell cell = board.getCell(cellX, cellY);
						cell.setTroops(cellTroops);
						Player owner = null;
						for(Player player : players) {
							if(player.getId() == cellOwnerId) {
								owner = player;
								break;
							}
						}
						cell.setOwner(owner);
						cell.setElevation(cellElevation);
						for(int i = 0; i < 4; i++)
							cell.setPath(i, cellPaths[i]);
						cell.setBase(cellBase);
					}
					case 6: {
						running = false;
						break;
					}

					default:
						break;
				}
			} catch(IOException e) {
				return;
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
			
		}
	}
}
