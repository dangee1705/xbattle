package com.dangee1705.xbattle.model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BetterOutputStream extends DataOutputStream {
	public BetterOutputStream(OutputStream out) {
		super(out);
	}

	public void writeString(String string) throws IOException {
		byte[] bytes = string.getBytes();
		writeInt(bytes.length);
		write(bytes);
	}
}
