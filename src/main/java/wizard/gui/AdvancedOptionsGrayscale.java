package wizard.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;

import wizard.app.Wizard;
import wizard.enums.PageEnum;
import wizard.util.Constants;
import wizard.util.Keys;
import wizard.util.Texts;

public class AdvancedOptionsGrayscale extends JDialog {
	
	/*
	 * This is disabled. Used to only be imagej macro filters here, 
	 * but they were moved to basic menu (GrascaleElement.java).
	 * Keeping code in case of extending functionality later here.
	 */

	private JButton backButton;
	public static JButton previewButton = new JButton(Texts.PREVIEW_BUTTON);
	public static JButton finishButton = new JButton(Texts.FINISH_BUTTON);
	public static JButton cancelButton = new JButton(Texts.CANCEL_BUTTON);
	private static JCheckBox filterEnableInStackCheckBox;
	private static JCheckBox filterEnableZProjection;

	public AdvancedOptionsGrayscale() {

		this.setTitle(Constants.APP_TITLE);
		this.setSize(new Dimension(800, 360));
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 10));
		headerPanel.setPreferredSize(new Dimension(800, 30));
		headerPanel.setBackground(Constants.BACKGROUND_COLOR);
		Label headerLabel = new Label(Texts.ADVANCED_OPTIONS_GRAYSCALE_HEADER);
		headerLabel.setForeground(Constants.TEXT_COLOR);
		headerLabel.setFont(Constants.SMALL_FONT_BOLD);
		headerPanel.add(headerLabel);

		JPanel imageJMacro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		imageJMacro.setPreferredSize(new Dimension(800, 250));
		imageJMacro.setBackground(Constants.BACKGROUND_COLOR);
		JLabel label = new JLabel(Texts.IMAGE_J_MACRO_TEXT_GRAYSCALE);
		label.setPreferredSize(new Dimension(800, 100));
		label.setForeground(Constants.TEXT_COLOR);
		label.setFont(Constants.SMALL_FONT);
		imageJMacro.add(label);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setBackground(Constants.BACKGROUND_COLOR);
		JButton addMacroButton = new JButton(Texts.ADD_MACRO_COMMANDS);
		addMacroButton.addActionListener(addMacroButtonListener);
		buttonPanel.add(addMacroButton);
		buttonPanel.setPreferredSize(new Dimension(800, 45));
		imageJMacro.add(buttonPanel);
		imageJMacro.add(makeMacroCheckBoxes());

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBackground(Constants.BACKGROUND_COLOR);
		buttonsPanel.setPreferredSize(new Dimension(800, 50));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 10));
		backButton = new JButton(Texts.BACK_BUTTON);
		backButton.addActionListener(backListener);
		buttonsPanel.add(previewButton);
		buttonsPanel.add(backButton);
		buttonsPanel.add(finishButton);
		buttonsPanel.add(cancelButton);
		finishButton.addActionListener(closeWindowListener);
		cancelButton.addActionListener(closeWindowListener);

		this.add(headerPanel);
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

	private JPanel makeMacroCheckBoxes() {
		JPanel macroCheckBoxesPanel = new JPanel();
		macroCheckBoxesPanel.setBackground(Constants.BACKGROUND_COLOR);
		macroCheckBoxesPanel.setLayout(new BoxLayout(macroCheckBoxesPanel, BoxLayout.Y_AXIS));
		filterEnableInStackCheckBox = new JCheckBox(Texts.ENABLE_INSTACK);
		filterEnableInStackCheckBox.setBackground(Constants.BACKGROUND_COLOR);
		filterEnableZProjection = new JCheckBox(Texts.Z_PROJECTION);
		filterEnableZProjection.setBackground(Constants.BACKGROUND_COLOR);
		macroCheckBoxesPanel.add(filterEnableInStackCheckBox);
		macroCheckBoxesPanel.add(filterEnableZProjection);
		return macroCheckBoxesPanel;
	}

	public ActionListener backListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Wizard.globalMap.remove(Keys.wizardVar_Red);
			Wizard.globalMap.remove(Keys.wizardVar_Green);
			Wizard.globalMap.remove(Keys.wizardVar_Blue);
			Wizard.globalMap.remove(Keys.wizardVar_Yellow);
			Wizard.globalMap.remove(Keys.wizardVar_Cyan);
			Wizard.globalMap.remove(Keys.wizardVar_Magenta);
			Wizard.globalMap.remove(Keys.wizardVar_Grays);
			AdvancedOptionsGrayscale.this.setVisible(false);
			PageCaller.showPage(PageEnum.GRAYSCALE_ELEMENT);
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
	
	private ActionListener closeWindowListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			AdvancedOptionsGrayscale.this.setVisible(false);
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
