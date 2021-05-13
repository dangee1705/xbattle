package com.dangee1705.xbattle.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BetterInputStream extends DataInputStream {
	public BetterInputStream(InputStream in) {
		super(in);
	}

	public String readString() throws IOException {
		int length = readInt();
		byte[] bytes = readNBytes(length);
		return new String(bytes);
	}
}
