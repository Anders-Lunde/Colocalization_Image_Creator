package wizard.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;

import wizard.app.Wizard;
import wizard.enums.PageEnum;
import wizard.util.Constants;
import wizard.util.Keys;
import wizard.util.Texts;

public class GrayscaleElement extends JDialog {

	private JButton advancedOptionsButton;
	private JButton backButton;
	public static JButton previewButton = new JButton(Texts.PREVIEW_BUTTON);
	public static JButton finishButton = new JButton(Texts.FINISH_BUTTON);
	public static JButton cancelButton = new JButton(Texts.CANCEL_BUTTON);
	public static JCheckBox reuseButton = new JCheckBox(Texts.REUSE_VALUES);
	private static JCheckBox filterEnableInStackCheckBox;
	private static JCheckBox filterEnableZProjection;
	private static JComboBox<String> dropDownRed;
	private static JComboBox<String> dropDownBlue;
	private static JComboBox<String> dropDownGreen;
	private static JComboBox<String> dropDownYellow;
	private static JComboBox<String> dropDownCyan;
	private static JComboBox<String> dropDownMagenta;
	private static JComboBox<String> dropDownWhite;
	private static JComboBox<String> insertDropDown;

	public GrayscaleElement() {

		this.setTitle(Constants.APP_TITLE);
		this.setSize(new Dimension(800, 602));
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		StringBuilder MacroSB = new StringBuilder();
		MacroSB.append("//Example commands below. Disable code by inserting \"//\" in front of text.");
		MacroSB.append(System.getProperty("line.separator"));
		MacroSB.append("//Try to adjust order, values, or add new commands.");
		MacroSB.append(System.getProperty("line.separator"));
		MacroSB.append("//Remember to enable macro for \"individual sections\" and/or \"Z-projection\" in the previous menu.");
		MacroSB.append(System.getProperty("line.separator"));
		MacroSB.append(System.getProperty("line.separator"));
		MacroSB.append("//run(\"Minimum...\", \"radius=2\");");
		MacroSB.append(System.getProperty("line.separator"));
		MacroSB.append("//run(\"Erode\");");
		MacroSB.append(System.getProperty("line.separator"));
		MacroSB.append("//run(\"Remove Outliers...\", \"radius=8 threshold=50 which=Bright\");");
		MacroSB.append(System.getProperty("line.separator"));
		String defaultMacroString = MacroSB.toString();
		
		Wizard.globalMap.put(Keys.wizardVar_Filters, defaultMacroString);

		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 10));
		headerPanel.setPreferredSize(new Dimension(800, 50));
		headerPanel.setBackground(Constants.BACKGROUND_COLOR);
		Label headerLabel = new Label(Texts.GRAYSCALE_ELEMENT);
		headerLabel.setForeground(Constants.TEXT_COLOR);
		headerLabel.setFont(Constants.SMALL_FONT_BOLD);
		headerPanel.add(headerLabel);

		JPanel inputChannelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		inputChannelsPanel.setPreferredSize(new Dimension(800, 300));
		inputChannelsPanel.setBackground(Constants.BACKGROUND_COLOR);
		inputChannelsPanel.add(makeLabel(Texts.INPUT_CHANNELS_LABEL, 800));
		inputChannelsPanel.add(makeInputChannelDropDownsPanel());

		JPanel channelInsertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		channelInsertPanel.setPreferredSize(new Dimension(800, 50));
		channelInsertPanel.setBackground(Constants.BACKGROUND_COLOR);
		channelInsertPanel.add(makeLabel(Texts.CHANNEL_TO_INSERT, 130));
		channelInsertPanel.add(makeChannelInsertDropDownsPanel());
		
		JPanel imageJMacro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		imageJMacro.setPreferredSize(new Dimension(800, 115));
		imageJMacro.setBackground(Constants.BACKGROUND_COLOR);
		JLabel label = new JLabel(Texts.IMAGE_J_MACRO_TEXT_GRAYSCALE);
		label.setPreferredSize(new Dimension(800, 50));
		label.setForeground(Constants.TEXT_COLOR);
		label.setFont(Constants.SMALL_FONT);
		imageJMacro.add(label);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setBackground(Constants.BACKGROUND_COLOR);
		JButton addMacroButton = new JButton(Texts.ADD_MACRO_COMMANDS);
		addMacroButton.addActionListener(addMacroButtonListener);
		buttonPanel.add(addMacroButton);
		buttonPanel.setPreferredSize(new Dimension(150, 30));
		imageJMacro.add(buttonPanel);
		imageJMacro.add(makeMacroCheckBoxes());

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBackground(Constants.BACKGROUND_COLOR);
		buttonsPanel.setPreferredSize(new Dimension(800, 50));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 10));
		//advancedOptionsButton = new JButton(Texts.ADVANCED_OPTIONS_BUTTON);
		//advancedOptionsButton.addActionListener(advancedOptionsListener);
		reuseButton.setBackground(Constants.BACKGROUND_COLOR);
		reuseButton.setToolTipText(Texts.REUSEBUTTON_HOVER);
		reuseButton.setSelected(true);
		buttonsPanel.add(reuseButton);
		buttonsPanel.add(previewButton);
		//buttonsPanel.add(advancedOptionsButton);
		backButton = new JButton(Texts.BACK_BUTTON);
		backButton.addActionListener(backListener);
		buttonsPanel.add(backButton);
		buttonsPanel.add(finishButton);
		buttonsPanel.add(cancelButton);
		finishButton.addActionListener(finishButtonListener);
		cancelButton.addActionListener(closeWindowListener);

		this.add(headerPanel);
		this.add(inputChannelsPanel);
		this.add(getSeparator());
		this.add(channelInsertPanel);
		this.add(getSeparator());
		this.add(imageJMacro);
		this.add(getSeparator());
		this.add(buttonsPanel);

	}

	private JPanel getSeparator() {
		JPanel separatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
		separatorPanel.setPreferredSize(new Dimension(800, 3));
		separatorPanel.setBackground(Constants.BACKGROUND_COLOR);
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		separator.setPreferredSize(new Dimension(750, 2));
		separator.setBackground(Constants.SEPARATOR_COLOR);
		separatorPanel.add(separator);
		return separatorPanel;
	}

	private JLabel makeLabel(String labelText, int width) {
		JLabel label = new JLabel(labelText);
		label.setPreferredSize(new Dimension(width, 25));
		label.setForeground(Constants.TEXT_COLOR);
		label.setFont(Constants.SMALL_FONT);
		return label;
	}

	private JPanel makeInputChannelDropDownsPanel() {
		JPanel insertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		insertPanel.setPreferredSize(new Dimension(800, 300));
		insertPanel.setBackground(Constants.BACKGROUND_COLOR);
		JPanel dropsPanel = new JPanel();
		dropsPanel.setLayout(new BoxLayout(dropsPanel, BoxLayout.Y_AXIS));
		int tic = Wizard.nInputImpChannels;
		String[] items = new String[tic + 1];
		items[0] = "None";
		for (int i = 1; i <= tic; i++) {
			items[i] = String.valueOf(i);
		}

		JPanel redPanel = new JPanel();
		JLabel labelRed = new JLabel(Texts.COLOR_RED_PR);
		dropDownRed = getDropDown(items);
		labelRed.setPreferredSize(new Dimension(80, 25));
		redPanel.add(labelRed);
		redPanel.add(dropDownRed);
		redPanel.setBackground(Constants.BACKGROUND_COLOR);

		JPanel greenPanel = new JPanel();
		JLabel labelGreen = new JLabel(Texts.COLOR_GREEN_PR);
		dropDownGreen = getDropDown(items);
		labelGreen.setPreferredSize(new Dimension(80, 25));
		greenPanel.add(labelGreen);
		greenPanel.add(dropDownGreen);
		greenPanel.setBackground(Constants.BACKGROUND_COLOR);

		JPanel bluePanel = new JPanel();
		JLabel labelBlue = new JLabel(Texts.COLOR_BLUE_PR);
		dropDownBlue = getDropDown(items);
		labelBlue.setPreferredSize(new Dimension(80, 25));
		bluePanel.add(labelBlue);
		bluePanel.add(dropDownBlue);
		bluePanel.setBackground(Constants.BACKGROUND_COLOR);

		JPanel yellowPanel = new JPanel();
		JLabel labelYellow = new JLabel(Texts.COLOR_YELLOW_PR);
		dropDownYellow = getDropDown(items);
		labelYellow.setPreferredSize(new Dimension(80, 25));
		yellowPanel.add(labelYellow);
		yellowPanel.add(dropDownYellow);
		yellowPanel.setBackground(Constants.BACKGROUND_COLOR);

		JPanel cyanPanel = new JPanel();
		JLabel labelCyan = new JLabel(Texts.COLOR_CYAN_PR);
		dropDownCyan = getDropDown(items);
		labelCyan.setPreferredSize(new Dimension(80, 25));
		cyanPanel.add(labelCyan);
		cyanPanel.add(dropDownCyan);
		cyanPanel.setBackground(Constants.BACKGROUND_COLOR);

		JPanel magentaPanel = new JPanel();
		JLabel labelMagenta = new JLabel(Texts.COLOR_MAGENTA_PR);
		dropDownMagenta = getDropDown(items);
		labelMagenta.setPreferredSize(new Dimension(80, 25));
		magentaPanel.add(labelMagenta);
		magentaPanel.add(dropDownMagenta);
		magentaPanel.setBackground(Constants.BACKGROUND_COLOR);

		JPanel whitePanel = new JPanel();
		JLabel labelWhite = new JLabel(Texts.COLOR_WHITE_PR);
		dropDownWhite = getDropDown(items);
		labelWhite.setPreferredSize(new Dimension(80, 25));
		whitePanel.add(labelWhite);
		whitePanel.add(dropDownWhite);
		whitePanel.setBackground(Constants.BACKGROUND_COLOR);

		dropsPanel.add(redPanel);
		dropsPanel.add(greenPanel);
		dropsPanel.add(bluePanel);
		dropsPanel.add(yellowPanel);
		dropsPanel.add(cyanPanel);
		dropsPanel.add(magentaPanel);
		dropsPanel.add(whitePanel);
		insertPanel.add(dropsPanel);
		return insertPanel;
	}

	private JPanel makeChannelInsertDropDownsPanel() {
		JPanel insertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
		insertPanel.setPreferredSize(new Dimension(150, 50));
		int moc = Wizard.nOutputImpChannels;
		String[] items = new String[moc+1];
		for (int i = 0; i < moc; i++) {
			items[i] = String.valueOf(i + 1);
		}
		items[moc] = String.valueOf(moc+1) + Texts.CREATES_NEW;
		insertDropDown = new JComboBox<>(items);
		insertDropDown.setPreferredSize(new Dimension(100, 25));
		insertPanel.add(insertDropDown);
		insertPanel.setBackground(Constants.BACKGROUND_COLOR);
		return insertPanel;
	}

	private JComboBox<String> getDropDown(String[] items) {
		JComboBox<String> dropDown = new JComboBox<>(items);
		dropDown.setPreferredSize(new Dimension(100, 25));
		return dropDown;
	}

	public static boolean validateForm() {
		if (dropDownRed.getSelectedIndex() == 0 && dropDownGreen.getSelectedIndex() == 0
				&& dropDownBlue.getSelectedIndex() == 0 && dropDownYellow.getSelectedIndex() == 0
				&& dropDownCyan.getSelectedIndex() == 0 && dropDownMagenta.getSelectedIndex() == 0
				&& dropDownWhite.getSelectedIndex() == 0) {
			JOptionPane.showMessageDialog(new JFrame(), Texts.COLOR_NOT_SELECTED, "Alert",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		String ddRed = dropDownRed.getSelectedItem().toString().equals("None") ? "*None*" : dropDownRed.getSelectedItem().toString();
		String ddGreen = dropDownGreen.getSelectedItem().toString().equals("None") ? "*None*" : dropDownGreen.getSelectedItem().toString();
		String ddBlue = dropDownBlue.getSelectedItem().toString().equals("None") ? "*None*" : dropDownBlue.getSelectedItem().toString();
		String ddYellow = dropDownYellow.getSelectedItem().toString().equals("None") ? "*None*" : dropDownYellow.getSelectedItem().toString();
		String ddCyan = dropDownCyan.getSelectedItem().toString().equals("None") ? "*None*" : dropDownCyan.getSelectedItem().toString();
		String ddMagenta = dropDownMagenta.getSelectedItem().toString().equals("None") ? "*None*" : dropDownMagenta.getSelectedItem().toString();
		String ddWhite = dropDownWhite.getSelectedItem().toString().equals("None") ? "*None*" : dropDownWhite.getSelectedItem().toString();
		
		Wizard.globalMap.put(Keys.wizardVar_Red, ddRed);
		Wizard.globalMap.put(Keys.wizardVar_Green, ddGreen);
		Wizard.globalMap.put(Keys.wizardVar_Blue, ddBlue);
		Wizard.globalMap.put(Keys.wizardVar_Yellow, ddYellow);
		Wizard.globalMap.put(Keys.wizardVar_Cyan, ddCyan);
		Wizard.globalMap.put(Keys.wizardVar_Magenta, ddMagenta);
		Wizard.globalMap.put(Keys.wizardVar_Grays, ddWhite);
		
		String value = String.valueOf(insertDropDown.getSelectedIndex() + 1);
		if(value.contains(Texts.CREATES_NEW)) {
			value = value.substring(0, value.indexOf(" "));
		}
		Wizard.globalMap.put(Keys.wizardVar_Output_Channel, value);
		return true;
	}
	
	private boolean shortValidateForm() {
		if (dropDownRed.getSelectedIndex() == 0 && dropDownGreen.getSelectedIndex() == 0
				&& dropDownBlue.getSelectedIndex() == 0 && dropDownYellow.getSelectedIndex() == 0
				&& dropDownCyan.getSelectedIndex() == 0 && dropDownMagenta.getSelectedIndex() == 0
				&& dropDownWhite.getSelectedIndex() == 0) {
			return false;
		}
		return true;
	}

	private ActionListener advancedOptionsListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (validateForm()) {
				GrayscaleElement.this.setVisible(false);
				PageCaller.showPage(PageEnum.ADVANCED_OPTIONS_GRAYSCALE);
			}
		}
	};

	public ActionListener backListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Wizard.globalMap.remove(Keys.wizardVar_Type);
			Wizard.globalMap.remove(Keys.wizardVar_Color);
			Wizard.globalMap.remove(Keys.wizardVar_AND_Channels);
			Wizard.globalMap.remove(Keys.wizardVar_Outline);
			Wizard.globalMap.remove(Keys.wizardVar_Output_Channel);
			Wizard.globalMap.remove(Keys.wizardVar_NOT_Channels);
			Wizard.globalMap.remove(Keys.wizardVar_rmv_smaller_than);
			Wizard.globalMap.remove(Keys.wizardVar_unit);
			Wizard.globalMap.remove(Keys.wizardVar_rmv_nonstack);
			Wizard.globalMap.remove(Keys.wizardVar_rmv_stack);
			Wizard.globalMap.remove(Keys.wizardVar_rmv_zproj);
			Wizard.globalMap.remove(Keys.wizardVar_Filters);
			Wizard.globalMap.remove(Keys.wizardVar_Filters_nonstack);
			Wizard.globalMap.remove(Keys.wizardVar_Filters_stack);
			Wizard.globalMap.remove(Keys.wizardVar_Filters_zproj);
			Wizard.globalMap.remove(Keys.wizardVar_Red);
			Wizard.globalMap.remove(Keys.wizardVar_Green);
			Wizard.globalMap.remove(Keys.wizardVar_Blue);
			Wizard.globalMap.remove(Keys.wizardVar_Yellow);
			Wizard.globalMap.remove(Keys.wizardVar_Cyan);
			Wizard.globalMap.remove(Keys.wizardVar_Magenta);
			Wizard.globalMap.remove(Keys.wizardVar_Grays);
			PageCaller.showPage(PageEnum.FIRST_PAGE);
			GrayscaleElement.this.setVisible(false);
		}
	};
	
	
	private ActionListener addMacroButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			MacroDialog macroDialog = new MacroDialog();
			String text = Wizard.globalMap.get(Keys.wizardVar_Filters);
			if (text != null) {
				macroDialog.setText(text);
			}
			macroDialog.setVisible(true);
		}
	};
	
	private JPanel makeMacroCheckBoxes() {
		JPanel macroCheckBoxesPanel = new JPanel();
		macroCheckBoxesPanel.setBackground(Constants.BACKGROUND_COLOR);
		//macroCheckBoxesPanel.setLayout(new BoxLayout(macroCheckBoxesPanel, BoxLayout.Y_AXIS));
		macroCheckBoxesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		filterEnableInStackCheckBox = new JCheckBox(Texts.ENABLE_INSTACK);
		filterEnableInStackCheckBox.setBackground(Constants.BACKGROUND_COLOR);
		filterEnableZProjection = new JCheckBox(Texts.Z_PROJECTION);
		filterEnableZProjection.setBackground(Constants.BACKGROUND_COLOR);
		macroCheckBoxesPanel.add(filterEnableInStackCheckBox);
		macroCheckBoxesPanel.add(filterEnableZProjection);
		return macroCheckBoxesPanel;
	}
	
	private ActionListener closeWindowListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			GrayscaleElement.this.setVisible(false);
		}
	};
	
	private ActionListener finishButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(shortValidateForm()) {
				GrayscaleElement.this.setVisible(false);
			}
		}
	};

	public static void fillChosenOptions() {
		if(Wizard.globalMap.get(Keys.wizardVar_Filters) == null) {
			Wizard.globalMap.put(Keys.wizardVar_Filters, "");
		}
		if (filterEnableInStackCheckBox.isSelected()) {
			Wizard.globalMap.put(Keys.wizardVar_Filters_nonstack, "1");
			Wizard.globalMap.put(Keys.wizardVar_Filters_stack, "1");
		} else {
			Wizard.globalMap.put(Keys.wizardVar_Filters_nonstack, "0");
			Wizard.globalMap.put(Keys.wizardVar_Filters_stack, "0");
		}
		if (filterEnableZProjection.isSelected()) {
			Wizard.globalMap.put(Keys.wizardVar_Filters_zproj, "1");
		} else {
			Wizard.globalMap.put(Keys.wizardVar_Filters_zproj, "0");
		}
	}
}
