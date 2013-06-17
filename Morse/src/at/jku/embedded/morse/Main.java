package at.jku.embedded.morse;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.UnsupportedAudioFileException;

import at.jku.embedded.morse.audio.BinarySignalProcessor;
import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.filters.BandPass;
import be.hogent.tarsos.dsp.filters.HighPass;
import be.hogent.tarsos.dsp.pitch.McLeodPitchMethod;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchDetector;

public class Main {

	public static void main(String[] args) throws IOException {
		File audioFile = new File("../sos.wav");
		
		try {
			AudioDispatcher dispatcher = AudioDispatcher.fromFile(audioFile, 4410, 0);

			final long frames = dispatcher.durationInFrames();
			System.out.println(frames);
			
			dispatcher.addAudioProcessor(new BandPass(2000, 100, 44100));
			
			dispatcher.addAudioProcessor(new BinarySignalProcessor());
			
			dispatcher.run();
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}
}
