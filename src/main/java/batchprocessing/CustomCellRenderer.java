package batchprocessing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class CustomCellRenderer extends JPanel implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int vColIndex) {

		super.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
		if (value instanceof CellItem) {
			CellItem val = (CellItem) value;
			JLabel minLabel = new JLabel(String.valueOf(val.getMin()), JLabel.CENTER);
			minLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			minLabel.setPreferredSize(new Dimension(50, 20));
			minLabel.setHorizontalAlignment(SwingConstants.CENTER);
			JLabel maxLabel = new JLabel(String.valueOf(val.getMax()), JLabel.CENTER);
			maxLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			maxLabel.setPreferredSize(new Dimension(50, 20));
			maxLabel.setHorizontalAlignment(SwingConstants.CENTER);
			super.setLayout(new FlowLayout(FlowLayout.CENTER, 60, 5));
			super.removeAll();
			super.add(minLabel);
			super.add(maxLabel);
		} else if (value instanceof String) {
			super.removeAll();
			JLabel label = new JLabel(value.toString());
			super.add(label);
		}

		if (isSelected) {
			super.setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		} else {
			super.setForeground(table.getForeground());
			super.setBackground(table.getBackground());
		}

		return this;
	}
}
