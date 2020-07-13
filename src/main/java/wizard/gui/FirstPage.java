package wizard.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import wizard.app.Wizard;
import wizard.enums.PageEnum;
import wizard.util.Constants;
import wizard.util.Keys;
import wizard.util.Texts;
import wizard.util.Values;

public class FirstPage extends JDialog {
	

	private JButton nextButton;
	public static JButton cancelButton = new JButton(Texts.CANCEL_BUTTON);
	private ButtonGroup radioButtonGroup = new ButtonGroup();

	public FirstPage() {
		Wizard.globalMap = new HashMap<>();
		
		this.setTitle(Constants.APP_TITLE);
		this.setSize(new Dimension(905, 600));
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.add(createHeaderPanel());
		this.add(createBinaryPanel());
		this.add(createGrayscalePanel());
		this.add(createButtonPanel());
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel();
		headerPanel.setPreferredSize(new Dimension(900, 50));
		headerPanel.setBackground(Constants.BACKGROUND_COLOR);
		Label headerLabel = new Label(Texts.FIRST_PAGE_HEADER, 1);
		headerLabel.setForeground(Constants.TEXT_COLOR);
		headerLabel.setFont(Constants.BIG_FONT_BOLD);
		headerPanel.add(headerLabel);
		return headerPanel;
	}

	private JPanel createBinaryPanel() {
		JPanel binaryPanel = new JPanel();
		binaryPanel.setBackground(Constants.BACKGROUND_COLOR);
		binaryPanel.setPreferredSize(new Dimension(900, 236));
		binaryPanel.setLayout(new BoxLayout(binaryPanel, BoxLayout.Y_AXIS));
		JRadioButton binaryRadioButton = new JRadioButton(Texts.BINARY_ELEMENT);
		binaryRadioButton.setForeground(Constants.TEXT_COLOR);
		binaryRadioButton.setBackground(Constants.BACKGROUND_COLOR);
		binaryRadioButton.setFont(Constants.SMALL_FONT_BOLD);
		binaryRadioButton.setHorizontalTextPosition(SwingConstants.LEFT);
		binaryRadioButton.addActionListener(binaryRadioButtonListener);
		JLabel binaryElText = new JLabel(Texts.BINARY_ELEMENT_TEXT);
		binaryPanel.add(binaryRadioButton);
		binaryPanel.add(binaryElText);

		radioButtonGroup.add(binaryRadioButton);
		return binaryPanel;

	}

	private JPanel createGrayscalePanel() {
		JPanel grayscalePanel = new JPanel();
		grayscalePanel.setBackground(Constants.BACKGROUND_COLOR);
		grayscalePanel.setPreferredSize(new Dimension(900, 236));
		grayscalePanel.setLayout(new BoxLayout(grayscalePanel, BoxLayout.Y_AXIS));
		JRadioButton grayscaleRadioButton = new JRadioButton(Texts.GRAYSCALE_ELEMENT);
		grayscaleRadioButton.setForeground(Constants.TEXT_COLOR);
		grayscaleRadioButton.setBackground(Constants.BACKGROUND_COLOR);
		grayscaleRadioButton.setFont(Constants.SMALL_FONT_BOLD);
		grayscaleRadioButton.setHorizontalTextPosition(SwingConstants.LEFT);
		grayscaleRadioButton.addActionListener(grayscaleRadioButtonListener);
		JLabel grayscaleElText = new JLabel(Texts.GRAYSCALE_ELEMENT_TEXT);
		grayscalePanel.add(grayscaleRadioButton);
		grayscalePanel.add(grayscaleElText);

		radioButtonGroup.add(grayscaleRadioButton);
		return grayscalePanel;
	}

	private Component createButtonPanel() {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBackground(Constants.BACKGROUND_COLOR);
		buttonsPanel.setPreferredSize(new Dimension(900, 50));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 0));
		nextButton = new JButton(Texts.NEXT_BUTTON);
		nextButton.addActionListener(nextButtonListener);
		buttonsPanel.add(nextButton);
		nextButton.setEnabled(false);
		buttonsPanel.add(cancelButton);
		cancelButton.addActionListener(cancelButtonListener);
		return buttonsPanel;
	}

	private ActionListener binaryRadioButtonListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Wizard.globalMap.put(Keys.wizardVar_Type, Values.THRESHOLD_ELEMENT);
			nextButton.setEnabled(true);
		}
	};

	private ActionListener grayscaleRadioButtonListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Wizard.globalMap.put(Keys.wizardVar_Type, Values.CONTRAST_ELEMENT);
			nextButton.setEnabled(true);
		}
	};

	private ActionListener nextButtonListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			FirstPage.this.setVisible(false);
			if (Values.THRESHOLD_ELEMENT.equals(Wizard.globalMap.get(Keys.wizardVar_Type))) {
				PageCaller.showPage(PageEnum.BINARY_ELEMENT);
			} else {
				PageCaller.showPage(PageEnum.GRAYSCALE_ELEMENT);
			}
		}
	};
	
	private ActionListener cancelButtonListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			FirstPage.this.setVisible(false);
		}
	};
	


}
