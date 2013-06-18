package at.jku.embedded.morse;

import java.io.IOException;

import at.jku.embedded.morse.ui.CommandLineApplication;
import at.jku.embedded.morse.ui.MorseApplication;

public class Main {

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			MorseApplication.main(args);
		} else {
			CommandLineApplication.main(args);
		}
	}
}
