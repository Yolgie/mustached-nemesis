package at.jku.embedded.morse;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;

public class Main {

	public static void main(String[] args) throws IOException {
		MorseCoder coder = new MorseCoder();
		final File inFile = new File("../sample2.txt");
		
		String input = new String(Files.readAllBytes(inFile.toPath()));
		input = input.trim();
		
		StringWriter w = new StringWriter();
		System.out.println("Input:   " + input);
		coder.decodeText(new MorseIn(new StringReader(input)), w);
		
		String s = w.getBuffer().toString();
		System.out.println("Decoded: " + s);
		
		StringWriter w2 = new StringWriter();
		coder.encodeText(new StringReader(s), new MorseOut(w2));
		
		String result = w2.getBuffer().toString().trim();
		
		System.out.println("Encoded: " + result);
		System.out.println("Valid: " + input.equals(result));
	}
	
}
