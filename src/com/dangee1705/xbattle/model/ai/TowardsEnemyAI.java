package com.dangee1705.xbattle.model.ai;

import com.dangee1705.xbattle.model.Board;
import com.dangee1705.xbattle.model.Cell;
import com.dangee1705.xbattle.model.Client;

public class TowardsEnemyAI extends AI {
	public TowardsEnemyAI(Client client) {
		super(client);
	}

	private int distanceToCell(Cell from, Cell to) {
		return Math.abs(to.getX() - from.getX()) + Math.abs(to.getY() - from.getY());
	}

	@Override
	protected void onCellUpdated() {
		Board board = client.getBoard();

		for(Cell originCell : board) {
			if(originCell.getOwner() == client.getPlayer()) {
				if(originCell.getBase() == 8) {
					Cell bestTarget = null;
					int bestScore = 0;

					for(Cell targetCell : board) {
						if(targetCell.getOwner() != null && targetCell.getOwner() != client.getPlayer()) {
							int distance = (board.getWidth() + board.getHeight()) - distanceToCell(originCell, targetCell);
							int weakness = 101 - targetCell.getTroops();

							int score = distance * weakness;
							if(bestTarget == null || score > bestScore) {
								bestTarget = targetCell;
								bestScore = score;
							}
						}
					}

					if(bestTarget != null) {
						double angle = Math.atan2(bestTarget.getY() - originCell.getY(), bestTarget.getX() - originCell.getX());
						if(angle < Math.PI / 4 && angle >= -Math.PI / 4)
							originCell.setPipe(Cell.EAST, true);
						else if(angle < 3 * (Math.PI / 4) && angle >= Math.PI / 4)
							originCell.setPipe(Cell.SOUTH, true);
						else if(angle < -Math.PI / 4 && angle >= -3 * (Math.PI / 4))
							originCell.setPipe(Cell.NORTH, true);
						else
							originCell.setPipe(Cell.WEST, true);
					}
				}

				if(originCell.getTroops() == 100 && originCell.getBase() < 8) {
					originCell.setTroops(0);
					originCell.increaseBase();
				}
			}
		}

		client.sendCellUpdates();
	}
}
