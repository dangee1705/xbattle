package com.dangee1705.xbattle.model;

import java.io.BufferedInputStream;
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
	private int winnerId = -1;

	private Listeners onConnectListeners = new Listeners();
	private Listeners onConnectErrorListeners = new Listeners();
	private Listeners onPlayerUpdateListeners = new Listeners();
	private Listeners onGameStartListeners = new Listeners();
	private Listeners onCellUpdatedListeners = new Listeners();
	private Listeners onGameEndListeners = new Listeners();

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

	public Player getPlayer() {
		return player;
	}

	private Player getPlayerById(int playerId) {
		for(Player player : players)
			if(player.getId() == playerId)
				return player;
		return null;
	}

	public Board getBoard() {
		return board;
	}

	public void addOnConnectListener(Listener listener) {
		onConnectListeners.add(listener);
	}

	public void addOnConnectErrorListener(Listener listener) {
		onConnectErrorListeners.add(listener);
	}

	public void addOnPlayerUpdateListener(Listener listener) {
		onPlayerUpdateListeners.add(listener);
	}

	public void addOnGameStartListener(Listener listener) {
		onGameStartListeners.add(listener);
	}

	public void addOnCellUpdatedListener(Listener listener) {
		onCellUpdatedListeners.add(listener);
	}

	public void addOnGameEndListener(Listener listener) {
		onGameEndListeners.add(listener);
	}

	@Override
	public void run() {
		try {
			socket = new Socket(serverAddress, serverPort);
			dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
		} catch(IOException e) {
			running = false;
			onConnectErrorListeners.on();
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
						if(!players.contains(player))
							players.add(player);
						break;
					}
					// player update message
					case 1: {
						int playerId = dataInputStream.readInt();
						int playerNameLength = dataInputStream.readInt();
						byte[] playerNameBytes = dataInputStream.readNBytes(playerNameLength);
						String playerName = new String(playerNameBytes);
						int playerColorId = dataInputStream.readInt();
						
						Player playerToUpdate = null;
						for(Player player : players) {
							if(player.getId() == playerId) {
								playerToUpdate = player;
								break;
							}
						}
						if(playerToUpdate == null) {
							playerToUpdate = new Player(playerId, playerName, playerColorId);
							players.add(playerToUpdate);
						} else {
							playerToUpdate.setName(playerName);
							playerToUpdate.setColorId(playerColorId);
						}

						onPlayerUpdateListeners.on();
						break;
					}
					case 2: {
						int playerId = dataInputStream.readByte();
						// TODO: remove player from lobby
						// onPlayerLeave.on();
						break;
					}
					// game start message
					case 3: {
						int boardWidth = dataInputStream.readInt();
						int boardHeight = dataInputStream.readInt();
						board = new Board(players, boardWidth, boardHeight);
						onGameStartListeners.on();
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

						Cell cell = board.getCell(x, y);
						cell.setTroops(troops);
						cell.setOwner(getPlayerById(ownerId));
						cell.setElevation(elevation);
						for(int i = 0; i < 4; i++)
							cell.setPath(i, paths[i]);
						cell.setBase(base);
						// set the cell to no updates so we dont send it back to the server unnecessarily
						cell.setHasUpdate(false);
						onCellUpdatedListeners.on();
						break;
					}
					case 5: {
						winnerId = dataInputStream.readInt();
						onGameEndListeners.on();
						running = false;
						break;
					}
					default:
						// TODO: handle message of wrong type
						System.out.println("wrong message type" + b);
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
		synchronized(dataOutputStream) {
			dataOutputStream.writeByte(0);
			byte[] nameBytes = player.getName().getBytes();
			dataOutputStream.writeInt(nameBytes.length);
			dataOutputStream.write(nameBytes);
			dataOutputStream.writeInt(player.getColorId());
		}
	}

	public void sendCellUpdate(Cell cell) throws IOException {
		synchronized(dataOutputStream) {
			dataOutputStream.writeByte(1);
			dataOutputStream.writeInt(cell.getX());
			dataOutputStream.writeInt(cell.getY());
			dataOutputStream.writeInt(cell.getElevation());
			for(boolean path : cell.getPaths())
				dataOutputStream.writeBoolean(path);
			dataOutputStream.writeInt(cell.getBase());
		}
	}

	public void sendCellUpdates() throws IOException {
		synchronized(board) {
			for(int y = 0; y < board.getHeight(); y++) {
				for(int x = 0; x < board.getWidth(); x++) {
					Cell cell = board.getCell(x, y);
					if(cell.getHasUpdate())
						sendCellUpdate(cell);
				}
			}
			board.clearHasUpdates();
		}
	}

	public int getWinnerId() {
		return winnerId;
	}
}
