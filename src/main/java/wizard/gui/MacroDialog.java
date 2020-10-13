package wizard.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import wizard.app.Wizard;
import wizard.util.Constants;
import wizard.util.Keys;
import wizard.util.Texts;

public class MacroDialog extends JDialog {

	private JButton backButton;
	private JButton saveButton;
	private JTextArea textArea;

	public MacroDialog() {

		Dimension screenSize = getHalfScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();

		this.setTitle(Constants.APP_TITLE);
		this.setLayout(new GridBagLayout());

		this.setBackground(Constants.BACKGROUND_COLOR);
		this.setSize(width, height);
		this.setLocationRelativeTo(null);

		textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setPreferredSize(new Dimension(width - 5, height - 75));
		scroll.setBorder(BorderFactory.createLineBorder(Constants.SEPARATOR_COLOR, 3));

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBackground(Constants.BACKGROUND_COLOR);
		buttonsPanel.setPreferredSize(new Dimension(width, 50));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 10));
		backButton = new JButton(Texts.BACK_BUTTON);
		backButton.addActionListener(backListener);
		buttonsPanel.add(backButton);
		saveButton = new JButton(Texts.SAVE_BUTTON);
		saveButton.addActionListener(saveListener);
		buttonsPanel.add(saveButton);

		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 15;
		this.add(scroll, gc);
		gc.gridy = 15;
        gc.weightx = 1;
        gc.weighty = 1;
		this.add(buttonsPanel, gc);
	}

	public void setText(String text) {
		this.textArea.setText(text);
	}

	private Dimension getHalfScreenSize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		return new Dimension((int) Math.round(width / 2), (int) Math.round(height / 2));
	}

	private ActionListener backListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			MacroDialog.this.setVisible(false);
		}
	};

	private ActionListener saveListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String macroText = textArea.getText();
			if (macroText != null) {
				Wizard.globalMap.put(Keys.wizardVar_Filters, macroText);
				MacroDialog.this.setVisible(false);
			}
		}
	};

}
