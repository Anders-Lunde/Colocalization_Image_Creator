package batchprocessing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class HeaderCellRenderer extends JPanel implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int vColIndex) {
		
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		super.setLayout(boxLayout);

		super.removeAll();
		
		JPanel mainPanel = new JPanel();
		JLabel mainLabel = new JLabel(value.toString());
		mainPanel.add(mainLabel);
		this.add(mainPanel);

		if(vColIndex != 0) {
			JPanel secondPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 10));
			JLabel minLabel = new JLabel("Min:", JLabel.CENTER);
			minLabel.setPreferredSize(new Dimension(50, 20));
			JLabel maxLabel = new JLabel("Max:", JLabel.CENTER);
			maxLabel.setPreferredSize(new Dimension(50, 20));
			secondPanel.add(minLabel);
			secondPanel.add(maxLabel);		
			super.add(secondPanel);
		}

		return this;
	}
}
