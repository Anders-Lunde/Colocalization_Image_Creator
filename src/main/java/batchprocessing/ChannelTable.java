package batchprocessing;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class ChannelTable extends JTable {

	List<Point> selected = new ArrayList<Point>();
	boolean[] colsSelected;

	public ChannelTable(DefaultTableModel tableModel) {
		super(tableModel);
		this.setDefaultRenderer(Object.class, new CustomCellRenderer());
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setShowGrid(false);
		this.setRowSelectionAllowed(true);
		this.setCellSelectionEnabled(true);
		this.getTableHeader().setReorderingAllowed(false);
		this.getTableHeader().setResizingAllowed(false);
		this.setAutoCreateRowSorter(true);
		colsSelected = new boolean[this.getColumnCount()];

		TableColumnModel columnModel = this.getColumnModel();
		CustomCellEditor cellEditor = new CustomCellEditor(new JTextField());
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			columnModel.getColumn(i).setMinWidth(250);
			columnModel.getColumn(i).setHeaderRenderer(new HeaderCellRenderer());
			if (i != 0) {
				columnModel.getColumn(i).setCellEditor(cellEditor);
			} else {
				columnModel.getColumn(i).setCellEditor(null);
			}
		}
		this.setRowHeight(30);

		this.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int col = ChannelTable.this.columnAtPoint(e.getPoint());
					if (colsSelected[col]) {
						List<Point> pointsToRemove = new ArrayList<>();
						for (Point p : selected) {
							if (p.getY() == col) {
								pointsToRemove.add(p);
							}
						}
						for (Point p : pointsToRemove) {
							selected.remove(p);
						}
						colsSelected[col] = false;
					} else {
						for (int i = 0; i < ChannelTable.this.getRowCount(); i++) {
							Point p2 = new Point(i, col);
							if (!selected.contains(p2)) {
								selected.add(new Point(i, col));
							}
						}
						colsSelected[col] = true;
					}
					ChannelTable.this.repaint();
				}
			}
		});

	}

	@Override
	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() != MouseEvent.MOUSE_PRESSED)
			return;
		int row = ((JTable) e.getSource()).rowAtPoint(e.getPoint());
		int col = ((JTable) e.getSource()).columnAtPoint(e.getPoint());
		if (row >= 0 && col >= 0) {
			Point p = new Point(row, col);
			if (selected.contains(p))
				selected.remove(p);
			else
				selected.add(p);
		}
		((JTable) e.getSource()).repaint();
	}

	@Override
	public boolean isCellSelected(int arg0, int arg1) {
		return selected.contains(new Point(arg0, arg1));
	}

	public void deselectAllCells() {
		this.selected = new ArrayList<>();
		Arrays.fill(colsSelected, false);
		this.repaint();
	}

	public void selectAllCells() {
		this.selected = new ArrayList<>();
		for (int i = 0; i < this.getRowCount(); i++) {
			for (int j = 0; j < this.getColumnCount(); j++) {
				this.selected.add(new Point(i, j));
			}
		}
		Arrays.fill(colsSelected, true);
		this.repaint();
	}

	public Map<String, List<IndexObject>> getSelections() {
		Map<String, List<IndexObject>> selections = new HashMap<>();
		for (Point p : selected) {
			if (p.getY() == 0) {
				String filename = this.getValueAt(p.x, p.y).toString();
				List<IndexObject> channels = new ArrayList<>();
				for (Point p1 : selected) {
					if (p1.getX() == p.getX() && p1.getY() != p.getY()) {
						channels.add(new IndexObject(this.getColumnName(p1.y), this.convertRowIndexToModel(p1.x),
								this.convertColumnIndexToModel(p1.y)));
					}
				}
				if (!channels.isEmpty()) {
					selections.put(filename, channels);
				}
			}
		}
		return selections;
	}

	public List<Point> getSelectedChannelCells() {
		return this.selected.stream().filter(point -> point.getY() != 0).sorted(Comparator.comparing(Point::getX))
				.collect(Collectors.toList());
	}

	public List<Point> getSelectedFiles() {
		return this.selected.stream().filter(point -> point.getY() == 0).sorted(Comparator.comparing(Point::getX))
				.collect(Collectors.toList());
	}
}
