package at.jku.embedded.morse;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;


public class MorseCoder {
	
	private static final String TREE = "#ETIANMSURWDKGOHVFULÄPJBXCYZQÖ#54#3###2#######16#######7###8#90";
	private static final char[] TREE_ARRAY = TREE.toCharArray();
	
	public static String getAllowedCharacters() {
		StringBuilder b = new StringBuilder();
		b.append(" ");
		for (int i = 0; i < TREE_ARRAY.length; i++) {
			char c = TREE_ARRAY[i];
			if (c != '#') {
				b.append(c);
			}
		}
		return b.toString();
	}
	
	
	private static final int MAX_TREE_DEPTH = 5;
	
	public void decodeText(MorseIn in, Writer writer) throws IOException {
		Morse m = Morse.END;
			decodeWord(in, m, writer);
	}

	private void decodeWord(MorseIn in, Morse m, Writer w) throws IOException {
		Morse next = Morse.GAP;
		Morse prev = null;
		while (next != Morse.END) {
			next = in.read();
			
			if (prev == Morse.GAP && next == Morse.GAP) {
				next = in.read();
				next = in.read();
				w.write(' ');
			}
			
			prev = decodeChar(in, next, 0, w);
		}
	}

	private Morse decodeChar(MorseIn in, Morse m, int index, Writer w) throws IOException {
		switch (m) {
		case DASH:
			Morse next = in.read();
			index = (index * 2) + 2;
			return decodeChar(in, next, index, w);
		case DOT:
			next = in.read();
			index = (index * 2) + 1;
			return decodeChar(in, next, index, w);
		case GAP:
		case END:
		default:
			if (index == 0) {
				return m;
			} else if (index < TREE_ARRAY.length) {
				char result = TREE_ARRAY[index];
				if (result == '#') {
					// just filter invalid
					throw new IllegalArgumentException("Invalid character at index " + index);
				} else {
					w.write(result);
				}
				return m;
			} else {
				throw new IllegalArgumentException("Invalid character at index " + index);
			}
		}
	}
	
	public void encodeText(Reader in, MorseOut out) throws IOException {
		int c = -1;
		final Morse[] buf = new Morse[MAX_TREE_DEPTH];
		while ((c = in.read()) != -1) {
			if (c == ' ') {
				out.write(Morse.GAP, 2);
			} else {
				int index = TREE.indexOf(c);
				if (index != -1) {
					int i = -1;
					while (index >= 1) {
						Morse m = index % 2 == 0 ? Morse.DASH : Morse.DOT;
						if (m == Morse.DOT) {
							index  = index - 1;
						} else if (m == Morse.DASH) {
							index  = index - 2;
						}
						index = (index / 2);
						buf[++i] = m;
						assert i < MAX_TREE_DEPTH;
					}
					
					// print in reverse order
					while (i >= 0) {
						out.write(buf[i], 1);
						i--;
					}
					
					out.write(Morse.GAP, 1);
				} else {
					throw new IOException("Invalid character " + (char) c);
				}
			}
		}
	}
}
