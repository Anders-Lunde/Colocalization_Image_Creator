package wizard.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import wizard.app.Wizard;
import wizard.enums.PageEnum;
import wizard.listeners.DoubleInputTextFieldListener;
import wizard.util.Constants;
import wizard.util.Keys;
import wizard.util.Texts;
import wizard.util.Values;

public class BinaryElement extends JDialog {

	private JButton advancedOptionsButton;
	private JButton backButton;
	public static JCheckBox reuseButton = new JCheckBox(Texts.REUSE_VALUES);
	public static JButton previewButton = new JButton(Texts.PREVIEW_BUTTON);
	public static JButton finishButton = new JButton(Texts.FINISH_BUTTON);
	public static JButton cancelButton = new JButton(Texts.CANCEL_BUTTON);
	private static ButtonGroup radioButtonGroup;
	private static ButtonGroup outlineRadioButtonGroup;
	private static JCheckBox[] checkBoxes;
	private static JComboBox<String>[] channelDropDowns;
	private static JComboBox<String> dropDown;
	private static JCheckBox filterEnableInStackCheckBox;
	private static JCheckBox filterEnableZProjection;
	private  JPanel wrapperPanel;
	private static JTextField scaleTextField;
	private static JCheckBox removeEnableInStackCheckBox;
	private static JCheckBox removeZProjectionCheckBox;
	private static ButtonGroup bg;

	public BinaryElement() {
		//Set as selected, or preview doesn't work (depends on both being selected)
		AdvancedOptionsBinary.reuseButton.setSelected(true);
		
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
		MacroSB.append("//run(\"Fill Holes\");");
		MacroSB.append(System.getProperty("line.separator"));
		String defaultMacroString = MacroSB.toString();
		
		Wizard.globalMap.put(Keys.wizardVar_Filters, defaultMacroString);
		
		this.setTitle(Constants.APP_TITLE);
		this.setSize(new Dimension(700, 730));
		this.setBackground(Constants.BACKGROUND_COLOR);
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		headerPanel.setPreferredSize(new Dimension(700, 20));
		headerPanel.setBackground(Constants.BACKGROUND_COLOR);
		Label headerLabel = new Label(Texts.BINARY_ELEMENT);
		headerLabel.setForeground(Constants.TEXT_COLOR);
		headerLabel.setFont(Constants.SMALL_FONT_BOLD);
		headerPanel.add(headerLabel);
		
		JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 3));
		colorPanel.setPreferredSize(new Dimension(700, 70));
		colorPanel.setBackground(Constants.BACKGROUND_COLOR);
		colorPanel.add(makeLabel(Texts.COLOR_LABEL, 95)); 
		colorPanel.add(makeColorRadioButtonPanel());
		colorPanel.add(makeLabel("(Current binary element color priority: " + Wizard.color_priority_forWizard.toString().replaceAll("Grays", "White") + ")", 600));

		JPanel channelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		channelPanel.setPreferredSize(new Dimension(700, 210));
		channelPanel.setBackground(Constants.BACKGROUND_COLOR);
		channelPanel.add(makeLabel(Texts.CHANNEL_LABEL, 700));
		channelPanel.add(makeChannelCheckButtonsPanel());

		JPanel outlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));		
		outlinePanel.setPreferredSize(new Dimension(700, 50));
		outlinePanel.setBackground(Constants.BACKGROUND_COLOR);
		outlinePanel.add(makeLabel(Texts.OUTLINE_LABEL, 220));
		outlinePanel.add(makeOutlineCheckButtonsPanel());

		JPanel insertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		insertPanel.setPreferredSize(new Dimension(700, 50));
		insertPanel.setBackground(Constants.BACKGROUND_COLOR);
		insertPanel.add(makeLabel(Texts.INSERT_LABEL, 130));
		insertPanel.add(makeInsertDropDownPanel());
		
		JPanel imageJMacro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		imageJMacro.setPreferredSize(new Dimension(700, 115));
		imageJMacro.setBackground(Constants.BACKGROUND_COLOR);
		JLabel label = new JLabel(Texts.IMAGE_J_MACRO_TEXT_BINARY);
		label.setPreferredSize(new Dimension(700, 50));
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
		
		JPanel removePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		removePanel.setPreferredSize(new Dimension(700, 120));
		removePanel.setBackground(Constants.BACKGROUND_COLOR);
		removePanel.add(makeLabel(Texts.REMOVE_LABEL, 700));
		removePanel.add(makeRemovePanel());		
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBackground(Constants.BACKGROUND_COLOR);
		buttonsPanel.setPreferredSize(new Dimension(700, 50));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 10));
		reuseButton.setBackground(Constants.BACKGROUND_COLOR);
		reuseButton.setToolTipText(Texts.REUSEBUTTON_HOVER);
		reuseButton.setSelected(true);
		buttonsPanel.add(reuseButton);
		buttonsPanel.add(previewButton);
		advancedOptionsButton = new JButton(Texts.ADVANCED_OPTIONS_BUTTON);
		advancedOptionsButton.addActionListener(advancedOptionsListener);
		buttonsPanel.add(advancedOptionsButton);
		backButton = new JButton(Texts.BACK_BUTTON);
		backButton.addActionListener(backListener);
		buttonsPanel.add(backButton);
		buttonsPanel.add(finishButton);
		buttonsPanel.add(cancelButton);
		finishButton.addActionListener(finishButtonListener);
		cancelButton.addActionListener(cancelButtonListener);	

		this.add(headerPanel);
		this.add(colorPanel);
		this.add(getSeparator());
		this.add(channelPanel);
		this.add(getSeparator());
		this.add(outlinePanel);
		this.add(getSeparator());
		this.add(insertPanel);
		this.add(getSeparator());
		this.add(imageJMacro);
		this.add(getSeparator());
		this.add(removePanel);
		this.add(getSeparator());
		this.add(buttonsPanel);
		
	}

	private JPanel getSeparator() {
		JPanel separatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
		separatorPanel.setPreferredSize(new Dimension(700, 3));
		separatorPanel.setBackground(Constants.BACKGROUND_COLOR);
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		separator.setPreferredSize(new Dimension(650, 2));
		separator.setBackground(Constants.SEPARATOR_COLOR);
		separatorPanel.add(separator);
		return separatorPanel;
	}
	
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
	
	private JRadioButton getRadioButton(String label) {
		return new JRadioButton(label);
	}

	private JLabel makeLabel(String labelText, int width) {
		JLabel label = new JLabel(labelText);
		label.setPreferredSize(new Dimension(width, 25));
		label.setForeground(Constants.TEXT_COLOR);
		label.setFont(Constants.SMALL_FONT);
		return label;
	}
	
	private JPanel makeColorRadioButtonPanel() {
		JPanel colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
		colorsPanel.setPreferredSize(new Dimension(400, 40));
		JPanel radioButtonColorPanel = new JPanel();
		radioButtonColorPanel.setBackground(Constants.BACKGROUND_COLOR);
		radioButtonColorPanel.setLayout(new BoxLayout(radioButtonColorPanel, BoxLayout.X_AXIS));
		JRadioButton redButton = getRadioButton(Texts.COLOR_RED);
		redButton.setBackground(Constants.BACKGROUND_COLOR);
		JRadioButton greenButton = getRadioButton(Texts.COLOR_GREEN);
		greenButton.setBackground(Constants.BACKGROUND_COLOR);
		JRadioButton blueButton = getRadioButton(Texts.COLOR_BLUE);
		blueButton.setBackground(Constants.BACKGROUND_COLOR);
		JRadioButton yellowButton = getRadioButton(Texts.COLOR_YELLOW);
		yellowButton.setBackground(Constants.BACKGROUND_COLOR);
		JRadioButton cyanButton = getRadioButton(Texts.COLOR_CYAN);
		cyanButton.setBackground(Constants.BACKGROUND_COLOR);
		JRadioButton magentaButton = getRadioButton(Texts.COLOR_MAGENTA);
		magentaButton.setBackground(Constants.BACKGROUND_COLOR);
		JRadioButton whiteButton = getRadioButton(Texts.COLOR_WHITE);
		whiteButton.setBackground(Constants.BACKGROUND_COLOR);
		//radioButtonColorPanel.setToolTipText(Texts.COLOR_CHOICE_TOOLTIP);
		
		whiteButton.setToolTipText("Current color-priority-list: " + Wizard.color_priority_forWizard.toString());
		yellowButton.setToolTipText("Current color-priority-list: " + Wizard.color_priority_forWizard.toString());
		cyanButton.setToolTipText("Current color-priority-list: " + Wizard.color_priority_forWizard.toString());
		greenButton.setToolTipText("Current color-priority-list: " + Wizard.color_priority_forWizard.toString());
		magentaButton.setToolTipText("Current color-priority-list: " + Wizard.color_priority_forWizard.toString());
		redButton.setToolTipText("Current color-priority-list: " + Wizard.color_priority_forWizard.toString());
		blueButton.setToolTipText("Current color-priority-list: " + Wizard.color_priority_forWizard.toString());
		
		radioButtonColorPanel.add(whiteButton);
		radioButtonColorPanel.add(yellowButton);
		radioButtonColorPanel.add(cyanButton);
		radioButtonColorPanel.add(magentaButton);
		radioButtonColorPanel.add(greenButton);
		radioButtonColorPanel.add(redButton);
		radioButtonColorPanel.add(blueButton);
		radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(redButton);
		radioButtonGroup.add(greenButton);
		radioButtonGroup.add(blueButton);
		radioButtonGroup.add(yellowButton);
		radioButtonGroup.add(cyanButton);
		radioButtonGroup.add(magentaButton);
		radioButtonGroup.add(whiteButton);
		colorsPanel.add(radioButtonColorPanel);
		colorsPanel.setBackground(Constants.BACKGROUND_COLOR);
		return colorsPanel;
	}

	private JPanel makeChannelCheckButtonsPanel() {
		JPanel channelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		channelPanel.setPreferredSize(new Dimension(700, 250));

		int tic = Wizard.nInputImpChannels;
		checkBoxes = new JCheckBox[tic];
		channelDropDowns = new JComboBox[tic];
		int index = 0;
		String[] fillDropDown = getComboBoxModel();

		for (int i = 1; i <= tic / 7 + 1; i++) {
			JPanel checkBoxesPanel = new JPanel();
			checkBoxesPanel.setLayout(new BoxLayout(checkBoxesPanel, BoxLayout.Y_AXIS));
			for (int j = 0; j < 7; j++) {
				if (index < tic) {
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
					panel.setBackground(Constants.BACKGROUND_COLOR);
					final int indexFinal = index;
					checkBoxes[index] = new JCheckBox(Texts.CHANNEL + (index + 1));
					checkBoxes[index].setBackground(Constants.BACKGROUND_COLOR);
					checkBoxes[index].addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							channelDropDowns[indexFinal].setEnabled(checkBoxes[indexFinal].isSelected());
						}
					});
					channelDropDowns[index] = new JComboBox<String>(fillDropDown);
					channelDropDowns[index].setPreferredSize(new Dimension(100, 20));
					channelDropDowns[index].setEnabled(false);
					panel.add(checkBoxes[index]);
					panel.add(Box.createRigidArea(new Dimension(10, 0)));
					panel.add(channelDropDowns[index]);
					checkBoxesPanel.add(panel);
					index++;
				}
			}
			channelPanel.add(checkBoxesPanel);
		}

		channelPanel.setBackground(Constants.BACKGROUND_COLOR);
		return channelPanel;
	}

	private JPanel makeOutlineCheckButtonsPanel() {
		JPanel outlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
		outlinePanel.setPreferredSize(new Dimension(250, 40));
		JRadioButton offButton = new JRadioButton(Texts.OFF_LABEL);
		offButton.setSelected(true);
		JRadioButton thickOutlinesButton = new JRadioButton(Texts.THICK_OUTLINE_LABEL);
		JRadioButton thinOutlinesButton = new JRadioButton(Texts.THIN_OUTLINE_LABEL);
		outlineRadioButtonGroup = new ButtonGroup();
		outlineRadioButtonGroup.add(offButton);
		outlineRadioButtonGroup.add(thickOutlinesButton);
		outlineRadioButtonGroup.add(thinOutlinesButton);
		offButton.setBackground(Constants.BACKGROUND_COLOR);
		thickOutlinesButton.setBackground(Constants.BACKGROUND_COLOR);
		thinOutlinesButton.setBackground(Constants.BACKGROUND_COLOR);
		outlinePanel.add(offButton);
		outlinePanel.add(thickOutlinesButton);
		outlinePanel.add(thinOutlinesButton);
		outlinePanel.setBackground(Constants.BACKGROUND_COLOR);
		return outlinePanel;
	}

	private JPanel makeInsertDropDownPanel() {
		JPanel insertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
		insertPanel.setPreferredSize(new Dimension(150, 50));
		int moc = Wizard.nOutputImpChannels;
		String[] items = new String[moc + 1];
		for (int i = 0; i < moc; i++) {
			items[i] = String.valueOf(i + 1);
		}
		items[moc] = String.valueOf(moc + 1) + Texts.CREATES_NEW;
		dropDown = new JComboBox<>(items);
		dropDown.setPreferredSize(new Dimension(100, 25));
		insertPanel.add(dropDown);
		insertPanel.setBackground(Constants.BACKGROUND_COLOR);
		return insertPanel;
	}

	private JPanel makeRemovePanel() {
		JPanel removePanel = new JPanel();
		removePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 10));
		removePanel.setBackground(Constants.BACKGROUND_COLOR);
		JPanel scaleUnitPanel = new JPanel();
		scaleUnitPanel.setBackground(Constants.BACKGROUND_COLOR);
		scaleTextField = new JTextField();
		scaleTextField.addKeyListener(new DoubleInputTextFieldListener(scaleTextField));
		scaleTextField.setColumns(8);
		JPanel unitPanel = new JPanel();
		unitPanel.setLayout(new BoxLayout(unitPanel, BoxLayout.Y_AXIS));
		JRadioButton pixelRadioButton = new JRadioButton(Texts.PIXELS);
		pixelRadioButton.setBackground(Constants.BACKGROUND_COLOR);
		unitPanel.add(pixelRadioButton);
		JRadioButton variableRadioButton = new JRadioButton(Wizard.scale_unit);
		variableRadioButton.setBackground(Constants.BACKGROUND_COLOR);
		unitPanel.add(variableRadioButton);
		unitPanel.setBackground(Constants.BACKGROUND_COLOR);
		scaleUnitPanel.add(scaleTextField);
		scaleUnitPanel.add(unitPanel);
		bg = new ButtonGroup();
		bg.add(pixelRadioButton);
		bg.add(variableRadioButton);
		removePanel.add(scaleUnitPanel);
		removeEnableInStackCheckBox = new JCheckBox(Texts.ENABLE_INSTACK);
		removeEnableInStackCheckBox.setBackground(Constants.BACKGROUND_COLOR);
		removeZProjectionCheckBox = new JCheckBox(Texts.Z_PROJECTION);
		removeZProjectionCheckBox.setBackground(Constants.BACKGROUND_COLOR);
		removePanel.add(removeEnableInStackCheckBox);
		removePanel.add(removeZProjectionCheckBox);
		return removePanel;
	}
	
	//Validate and fill in wizard.globalMap values
	public static boolean validateForm() {
		String selectedColor = null;
		for (Enumeration<AbstractButton> buttons = radioButtonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				selectedColor = button.getText();
			}
		}
		if (selectedColor == null) {
			showErrorPopup(Texts.COLOR_NOT_SELECTED);
			return false;
		} else {
			if (selectedColor.equals("White")) {
				selectedColor = "Grays";
			}
			Wizard.globalMap.put(Keys.wizardVar_Color, selectedColor);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < checkBoxes.length; i++) {
			if (checkBoxes[i].isSelected()) {
				sb.append(i + 1);
				sb.append(",");
			}
		}
		if (sb.toString().equals("")) {
			showErrorPopup(Texts.CHANNEL_NOT_SELECTED);
			return false;
		} else {
			Wizard.globalMap.put(Keys.wizardVar_AND_Channels, sb.toString());
		}

		String selectedOutline = null;
		for (Enumeration<AbstractButton> buttons = outlineRadioButtonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				if (button.getText().equals(Texts.OFF_LABEL)) {
					selectedOutline = Values.OUTLINE_NOT_SELECTED;
				} else if (button.getText().equals(Texts.THICK_OUTLINE_LABEL)) {
					selectedOutline = Values.OUTLINE_THICK_SELECTED;
				} else {
					selectedOutline = Values.OUTLINE_THIN_SELECTED;
				}
			}
		}
		Wizard.globalMap.put(Keys.wizardVar_Outline, selectedOutline);
		if (dropDown.getSelectedIndex() != -1) {
			String value = String.valueOf(dropDown.getSelectedIndex() + 1);
			if (value.contains(Texts.CREATES_NEW)) {
				value = value.substring(0, value.indexOf(" "));
			}
			Wizard.globalMap.put(Keys.wizardVar_Output_Channel, value);
		}
		StringBuilder thmsb = new StringBuilder();
		for (int i = 0; i < channelDropDowns.length; i++) {
			String value = Values.MANUAL;
			if (checkBoxes[i].isSelected()) {
				value = channelDropDowns[i].getSelectedItem().toString();
			}
			thmsb.append(value);
			thmsb.append(",");
		}
		Wizard.globalMap.put(Keys.wizardVar_Threshold_methods, thmsb.substring(0, thmsb.length() - 1));
		
		//Macro filters
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
		if (Wizard.globalMap.get(Keys.wizardVar_Filters) == null) {
			Wizard.globalMap.put(Keys.wizardVar_Filters, "");
		}
		
		//Remove small particles:
		String selectedUnit = null;
		for (Enumeration<AbstractButton> buttons = bg.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				selectedUnit = button.getText();
			}
		}
		if (selectedUnit == null) {
			Wizard.globalMap.put(Keys.wizardVar_unit, Values.PIXEL);
		} else {
			Wizard.globalMap.put(Keys.wizardVar_unit, selectedUnit);
		}
		if (scaleTextField.getText() == null) {
			Wizard.globalMap.put(Keys.wizardVar_rmv_smaller_than, "");
		} else {
			Wizard.globalMap.put(Keys.wizardVar_rmv_smaller_than, scaleTextField.getText());
		}
		if (removeEnableInStackCheckBox.isSelected()) {
			Wizard.globalMap.put(Keys.wizardVar_rmv_nonstack, "1");
			Wizard.globalMap.put(Keys.wizardVar_rmv_stack, "1");
		} else {
			Wizard.globalMap.put(Keys.wizardVar_rmv_nonstack, "0");
			Wizard.globalMap.put(Keys.wizardVar_rmv_stack, "0");
		}
		if (removeZProjectionCheckBox.isSelected()) {
			Wizard.globalMap.put(Keys.wizardVar_rmv_zproj, "1");
		} else {
			Wizard.globalMap.put(Keys.wizardVar_rmv_zproj, "0");
		}
		
		//Set all wizard values from advanced options to 0 or default:
		Wizard.globalMap.put(Keys.wizardVar_NOT_Channels, "");
		Wizard.globalMap.put(Keys.wizardVar_3dRmvEnable, "0");	
		Wizard.globalMap.put(Keys.wizardVar_3dRmvExclude, "0");	
		Wizard.globalMap.put(Keys.wizardVar_3dRmvMin, "0");
		Wizard.globalMap.put(Keys.wizardVar_3dRmvMax, "0");	
		Wizard.globalMap.put(Keys.wizardVar_3dRmvWhen, "0");	

		return true;
	}

	private boolean shortValidateForm() {
		String selectedColor = null;
		for (Enumeration<AbstractButton> buttons = radioButtonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				selectedColor = button.getText();
			}
		}
		if (selectedColor == null) {
			return false;
		}
		for (int i = 0; i < checkBoxes.length; i++) {
			if (checkBoxes[i].isSelected()) {
				return true;
			}
		}
		return false;
	}

	private String[] getComboBoxModel() {
		String[] methods = ij.process.AutoThresholder.getMethods();
		String[] result = new String[methods.length + 1];
		result[0] = Values.MANUAL;
		System.arraycopy(methods, 0, result, 1, methods.length);
		return result;
	}

	private static void showErrorPopup(String error) {
		JOptionPane.showMessageDialog(new JFrame(), error, Texts.ALERT, JOptionPane.ERROR_MESSAGE);
	}

	
	private ActionListener advancedOptionsListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (validateForm()) {
				BinaryElement.this.setVisible(false);
				PageCaller.showPage(PageEnum.ADVANCED_OPTIONS_BINARY);
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
			Wizard.globalMap.remove(Keys.wizardVar_Threshold_methods);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvEnable);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvExclude);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvMin);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvMax);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvWhen);
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
			Wizard.globalMap.remove(Keys.wizardVar_Filters);
			Wizard.globalMap.remove(Keys.wizardVar_Filters_nonstack);
			Wizard.globalMap.remove(Keys.wizardVar_Filters_stack);
			Wizard.globalMap.remove(Keys.wizardVar_Filters_zproj);
			Wizard.globalMap.remove(Keys.wizardVar_unit);
			Wizard.globalMap.remove(Keys.wizardVar_rmv_smaller_than);
			Wizard.globalMap.remove(Keys.wizardVar_rmv_nonstack);
			Wizard.globalMap.remove(Keys.wizardVar_rmv_stack);
			Wizard.globalMap.remove(Keys.wizardVar_rmv_zproj);
			BinaryElement.this.setVisible(false);
			PageCaller.showPage(PageEnum.FIRST_PAGE);
		}
	};

	private ActionListener cancelButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			BinaryElement.this.setVisible(false);
		}
	};

	private ActionListener finishButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (shortValidateForm()) {
				BinaryElement.this.setVisible(false);
			}
		}
	};
}