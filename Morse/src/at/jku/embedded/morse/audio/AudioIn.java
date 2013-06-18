package at.jku.embedded.morse.audio;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import at.jku.embedded.morse.Morse;
import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.filters.BandPass;
import be.hogent.tarsos.dsp.filters.HighPass;
import be.hogent.tarsos.dsp.filters.LowPassFS;

public class AudioIn {

	private ExecutorService exec = Executors.newScheduledThreadPool(1);

	public static final int BITRATE = 44100;
	
	public static final int FRAME_BUFFER = 44100 / 16;
	public static final int FRAME_OVERLAP = 0;
	
	private int ditLength = 250;

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

	private boolean up;
	
	private void processImpl(AudioDispatcher dispatcher) {
		final long frames = dispatcher.durationInFrames();
		System.out.println(frames);

		//dispatcher.addAudioProcessor(new HighPass(1800, BITRATE));
		//dispatcher.addAudioProcessor(new LowPassFS(2200, BITRATE));
		
		dispatcher.addAudioProcessor(new BinarySignalProcessor() {
			@Override
			protected void notifyChange(long frameIndex, boolean up,
					long framesSinceLastChange, int sampleRate) {
				notifyChangeImpl(sampleRate, frameIndex, framesSinceLastChange, up);
			}
		});
		
		dispatcher.addAudioProcessor(new AudioProcessor() {
			@Override
			public void processingFinished() {
				notifyDone();
			}

			int floats;
			long time = System.currentTimeMillis();
			
			@Override
			public boolean process(AudioEvent audioEvent) {
				int bitRate = (int)audioEvent.getSampleRate();
				floats += audioEvent.getFloatBuffer().length;
				
				
				System.out.println("floats" + floats +": " +(System.currentTimeMillis() - time) + "ms");
				
				System.out.println(audioEvent.getTimeStamp());
				
				notifySignalProcessed(bitRate, audioEvent.getFloatBuffer(), up);
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
			return process(AudioDispatcher.fromDefaultMicrophone(FRAME_BUFFER, FRAME_OVERLAP));
		} catch (UnsupportedAudioFileException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void notifyChangeImpl(int bitrate, long frameIndex, long durationFrames, boolean value) {
		System.out.println(bitrate);
		long durationMs = durationFrames / (bitrate / 1000);
		System.out.println(durationMs);
		this.up = value;
		double dits = durationMs / (double)ditLength;
		if (!value) {
			// downwards
			if (dits < 0.5 || dits > 4.5) {
				// error
				System.out.println("Error down: " + dits);
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
				System.out.println(spaces);
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

	protected void notifySignalProcessed(int bitRate, float[] points, boolean upOrDown) {

	}
	
	protected void notifyDone() {
	}
}
