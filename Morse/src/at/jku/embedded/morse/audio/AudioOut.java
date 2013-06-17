package at.jku.embedded.morse.audio;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import at.jku.embedded.morse.Morse;
import at.jku.embedded.morse.MorseCode;

public class AudioOut {

	private ExecutorService exec = Executors.newScheduledThreadPool(1);

	private int ditLength = 100; // ms
	private final int sampleRate = 16000;
	
	private Future<?> active;
	
	public void setDitLength(int ditLength) {
		this.ditLength = ditLength;
	}
	
	public int getDitLength() {
		return ditLength;
	}
	
	public Future<?> play(final MorseCode out) {
		if (active != null) {
			return active;
		}
		active = exec.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				playAsync(out);
				return null;
			}
		});
		return active;
	}
	
	private void playAsync(MorseCode out) throws LineUnavailableException {
		AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
		SourceDataLine line = AudioSystem.getSourceDataLine(format);
		line.open(format, 4096); // open line with 4 KB buffer
		
		int index = 0;
		for (Morse morse : out.getMorses()) {
			
			notifyPlayingIndex(index);
			
			switch (morse) {
			case DASH:
				play(line, true);
				play(line, true);
				play(line, true);
				play(line, false);
				break;
			case DOT:
				play(line, true);
				play(line, false);
				break;
			case GAP:
				play(line, false);
				play(line, false);
				break;
			case END:
				break;
			}
			
			if (active.isCancelled()) {
				break;
			}
			
			index++;
		}
		
		line.drain();
		line.close(); // close line
		
		active = null;
		notifyPlayedDone();
	}
	
	protected void notifyPlayingIndex(int index) {
		
	}
	
	protected void notifyPlayedDone() {
		
	}
	
	private void play(SourceDataLine line, boolean enabled) {
		int ditLength = (sampleRate / 1000) * this.ditLength;
		
		for (int i = 0; i < ditLength; i++) { // play 1 sec tone (16000 samples)
			// start to play when buffer is 3/4 filled (avoids buffer
			// underrun)
			if (line.available() < 1024)
				line.start();
			// v(i) = sin( 2 * pi * frequency * i/sampleRate ) * amplitude
			short v = 0;
			if (enabled) {
				v = (short) (Math.sin(2 * Math.PI * 2000 * i / 16000) * (Short.MAX_VALUE));
			}
			// write short v in big endian format to line. blocks if buffer
			// full
			line.write(new byte[] { (byte) (v >> 8), (byte) v }, 0, 2);
		}
	}
	

}
