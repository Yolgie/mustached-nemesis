package at.jku.embedded.morse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class MorseIn {

	private final Reader in;
	
	public MorseIn(Reader s) {
		this.in = s;
	}
	
	public MorseIn(InputStream s) {
		this.in = new InputStreamReader(s);
	}
	
	public MorseIn(String s) {
		this.in = new StringReader(s);
	}
	
	public Morse read() throws IOException {
		int c = in.read();
		switch (c) {
		case '-':
			return Morse.DASH;
		case '.':
			return Morse.DOT;
		case ' ':
			return Morse.GAP;
		case -1 :
			return Morse.END;
		}
		throw new IllegalArgumentException("Invalid character: " + (char) c);
	}
	
}
