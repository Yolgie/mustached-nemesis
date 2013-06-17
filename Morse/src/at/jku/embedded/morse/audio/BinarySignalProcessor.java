package at.jku.embedded.morse.audio;

import java.util.Arrays;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;

public class BinarySignalProcessor implements AudioProcessor {
	int windowPartitions = 10;
	
	long lastChangeFrame = 0;
	boolean lastStatus = false;
	double threshold = 0.0002;
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		int sampleRate = (int) audioEvent.getSampleRate();
		int framesPerWindow = audioEvent.getBufferSize() / windowPartitions;
		
		long frameIndex;
		double rms;
		
		for (int frameDelta = 0; frameDelta < audioEvent.getBufferSize(); frameDelta += framesPerWindow) {
			float[] frameBuffer = Arrays.copyOfRange(audioEvent.getFloatBuffer(), frameDelta, Math.min(frameDelta + framesPerWindow, audioEvent.getBufferSize()));
			
			rms = AudioEvent.calculateRMS(frameBuffer);
			
			
			// rms > threshold means we hat a signal
			frameIndex = audioEvent.getSamplesProcessed() + frameDelta;
			if (rms > threshold) {
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

			//System.out.println((1000*frameIndex/sampleRate) + " ms ## " + rms + " ## " + Math.abs(rms - threshold) + " (" + frameBuffer.length + ")");
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

}
