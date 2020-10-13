package batchprocessing;

import static batchprocessing.BatchProcessingConstants.ADJUST_VALUES_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.BINARY;
import static batchprocessing.BatchProcessingConstants.BUTTON_FONT;
import static batchprocessing.BatchProcessingConstants.CANCEL_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.COLUMN_TOOLTIP_LABEL;
import static batchprocessing.BatchProcessingConstants.FILENAME_TABLE_HEADER;
import static batchprocessing.BatchProcessingConstants.FILTER_BY_FILE_EXTENSION_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.GRAYSCALE;
import static batchprocessing.BatchProcessingConstants.INPUT_FOLDER_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.LOAD_ELEMENT_PROFILE_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.LOAD_VALUE_SET_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.MAIN_DIALOG_HEIGHT;
import static batchprocessing.BatchProcessingConstants.MAIN_DIALOG_WIDTH;
import static batchprocessing.BatchProcessingConstants.MANUAL_SET_VALUES_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.OUPUT_FOLDER_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.REMOVE_SELECTED_FILES_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.SAVE_VALUE_SET_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.SELECT_ALL_FILES_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.SELECT_NONE_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.START_BATCH_PROCESSING_BUTTON_LABEL;
import static batchprocessing.BatchProcessingConstants.TITLE;
import static batchprocessing.BatchProcessingConstants.USE_BIOFORMATS_CHECK_BOX_LABEL;
import static batchprocessing.BatchProcessingConstants.USE_BIOFORMATS_SAVE_CHECK_BOX_LABEL;
import static batchprocessing.BatchProcessingConstants.USE_CURRENT_ELEMENT_PROFILE_BUTTON_LABEL;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import batchprocessing.json.JSonChannel;
import batchprocessing.json.JSonTableObject;

public class BatchProcessingDialog extends JFrame {

	public JButton useCurrentElementProfileButton = new JButton(USE_CURRENT_ELEMENT_PROFILE_BUTTON_LABEL);
	public JButton loadElementProfileButton = new JButton(LOAD_ELEMENT_PROFILE_BUTTON_LABEL);
	public JButton inputFolderButton = new JButton(INPUT_FOLDER_BUTTON_LABEL);
	public JButton outputFolderButton = new JButton(OUPUT_FOLDER_BUTTON_LABEL);
	public JPanel labelPanel = new JPanel();

	public JButton removeSelectedFilesButton = new JButton(REMOVE_SELECTED_FILES_BUTTON_LABEL);
	public JButton filterByFileButton = new JButton(FILTER_BY_FILE_EXTENSION_BUTTON_LABEL);
	public JButton selectAllFilesButton = new JButton(SELECT_ALL_FILES_BUTTON_LABEL);
	public JButton selectNoneButton = new JButton(SELECT_NONE_BUTTON_LABEL);
	public JButton manualSetValuesButton = new JButton(MANUAL_SET_VALUES_BUTTON_LABEL);
	public JButton adjustValuesButton = new JButton(ADJUST_VALUES_BUTTON_LABEL);
	public JButton loadValueSetButton = new JButton(LOAD_VALUE_SET_BUTTON_LABEL);
	public JButton saveValueSetButton = new JButton(SAVE_VALUE_SET_BUTTON_LABEL);
	public JButton cancelButton = new JButton(CANCEL_BUTTON_LABEL);
	public JButton startBatchingProcessingButton = new JButton(START_BATCH_PROCESSING_BUTTON_LABEL);

	public JTextField inputFolderTextField = new JTextField();
	public JTextField outputFolderTextField = new JTextField();
	public JTextField filterTextField = new JTextField();
	public JCheckBox useBioformatsCheckbox = new JCheckBox(USE_BIOFORMATS_CHECK_BOX_LABEL);
	public JCheckBox useBioformatsSaveCheckbox = new JCheckBox(USE_BIOFORMATS_SAVE_CHECK_BOX_LABEL);
	public JScrollPane tableScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	public ChannelTable table;
	public TableRowSorter<? extends TableModel> rowSorter = null;

	public BatchProcessingDialog() {
		this.setTitle(TITLE);
		this.setSize(new Dimension(MAIN_DIALOG_WIDTH, MAIN_DIALOG_HEIGHT));
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		this.add(createHeader());
		this.add(createBody());
		this.add(createFooter());
	}

	public void loadTableData(List<String> fileNames, List<Integer> thresholdChannels, List<int[]> thresholdValues,
			List<Integer> contrastChannels, List<int[]> contrastValues) {

		List<Channel> channels = new ArrayList<>();
		for (int i = 0; i < thresholdChannels.size(); i++) {
			channels.add(new Channel(BINARY, thresholdChannels.get(i).intValue(), thresholdValues.get(i)[0],
					thresholdValues.get(i)[1]));
		}

		for (int i = 0; i < contrastChannels.size(); i++) {
			channels.add(new Channel(GRAYSCALE, contrastChannels.get(i).intValue(), contrastValues.get(i)[0],
					contrastValues.get(i)[1]));
		}

		DefaultTableModel tableModel = new DefaultTableModel(getTableData(fileNames, channels),
				getColumnNames(channels));
		table = new ChannelTable(tableModel);
		setTableOptions();
	}

	public void loadTableData(List<JSonTableObject> data) {

		Vector<Vector<Object>> tableData = new Vector<>();
		for (int i = 0; i < data.size(); i++) {
			Vector<Object> rowData = new Vector<>();
			rowData.addElement(data.get(i).getFilename());
			for (JSonChannel channel : data.get(i).getChannels()) {
				rowData.addElement(new CellItem(channel.getMin(), channel.getMax()));
			}
			tableData.addElement(rowData);
		}
		
		Vector<String> columnNames = new Vector<>();
		columnNames.addElement(FILENAME_TABLE_HEADER);
		for (JSonChannel ch : data.get(0).getChannels()) {
			columnNames.addElement(translateIn(ch.getChannelName()));
		}

		DefaultTableModel tableModel = new DefaultTableModel(tableData, columnNames);
		table = new ChannelTable(tableModel);
		setTableOptions();

	}

	public void filterByExtension(String text) {
		if (text.trim().length() == 0) {
			rowSorter.setRowFilter(null);
		} else {
			rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + "\\." + text));
		}
	}

	public void adjustValues(int row, int column, CellItem item) {
		table.setValueAt(item, row, column);
	}

	public String translateIn(String input) {
		return input.replaceAll("Threshold", "Binary").replaceAll("Contrast", "Grayscale");
	}

	public String translateOut(String input) {
		return input.replaceAll("Binary", "Threshold").replaceAll("Grayscale", "Contrast");
	}

	private JPanel createHeader() {
		JPanel contentPanel = new JPanel();
		BoxLayout box = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
		contentPanel.setLayout(box);
		JPanel header1Panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 5));
		JPanel header2Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		JPanel header3Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		header1Panel.add(useBioformatsCheckbox);
		header1Panel.add(Box.createHorizontalStrut(30));
		header1Panel.add(useBioformatsSaveCheckbox);
		useCurrentElementProfileButton.setPreferredSize(new Dimension(180, 25));
		useCurrentElementProfileButton.setFont(BUTTON_FONT);
		inputFolderButton.setPreferredSize(new Dimension(120, 25));
		inputFolderButton.setFont(BUTTON_FONT);
		inputFolderTextField.setPreferredSize(new Dimension(750, 25));
		header2Panel.add(useCurrentElementProfileButton);
		header2Panel.add(Box.createHorizontalStrut(35));
		header2Panel.add(inputFolderButton);
		header2Panel.add(Box.createHorizontalStrut(5));
		header2Panel.add(inputFolderTextField);
		loadElementProfileButton.setPreferredSize(new Dimension(180, 25));
		loadElementProfileButton.setFont(BUTTON_FONT);
		outputFolderButton.setPreferredSize(new Dimension(120, 25));
		outputFolderButton.setFont(BUTTON_FONT);
		outputFolderTextField.setPreferredSize(new Dimension(750, 25));
		header3Panel.add(loadElementProfileButton);
		header3Panel.add(Box.createHorizontalStrut(35));
		header3Panel.add(outputFolderButton);
		header3Panel.add(Box.createHorizontalStrut(5));
		header3Panel.add(outputFolderTextField);
		contentPanel.add(header1Panel);
		contentPanel.add(header2Panel);
		contentPanel.add(header3Panel);
		return contentPanel;
	}

	private JPanel createBody() {
		JPanel contentPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
		contentPanel.setLayout(boxLayout);
		JLabel tooltipLabel = new JLabel(COLUMN_TOOLTIP_LABEL, JLabel.CENTER);
		tooltipLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		labelPanel.add(tooltipLabel);
		labelPanel.setVisible(false);
		tableScrollPane.setPreferredSize(new Dimension(MAIN_DIALOG_WIDTH - 100, (int) (0.66 * MAIN_DIALOG_HEIGHT)));
		contentPanel.add(labelPanel);
		contentPanel.add(tableScrollPane);
		return contentPanel;
	}

	private JPanel createFooter() {
		JPanel contentPanel = new JPanel();
		BoxLayout box = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
		contentPanel.setLayout(box);
		JPanel footer1Panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		removeSelectedFilesButton.setPreferredSize(new Dimension(125, 35));
		removeSelectedFilesButton.setHorizontalAlignment(SwingConstants.CENTER);
		removeSelectedFilesButton.setFont(BUTTON_FONT);
		filterByFileButton.setPreferredSize(new Dimension(110, 35));
		filterByFileButton.setFont(BUTTON_FONT);
		filterTextField.setPreferredSize(new Dimension(100, 35));
		selectAllFilesButton.setPreferredSize(new Dimension(110, 25));
		selectAllFilesButton.setFont(BUTTON_FONT);
		selectNoneButton.setPreferredSize(new Dimension(95, 25));
		selectNoneButton.setFont(BUTTON_FONT);
		manualSetValuesButton.setPreferredSize(new Dimension(215, 35));
		manualSetValuesButton.setFont(BUTTON_FONT);
		adjustValuesButton.setPreferredSize(new Dimension(210, 45));
		adjustValuesButton.setFont(BUTTON_FONT);
		loadValueSetButton.setPreferredSize(new Dimension(110, 25));
		loadValueSetButton.setFont(BUTTON_FONT);
		saveValueSetButton.setPreferredSize(new Dimension(110, 25));
		saveValueSetButton.setFont(BUTTON_FONT);
		cancelButton.setPreferredSize(new Dimension(80, 25));
		cancelButton.setFont(BUTTON_FONT);
		startBatchingProcessingButton.setPreferredSize(new Dimension(150, 25));
		startBatchingProcessingButton.setFont(BUTTON_FONT);
		footer1Panel.add(Box.createHorizontalStrut(10));
		footer1Panel.add(removeSelectedFilesButton);
		footer1Panel.add(Box.createHorizontalStrut(15));
		footer1Panel.add(filterByFileButton);
		footer1Panel.add(Box.createHorizontalStrut(5));
		footer1Panel.add(filterTextField);
		footer1Panel.add(Box.createHorizontalStrut(70));
		footer1Panel.add(selectAllFilesButton);
		footer1Panel.add(Box.createHorizontalStrut(5));
		footer1Panel.add(selectNoneButton);
		footer1Panel.add(Box.createHorizontalStrut(70));
		footer1Panel.add(loadValueSetButton);
		footer1Panel.add(Box.createHorizontalStrut(285));
		JPanel footer2Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		footer2Panel.add(Box.createHorizontalStrut(150));
		footer2Panel.add(manualSetValuesButton);
		footer2Panel.add(Box.createHorizontalStrut(70));
		footer2Panel.add(adjustValuesButton);
		footer2Panel.add(Box.createHorizontalStrut(70));
		footer2Panel.add(saveValueSetButton);
		footer2Panel.add(Box.createHorizontalStrut(55));
		footer2Panel.add(cancelButton);
		footer2Panel.add(Box.createHorizontalStrut(5));
		footer2Panel.add(startBatchingProcessingButton);
		contentPanel.add(footer1Panel);
		contentPanel.add(footer2Panel);
		return contentPanel;
	}

	private Vector<String> getColumnNames(List<Channel> channels) {
		Vector<String> columnNames = new Vector<>();
		columnNames.addElement(FILENAME_TABLE_HEADER);
		for (Channel channel : channels) {
			columnNames.add(channel.getName());
		}
		return columnNames;
	}

	private Vector<Vector<Object>> getTableData(List<String> fileNames, List<Channel> channels) {
		Vector<Vector<Object>> data = new Vector<>();
		int rowCount = fileNames == null ? 1 : fileNames.size();
		for (int i = 0; i < rowCount; i++) {
			Vector<Object> rowData = new Vector<>();
			List<Channel> rowChannels = new ArrayList<>();
			rowData.addElement((fileNames == null || fileNames.size() < i) ? "" : fileNames.get(i));
			for (Channel channel : channels) {
				rowData.addElement(new CellItem(channel.getMin(), channel.getMax()));
				rowChannels.add(channel);
			}
			data.addElement(rowData);
		}
		return data;
	}

	private void setTableOptions() {
		labelPanel.setVisible(true);
		rowSorter = (TableRowSorter<? extends TableModel>) table.getRowSorter();
		for (int i = 0; i < table.getColumnCount(); i++) {
			rowSorter.setSortable(i, false);
		}
		tableScrollPane.getViewport().removeAll();
		tableScrollPane.getViewport().add(table);
		tableScrollPane.revalidate();
		tableScrollPane.repaint();
	}
}
