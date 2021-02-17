package com.dangee1705.xbattle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
			} catch (IOException e) {
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
					b = this.dataInputStream.readByte();
				} catch (IOException e) {
					break;
				}

				switch(b) {
					default:
						System.out.println(b);
				}
			}

			clientHandlers.remove(this);
			onClientDisconnectListeners.on();
		}

		public void sendHello() {
			try {
				dataOutputStream.writeByte(0);
				dataOutputStream.writeByte((byte) player.getId());
				dataOutputStream.writeByte((byte) player.getColorId());
			} catch (IOException e) {
				return;
			}
		}

		public void sendGameStart() {
			try {
				dataOutputStream.writeByte(3);
			} catch (IOException e) {
				return;
			}
		}
	}

	public void sendGameStart() {
		for(ClientHandler clientHandler : clientHandlers)
			clientHandler.sendGameStart();
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
			onStartErrorListeners.on();
			return;
		}

		onStartListeners.on();

		while(running) {
			try {
				Socket socket = serverSocket.accept();
				Player player = new Player(0, "PLAYER NAME", 0);
				ClientHandler clientHandler = new ClientHandler(socket, player);
				clientHandlers.add(clientHandler);
				onClientConnectListeners.on();
				clientHandler.sendHello();
			} catch(IOException e) {
				continue;
			}
		}

		onStopListeners.on();
	}
}
