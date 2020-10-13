package batchprocessing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public class CustomCellEditor extends DefaultCellEditor implements TableCellEditor {

	public CustomCellEditor(JTextField textField) {
		super(textField);
	}

	private JComponent component = new JTextField();
	private MinMaxSetDialog minMaxSetDialog;
	private int min;
	private int max;
	private JTable table;

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
			int vColIndex) {

		if(vColIndex == 0) {
			return component;
		}
		
		this.table = table;
		
		if(value instanceof CellItem) {
			CellItem item = (CellItem)value;
			this.min = item.getMin();
			this.max = item.getMax();
			minMaxSetDialog = new MinMaxSetDialog(this.min, this.max);
			minMaxSetDialog.saveButton.addActionListener(saveButtonActionListener);
			minMaxSetDialog.cancelButton.addActionListener(cancelButtonActionListener);
			minMaxSetDialog.setAlwaysOnTop(true);
			minMaxSetDialog.setVisible(true);
			
		}

		return component;
	}

	public Object getCellEditorValue() {
		return new CellItem(this.min, this.max);
	}
	
	ActionListener saveButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(minMaxSetDialog.minSpinner.getValue() instanceof Integer && minMaxSetDialog.maxSpinner.getValue() instanceof Integer) {
				CustomCellEditor.this.min = (int)minMaxSetDialog.minSpinner.getValue();
				CustomCellEditor.this.max = (int)minMaxSetDialog.maxSpinner.getValue();
			}
			minMaxSetDialog.dispose();
			CustomCellEditor.this.table.getCellEditor().stopCellEditing();
		}
	};

	ActionListener cancelButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			minMaxSetDialog.dispose();
			CustomCellEditor.this.table.getCellEditor().stopCellEditing();
		}
	};
}
