package com.dangee1705.xbattle.model;

import java.util.ArrayList;

public class Listeners extends ArrayList<Listener> {
	private static final long serialVersionUID = 4558121912059804805L;

	public void on() {
		for(Listener l : this)
			l.on();
	}
}
