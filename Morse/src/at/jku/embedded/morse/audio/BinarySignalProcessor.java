package at.jku.embedded.morse.audio;

import java.util.Arrays;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;

public class BinarySignalProcessor implements AudioProcessor {
	long lastChangeFrame = 0;
	boolean lastStatus = false;
	double threshold = 0.6;
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		int sampleRate = (int) audioEvent.getSampleRate();
		
		double max = maxValue(audioEvent.getFloatBuffer());
		long frameIndex = audioEvent.getSamplesProcessed();

		//System.out.println((1000*frameIndex/sampleRate) + " ms ## " + rms + " ## " + Math.abs(rms - threshold) + " (" + frameBuffer.length + ")");
		if (max > threshold) {
			if (!lastStatus) {
				notifyChange(frameIndex, true, frameIndex - lastChangeFrame, sampleRate);
				lastStatus = true;
				lastChangeFrame = frameIndex;
			}
		} else {
			if (lastStatus) {
				notifyChange(frameIndex, false, frameIndex - lastChangeFrame, sampleRate);
				lastStatus = false;
				lastChangeFrame = frameIndex;
			}
		}
		
		//System.out.println("Sample Rate: " + sampleRate + " Position: " + (Math.round(audioEvent.getTimeStamp()*100.0)/100.0) + " sec (" + audioEvent.getSamplesProcessed() + " frames processed)");
		
		//PitchDetector pitchDetector = new McLeodPitchMethod(sampleRate);
		//PitchDetectionResult detectionResult = pitchDetector.getPitch(audioEvent.getFloatBuffer());
		//System.out.println("PitchDetectionResult: " + detectionResult.getPitch() + " P() " + detectionResult.getProbability() + " is? " + detectionResult.isPitched());
		//System.out.println(Arrays.toString(audioEvent.getFloatBuffer()));

		//notifySignalProcessed(audioEvent.getFloatBuffer());
		return true;
	}

	@Override
	public void processingFinished() {
	}
	
	protected void notifyChange(long frameIndex, boolean up, long framesSinceLastChange, int sampleRate) {
		if (up) {
			System.out.println("UP    @" + frameIndex + "frames / " + (1000*frameIndex/sampleRate) + "ms since " + framesSinceLastChange + " frames.");
		} else {
			System.out.println("DOWN  @" + frameIndex + "frames / " + (1000*frameIndex/sampleRate) + "ms since " + framesSinceLastChange + " frames.");
		}
	}
	
	private static float maxValue(float[] floats) {
		float max = floats[0];
		for (int i = 0; i < floats.length; i++) {
			if (floats[i] > max) {
				max = floats[i];
			}
		}
		return max;
	}
}
