package at.jku.embedded.morse.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;
	
	private final String limitedCharacters;
	
	public LimitedDocument(String limitedCharacters) {
		this.limitedCharacters = limitedCharacters;
	}
	
	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		str = str.toUpperCase();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (limitedCharacters.indexOf(c) != -1) {
				b.append(c);
			}
		}
		super.insertString(offs, b.toString(), a);
	}
	
}
