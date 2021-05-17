package com.dangee1705.xbattle.model.ai;

import com.dangee1705.xbattle.model.Board;
import com.dangee1705.xbattle.model.Cell;
import com.dangee1705.xbattle.model.Client;
import com.dangee1705.xbattle.model.Player;

public class BaseAI extends AI {
	public BaseAI(Client client) {
		super(client);
	}

	@Override
	protected void onCellUpdated() {
		Player player = client.getPlayer();
		Board board = client.getBoard();

		for(int y = 0; y < board.getHeight(); y++) {
			for(int x = 0; x < board.getWidth(); x++) {
				Cell cell = board.getCell(x, y);
				if(cell.getOwner() == player) {
					if(cell.getBase() == 8) {
						for(int i = 0; i < 4; i++)
							cell.setPath(i, true);
					} else if(cell.getTroops() == 100) {
						cell.increaseBase();
						cell.setTroops(0);
					}
				}
					
			}
		}

		client.sendCellUpdates();
	}
}
