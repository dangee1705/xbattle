package com.dangee1705.xbattle;

import java.util.ArrayList;

public class Listeners extends ArrayList<Listener> {
	private static final long serialVersionUID = 1L;

	public void on() {
		for(Listener l : this)
			l.on();
	}
}
