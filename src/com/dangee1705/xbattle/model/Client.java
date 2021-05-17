package com.dangee1705.xbattle.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {
	
	private String serverAddress;
	private int serverPort;
	private Socket socket;
	private boolean running = false;
	private Thread thread;
	private BetterInputStream inputStream;
	private BetterOutputStream outputStream;

	private Player player = new Player(-1, "Player", -1);
	private ArrayList<Player> players = new ArrayList<>();
	private Board board;
	private int winnerId = -1;

	private Listeners onConnectListeners = new Listeners();
	private Listeners onConnectErrorListeners = new Listeners();
	private Listeners onPlayerUpdateListeners = new Listeners();
	private Listeners onPlayerLeaveListeners = new Listeners();
	private Listeners onGameStartListeners = new Listeners();
	private Listeners onCellUpdatedListeners = new Listeners();
	private Listeners onGameEndListeners = new Listeners();
	private Listeners onErrorListeners = new Listeners();

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

	public void addOnPlayerLeaveListener(Listener listener) {
		onPlayerLeaveListeners.add(listener);
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

	public void addOnErrorListener(Listener listener) {
		onErrorListeners.add(listener);
	}

	@Override
	public void run() {
		try {
			socket = new Socket(serverAddress, serverPort);
			inputStream = new BetterInputStream(new BufferedInputStream(socket.getInputStream()));
			outputStream = new BetterOutputStream(socket.getOutputStream());
		} catch(IOException e) {
			running = false;
			onConnectErrorListeners.on();
			return;
		}

		onConnectListeners.on();

		while(running){
			// TODO: make sure lobby commands cannot be run during game phase
			try {
				byte b = inputStream.readByte();
				switch(b) {
					case 0: {
						int playerId = inputStream.readInt();
						player.setId(playerId);
						if(!players.contains(player))
							players.add(player);
						break;
					}
					// player update message
					case 1: {
						int playerId = inputStream.readInt();
						String playerName = inputStream.readString();
						int playerColorId = inputStream.readInt();
						
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
						int playerId = inputStream.readByte();
						players.remove(getPlayerById(playerId));
						onPlayerLeaveListeners.on();
						break;
					}
					// game start message
					case 3: {
						int boardWidth = inputStream.readInt();
						int boardHeight = inputStream.readInt();
						board = new Board(players, boardWidth, boardHeight);
						onGameStartListeners.on();
						break;
					}
					case 4: {
						int x = inputStream.readInt();
						int y = inputStream.readInt();
						int troops = inputStream.readInt();
						int ownerId = inputStream.readInt();
						int elevation = inputStream.readInt();
						boolean[] paths = new boolean[4];
						for(int i = 0; i < 4; i++)
							paths[i] = inputStream.readBoolean();
						int base = inputStream.readInt();

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
						winnerId = inputStream.readInt();
						onGameEndListeners.on();
						running = false;
						break;
					}
					default:
						onErrorListeners.on();
						running = false;
						break;
				}
			} catch(IOException e) {
				running = false;
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPlayerUpdate() {
		synchronized(outputStream) {
			try {
				outputStream.writeByte(0);
				outputStream.writeString(player.getName());
				outputStream.writeInt(player.getColorId());
			} catch (IOException e) {
				onErrorListeners.on();
				running = false;
			}
		}
	}

	public void sendCellUpdate(Cell cell) {
		synchronized(outputStream) {
			try {
				outputStream.writeByte(1);
				outputStream.writeInt(cell.getX());
				outputStream.writeInt(cell.getY());
				outputStream.writeInt(cell.getElevation());
				for(boolean path : cell.getPaths())
					outputStream.writeBoolean(path);
				outputStream.writeInt(cell.getBase());
			} catch(IOException e) {
				onErrorListeners.on();
				running = false;
			}
		}
	}

	public void sendCellUpdates() {
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
