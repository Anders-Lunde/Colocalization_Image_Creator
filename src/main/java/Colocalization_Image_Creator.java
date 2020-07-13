import java.io.File;
import javax.swing.DefaultListModel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.plugin.ChannelSplitter;
import ij.plugin.MacroInstaller;
import ij.plugin.PlugIn;

public class Colocalization_Image_Creator implements PlugIn {
	static DefaultListModel<String> plugin_instance_counter = new DefaultListModel<String>();
	public ImagePlus tmpImp;
	@Override
	public void run(String arg) {
		//Having more than one plugin instance is problematic. Check for this
		plugin_instance_counter.addElement("incrementor");
		if (plugin_instance_counter.size() > 1) {
			IJ.error("ERROR", "Another ColocalizationImageCreator is running. Open old one, or save progress and restart ImageJ.");
			return;
		}
		
		//Open debug image: //TODO: Disable
		//File resourcesDirectory = new File("src/main/resources/");
		//ImagePlus debugimage = IJ.openImage("\\\\kant\\uv-isp-adm-u1\\andelu\\pc\\Pictures\\0 Stack 2.tif");
		//ImagePlus debugimage = IJ.openImage("\\\\kant\\uv-isp-adm-u1\\andelu\\pc\\Pictures\\0 Stack 2.tif");
		//debugimage.show();


		
		//Force one image to be open before plugin launch
		int[] imageIDs = WindowManager.getIDList();
		while (imageIDs == null || imageIDs.length != 1) {
			WaitForUserDialog wait = new ij.gui.WaitForUserDialog("Colocalization Image Creator", "Please open exactly one(!) multi-channel or RGB image and press ok");
			wait.show();
			imageIDs = WindowManager.getIDList();
		}
		
		//Convert to multichannel if RGB
		ImagePlus tmpImp = WindowManager.getCurrentImage();
		if (tmpImp.getBitDepth() == 24 && tmpImp.getNChannels() == 1) {
			IJ.showMessage("RGB input detected",
					"Plugin will not work on RGB images (only 8-bit, 16-bit, 32-bit) \r\n"
					+ "Input image is RGB with no channels. Press OK to convert to multichannel image");
				RGBto8bitChannels(tmpImp);
			}
		
		//Launch plugin UI
		installHotkeyMacros();
		UI_and_ImageProcessing pluginFrame = new UI_and_ImageProcessing();
		pluginFrame.main("");
	}
	
	
	// For debugging from Eclipse IDE. Not executed when running from ImageJ:
	public static void main(String[] args) {
		new ij.ImageJ();
		new Colocalization_Image_Creator().run("");
	}
	

	private void installHotkeyMacros() {
		GenericDialog hotkeyDialog = new GenericDialog("Enable hotkeys?");
		hotkeyDialog.enableYesNoCancel();
		
		String msg = "Enable the following hotkeys?\r\n\r\n";
		msg = msg + "Q -> Select channel 1\r\n";
		msg = msg + "W -> Select channel 2\r\n";
		msg = msg + "E -> Select channel 3\r\n";
		msg = msg + "A -> Select channel 4\r\n";
		msg = msg + "S -> Select channel 5\r\n";
		msg = msg + "D -> Select channel 6\r\n";
		msg = msg + "Z -> Select channel 7\r\n";
		msg = msg + "X -> Select channel 8\r\n";
		msg = msg + "C -> Select channel 9\r\n";
		msg = msg + "\r\n";
		msg = msg + "R -> Zoom in\r\n";
		msg = msg + "F -> Zoom out\r\n";
		msg = msg + "\r\n";
		msg = msg + "\r\n";
		msg = msg + "Hints:\r\n";
		msg = msg + "-Alt + left click to delete multipoints\r\n";
		msg = msg + "-Hold spacebar + drag mouse button to pan\r\n";
		msg = msg + "-Scroll mouse wheel to change Z-stack position\r\n";
		msg = msg + "-Restore multipoint selection with Ctrl+Shift+E\r\n";
		
		hotkeyDialog.addMessage(msg);
		hotkeyDialog.showDialog();
		
		if (hotkeyDialog.wasOKed()) {
		    StringBuffer sb = new StringBuffer();
		    sb.append("macro \"set chn 1 [q]\" {Stack.setChannel(1);}\n");
		    sb.append("macro \"set chn 2 [w]\" {Stack.setChannel(2);}\n");
		    sb.append("macro \"set chn 3 [e]\" {Stack.setChannel(3);}\n");
		    sb.append("macro \"set chn 4 [a]\" {Stack.setChannel(4);}\n");
		    sb.append("macro \"set chn 5 [s]\" {Stack.setChannel(5);}\n");
		    sb.append("macro \"set chn 6 [d]\" {Stack.setChannel(6);}\n");
		    sb.append("macro \"set chn 7 [z]\" {Stack.setChannel(7);}\n");
		    sb.append("macro \"set chn 8 [x]\" {Stack.setChannel(8);}\n");
		    sb.append("macro \"set chn 9 [c]\" {Stack.setChannel(9);}\n");
		    sb.append("macro \"zoom in [r]\" {run(\"In [+]\");}\n");
		    sb.append("macro \"zoom out 9 [f]\" {run(\"Out [-]\");}\n");
		    new MacroInstaller().install(sb.toString());
		}
	}
	
	//Method for converting RGB to multichannel upon launch
	//An (almost) identical method is in the "User_interface.java" used when user changes inital image
	private void RGBto8bitChannels(ImagePlus tmpImp) {
		ImagePlus[] channels = ChannelSplitter.split(tmpImp);
		ImageStack stack = new ImageStack(tmpImp.getWidth(), tmpImp.getHeight());
		stack.addSlice("red", channels[0].getChannelProcessor());
		stack.addSlice("green", channels[1].getChannelProcessor());
		stack.addSlice("blue", channels[2].getChannelProcessor());
		ImagePlus inputImp2 = new ImagePlus(tmpImp.getTitle(), stack);
		tmpImp.close();
		channels[0].close();
		channels[1].close();
		channels[2].close();
		tmpImp = inputImp2;
		tmpImp.show();
		IJ.run(tmpImp, "Properties...", "channels=3 slices=1 frames=1");
	}
	
}