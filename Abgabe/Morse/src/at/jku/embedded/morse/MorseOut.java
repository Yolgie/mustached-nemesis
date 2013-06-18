package at.jku.embedded.morse;

import java.io.IOException;
import java.io.Writer;

public class MorseOut {

	private final Writer w;
	
	public MorseOut(Writer w) {
		this.w = w;
	}
	
	public MorseOut write(Morse m, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			switch (m) {
			case DASH :
				w.write("-");
				break;
			case DOT:
				w.write(".");
				break;
			case GAP :
				w.write(" ");
				break;
			case END:
				break;
			}
		}
		return this;
	}

}
