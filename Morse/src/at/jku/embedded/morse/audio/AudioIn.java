package at.jku.embedded.morse.audio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import at.jku.embedded.morse.Morse;
import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;

public class AudioIn {

	private ExecutorService exec = Executors.newScheduledThreadPool(1);

	public static final int BITRATE = 22000;
	
	public static final int FRAME_BUFFER = 1024;
	public static final int FRAME_OVERLAP = 0;
	
	private int ditLength = 50;

	private Future<?> active;
	
	public int getDitLength() {
		return ditLength;
	}

	public void setDitLength(int ditLength) {
		this.ditLength = ditLength;
	}

	private Future<?> process(final AudioDispatcher dispatcher) {
		active = exec.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				processImpl(dispatcher);
				return null;
			}
		});
		return active;
	}

	private void processImpl(AudioDispatcher dispatcher) {
		final long frames = dispatcher.durationInFrames();
		System.out.println(frames);

		dispatcher.addAudioProcessor(new AudioProcessor() {
			@Override
			public void processingFinished() {
				notifyDone();
			}

			@Override
			public boolean process(AudioEvent audioEvent) {
				int bitRate = (int)audioEvent.getSampleRate();
				
				boolean[] upOrDown = new boolean[audioEvent.getFloatBuffer().length];
				
				notifySignalProcessed(bitRate, audioEvent.getFloatBuffer(), upOrDown);
				
				int dit = (bitRate / 1000) * ditLength;
				long frameIndex = audioEvent.getSamplesProcessed() - 6 * dit;
				
				Arrays.fill(upOrDown, 0, audioEvent.getFloatBuffer().length / 2, true);
				
				notifyChange(bitRate, (frameIndex += dit), dit, true);
				notifyChange(bitRate, (frameIndex += dit), dit, false);
				
				notifyChange(bitRate, (frameIndex += dit * 3), dit * 3, true);
				notifyChange(bitRate, (frameIndex += dit), dit, false);
				
				return !active.isCancelled();
			}
		});
		dispatcher.run();
		
	}

	public Future<?> load(File file) {
		try {
			return process(AudioDispatcher.fromFile(file, FRAME_BUFFER, FRAME_OVERLAP));
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Future<?> record() {
		try {
			final AudioFormat format = new AudioFormat(BITRATE, 16, 1, true,true);
			TargetDataLine line =  AudioSystem.getTargetDataLine(format);
			line.open(format, FRAME_BUFFER);
			line.start();
			AudioInputStream stream = new AudioInputStream(line);
			return process(new AudioDispatcher(stream, FRAME_BUFFER, FRAME_OVERLAP));
		} catch (UnsupportedAudioFileException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void notifyChange(int bitrate, long frameIndex, long durationFrames, boolean value) {
		long durationMs = durationFrames / (bitrate / 1000);
		
		double dits = durationMs / (double)ditLength;
		if (!value) {
			// downwards
			if (dits < 0.5 || dits > 3.5) {
				// error
				return;
			}
			if (dits <= 1.5) {
				// single dit
				notifyMorseAdded(Morse.DOT);
			} else {
				// three dit
				notifyMorseAdded(Morse.DASH);
			}
		} else {
			if (dits < 1.5) {
				// ignore just normal gap
			} else {
				double numberSpaces = (dits - 1.0d) / 2.0d;
				int spaces = Math.round((float) numberSpaces);
				for (int i = 0; i < spaces; i++) {
					notifyMorseAdded(Morse.GAP);
				}
			}
		}
	}
	
	protected void notifyMorseAdded(Morse morse) {
		
	}
	
	protected void notifyDitReceived(int ditIndex, int symbolIndex,
			boolean value) {
	}

	protected void notifySignalProcessed(int bitRate, float[] points, boolean[] upOrDown) {

	}
	
	protected void notifyDone() {
	}
}
