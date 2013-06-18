package at.jku.embedded.morse;

public enum Morse {

	DOT, DASH, GAP, END;

	public String toString() {
		switch (this) {
		case DASH : return "-";
		case DOT : return ".";
		case GAP : return " ";
		case END : return " ";
		}
		return "e";
	};
	
	
}
