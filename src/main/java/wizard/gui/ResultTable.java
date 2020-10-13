package wizard.gui;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import wizard.app.Wizard;

public class ResultTable extends JDialog {

	public ResultTable() {

		String data[][] = new String[25][2];
		int i=0;
		for (String name : Wizard.globalMap.keySet()) {
			if(!name.equals("tuc") && !name.equals("moc") && !name.equals("su")) {
				data[i][0] = name;
				String value = Wizard.globalMap.get(name);
				if(value != null) {
					data[i++][1] = value.toString();
				}
			}
		}
		String column[] = { "Key", "Value" };
		JTable jt = new JTable(data, column);

		jt.setBounds(30, 40, 200, 300);
		JScrollPane sp = new JScrollPane(jt);
		add(sp);

		this.setSize(new Dimension(500, 500));
		this.setLocationRelativeTo(null);
		setVisible(true);
	}

}
