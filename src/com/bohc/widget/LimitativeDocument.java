package com.bohc.widget;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * description:自定义的Document 可以控制最大行数 默认最大为10行 超过最大行时，上面的一行将被截取
 */
@SuppressWarnings("serial")
public class LimitativeDocument extends PlainDocument {
	private JTextComponent textComponent;
	private int lineMax = 10;

	public LimitativeDocument(JTextComponent tc, int lineMax) {
		textComponent = tc;
		this.lineMax = lineMax;
	}

	public LimitativeDocument(JTextComponent tc) {
		textComponent = tc;
	}

	public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException {

		String value = textComponent.getText();
		int overrun = 0;
		if (value != null && value.indexOf(' ') >= 0 && value.split(" ").length >= lineMax) {
			overrun = value.indexOf(' ') + 1;
			super.remove(0, overrun);
		}
		super.insertString(offset - overrun, s, attributeSet);
	}
}
