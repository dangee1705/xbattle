package com.dangee1705.xbattle.model;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.dangee1705.xbattle.ui.XBattle;

public class ClientHandler implements Runnable {
	private Server server;
	private Socket socket;
	private Player player;
	private boolean shouldBeRunning;
	private BetterOutputStream outputStream;
	private BetterInputStream inputStream;
	private Listeners onErrorListeners = new Listeners();

	public void addOnErrorListener(Listener listener) {
		onErrorListeners.add(listener);
	}

	public void removeOnErrorListener(Listener listener) {
		onErrorListeners.remove(listener);
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

		shouldBeRunning = true;
		Thread thread = new Thread(this);
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
						cell.setPath(Cell.NORTH, north);
						cell.setPath(Cell.EAST, east);
						cell.setPath(Cell.SOUTH, south);
						cell.setPath(Cell.WEST, west);

						// TODO: validate base logic

						if(cell.getBase() != base && cell.getTroops() == 100) {
							cell.setBase(base);
							cell.setTroops(0);
						}

						board.clearInvalidPaths(); // TODO: fix properly
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
		try {
			outputStream.writeByte(0);
			outputStream.writeInt(playerId);
			outputStream.flush();
		} catch (IOException e) {
			onErrorListeners.on();
			shouldBeRunning = false;
		}
	}

	public void sendPlayerUpdate(Player player) {
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

	public void sendPlayerLeft(int playerId) {
		try {
			outputStream.writeByte(2);
			outputStream.writeInt(playerId);
			outputStream.flush();
		} catch (IOException e) {
			onErrorListeners.on();
			shouldBeRunning = false;
		}
	}

	public void sendGameStarting(Board board) {
		try {
			outputStream.writeByte(3);
			outputStream.writeInt(board.getWidth());
			outputStream.writeInt(board.getHeight());
			outputStream.flush();
			sendBoard(board);
		} catch (IOException e) {
			onErrorListeners.on();
			shouldBeRunning = false;
		}
	}

	public void sendCellUpdate(Cell cell) {
		try {
			outputStream.writeByte(4);
			outputStream.writeInt(cell.getX());
			outputStream.writeInt(cell.getY());
			outputStream.writeInt(cell.getTroops());
			outputStream.writeInt(cell.getOwner() == null ? -1 : cell.getOwner().getId());
			outputStream.writeInt(cell.getElevation());
			for(boolean path : cell.getPaths())
				outputStream.writeBoolean(path);
			outputStream.writeInt(cell.getBase());
		} catch (IOException e) {
			onErrorListeners.on();
			shouldBeRunning = false;
		}
	}

	// convinience method to do multiple cell updates
	public void sendBoard(Board board) {
		try {
			for(int y = 0; y < board.getHeight(); y++) {
				for(int x = 0; x < board.getWidth(); x++) {
					Cell cell = board.getCell(x, y);
					if(cell.getHasUpdate())
						sendCellUpdate(cell);
				}
			}
			outputStream.flush();
		} catch(IOException e) {
			onErrorListeners.on();
			shouldBeRunning = false;
		}
	}

	public void sendEndOfGame(int winnerId) {
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
