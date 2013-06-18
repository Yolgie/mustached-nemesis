package at.jku.embedded.morse.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.apache.commons.cli.*;

import at.jku.embedded.morse.MorseCoder;
import at.jku.embedded.morse.MorseIn;
import at.jku.embedded.morse.MorseOut;



public class CommandLineApplication {

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("h", false, "print this message");
		options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("encode text as morse code").create('e'));
		options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("decode morse code to text").create('d'));
		options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("store result in file").create('s'));
		options.addOption(OptionBuilder.withArgName("length").hasArg().withDescription("set the dot length").create('l'));
		
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("CommandLineApplication -e|d <filename> [-s <filename>] [-l <length>] [-h]", options);
			} else {
				String result = null;
				
				if (cmd.hasOption('e')) {
					String input = readStringFromFile(cmd.getOptionValue('e')).toUpperCase().replaceAll("[^" + MorseCoder.getAllowedCharacters() + "]+","");
					//System.out.println(input);
					if (input != null) {
						MorseCoder coder = new MorseCoder();
						StringWriter w = new StringWriter();
						try {
							coder.encodeText(new StringReader(input), new MorseOut(w));
						} catch (IOException e) {
							System.err.println("Encoding failed. Reason: " + e.getMessage());
						}
						result = w.getBuffer().toString();
						System.out.println(result);
					}
				} else if (cmd.hasOption('d')){
					String input = readStringFromFile(cmd.getOptionValue('d')).replaceAll("[^-. ]+","");
					//System.out.println(input);
					if (input != null) {
						MorseCoder coder = new MorseCoder();
						StringWriter w = new StringWriter();
						try {
							coder.decodeText(new MorseIn(new StringReader(input)), w);
						} catch (IOException e) {
							System.err.println("Decoding failed. Reason: " + e.getMessage());
						}
						result = w.getBuffer().toString();
						System.out.println(result);
					}
				}
				
				if (cmd.hasOption('s')) {
					if (result != null && result.length() > 0) {
						try {
							File file = new File(cmd.getOptionValue('s'));
							BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charset.defaultCharset());
							writer.write(result);
							writer.close();
						} catch (IOException e) {
							System.err.println("Storing result failed. Reason: " + e.getMessage());
						}
					}
				}
			
			}		
		} catch (ParseException e) {
			System.err.println("Parsing of command line arguments failed. Reason: " + e.getMessage());
		}
	}
	
	private static String readStringFromFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			StringBuilder b = new StringBuilder();
			try {
				for (String line : Files.readAllLines(file.toPath(), Charset.defaultCharset())) {
					b.append(line);
				}
				return b.toString();
			} catch (IOException e) {
				System.err.println("Reading of input file " + path + " failed. Reason: " + e.getMessage());
			}
		}
		return null;
	}

}
