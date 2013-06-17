package at.jku.embedded.morse.audio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.filters.HighPass;

public class AudioIn {

	private ExecutorService exec = Executors.newScheduledThreadPool(1);

	private int ditLength = 50;

	public int getDitLength() {
		return ditLength;
	}

	public void setDitLength(int ditLength) {
		this.ditLength = ditLength;
	}

	public Future<?> load(File file) {
		try {
			return process(AudioDispatcher.fromFile(file, 1000, 0));
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Future<?> process(final AudioDispatcher dispatcher) {
		return exec.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				processImpl(dispatcher);
				return null;
			}
		});
	}

	private void processImpl(AudioDispatcher dispatcher) {
		final long frames = dispatcher.durationInFrames();
		System.out.println(frames);

		dispatcher.addAudioProcessor(new AudioProcessor() {
			@Override
			public void processingFinished() {
			}

			float frame;

			@Override
			public boolean process(AudioEvent audioEvent) {
				int sampleRate = (int) audioEvent.getSampleRate();
				System.out.println(Arrays.toString(audioEvent.getFloatBuffer()));

				notifySignalProcessed(audioEvent.getFloatBuffer());
				return true;
			}
		});
	}

	public Future<?> record() {
		try {
			return process(AudioDispatcher.fromDefaultMicrophone(1000, 0));
		} catch (UnsupportedAudioFileException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void notifyDitReceived(int ditIndex, int symbolIndex,
			boolean value) {

	}

	protected void notifySignalProcessed(float[] points) {

	}

	public static void main(String[] args) {
		AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
		TargetDataLine line;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			// Handle the error ...

		}
		// Obtain and open the line.
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);

			line.addLineListener(new LineListener() {

				@Override
				public void update(LineEvent event) {
					System.out.println(event);
				}
			});

			line.start();

			Thread.sleep(1000);

			line.stop();

			int available = line.available();
			byte[] buffer = new byte[available];
			line.read(buffer, 0, available);

			System.out.println(Arrays.toString(buffer));

		} catch (LineUnavailableException ex) {
			// Handle the error ...
			ex.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
