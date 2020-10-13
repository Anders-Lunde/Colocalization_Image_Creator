package wizard.app;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;

public class Wizard {

	public static Map<String, String> globalMap = new HashMap<>();
	public static int nInputImpChannels = 0;
	public static int nOutputImpChannels = 0; //MaxOutputChannels (current value of n channels in output image)
	public static String scale_unit = ""; // pixel/inch/cm/um:
	public static DefaultListModel<String> color_priority_forWizard = new DefaultListModel<String>();
	
}
