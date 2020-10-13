package batchprocessing;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MinMaxSetDialog extends JDialog {

	public JButton saveButton = new JButton(BatchProcessingConstants.SAVE_BUTTON_LABEL);
	public JButton cancelButton = new JButton(BatchProcessingConstants.CANCEL_BUTTON_LABEL);
	public JSpinner minSpinner;
	public JSpinner maxSpinner;

	public MinMaxSetDialog(int min, int max) {

		this.setTitle(BatchProcessingConstants.MIN_MAX_DIALOG_TITLE);
		this.setLocationRelativeTo(null);
		this.setSize(new Dimension(300, 200));
		BoxLayout boxLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
		this.setLayout(boxLayout);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		SpinnerModel minSpinnerModel = new SpinnerNumberModel(min, Integer.MIN_VALUE, Integer.MAX_VALUE, 10);
		JPanel minPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
		JLabel minLabel = new JLabel(BatchProcessingConstants.MIN_LABEL);
		minSpinner = new JSpinner(minSpinnerModel);
		minSpinner.setPreferredSize(new Dimension(100, 25));
		minPanel.add(minLabel);
		minPanel.add(minSpinner);

		SpinnerModel maxSpinnerModel = new SpinnerNumberModel(max, Integer.MIN_VALUE, Integer.MAX_VALUE, 10);
		JPanel maxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JLabel maxLabel = new JLabel(BatchProcessingConstants.MAX_LABEL);
		maxSpinner = new JSpinner(maxSpinnerModel);
		maxSpinner.setPreferredSize(new Dimension(100, 25));
		maxPanel.add(maxLabel);
		maxPanel.add(maxSpinner);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		minSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if ((int) minSpinner.getValue() >= (int) maxSpinner.getValue()) {
					maxSpinner.setValue(minSpinner.getValue());
				}
			}
		});

		maxSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if ((int) maxSpinner.getValue() <= (int) minSpinner.getValue()) {
					minSpinner.setValue(maxSpinner.getValue());
				}
			}
		});

		this.add(minPanel);
		this.add(maxPanel);
		this.add(buttonPanel);

	}

}
