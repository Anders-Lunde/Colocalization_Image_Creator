package wizard.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import ij.IJ;
import wizard.app.Wizard;
import wizard.enums.PageEnum;
import wizard.listeners.DoubleInputTextFieldListener;
import wizard.util.Constants;
import wizard.util.Keys;
import wizard.util.Texts;

public class AdvancedOptionsBinary extends JDialog {
	
	//NOT channels:
	private static JCheckBox[] checkBoxes;
	private static JComboBox<String>[] channelDropDowns;
	//3D filter:
	private static JCheckBox excludeEdges; 
	private static JCheckBox objects3dEnable;
	private static JTextField objects3dMinimum;
	private static JTextField objects3dMaximum;
	private static JComboBox objects3dOrderCombobox;
	
	private JButton backButton;
	public static JCheckBox reuseButton = new JCheckBox(Texts.REUSE_VALUES);
	public static JButton previewButton = new JButton(Texts.PREVIEW_BUTTON);
	public static JButton finishButton = new JButton(Texts.FINISH_BUTTON);
	public static JButton cancelButton = new JButton(Texts.CANCEL_BUTTON);

	public AdvancedOptionsBinary() {

		this.setTitle(Constants.APP_TITLE);
		this.setSize(new Dimension(890, 605));
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.setLocationRelativeTo(null);
		this.setResizable(true);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setBackground(Constants.BACKGROUND_COLOR);

		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 10));
		headerPanel.setPreferredSize(new Dimension(890, 50));
		headerPanel.setBackground(Constants.BACKGROUND_COLOR);
		Label headerLabel = new Label(Texts.ADVANCED_OPTIONS_HEADER);
		headerLabel.setForeground(Constants.TEXT_COLOR);
		headerLabel.setFont(Constants.SMALL_FONT_BOLD);
		headerPanel.add(headerLabel);


		/*
		 * Objects 3D panel. Not created from methods due to rushed deadline.
		 */
		
		JPanel objects3dPanel = new JPanel(new GridLayout(0,1, 5, 5));
		objects3dPanel.setPreferredSize(new Dimension(875, 250));
		objects3dPanel.setBackground(Constants.BACKGROUND_COLOR);
		objects3dPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		excludeEdges = new JCheckBox("Exclude 3d objects touching edges (X, Y, or Z)"); //Texts.
		excludeEdges.setFont(Constants.SMALL_FONT);
		excludeEdges.setBackground(Constants.BACKGROUND_COLOR);
		
		objects3dEnable = new JCheckBox("Enable 3D filter"); //Texts.
		objects3dEnable.setFont(Constants.SMALL_FONT_BOLD);
		objects3dEnable.setBackground(Constants.BACKGROUND_COLOR);

		objects3dMinimum = new JTextField();
		objects3dMinimum.addKeyListener(new DoubleInputTextFieldListener(objects3dMinimum));
		objects3dMinimum.setColumns(8);
		objects3dMinimum.setText("0");
		
		objects3dMaximum = new JTextField();
		objects3dMaximum.addKeyListener(new DoubleInputTextFieldListener(objects3dMaximum));
		objects3dMaximum.setColumns(8);
		objects3dMaximum.setText("2147483647");
		
		Label objects3d_header = new Label("Filter away 3D object (hard-coded 3D Objects Counter plugin method).");
		objects3d_header.setFont(Constants.SMALL_FONT_BOLD);
		Label objects3dMinimum_label = new Label("Remove 3d objects smaller than (pixels/voxels):");
		objects3dMinimum_label.setFont(Constants.SMALL_FONT);
		Label objects3dMaximum_label = new Label("Remove 3d objects bigger than (max possible size = 2147483647 pixels/voxels):");
		objects3dMaximum_label.setFont(Constants.SMALL_FONT);
		/*
		Label objects3d_conditionalLabel = new Label();
		objects3d_conditionalLabel.setFont(Constants.SMALL_FONT_BOLD);
		
		if (!ij.Menus.getCommands().toString().toLowerCase().contains("3d objects counter")) {
			objects3dEnable.setEnabled(false);
			objects3dEnable.setText("Enable 3D filter - ERROR: requires '3D Objects Counter plugin'");
			objects3d_conditionalLabel.setText("ERROR! The '3D Objects Counter' plugin was not found. This function requires this plugin. Please install the plugin, or use the FIJI version of ImageJ.");
		} else {
			objects3d_conditionalLabel.setText("The '3D Objects Counter' plugin is installed. This function can be used.");
		}
		*/
		
		Label objects3dComboboxLabel = new Label("Choose when to perform 3d objects removal:");
		objects3dComboboxLabel.setFont(Constants.SMALL_FONT);
		
		String[] objects3dOrder_strings = { "Before macros and small particle removal", "After macros, before small particle removal", "After both"};
		objects3dOrderCombobox = new JComboBox(objects3dOrder_strings);
		objects3dOrderCombobox.setSelectedIndex(1);

		objects3dPanel.add(objects3d_header);
		//objects3dPanel.add(objects3d_conditionalLabel);
		objects3dPanel.add(objects3dMinimum_label);
		objects3dPanel.add(objects3dMinimum);
		objects3dPanel.add(objects3dMaximum_label);
		objects3dPanel.add(objects3dMaximum);
		objects3dPanel.add(excludeEdges);
		objects3dPanel.add(objects3dComboboxLabel);
		objects3dPanel.add(objects3dOrderCombobox);
		objects3dPanel.add(objects3dEnable);
		
		

		
		JPanel overlapPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		overlapPanel.setPreferredSize(new Dimension(890, 200));
		overlapPanel.setBackground(Constants.BACKGROUND_COLOR);
		overlapPanel.add(makeLabel(Texts.OVERLAP_LABEL));
		overlapPanel.add(makeOverlapCheckButtonsPanel());

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBackground(Constants.BACKGROUND_COLOR);
		buttonsPanel.setPreferredSize(new Dimension(890, 50));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 10));
		reuseButton.setBackground(Constants.BACKGROUND_COLOR);
		reuseButton.setToolTipText(Texts.REUSEBUTTON_HOVER);
		reuseButton.setSelected(true);
		buttonsPanel.add(reuseButton);
		buttonsPanel.add(previewButton);
		backButton = new JButton(Texts.BACK_BUTTON);
		backButton.addActionListener(backListener);
		buttonsPanel.add(backButton);
		buttonsPanel.add(finishButton);
		buttonsPanel.add(cancelButton);
		finishButton.addActionListener(closeWindowListener);
		cancelButton.addActionListener(closeWindowListener);

		this.add(headerPanel);
		this.add(getSeparator());
		this.add(objects3dPanel);
		this.add(getSeparator());
		this.add(overlapPanel);
		this.add(getSeparator());
		this.add(buttonsPanel);

	}

	private JPanel getSeparator() {
		JPanel separatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
		separatorPanel.setPreferredSize(new Dimension(870, 3));
		separatorPanel.setBackground(Constants.BACKGROUND_COLOR);
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		separator.setPreferredSize(new Dimension(870, 2));
		separator.setBackground(Constants.SEPARATOR_COLOR);
		separatorPanel.add(separator);
		return separatorPanel;
	}

	private JLabel makeLabel(String labelText) {
		JLabel label = new JLabel(labelText);
		label.setPreferredSize(new Dimension(890, 25));
		label.setForeground(Constants.TEXT_COLOR);
		label.setFont(Constants.SMALL_FONT);
		return label;
	}

	private JPanel makeOverlapCheckButtonsPanel() {
		JPanel channelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		channelPanel.setPreferredSize(new Dimension(890, 215));

		String chosenAsString = Wizard.globalMap.get(Keys.wizardVar_AND_Channels);
		List<Integer> chosenAsIntegers = new ArrayList<>();
		if (chosenAsString != null) {
			String[] chosen = chosenAsString.split(",");
			for (String string : chosen) {
				chosenAsIntegers.add(Integer.parseInt(string));
			}
		}
		int tic = Wizard.nInputImpChannels;
		checkBoxes = new JCheckBox[tic - chosenAsIntegers.size()];
		channelDropDowns = new JComboBox[tic - chosenAsIntegers.size()];
		String[] fillDropDown = getComboBoxModel();
		int k = 0;
		for (int i = 1; i <= tic / 5 + 1; i++) {
			JPanel checkBoxesPanel = new JPanel();
			checkBoxesPanel.setLayout(new BoxLayout(checkBoxesPanel, BoxLayout.Y_AXIS));
			for (int j = 0; j < 5; j++) {
				int index = 5 * (i - 1) + j;
				if (index < tic && !chosenAsIntegers.contains(index + 1)) {

					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
					panel.setBackground(Constants.BACKGROUND_COLOR);

					final int kFinal = k;
					checkBoxes[k] = new JCheckBox(Texts.CHANNEL + (index + 1));
					checkBoxes[k].setBackground(Constants.BACKGROUND_COLOR);
					checkBoxes[k].addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							channelDropDowns[kFinal].setEnabled(checkBoxes[kFinal].isSelected());
						}
					});
					channelDropDowns[k] = new JComboBox<String>(fillDropDown);
					channelDropDowns[k].setPreferredSize(new Dimension(100, 20));
					channelDropDowns[k].setEnabled(false);
					panel.add(checkBoxes[k]);
					panel.add(Box.createRigidArea(new Dimension(10, 0)));
					panel.add(channelDropDowns[k]);
					checkBoxesPanel.add(panel);
					k++;
				}
			}
			channelPanel.add(checkBoxesPanel);
		}

		channelPanel.setBackground(Constants.BACKGROUND_COLOR);
		return channelPanel;
	}

	private String[] getComboBoxModel() {
		String[] methods = ij.process.AutoThresholder.getMethods();
		String[] result = new String[methods.length + 1];
		result[0] = "Manual";
		System.arraycopy(methods, 0, result, 1, methods.length);
		return result;
	}

	public ActionListener backListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Wizard.globalMap.remove(Keys.wizardVar_NOT_Channels);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvEnable);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvExclude);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvMin);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvMax);
			Wizard.globalMap.remove(Keys.wizardVar_3dRmvWhen);
			
			AdvancedOptionsBinary.this.setVisible(false);
			PageCaller.showPage(PageEnum.BINARY_ELEMENT);
		}
	};

	private ActionListener closeWindowListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			AdvancedOptionsBinary.this.setVisible(false);
		}
	};

	public static void fillChosenOptions() {
		//3D filter options	
		if (objects3dEnable.isSelected()) {
			Wizard.globalMap.put(Keys.wizardVar_3dRmvEnable, "1");	
		} else {
			Wizard.globalMap.put(Keys.wizardVar_3dRmvEnable, "0");	
		}
		if (excludeEdges.isSelected()) {
			Wizard.globalMap.put(Keys.wizardVar_3dRmvExclude, "1");	
		} else {
			Wizard.globalMap.put(Keys.wizardVar_3dRmvExclude, "0");	
		}
		Wizard.globalMap.put(Keys.wizardVar_3dRmvMin, objects3dMinimum.getText());
		Wizard.globalMap.put(Keys.wizardVar_3dRmvMax, objects3dMaximum.getText());	
		Wizard.globalMap.put(Keys.wizardVar_3dRmvWhen, Integer.toString(objects3dOrderCombobox.getSelectedIndex()));	

		
		
		//NOT channels options:
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < checkBoxes.length; i++) {
			if (checkBoxes[i].isSelected()) {
				String value = checkBoxes[i].getText();
				value = value.substring(value.lastIndexOf(" ") + 1);
				sb.append(value);
				sb.append(",");
			}
		}
		Wizard.globalMap.put(Keys.wizardVar_NOT_Channels, sb.toString());

		String thresholdMethods = Wizard.globalMap.get(Keys.wizardVar_Threshold_methods);
		String[] thresholdMethodsArray = thresholdMethods.split(",");
		for(int i=0; i<channelDropDowns.length; i++) {
			if(checkBoxes[i].isSelected()) {
				String indexSt = checkBoxes[i].getText();
				indexSt = indexSt.substring(indexSt.lastIndexOf("l")+2);
				int index = Integer.parseInt(indexSt);
				String value = channelDropDowns[i].getSelectedItem().toString();
				thresholdMethodsArray[index-1] = value;
			}
		}
		StringBuilder thmsb = new StringBuilder();
		for (String string : thresholdMethodsArray) {
			thmsb.append(string);
			thmsb.append(",");
		}
		Wizard.globalMap.put(Keys.wizardVar_Threshold_methods, thmsb.substring(0, thmsb.length() - 1));		
	}

}
