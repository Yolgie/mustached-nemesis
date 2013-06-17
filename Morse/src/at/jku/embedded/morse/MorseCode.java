package at.jku.embedded.morse;

public class MorseCode {

	private final Morse[] morses;
	
	private MorseCode(Morse[] morses) {
		this.morses = morses;
	}
	
	public Morse[] getMorses() {
		return morses;
	}
	
	public static MorseCode fromString(String s) {
		s = s.trim();
		Morse[] morses = new Morse[s.length()];
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '.' : morses[i] = Morse.DOT;break;
			case '-' : morses[i] = Morse.DASH;break;
			case ' ' : morses[i] = Morse.GAP;break;
			}
		}
		return new MorseCode(morses);
	}
	
}
