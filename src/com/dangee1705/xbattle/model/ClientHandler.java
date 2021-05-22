package com.dangee1705.xbattle.model;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;

import com.dangee1705.xbattle.ui.XBattle;

public class ClientHandler implements Runnable {
	private Server server;
	private Socket socket;
	private Player player;
	private boolean shouldBeRunning;
	private BetterOutputStream outputStream;
	private BetterInputStream inputStream;
	private Listeners onErrorListeners = new Listeners();
	private CellQueueRunnable cellQueueRunnable;

	public void addOnErrorListener(Listener listener) {
		onErrorListeners.add(listener);
	}

	public void removeOnErrorListener(Listener listener) {
		onErrorListeners.remove(listener);
	}

	// TODO: make just iterate the board instead its probably faster
	private class CellQueueRunnable implements Runnable {
		private HashSet<Cell> cells = new HashSet<>();

		public void addCell(Cell cell) {
			synchronized(cells) {
				cells.add(cell);
			}
		}

		@Override
		public void run() {
			while(shouldBeRunning) {
				Object[] cellsToSend = {};
				synchronized(cells) {
					if(cells.size() > 0) {
						cellsToSend = cells.toArray();
						cells = new HashSet<>();
					}
				}

				for(Object cell : cellsToSend)
					sendCellUpdate((Cell) cell);
			}
		}

		private void sendCellUpdate(Cell cell) {
			synchronized(outputStream) {
				try {
					outputStream.writeByte(4);
					outputStream.writeInt(cell.getX());
					outputStream.writeInt(cell.getY());
					outputStream.writeInt(cell.getTroops());
					outputStream.writeInt(cell.getOwner() == null ? -1 : cell.getOwner().getId());
					outputStream.writeInt(cell.getElevation());
					for(boolean pipe : cell.getPipes())
						outputStream.writeBoolean(pipe);
					outputStream.writeInt(cell.getBase());
					outputStream.flush();
				} catch (IOException e) {
					onErrorListeners.on();
					shouldBeRunning = false;
				}
			}
		}
	}

	public ClientHandler(Server server, Socket socket, Player player) {
		this.server = server;
		this.socket = socket;
		try {
			outputStream = new BetterOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			inputStream = new BetterInputStream(socket.getInputStream());
		} catch(Exception e) {
			// we ignore the error here because if there is one then it will be caught on the next read
		}
		this.player = player;

		sendHello(player.getId());
		sendPlayerUpdate(player);
		server.sendAllPlayers();

		shouldBeRunning = true;
		Thread thread = new Thread(this, "Client-Handler-Thread");
		thread.start();

		cellQueueRunnable = new CellQueueRunnable();
		Thread cellQueueThread = new Thread(cellQueueRunnable, "Client-Handler-Cell-Queue-Thread");
		cellQueueThread.start();
	}

	public Socket getSocket() {
		return socket;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public void run() {
		while(shouldBeRunning) {
			try {
				byte b = inputStream.readByte();
				if(b == 0) {
					String name = inputStream.readString();
					int colorId = inputStream.readInt();
					

					boolean canChange = true;
					for(Player player : server.getPlayers())
						if(player != getPlayer() && (player.getName().equals(name) || player.getColorId() == colorId || colorId < -1 || colorId >= XBattle.DEFAULT_NAMED_COLORS.length))
							canChange = false;

					if(canChange) {
						player.setName(name);
						player.setColorId(colorId);
					}

					server.sendAllPlayers();
				} else if(b == 1) {
					int x = inputStream.readInt();
					int y = inputStream.readInt();
					int elevation = inputStream.readInt();
					boolean north = inputStream.readBoolean();
					boolean east = inputStream.readBoolean();
					boolean south = inputStream.readBoolean();
					boolean west = inputStream.readBoolean();
					int base = inputStream.readInt();

					Board board = server.getBoard();
					Cell cell = board.getCell(x, y);
					if(cell.getOwner() == player) {
						cell.setElevation(elevation);
						cell.setPipe(Cell.NORTH, north);
						cell.setPipe(Cell.EAST, east);
						cell.setPipe(Cell.SOUTH, south);
						cell.setPipe(Cell.WEST, west);

						// TODO: elevation change checks

						if(cell.getBase() != base && Math.abs(cell.getBase() - base) == 1 && cell.getTroops() == 100) {
							cell.setBase(base);
							cell.setTroops(0);
						}

						board.clearInvalidPipes();
					} else {
						if(y > 0 && board.getCell(x, y - 1).getOwner() == player && board.getCell(x, y - 1).getTroops() == 100) {
							cell.setElevation(elevation);
							board.getCell(x, y - 1).setTroops(0);
						}
						if(x < board.getWidth() - 1 && board.getCell(x + 1, y).getOwner() == player && board.getCell(x + 1, y).getTroops() == 100) {
							cell.setElevation(elevation);
							board.getCell(x + 1, y).setTroops(0);
						}
						if(y < board.getHeight() && board.getCell(x, y + 1).getOwner() == player && board.getCell(x, y + 1).getTroops() == 100) {
							cell.setElevation(elevation);
							board.getCell(x, y + 1).setTroops(0);
						}
						if(x > 0 && board.getCell(x - 1, y).getOwner() == player && board.getCell(x - 1, y).getTroops() == 100) {
							cell.setElevation(elevation);
							board.getCell(x - 1, y).setTroops(0);
						}
					}
				} else {
					onErrorListeners.on();
					shouldBeRunning = false;
				}
			} catch(Exception e) {
				onErrorListeners.on();
				shouldBeRunning = false;
			}
		}
	}

	public void sendHello(int playerId) {
		synchronized(outputStream) {
			try {
				outputStream.writeByte(0);
				outputStream.writeInt(playerId);
				outputStream.flush();
			} catch (IOException e) {
				onErrorListeners.on();
				shouldBeRunning = false;
			}
		}
	}

	public void sendPlayerUpdate(Player player) {
		synchronized(outputStream) {
			try {
				outputStream.writeByte(1);
				outputStream.writeInt(player.getId());
				outputStream.writeString(player.getName());
				outputStream.writeInt(player.getColorId());
				outputStream.flush();
			} catch (IOException e) {
				onErrorListeners.on();
				shouldBeRunning = false;
			}
		}
	}

	public void sendPlayerLeft(int playerId) {
		synchronized(outputStream) {
			try {
				outputStream.writeByte(2);
				outputStream.writeInt(playerId);
				outputStream.flush();
			} catch (IOException e) {
				onErrorListeners.on();
				shouldBeRunning = false;
			}
		}
	}

	public void sendGameStarting(Board board) {
		synchronized(outputStream) {
			try {
				outputStream.writeByte(3);
				outputStream.writeInt(board.getWidth());
				outputStream.writeInt(board.getHeight());
				outputStream.flush();
			} catch (IOException e) {
				onErrorListeners.on();
				shouldBeRunning = false;
			}
		}
		sendBoard(board);
	}

	public void sendCellUpdate(Cell cell) {
		cellQueueRunnable.addCell(cell);
	}

	// convinience method to do multiple cell updates
	public void sendBoard(Board board) {
		for(int y = 0; y < board.getHeight(); y++) {
			for(int x = 0; x < board.getWidth(); x++) {
				Cell cell = board.getCell(x, y);
				if(cell.getHasUpdate())
					sendCellUpdate(cell);
			}
		}
	}

	public void sendEndOfGame(int winnerId) {
		synchronized(outputStream) {
			try {
				outputStream.writeByte(5);
				outputStream.writeInt(winnerId);
				outputStream.flush();
			} catch(IOException e) {
				e.printStackTrace();
				onErrorListeners.on();
				shouldBeRunning = false;
			}
		}
	}
}
