package wizard.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

public class DoubleInputTextFieldListener implements KeyListener {

	private JTextField source;

	public DoubleInputTextFieldListener(JTextField source) {
		this.source = source;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char character = e.getKeyChar();
		if (((character < '0') || (character > '9')) && (character != '\b') && (character != '.')
				|| (character == '.' && this.source.getText().toString().indexOf('.') > 0)) {
			e.consume();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
