package com.dangee1705.xbattle.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.dangee1705.xbattle.ui.XBattle;

public class Server implements Runnable {
	private int boardWidth = 10;
	private int boardHeight = 10;
	private int ticksPerSecond = 5;
	private boolean running = false;
	private Thread thread = null;
	private Board board;
	private ServerSocket serverSocket;

	private Listeners onStartListeners = new Listeners();
	private Listeners onStartErrorListeners = new Listeners();
	private ArrayList<ClientHandler> clientHandlers;
	private Listeners onClientConnectListeners = new Listeners();
	private Listeners onClientDisconnectListeners = new Listeners();
	private Listeners onStopListeners = new Listeners();

	public int getBoardWidth() {
		return boardWidth;
	}

	public void setBoardWidth(int boardWidth) {
		this.boardWidth = boardWidth;
	}

	public int getBoardHeight() {
		return boardHeight;
	}

	public void setBoardHeight(int boardHeight) {
		this.boardHeight = boardHeight;
	}

	public int getTicksPerSecond() {
		return ticksPerSecond;
	}

	public void setTicksPerSecond(int ticksPerSecond) {
		this.ticksPerSecond = ticksPerSecond;
	}

	public void start() {
		if(!running) {
			running = true;
			thread = new Thread(this, "Server-Thread");
			thread.start();
		}
	}

	public void addOnStartListener(Listener listener) {
		onStartListeners.add(listener);
	}

	public void addOnStartErrorListener(Listener listener) {
		onStartErrorListeners.add(listener);
	}

	public void stop() {
		try {
			running = false;
			thread.join();
		} catch(InterruptedException e) {
			
		}
	}

	public void addOnStopListener(Listener listener) {
		onStopListeners.add(listener);
	}

	public class ClientHandler implements Runnable {
		private Thread thread;
		private Socket socket;
		private Player player;
		private DataInputStream dataInputStream;
		private DataOutputStream dataOutputStream;

		public ClientHandler(Socket socket, Player player) {
			this.socket = socket;
			this.player = player;
			try {
				this.dataInputStream = new DataInputStream(socket.getInputStream());
				this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
			} catch(IOException e) {
				clientHandlers.remove(this);
				onClientDisconnectListeners.on();
				return;
			}
			thread = new Thread(this, "Client-Handler");
			thread.start();
		}

		public Socket getSocket() {
			return socket;
		}

		public Player getPlayer() {
			return player;
		}

		@Override
		public void run() {
			while(true) {
				byte b;
				try {
					b = dataInputStream.readByte();

					switch(b) {
						case 0: {
							int nameLength = dataInputStream.readInt();
							byte[] nameBytes = dataInputStream.readNBytes(nameLength);
							String name = new String(nameBytes);
							int colorId = dataInputStream.readInt();
							getPlayer().setName(name);
							getPlayer().setColorId(colorId);
							onClientConnectListeners.on(); // TODO: properly handle telling clients about updates
							break;
						} case 1: {
							int x = dataInputStream.readInt();
							int y = dataInputStream.readInt();
							int elevation = dataInputStream.readInt();
							boolean north = dataInputStream.readBoolean();
							boolean east = dataInputStream.readBoolean();
							boolean south = dataInputStream.readBoolean();
							boolean west = dataInputStream.readBoolean();
							int base = dataInputStream.readInt();
							// TODO: validate the values the user has sent
							Cell cell = board.getCell(x, y);
							cell.setElevation(elevation);
							cell.setPath(Cell.NORTH, north);
							cell.setPath(Cell.EAST, east);
							cell.setPath(Cell.SOUTH, south);
							cell.setPath(Cell.WEST, west);
							cell.setBase(base);
							break;
						}
						default:
							// TODO: do something with the invalid message
							break;
					}
				} catch(IOException e) {
					e.printStackTrace();
					break;
				}				
			}

			clientHandlers.remove(this);
			onClientDisconnectListeners.on();
		}

		public void sendHello() throws IOException {
			dataOutputStream.writeByte(0);
			dataOutputStream.writeInt((byte) player.getId());
		}

		public void sendPlayerUpdate(Player player) throws IOException {
			dataOutputStream.writeByte(1);
			dataOutputStream.writeInt(player.getId());
			dataOutputStream.writeInt(player.getName().length());
			dataOutputStream.writeBytes(player.getName());
			dataOutputStream.writeInt(player.getColorId());
		}

		public void sendPlayerLeft(Player player) throws IOException {
			dataOutputStream.writeByte(2);
			dataOutputStream.writeInt(player.getId());
		}

		public void sendGameStarting() throws IOException {
			dataOutputStream.writeByte(3);
			dataOutputStream.writeInt(board.getWidth());
			dataOutputStream.writeInt(board.getHeight());
		}

		public void sendCellUpdate(Cell cell) throws IOException {
			dataOutputStream.writeByte(4);
			dataOutputStream.writeInt(cell.getX());
			dataOutputStream.writeInt(cell.getY());
			dataOutputStream.writeInt(cell.getTroops());
			dataOutputStream.writeInt(cell.getOwner() == null ? -1 : cell.getOwner().getId());
			dataOutputStream.writeInt(cell.getElevation());
			for(boolean path : cell.getPaths())
				dataOutputStream.writeBoolean(path);
			dataOutputStream.writeInt(cell.getBase());
		}

		public void sendGameEnd(Player player) throws IOException {
			dataOutputStream.writeByte(5);
			dataOutputStream.writeInt(player.getId());
		}

		public void sendBoard() throws IOException {
			sendBoard(false);
		}

		public void sendBoard(boolean updatesOnly) throws IOException {
			for(int y = 0; y < board.getHeight(); y++) {
				for(int x = 0; x < board.getWidth(); x++) {
					if(!updatesOnly || board.getCell(x, y).getHasUpdate()) {
						sendCellUpdate(board.getCell(x, y));
					}
				}
			}
		}
	}

	public void sendGameStart() throws IOException {
		for(ClientHandler clientHandler : clientHandlers)
			clientHandler.sendGameStarting();
		for(ClientHandler clientHandler : clientHandlers)
			clientHandler.sendPlayerUpdate(clientHandler.getPlayer());
	}

	public void sendBoard() throws IOException {
		sendBoard(false);
	}

	public void sendBoard(boolean updatesOnly) throws IOException {
		for(ClientHandler clientHandler : clientHandlers)
			clientHandler.sendBoard(updatesOnly);
	}

	public ArrayList<ClientHandler> getClientHandlers() {
		return clientHandlers;
	}

	public void addOnClientConnectListener(Listener listener) {
		onClientConnectListeners.add(listener);
	}

	public void addOnClientDisconnectListener(Listener listener) {
		onClientDisconnectListeners.add(listener);
	}

	@Override
	public void run() {
		board = new Board(boardWidth, boardHeight);
		clientHandlers = new ArrayList<>();

		try {
			serverSocket = new ServerSocket(XBattle.DEFAULT_PORT);
		} catch (IOException e) {
			running = false;
			onStartErrorListeners.on();
			e.printStackTrace();
			return;
		}

		onStartListeners.on();

		int nextPlayerId = 0;

		while(running) {
			try {
				Socket socket = serverSocket.accept();
				Player player = new Player(nextPlayerId, "Player " + (nextPlayerId + 1), -1);
				ClientHandler clientHandler = new ClientHandler(socket, player);
				clientHandlers.add(clientHandler);
				onClientConnectListeners.on();
				clientHandler.sendHello();
			} catch(IOException e) {
				e.printStackTrace();
				continue;
			}
		}

		try {
			// this will tell each client the game is starting, then send the board to them
			sendGameStart();
			sendBoard();
		} catch(IOException e) {
			e.printStackTrace();
		}

		// while(true) {
		// 	sendBoard();
		// }
		
		onStopListeners.on();
	}
}
