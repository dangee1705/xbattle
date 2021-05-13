package com.dangee1705.xbattle.model.ai;

import com.dangee1705.xbattle.model.Client;

public abstract class AI {
	protected Client client;

	public AI(Client client) {
		this.client = client;
		client.addOnConnectListener(() -> {
			System.out.println("connected");
		});
		client.addOnConnectErrorListener(() -> {
			System.out.println("connect error");
		});
		client.addOnPlayerUpdateListener(() -> {});
		client.addOnGameStartListener(() -> {});
		client.addOnCellUpdatedListener(() -> onCellUpdated());
	}

	protected Client getClient() {
		return client;
	}

	protected abstract void onCellUpdated();
}
