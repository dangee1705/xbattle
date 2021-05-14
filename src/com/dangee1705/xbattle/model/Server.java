package com.dangee1705.xbattle.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.dangee1705.xbattle.ui.XBattle;

public class Server implements Runnable {
	private int boardWidth = 0;
	private int boardHeight = 0;
	private int ticksPerSecond = 5;
	private boolean running = false;
	private Thread thread = null;
	private ServerSocket serverSocket;

	private ArrayList<Player> players = new ArrayList<>();
	private Board board;

	private Listeners onStartListeners = new Listeners();
	private Listeners onStartErrorListeners = new Listeners();
	private ArrayList<ClientHandler> clientHandlers;
	private Listeners onClientConnectListeners = new Listeners();
	private Listeners onClientDisconnectListeners = new Listeners();
	private Listeners onStopListeners = new Listeners();
	private Listeners onGameStartListeners = new Listeners();
	private Listeners onEndOfGameListeners = new Listeners();

	public Board getBoard() {
		return board;
	}

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

	public void addOnGameStartListener(Listener listener) {
		onGameStartListeners.add(listener);
	}

	public void sendGameStart() throws IOException {
		// make sure that all the players have selected a colour
		for(Player player : players)
			if(player.getColorId() == -1)
				return;
		
		// send every player to every client
		sendAllPlayers();

		// send game start to each client
		for(ClientHandler clientHandler : clientHandlers)
			clientHandler.sendGameStarting(board);
	}

	public void sendAllPlayers() throws IOException {
		for(ClientHandler clientHandler : clientHandlers)
			for(Player player : players)
				clientHandler.sendPlayerUpdate(player);
	}

	public void sendBoard() throws IOException {
		for(ClientHandler clientHandler : clientHandlers)
			clientHandler.sendBoard(board);
		board.clearHasUpdates();
	}

	public ArrayList<ClientHandler> getClientHandlers() {
		return clientHandlers;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void addOnClientConnectListener(Listener listener) {
		onClientConnectListeners.add(listener);
	}

	public void addOnClientDisconnectListener(Listener listener) {
		onClientDisconnectListeners.add(listener);
	}

	private boolean inLobbyPhase;

	@Override
	public void run() {
		clientHandlers = new ArrayList<>();
		try {
			serverSocket = new ServerSocket(XBattle.DEFAULT_PORT);
			serverSocket.setSoTimeout(500);
		} catch (IOException e) {
			running = false;
			onStartErrorListeners.on();
			return;
		}

		onStartListeners.on();

		int nextPlayerId = 0;
		inLobbyPhase = true;
		while(inLobbyPhase) {
			try {
				Socket socket = serverSocket.accept();
				Player player = new Player(nextPlayerId++, "Player " + nextPlayerId, -1);
				ClientHandler clientHandler = new ClientHandler(this, socket, player);
				// TODO: register error handlers and unregister at end
				clientHandlers.add(clientHandler);
				players.add(player);

				onClientConnectListeners.on();
				// clientHandler.sendHello();
			} catch(IOException e) {
				continue;
			}
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}

		// generate the board
		board = new Board(players, boardWidth, boardHeight, 0.4f, 10);

		onGameStartListeners.on();
		try {
			// this will tell each client the game is starting
			sendGameStart();
		} catch(IOException e) {
			e.printStackTrace();
		}

		while(true) {
			try {
				board.update();
				sendBoard();

				Player winner = board.winner();
				if(winner != null) {
					sendEndOfGame(winner.getId());
					onEndOfGameListeners.on();
					break;
				}

				Thread.sleep(1000 / ticksPerSecond);
			} catch (Exception e) {
				
			}
		}

		onStopListeners.on();
	}

	public void startGame() {
		inLobbyPhase = false;
	}

	public void addOnEndOfGameListener(Listener listener) {
		onEndOfGameListeners.add(listener);
	}

	public void removeOnEndOfGameListener(Listener listener) {
		onEndOfGameListeners.remove(listener);
	}

	public void sendEndOfGame(int winnerId) {
		for(ClientHandler clientHandler : clientHandlers)
			clientHandler.sendEndOfGame(winnerId);
	}
}
