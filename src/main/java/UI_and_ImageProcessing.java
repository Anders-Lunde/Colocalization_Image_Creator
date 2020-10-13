import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONObject;

import Utilities.Counter3D;
import batchprocessing.BatchProcessingConstants;
import batchprocessing.BatchProcessingDialog;
import batchprocessing.CellItem;
import batchprocessing.ChannelTable;
import batchprocessing.IndexObject;
import batchprocessing.MinMaxSetDialog;
import batchprocessing.json.JSonChannel;
import batchprocessing.json.JSonTableObject;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.io.SaveDialog;
import ij.macro.Interpreter;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.plugin.ZProjector;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.LUT;
import wizard.app.Wizard;
import wizard.enums.PageEnum;
import wizard.gui.AdvancedOptionsBinary;
import wizard.gui.AdvancedOptionsGrayscale;
import wizard.gui.BinaryElement;
import wizard.gui.FirstPage;
import wizard.gui.GrayscaleElement;
import wizard.gui.PageCaller;
import wizard.util.Keys;

public class UI_and_ImageProcessing {
	/*
	 * This class contains all the main menu GUI code, logic for verifying valid images,
	 * and some code that connects the main menu with the
	 * "Add element" wizard, and the "Batch processing" menu.
	 * It also contains the calculateAndShowOutput() method which does all the image processing:
	 * Creating the output image (impFinalOutput) from the input image (impInput). 
	 * 
	 * The "Add element" wizard populates variables in HashMap_AllInfo.
	 * Image processing (calculateAndShowOutput()) only needs
	 * HashMap_AllInfo, impInput, and nElements to do its job. 
	 * This means that only those 3 variables need to be 
	 * manipulated for getting a desired output image. 
	 * These variables are set upon either: 
	 * 1) exiting the "Add Element" wizard
	 * 2) during "preview" in the "Add Element" wizard 
	 * 3) during batch processing.  
	 */
	

	 /* HashMap_AllInfo 
	  * This is the main data structure that holds all the elements information and metadata. 
	  * The content of HashMap_AllInfo is manipulated through the "Add Element" wizard, or some of the
	  * buttons in the main menu (zProjection options, color priority, delete/save elements, batch processing). 
	  * Elements are named as integers, starting at 1, in string representation. 
	  * To access all elements in the HashMap, we loop from 1 to nElements.
	  * e.g. HashMap_AllInfo.get("1") holds all info for the first image element
	  * 
	  * HashMap_AllInfo holds all the parameters for generating an output image from an input image, 
	  * including threshold and contrast values. 
	  * e.g. HashMap_AllInfo.get("Threshold Values").get("Channel" + (n) + " Min")
	 */
	
	//HashMap_AllInfo - Holds all the info required to produce the output image from the input:
	HashMap<String, HashMap<String, String>> hashMap_AllInfo = new HashMap<String, HashMap<String, String>>();
	//HashMap_AllInfo_Tmp_Backup is used as a backup of "HashMap_AllInfo" during "preview" in "add element".
	//After preview, original HashMap_AllInfo data is restored.
	HashMap<String, HashMap<String, String>> hashMap_AllInfo_Tmp_Backup = new HashMap<String, HashMap<String, String>>();
	//valsSetDuringPreview - Hashmap for holding threshold/contrast values set during preview in the wizard:
	Map<String, String> valsSetDuringPreview = new HashMap<String, String>();
	
	ImagePlus impInput; //current input image
	//Temporary images used in calculateAndShowOutput():
	ImagePlus impAllContrastElements;
	ImagePlus impOneContrastElement;
	ImagePlus impAllThresholdElements;
	ImagePlus impSingleThresholdChannel;
	ImagePlus impOneThresholdElement;
	ImagePlus rmv_small_particles_input;
	ImagePlus impZProjection;
	ImagePlus impFinalOutput;
	ImageCalculator ic;
	
	//Parameters extracted from HashMap_AllInfo during calculateAndShowOutput():
	String zProjectionOutputMode;
	String calledFrom;
	String scale_unit;
	boolean includeOutlineElements;
	boolean doOutlineConversion;
	boolean doRmvParticles;
	int finalOutputSlices;
	int curOutChnIterator;
	
	//Main menu GUI related:
	JFrame Frame_MainMenu;
	JTextField TextField_N_Element_Displayer;
	JTextField TextField_ThresholdMin[];
	JTextField TextField_ThresholdMax[];
	JTextField TextField_ContrastMin[];
	JTextField TextField_ContrastMax[];
	JButton Button_ThresholdChannel[];
	JButton Button_ContrastChannel[];
	JLabel Label_Adjust_Value;
	JLabel Label_AdjustContrast_Value;
	JLabel Label_AdjustThreshold_Value;
	ArrayList<Integer> List_Temp_ThresholdedChannels = new ArrayList<Integer>();
	ArrayList<Integer> List_Temp_ContrastedChannels = new ArrayList<Integer>();

	//Batch processing variables:
	//Used in batch processing to initialize contrast/threshold values fields:
	List<Integer> HashMap_thresholdChns = new ArrayList<Integer>(); 
	//Used in batch processing to initialize contrast/threshold values fields:
	List<Integer> HashMap_contrastChns = new ArrayList<Integer>(); 
	//Used in batch processing to initialize contrast/threshold values fields. Min, Max values:
	List<int[]> HashMap_thresholdVals = new ArrayList<int[]>(); 
	//Used in batch processing to initialize contrast/threshold values fields. Min, Max values:
	List<int[]> HashMap_contrastVals = new ArrayList<int[]>(); 
	
	DefaultListModel<String> color_priority = new DefaultListModel<String>();
	ArrayList<String> List_ZProjection = new ArrayList<String>();
	
	//Graphics related
	GraphicsEnvironment GE = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice DefaultScreen = GE.getDefaultScreenDevice();
	Rectangle Rect = DefaultScreen.getDefaultConfiguration().getBounds();
	BatchProcessingDialog batchProcessingDialog;
	
	//Various variables:
	int nElements = 0; //Keeps track of how many elements are active. Temporarily set to 1 during preview
	int nElements_tmpBackup; //Stores the original nElements during preview
	static int TotalChannels; //How many channels in impInput
	int impInputMiddleSlice;
	int n_outputChannels; // number of channels in final output image
	boolean firstRun = true;
	int counter = 0; //Used during interactive manual setting of contrast/threshold values
	int TotalChannelsFirstRun;
	//Keeps track of which images are output images, so we never confuse them for input images:
	Set<Integer> outputImageIDlist = new HashSet<Integer>();
	//Set that keeps track of output channels with more than one grayscale element:
	Set<Integer> outChannelsWithMoreThanOneGrayscaleElement = new HashSet<Integer>();
	boolean displayGrayscaleError1 = true;
	//Checks if the current grayscale element had more than one input chn within single element
	boolean isManyGrayscaleInOneElement = false;
	boolean displayGrayscaleError2 = true;
	boolean previewMode = false;


	public void main(String arg) {
		// The plugin expects certain preferences, and can fail if not set correctly
		IJ.run("Misc...", "divide=Infinity hide run reverse");
		IJ.run(impInput, "Options...", "iterations=1 count=1 black");
		IJ.run("Colors...", "foreground=white background=black selection=yellow");

		addEventListenersForWizard();
		
		//Set threshold mode
		IJ.run("Threshold...");
		ij.plugin.frame.ThresholdAdjuster.setMode("Over/Under");
		IJ.selectWindow("Threshold");
		IJ.run("Close");

		// Initializing ic
		ic = new ImageCalculator();

		// Setting the default Color priority
		color_priority.addElement("White");
		color_priority.addElement("Yellow");
		color_priority.addElement("Cyan");
		color_priority.addElement("Magenta");
		color_priority.addElement("Green");
		color_priority.addElement("Red");
		color_priority.addElement("Blue");
		
		//Check if input imp is present and accepted format
		boolean error = InitializeInputImp(); // initiates the impInput object,
		if (!error) {
			TextField_N_Element_Displayer = new JTextField();
			TextField_ThresholdMin = new JTextField[TotalChannels];
			TextField_ThresholdMax = new JTextField[TotalChannels];
			TextField_ContrastMin = new JTextField[TotalChannels];
			TextField_ContrastMax = new JTextField[TotalChannels];
			Button_ThresholdChannel = new JButton[TotalChannels];
			Button_ContrastChannel = new JButton[TotalChannels];
			initializeHashMapMetaData();
			MainMenu();
		}
	}

	
	public void initializeHashMapMetaData() {
		hashMap_AllInfo.put("Threshold Values", new HashMap<String, String>() {
			{
				for (int i = 1; i <= TotalChannels; i++) {
					put(("Channel" + i + " Min"), "-1");
					put(("Channel" + i + " Max"), "-1");
				}
			}
		});
		hashMap_AllInfo.put("Contrast Values", new HashMap<String, String>() {
			{
				for (int i = 1; i <= TotalChannels; i++) {
					put(("Channel" + i + " Min"), "-1");
					put(("Channel" + i + " Max"), "-1");
				}
			}
		});
		hashMap_AllInfo.put("Z Projection Options", new HashMap<String, String>() {
			{
				put("output", "both");
				put("method", "1");
				put("zMapMode", "topPriority");
			}
		});
		hashMap_AllInfo.put("Color Priority List", new HashMap<String, String>() {
			{
				put("StoredAsList", color_priority.toString());
			}
		});
	}

	/* This is called on startup and when most of the main menu buttons are clicked. */
	public boolean InitializeInputImp() {
		/*
		 * First, check that only 1 input image is open, and close any previous
		 * output images: 
		 * -get list of all open image IDs 
		 * -Remove the ones that
		 * are output images (outputImageIDlist) 
		 * -If more than 1 -> Error: more
		 * than one image open 
		 * -If only 1 -> initialize as impInput 
		 * -If none -> Error: no images open
		 */
		boolean error = false;
		int[] imageIDs = WindowManager.getIDList();
		if (imageIDs == null) {
			IJ.error("No input images open. Please open an image.");
			error = true;
		} else {
			Set<Integer> imageIDsSet = Arrays.stream(imageIDs).boxed().collect(Collectors.toSet());
			imageIDsSet.removeAll(outputImageIDlist);
			if (imageIDsSet.size() > 1) {
				IJ.error("More than 1 input image open. Please close other windows.");
				error = true;
			} else if (imageIDsSet.size() == 0) {
				IJ.error("No input images open. Please open an image.");
				error = true;
			} else if (imageIDsSet.size() == 1) {
				// No errors. Get some variables from the input img:
				impInput = WindowManager.getImage((int) imageIDsSet.toArray()[0]);
				TotalChannels = impInput.getNChannels();
				if (firstRun) {
					TotalChannelsFirstRun = impInput.getNChannels();
					firstRun = false;
				}
				//Set middle slice variable
				if (impInput.getNSlices() == 1)
					impInputMiddleSlice = 1;
				else if (impInput.getNSlices() % 2 == 0)
					impInputMiddleSlice = impInput.getNSlices() / 2;
				else if (impInput.getNSlices() % 2 != 0)
					impInputMiddleSlice = impInput.getNSlices() / 2 + 1;
				
				// Give user option to convert if input is RGB
				boolean convert;
				if (impInput.getBitDepth() == 24 && impInput.getNChannels() == 1) {
					convert = IJ.showMessageWithCancel("RGB input detected",
							"Input image is RGB with no channels. Convert to RedGreenBlue to channels?");
					if (convert) {
						RGBto8bitChannels();
						TotalChannels = impInput.getNChannels();
					} else {
						error = true;
						IJ.error("Plugin will not work on RGB images (only 8-bit, 16-bit, 32-bit)");
					}
				} else if (impInput.getBitDepth() == 24 && impInput.getNChannels() > 1) {
					convert = IJ.showMessageWithCancel("RGB input detected",
							"Input image is RGB with channels. Convert to channels to 8-bit?");
					if (convert) {
						IJ.run(impInput, "8-bit", "");
						TotalChannels = impInput.getNChannels();
					} else {		
						IJ.error("Plugin will not work on RGB images (only 8-bit, 16-bit, 32-bit)");
					}
				}
				// Check if channels are consistent
				if (impInput.getNChannels() != TotalChannelsFirstRun) {
					IJ.showMessage("ERROR!",
							"ERROR: Number of channels in the initial image and current image does not match. "
							+ "This can lead to unexpected results. Please save progress and restart both *ImageJ* "
							+ "and the *plugin*. \n\nInitial image: "
									+ TotalChannelsFirstRun + " channels\n" + "Current image: "
									+ impInput.getNChannels() + " channels");
					error = true;
				}
				// Reset threshold and contrast
				for (int i = 1; i <= impInput.getNChannels(); i++) {
					IJ.resetThreshold(impInput);
					IJ.resetMinAndMax(impInput);
				}
			}
		}
		return error;
	}

	private void RGBto8bitChannels() {
		ImagePlus[] channels = ChannelSplitter.split(impInput);
		ImageStack stack = new ImageStack(impInput.getWidth(), impInput.getHeight());
		stack.addSlice("red", channels[0].getChannelProcessor());
		stack.addSlice("green", channels[1].getChannelProcessor());
		stack.addSlice("blue", channels[2].getChannelProcessor());
		ImagePlus inputImp2 = new ImagePlus(impInput.getTitle(), stack);
		impInput.close();
		channels[0].close();
		channels[1].close();
		channels[2].close();
		impInput = inputImp2;
		impInput.show();
		IJ.run(impInput, "Properties...", "channels=3 slices=1 frames=1");
	}

	
	// Construct the main menu UI:
	public void MainMenu() {
		int buttonYGap = 27;
		int buttonYStart = 20;
		// Adding the Setting Frame on the right side of the Image.
		Frame_MainMenu = new JFrame("Colocalization Image Creator");
		//Avoid having the user create multiple instances of plugin (leads to some problems)
		//By disabling X button.
		Frame_MainMenu.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		Frame_MainMenu.setBounds(0, 0, 275, 350);
		Frame_MainMenu.getContentPane().setLayout(null);
		int x = (int) Rect.getMaxX() - Frame_MainMenu.getWidth();
		int y = (int) Rect.getMaxY() - Frame_MainMenu.getHeight();
		Frame_MainMenu.setLocation(x, y / 2);

		//BUTTONS:
		
		// ADD ELEMENT - AddElement Button to the "MainMenu Frame"
		JButton Button_AddElement = new JButton("Add Element");
		Button_AddElement.setBounds(40, (buttonYStart+(buttonYGap*0)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_AddElement);
		Button_AddElement.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				boolean error = InitializeInputImp();
				if (!error) {
					Frame_MainMenu.setVisible(false);
					AddElement();
				}

			}
		});

		// RECALCULATE OUTPUT - Recalculate Output Button to the "MainMenu Frame"
		JButton Button_Recalculate_Output = new JButton("Redraw Output");
		Button_Recalculate_Output.setBounds(40, (buttonYStart+(buttonYGap*1)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_Recalculate_Output);
		Button_Recalculate_Output.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				boolean error = InitializeInputImp();
				if (!error) {
					calculateAndShowOutput_threader();
					printHashMap();
					MainMenu_Update_Structure_Values();
				}
			}
		});

		// DELETE ELEMENT - DeleteElement Button to the "MainMenu Frame"
		JButton Button_DeleteElement = new JButton("Delete Last Element");
		Button_DeleteElement.setBounds(40, (buttonYStart+(buttonYGap*2)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_DeleteElement);
		Button_DeleteElement.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				DeleteElement();
			}
		});

		// DELETE ALL ELEMENTS - Button to the "MainMenu Frame"
		JButton Button_SetChannel = new JButton("Delete All Elements");
		Button_SetChannel.setBounds(40, (buttonYStart+(buttonYGap*3)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_SetChannel);
		Button_SetChannel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				boolean delete_all = IJ.showMessageWithCancel("Delete All Elements",
						"This will reset and delete all element information. Continue?");
				if (delete_all) {
					deleteAllElements();
				}
			}
		});

		// SAVE SETTING - SaveSetting Button to the "MainMenu Frame"
		JButton Button_SaveSettings = new JButton("Save Settings");
		Button_SaveSettings.setBounds(40, (buttonYStart+(buttonYGap*4)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_SaveSettings);
		Button_SaveSettings.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				try {
					SaveSettings();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// LOAD SETTING - LoadSetting Button to the "MainMenu Frame"
		JButton Button_LoadSettings = new JButton("Load Settings");
		Button_LoadSettings.setBounds(40, (buttonYStart+(buttonYGap*5)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_LoadSettings);
		Button_LoadSettings.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				try {
					boolean error = InitializeInputImp();
					if (!error) {
						if (LoadSettings()) {// if load settings was successful
							MainMenu_Update_Structure_Values();
							// Call output image to be displayed
							calculateAndShowOutput_threader();
							printHashMap();
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// COLOR PRIORITY LIST- ColorPriority Button to the "MainMen Frame"
		JButton Button_ColorPriority = new JButton("Color Priority");
		Button_ColorPriority.setBounds(40, (buttonYStart+(buttonYGap*6)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_ColorPriority);
		Button_ColorPriority.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				boolean error = InitializeInputImp();
				if (!error) {
					Frame_MainMenu.setVisible(false);
					ColorPriorityList();
				}
			}
		});

		// Z PROJECTION- Z Projection Button to the "MainMenu Frame"
		JButton Button_ZProjection = new JButton("Z-Projection Options");
		Button_ZProjection.setBounds(40, (buttonYStart+(buttonYGap*7)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_ZProjection);
		Button_ZProjection.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				boolean error = InitializeInputImp();
				if (!error) {
					zProjectionOptions();
				}
			}
		});

		// BATCH PROCESSING - BatchProcessing Button to the "MainMenu Frame"
		JButton Button_BatchProcessing = new JButton("Batch Processing");
		Button_BatchProcessing.setBounds(40, (buttonYStart+(buttonYGap*8)), 151, 20);
		Frame_MainMenu.getContentPane().add(Button_BatchProcessing);
		Button_BatchProcessing.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				Frame_MainMenu.setVisible(false);
				BatchProcessing();
			}
		});

		//UI element: Box display of "Current number of elements" - in main menu frame
		JLabel Label_ChannelResponse = new JLabel("Current elements:");
		Label_ChannelResponse.setFont(new Font("", Font.PLAIN, 11));
		Label_ChannelResponse.setBounds(45, 270, 120, 20);
		Frame_MainMenu.getContentPane().add(Label_ChannelResponse);
		
		TextField_N_Element_Displayer.setHorizontalAlignment(SwingConstants.RIGHT);
		TextField_N_Element_Displayer.setBackground(null);
		TextField_N_Element_Displayer.setEditable(false);
		TextField_N_Element_Displayer.setBounds(150, 270, 40, 20);
		Frame_MainMenu.getContentPane().add(TextField_N_Element_Displayer);


		// UI elements: MAIN MENU BUTTONS FOR CONTRAST AND THRESHOLD ADJUST 
		//(Buttons appears after elements are made)
		Label_Adjust_Value = new JLabel("<html> Adjust Values: </html>");
		Label_Adjust_Value.setFont(new Font("", Font.BOLD, 11));
		Frame_MainMenu.getContentPane().add(Label_Adjust_Value);

		
		// ADJUST THRESHOLD BUTTON IN MAIN MENU (Not in Wizard)
		// Adding Adjust Threshold Value Label.
		// setBound Parameter added later from the
		// MainMenu_Update_Structure_Values function
		Label_AdjustThreshold_Value = new JLabel(
				"<html> &nbsp Threshold &nbsp&nbsp&nbsp&nbsp&nbsp Min &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp Max </html>");
		Label_AdjustThreshold_Value.setFont(new Font("", Font.BOLD, 10));
		Frame_MainMenu.getContentPane().add(Label_AdjustThreshold_Value);
		// Display Buttons and Text Fields with Values on Main Menu Frame
		for (int i = 0; i < TotalChannels; i++) {
			// Adding Text Fields
			final int f = i;
			// Minimum
			TextField_ThresholdMin[i] = new JTextField();
			TextField_ThresholdMin[i].setHorizontalAlignment(SwingConstants.RIGHT);
			TextField_ThresholdMin[i].setBackground(null);
			TextField_ThresholdMin[i].setEditable(false);
			Frame_MainMenu.getContentPane().add(TextField_ThresholdMin[i]);
			// Maximum
			TextField_ThresholdMax[i] = new JTextField();
			TextField_ThresholdMax[i].setHorizontalAlignment(SwingConstants.RIGHT);
			TextField_ThresholdMax[i].setBackground(null);
			TextField_ThresholdMax[i].setEditable(false);
			Frame_MainMenu.getContentPane().add(TextField_ThresholdMax[i]);
			// Adding Buttons
			Button_ThresholdChannel[i] = new JButton("Ch. " + (i + 1));
			Button_ThresholdChannel[i].setFont(new Font("", Font.PLAIN, 11));
			Frame_MainMenu.getContentPane().add(Button_ThresholdChannel[i]);
			// Buttons to edit Text Fields
			Button_ThresholdChannel[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean error = InitializeInputImp();
					if (!error) {					
						impInput.show();
						Frame_MainMenu.setVisible(false);
						// Close the output window if open
						closeOutputImage();
	
						// Go to the middle Slice
						impInput.setC(f + 1);
						impInput.setZ(impInputMiddleSlice);
						// Set LUT temporarily to grays, since its best to see weak signals
						impInput.getProcessor()
								.setLut(ij.process.LUT.createLutFromColor(new java.awt.Color(255, 255, 255)));
						IJ.run("Threshold...");
						double min = Double
								.parseDouble(hashMap_AllInfo.get("Threshold Values").get("Channel" + (f + 1) + " Min"));
						double max = 999999999;
						// We always want to default this slider to the max level. 
						//It's the upper slider that is used to threshold (black background):
						IJ.setThreshold(min, max); 
						final JFrame Frame_EditThresholdValue = new JFrame("Set Threshold");
						//Restore main menu on clicking X of this JFrame
						Frame_EditThresholdValue.addWindowListener(new WindowAdapter() {
							  public void windowClosing(WindowEvent we) {
								  Frame_MainMenu.setVisible(true);
								  }
								});
						Frame_EditThresholdValue.setBounds(0, 0, 250, 150);
						Frame_EditThresholdValue.getContentPane().setLayout(null);
						int x = (int) Rect.getMaxX() - Frame_EditThresholdValue.getWidth();
						int y = (int) Rect.getMaxY() - Frame_EditThresholdValue.getHeight();
						Frame_EditThresholdValue.setLocation(x, y / 2);
						Frame_EditThresholdValue.setVisible(true);
						JLabel Label_EditThresholdValue = new JLabel(
								"<html>Adjust the UPPER SLIDER <br> for channel" + (f + 1) + " and press OK</html>");
						Label_EditThresholdValue.setBounds(20, 25, 190, 30);
						Frame_EditThresholdValue.getContentPane().add(Label_EditThresholdValue);
						JButton Button_EditThresholdValue = new JButton("OK");
						Button_EditThresholdValue.setBounds(85, 66, 60, 20);
						Frame_EditThresholdValue.getContentPane().add(Button_EditThresholdValue);
						// Change Values
						Button_EditThresholdValue.addMouseListener(new MouseAdapter() {
							public void mouseClicked(MouseEvent arg0) {
								TextField_ThresholdMin[f]
										.setText(Integer.toString((int) impInput.getProcessor().getMinThreshold()));
								TextField_ThresholdMax[f]
										.setText(Integer.toString((int) impInput.getProcessor().getMaxThreshold()));
								// HashMap_AllElements
								hashMap_AllInfo.get("Threshold Values").put(("Channel" + (f + 1) + " Min"),
										Integer.toString((int) impInput.getProcessor().getMinThreshold()));
								hashMap_AllInfo.get("Threshold Values").put(("Channel" + (f + 1) + " Max"),
										Integer.toString((int) impInput.getProcessor().getMaxThreshold()));
	
								IJ.selectWindow("Threshold");
								IJ.run("Close");
								Frame_EditThresholdValue.dispose();
								Frame_EditThresholdValue.setVisible(false);
								calculateAndShowOutput_threader();
								Frame_MainMenu.setVisible(true);
							}
						});
					}
				}
			});
		}

		
		// ADJUST CONTRAST BUTTON IN MAIN MENU (Not in wizard)
		// Adding Adjust Contrast Value Label.
		// setBound parameter added later from the
		// MainMenu_Update_Structure_Values function
		Label_AdjustContrast_Value = new JLabel(
				"<html> &nbsp&nbsp Contrast &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp Min &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp Max</html>");
		Label_AdjustContrast_Value.setFont(new Font("", Font.BOLD, 10));
		Frame_MainMenu.getContentPane().add(Label_AdjustContrast_Value);
		// Display Buttons and Text Fields with Values on Main Menu Frame
		for (int i = 0; i < TotalChannels; i++) {
			// Adding Text Fields
			final int f = i;
			// Minimum
			TextField_ContrastMin[i] = new JTextField();
			TextField_ContrastMin[i].setHorizontalAlignment(SwingConstants.RIGHT);
			TextField_ContrastMin[i].setBackground(null);
			TextField_ContrastMin[i].setEditable(false);
			Frame_MainMenu.getContentPane().add(TextField_ContrastMin[i]);
			// Maximum
			TextField_ContrastMax[i] = new JTextField();
			TextField_ContrastMax[i].setHorizontalAlignment(SwingConstants.RIGHT);
			TextField_ContrastMax[i].setBackground(null);
			TextField_ContrastMax[i].setEditable(false);
			Frame_MainMenu.getContentPane().add(TextField_ContrastMax[i]);
			// Adding Buttons
			Button_ContrastChannel[i] = new JButton("Ch. " + (i + 1));
			Button_ContrastChannel[i].setFont(new Font("", Font.PLAIN, 11));
			Frame_MainMenu.getContentPane().add(Button_ContrastChannel[i]);
			// Buttons to edit Text Fields
			Button_ContrastChannel[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean error = InitializeInputImp();
					if (!error) {
						impInput.show();
						Frame_MainMenu.setVisible(false);
						// Close the output window if open
						closeOutputImage();
						// Go to the middle Slice
						impInput.setC(f + 1);
						impInput.setZ(impInputMiddleSlice);
						// Just for visualization, so color matches that of output
						// channel. No permanent change
						setTempLutFromHashmap();
						IJ.run("Brightness/Contrast...");
						Window bc = WindowManager.getWindow("B&C");
						Rectangle bcBounds = bc.getBounds();
						// Set B&C just left to the screen
						bc.setBounds((int) Rect.getMaxX() - bcBounds.width, bcBounds.y, bcBounds.width, bcBounds.height);
						final JFrame Frame_EditContrastValue = new JFrame("Set Contrast");
						//Restore main menu on clicking X of this JFrame
						Frame_EditContrastValue.addWindowListener(new WindowAdapter() {
							  public void windowClosing(WindowEvent we) {
								  Frame_MainMenu.setVisible(true);
								  }
								});
						Frame_EditContrastValue.setBounds(0, 0, 250, 150);
						int x = (int) Rect.getMaxX() - Frame_EditContrastValue.getWidth();
						int y = (int) Rect.getMaxY() - Frame_EditContrastValue.getHeight();
						Frame_EditContrastValue.setLocation(x, (int) (y / 2) + 200);
						Frame_EditContrastValue.setVisible(true);
						JLabel Label_EditContrastValue = new JLabel(
								"<html>Adjust MINIMUM and/or MAXIMUM for channel" + (f + 1) + " and press OK</html>");
						Label_EditContrastValue.setBounds(20, 5, 190, 50);
						Frame_EditContrastValue.getContentPane().add(Label_EditContrastValue);
						// JButton Button_EditContrastValue = new JButton("OK");
						// Bug: Button takes up whole frame. Putting texst in
						// button.
						JButton Button_EditContrastValue = new JButton(
								"<html>Adjust MINIMUM and/or MAXIMUM for channel" + (f + 1) + " and press OK</html>");
						Button_EditContrastValue.setBounds(85, 66, 60, 20);
						Frame_EditContrastValue.getContentPane().add(Button_EditContrastValue);
						// Change Values
						Button_EditContrastValue.addMouseListener(new MouseAdapter() {
							public void mouseClicked(MouseEvent arg0) {
								TextField_ContrastMin[f].setText(Integer.toString((int) impInput.getProcessor().getMin()));
								TextField_ContrastMax[f].setText(Integer.toString((int) impInput.getProcessor().getMax()));
								// HashMap_AllElements
								hashMap_AllInfo.get("Contrast Values").put(("Channel" + (f + 1) + " Min"),
										Integer.toString((int) impInput.getProcessor().getMin()));
								hashMap_AllInfo.get("Contrast Values").put(("Channel" + (f + 1) + " Max"),
										Integer.toString((int) impInput.getProcessor().getMax()));
								IJ.selectWindow("B&C");
								IJ.run("Close");
								Frame_EditContrastValue.dispose();
								Frame_EditContrastValue.setVisible(false);
								calculateAndShowOutput_threader();
								Frame_MainMenu.setVisible(true);
							}
						});
					}
				}
			});
		}
		
		Frame_MainMenu.setVisible(true);
	}

	
	//ADD ELEMENT WIZARD: ASK USER FOR THRESHOLD LEVEL
	public void Input_Threshold()
	// This is called when adding an threshold element. Its job is to get
	// threshold values for all
	// channels that the user has selected ( unless thresholds are already set
	// for those channels)
	{
		impInput.show();
		// Close the output window if open 
		//Important, so that correct window
		// is selected if user value input is needed
		closeOutputImage();
		counter = 0;
		// For the first Channel
		final JFrame Frame_ThresholdNotifier = new JFrame("Set Threshold");
		Frame_ThresholdNotifier.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		Frame_ThresholdNotifier.setBounds(0, 0, 250, 150);
		Frame_ThresholdNotifier.getContentPane().setLayout(null);
		int x = (int) Rect.getMaxX() - Frame_ThresholdNotifier.getWidth();
		int y = (int) Rect.getMaxY() - Frame_ThresholdNotifier.getHeight();
		Frame_ThresholdNotifier.setLocation(x, y / 2);
		Frame_ThresholdNotifier.setVisible(true);
		final JLabel Label = new JLabel("<html>Adjust the UPPER SLIDER <br>for channel"
				+ List_Temp_ThresholdedChannels.get(counter) + " and press OK</html>");
		Label.setBounds(20, 25, 190, 30);
		Frame_ThresholdNotifier.getContentPane().add(Label);
		final JButton Button_AddThresholdValues = new JButton("OK");
		Button_AddThresholdValues.setBounds(85, 66, 60, 20);
		Frame_ThresholdNotifier.getContentPane().add(Button_AddThresholdValues);
		// Go to the middle slice of the respective channel
		impInput.setC(List_Temp_ThresholdedChannels.get(counter));
		impInput.setZ(impInputMiddleSlice);
		// Set LUT temporarily to grays, since its best to see weak signal
		impInput.getProcessor().setLut(ij.process.LUT.createLutFromColor(new java.awt.Color(255, 255, 255)));
		IJ.run("Threshold...");
		double min = impInput.getProcessor().getMinThreshold();
		double max = 999999999;
		IJ.setThreshold(min, max);
		Button_AddThresholdValues.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				// Step 1-
				// Update the values once the OK button is clicked.
				hashMap_AllInfo.get("Threshold Values").put(
						("Channel" + List_Temp_ThresholdedChannels.get(counter) + " Min"),
						Integer.toString((int) impInput.getProcessor().getMinThreshold()));
				hashMap_AllInfo.get("Threshold Values").put(
						("Channel" + List_Temp_ThresholdedChannels.get(counter) + " Max"),
						Integer.toString((int) impInput.getProcessor().getMaxThreshold()));
				// Close the threshold windows and the frame for the first
				// channel
				IJ.selectWindow("Threshold");
				IJ.run("Close");
				Frame_ThresholdNotifier.setVisible(false);
				counter++;
				// Step 2-
				if (counter < List_Temp_ThresholdedChannels.size()) {
					// Add Frame properties. Add Label and OK Button to the
					// Frame.
					Frame_ThresholdNotifier.setVisible(true);
					Label.setText("<html>Adjust the UPPER SLIDER <br>for channel"
							+ List_Temp_ThresholdedChannels.get(counter) + " and press OK</html>");
					// Go to the middle slice of the respective channel
					impInput.setC(List_Temp_ThresholdedChannels.get(counter));
					impInput.setZ(impInputMiddleSlice);
					// Set LUT temporarily to grays, since its best to see weak
					// signal
					impInput.getProcessor()
							.setLut(ij.process.LUT.createLutFromColor(new java.awt.Color(255, 255, 255)));
					// Call new Threshold Window
					IJ.run("Threshold...");
					double min = impInput.getProcessor().getMinThreshold();
					double max = 999999999;
					IJ.setThreshold(min, max);
				} else {
					MainMenu_Update_Structure_Values();
					// Call output image to be displayed
					calculateAndShowOutput_threader();
				}
			}
		});
	}

	
	//ADD ELEMENT WIZARD: ASK USER FOR CONTRAST LEVEL
	public void Input_Contrast()
	// This is called when adding an contrast element. Its job is to get
	// contrast values for all
	// channels that the user has selected ( unless contrast are already set
	// for those channels)
	{
		impInput.show();
		// Close the output window if open //Important, so that correct window
		// is selected if user value input is needed
		closeOutputImage();
		counter = 0;
		// For the first Channel
		final JFrame Frame_ContrastNotifier = new JFrame("Set Contrast");
		Frame_ContrastNotifier.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		Frame_ContrastNotifier.setBounds(0, 0, 250, 150);
		Frame_ContrastNotifier.getContentPane().setLayout(null);
		int x = (int) Rect.getMaxX() - Frame_ContrastNotifier.getWidth();
		int y = (int) Rect.getMaxY() - Frame_ContrastNotifier.getHeight();
		Frame_ContrastNotifier.setLocation(x, (int) (y / 2) + 200);
		Frame_ContrastNotifier.setVisible(true);
		final JLabel Label = new JLabel("<html>Adjust MINIMUM and/or MAXIMUM for channel"
				+ List_Temp_ContrastedChannels.get(counter) + " and press OK</html>");
		Label.setBounds(20, 5, 190, 50);
		Frame_ContrastNotifier.getContentPane().add(Label);
		final JButton Button_AddContrastValue = new JButton("OK");
		Button_AddContrastValue.setBounds(85, 66, 60, 20);
		Frame_ContrastNotifier.getContentPane().add(Button_AddContrastValue);
		// Go to the middle slice of the respective channel
		impInput.setC(List_Temp_ContrastedChannels.get(counter));
		impInput.setZ(impInputMiddleSlice);
		// Just for visualization, so color matches that of output channel. No
		// permanent change
		setTempLutFromHashmap();
		IJ.run("Brightness/Contrast...");
		Window bc = WindowManager.getWindow("B&C");
		Rectangle bcBounds = bc.getBounds();
		// Set B&C just left to the screen
		bc.setBounds((int) Rect.getMaxX() - bcBounds.width, bcBounds.y, bcBounds.width, bcBounds.height);
		Button_AddContrastValue.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				// Step 1-
				// Update the values once the OK button is clicked.
				hashMap_AllInfo.get("Contrast Values").put(
						("Channel" + List_Temp_ContrastedChannels.get(counter) + " Min"),
						Integer.toString((int) impInput.getProcessor().getMin()));
				hashMap_AllInfo.get("Contrast Values").put(
						("Channel" + List_Temp_ContrastedChannels.get(counter) + " Max"),
						Integer.toString((int) impInput.getProcessor().getMax()));

				// Close the Contrast windows and the frame for the first
				// channel
				IJ.selectWindow("B&C");
				IJ.run("Close");
				Frame_ContrastNotifier.setVisible(false);
				counter++;
				// Step 2-
				if (counter < List_Temp_ContrastedChannels.size()) {
					// Add Frame properties. Add Label and OK Button to the
					// Frame.
					Frame_ContrastNotifier.setVisible(true);
					Label.setText("<html>Please set the Contrast value<br>for channel"
							+ List_Temp_ContrastedChannels.get(counter) + " and then press OK</html>");
					// Go to the middle slice of the respective channel
					impInput.setC(List_Temp_ContrastedChannels.get(counter));
					impInput.setZ(impInputMiddleSlice);
					// Just for visualization, so color matches that of output
					// channel. No permanent change
					setTempLutFromHashmap();
					// Call new Contrast Window
					IJ.run("Brightness/Contrast...");
				} else {
					MainMenu_Update_Structure_Values();
					// Call output image to be displayed
					calculateAndShowOutput_threader();
				}
			}
		});
	}

	
	public void MainMenu_Update_Structure_Values()
	// Populating TextFields and Positioning the Adjust (Buttons and TextFields)
	{
		TextField_N_Element_Displayer.setText(Integer.toString(nElements));
		// REMOVING PREVIOUS
		Label_AdjustThreshold_Value.setBounds(0, 0, 0, 0);
		Label_AdjustContrast_Value.setBounds(0, 0, 0, 0);
		for (int Loop_Row = 0; Loop_Row < TotalChannels; Loop_Row++) {
			Button_ThresholdChannel[Loop_Row].setBounds(0, 0, 0, 0);
			TextField_ThresholdMin[Loop_Row].setBounds(0, 0, 0, 0);
			TextField_ThresholdMax[Loop_Row].setBounds(0, 0, 0, 0);
			Button_ContrastChannel[Loop_Row].setBounds(0, 0, 0, 0);
			TextField_ContrastMin[Loop_Row].setBounds(0, 0, 0, 0);
			TextField_ContrastMax[Loop_Row].setBounds(0, 0, 0, 0);
		}

		// ADDING NEW
		// Value
		Label_Adjust_Value.setBounds(75, 312, 120, 20);
		// THRESHOLD
		int SpaceTC = 0;
		boolean ThresholdCalled = false;
		int Int_ThressholdCalled = 0;

		for (int i = 0; i < TotalChannels; i++) {
			String T_Min = hashMap_AllInfo.get("Threshold Values").get("Channel" + (i + 1) + " Min");
			String T_Max = hashMap_AllInfo.get("Threshold Values").get("Channel" + (i + 1) + " Max");
			TextField_ThresholdMin[i].setText(T_Min);
			TextField_ThresholdMax[i].setText(T_Max);

			if (!T_Min.equals("-1") || !T_Max.equals("-1")) 
			{
				ThresholdCalled = true;
				Int_ThressholdCalled = 30;
			}
		}
		// Adding the setBound parameter to the Threshold Value Label for Main
		// Menu Frame
		if (ThresholdCalled == true)
			Label_AdjustThreshold_Value.setBounds(40, 340, 140, 20);

		// Position of Button and Text Field on Main Menu Frame
		for (int i = 0; i < TotalChannels; i++) {
			if (!TextField_ThresholdMin[i].getText().equals("-1")) {
				Button_ThresholdChannel[i].setBounds(40, (360 + SpaceTC * 30), 60, 20);
				TextField_ThresholdMin[i].setBounds(105, (360 + SpaceTC * 30), 40, 20);
				TextField_ThresholdMax[i].setBounds(150, (360 + SpaceTC * 30), 40, 20);
				SpaceTC++;
			}
		}

		// CONTRAST
		boolean ContrastCalled = false;
		// Adding Contrast Value in the TextField_Contrast
		for (int i = 0; i < TotalChannels; i++) {
			String C_Min = hashMap_AllInfo.get("Contrast Values").get("Channel" + (i + 1) + " Min");
			String C_Max = hashMap_AllInfo.get("Contrast Values").get("Channel" + (i + 1) + " Max");
			TextField_ContrastMin[i].setText(C_Min);
			TextField_ContrastMax[i].setText(C_Max);

			if (!C_Min.equals("-1") || !C_Max.equals("-1"))
				ContrastCalled = true;
		}

		// Adding the setBound parameter to the Contrast Value Label for Main
		// Menu Frame
		if (ContrastCalled == true)
			Label_AdjustContrast_Value.setBounds(40, (340 + Int_ThressholdCalled + (SpaceTC * 30)), 140, 20);
		// Position of Button and Text Field on Main Menu Frame
		for (int i = 0; i < TotalChannels; i++) {
			if (!TextField_ContrastMin[i].getText().equals("-1")) {
				Button_ContrastChannel[i].setBounds(40, (360 + Int_ThressholdCalled + (SpaceTC * 30)), 60, 20);
				TextField_ContrastMin[i].setBounds(105, (360 + Int_ThressholdCalled + (SpaceTC * 30)), 40, 20);
				TextField_ContrastMax[i].setBounds(150, (360 + Int_ThressholdCalled + (SpaceTC * 30)), 40, 20);
				SpaceTC++;
			}
		}
		// MainMenu and show output image should be called from here:
		// Otherwise the MainMenu comes out earlier and also the output image
		// doesn't show up as there is no input for it.

		// Display Main Menu Frame
		if (ThresholdCalled == true && ContrastCalled == true)
			Frame_MainMenu.setBounds(0, 0, 250, 430 + (SpaceTC * 30));
		else
			Frame_MainMenu.setBounds(0, 0, 250, 400 + (SpaceTC * 30));
		int x = (int) Rect.getMaxX() - Frame_MainMenu.getWidth();
		int y = (int) Rect.getMaxY() - Frame_MainMenu.getHeight();
		Frame_MainMenu.setLocation(x, y / 2);
		Frame_MainMenu.setVisible(true);
	}
	
	
	
	
	

	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	// MAIN MENU BUTTON METHODS
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////

	public void AddElement() {
		// Get the Max OutputChannel
		for (int i = nElements; i > 0; i--) {
			int tmp = Integer.parseInt(hashMap_AllInfo.get(Integer.toString(i)).get("Output_Channel"));
			if (tmp > n_outputChannels) {
				n_outputChannels = tmp;
			}
		}
		// Updating the number of elements
		nElements++;
		// Setting main menu invisible
		Frame_MainMenu.setVisible(false);
		PageCaller.showPage(PageEnum.FIRST_PAGE);
		//Send required info to the wizard:
		Wizard.nInputImpChannels = TotalChannels;
		Wizard.nOutputImpChannels = n_outputChannels; //MaxOutputChannels
		Wizard.scale_unit = String.valueOf(impInput.getCalibration().getUnit()); //'scale_unit' pixel/inch/cm/um:
		Wizard.color_priority_forWizard = color_priority;
	}

	
	public void DeleteElement() {
		if (nElements > 0) {
			HashMap<String, String> elementToDelete = hashMap_AllInfo.get(Integer.toString(nElements));
			String type = elementToDelete.get("Type");
			// Which channels does this element use?
			String elementToDeleteChannels = elementToDelete.get("All Selected Channels");
			String[] elementToDeleteChannels_String = elementToDeleteChannels
					.substring(0, elementToDeleteChannels.length() - 1).split(",");
			int[] elementToDeleteChannels_Int = Arrays.stream(elementToDeleteChannels_String)
					.mapToInt(Integer::parseInt).toArray();

			// Are these used by any other elements?
			hashMap_AllInfo.remove(Integer.toString(nElements)); 

			nElements--;
			Set<Integer> contrastChnsInUse = new HashSet<Integer>();
			Set<Integer> thresholdChnsInUse = new HashSet<Integer>();
			for (int i = 1; i <= nElements; i++) {
				HashMap<String, String> element = hashMap_AllInfo.get(Integer.toString(i));
				String elementChannels = element.get("All Selected Channels");
				String[] elementChannels_String = elementChannels.substring(0, elementChannels.length() - 1).split(",");
				int[] elementChannels_Int = Arrays.stream(elementChannels_String).mapToInt(Integer::parseInt).toArray();
				if (element.get("Type").equals("Contrast Element"))
					for (int j = 0; j < elementChannels_Int.length; j++)
						contrastChnsInUse.add(elementChannels_Int[j]);
				if (element.get("Type").equals("Threshold Element"))
					for (int j = 0; j < elementChannels_Int.length; j++)
						thresholdChnsInUse.add(elementChannels_Int[j]);
			}
			// Delete user input values (from HashMap_AllInfo) if channels are
			// no longer used:
			for (int i = 0; i < elementToDeleteChannels_Int.length; i++) {
				if (type.equals("Contrast Element")) {
					if (!contrastChnsInUse.contains(elementToDeleteChannels_Int[i])) {
						hashMap_AllInfo.get("Contrast Values")
								.put(("Channel" + elementToDeleteChannels_Int[i] + " Min"), "-1");
						hashMap_AllInfo.get("Contrast Values")
								.put(("Channel" + elementToDeleteChannels_Int[i] + " Max"), "-1");
					}
				}
				if (type.equals("Threshold Element")) {
					if (!thresholdChnsInUse.contains(elementToDeleteChannels_Int[i])) {
						hashMap_AllInfo.get("Threshold Values")
								.put(("Channel" + elementToDeleteChannels_Int[i] + " Min"), "-1");
						hashMap_AllInfo.get("Threshold Values")
								.put(("Channel" + elementToDeleteChannels_Int[i] + " Max"), "-1");
					}
				}
			}
		} else {
			IJ.showMessage("No elements to delete!");
		}
		updateOutputImageHashmapMetadata();
		Frame_MainMenu.setVisible(false);
		MainMenu_Update_Structure_Values();
		// Call output image to be displayed
		calculateAndShowOutput_threader();
	}

	
	@SuppressWarnings("unchecked")
	public void SaveSettings() throws IOException {

		SaveDialog saveDialog = new ij.io.SaveDialog("Save settings as .json", ij.io.OpenDialog.getLastDirectory(),
				"Element Settings", ".json");
		String FolderDestination = saveDialog.getDirectory() + saveDialog.getFileName();

		// Write in file
		JSONObject json = new JSONObject();
		json.putAll(hashMap_AllInfo);
		FileWriter file = new FileWriter(FolderDestination);
		// FileWriter file = new FileWriter("FileDestination");
		try {
			file.write(json.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		file.close();
	}

	
	public boolean LoadSettings() throws IOException, ClassNotFoundException {
		// Close Existing Output Image
		closeOutputImage();

		// Browse
		OpenDialog fileChooser = new ij.io.OpenDialog("Open profile file", ij.io.OpenDialog.getLastDirectory(),
				"*.json");
		String filepath = fileChooser.getPath();
		if (filepath == null || "".equals(filepath)) {
			return false;
		}
		InputStream File = new FileInputStream(filepath);
		InputStreamReader ISR = new InputStreamReader(File, Charset.forName("UTF-8"));
		BufferedReader BR = new BufferedReader(ISR);
		String Line = BR.readLine();
		if (!BR.equals(null)) {
			// Remove previous values (Unsaved)
			hashMap_AllInfo.clear();
			// Add new values
			ObjectMapper mapper = new ObjectMapper();
			hashMap_AllInfo = mapper.readValue(Line, new TypeReference<HashMap<String, HashMap<String, String>>>() {
			});
		}
		BR.close();
		// After successful loading
		if (hashMap_AllInfo.isEmpty())
			IJ.showMessage("File was empty!");
		else {
			// How many elements in this settings?
			Set<String> keys = hashMap_AllInfo.keySet();
			nElements = 0;
			for (String key : keys) {
				for (int i = 0; i < 1000; i++) {
					if (key.equals(Integer.toString(i))) {
						nElements++;
					}
				}
			}
		}
		if (nElements == 0) {
			IJ.error("Profile did not contain any elements");
			return false;
		}
		return true;
	}

	
	@SuppressWarnings("serial")
	public void ColorPriorityList() {
		String InitialList_Colors = color_priority.toString();
		InitialList_Colors = InitialList_Colors.substring(1, InitialList_Colors.length() - 1);
		InitialList_Colors = InitialList_Colors.replaceAll("\\s", "");
		final String[] Array_InitialList_Colors = InitialList_Colors.split(",");

		final JFrame Frame_ColorPriority = new JFrame("Color Priority List");
		//Restore main menu on clicking X of this JFrame
		Frame_ColorPriority.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				  Frame_MainMenu.setVisible(true);
				  }
				});
		Frame_ColorPriority.setBounds(0, 0, 180, 255);
		Frame_ColorPriority.getContentPane().setLayout(null);
		int x = (int) Rect.getMaxX() - Frame_ColorPriority.getWidth();
		int y = (int) Rect.getMaxY() - Frame_ColorPriority.getHeight();
		Frame_ColorPriority.setLocation(x, y / 2);
		Frame_ColorPriority.setVisible(true);

		final JList<String> List_CPT = new JList<String>(color_priority);
		List_CPT.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		List_CPT.setBackground(null);
		List_CPT.setBounds(60, 20, 55, 130);
		Frame_ColorPriority.getContentPane().add(List_CPT);

		JButton Button_Up = new JButton("UP");
		Button_Up.setFont(new Font("", Font.BOLD, 10));
		Button_Up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String temp = color_priority.get(List_CPT.getSelectedIndex());
				int tmpIndex = List_CPT.getSelectedIndex() - 1;
				tmpIndex = Math.max(tmpIndex, 0); //Avoid index error
				color_priority.set(List_CPT.getSelectedIndex(), color_priority.get(tmpIndex));
				color_priority.set(tmpIndex, temp);
				List_CPT.setSelectedIndex(tmpIndex);
			}
		});
		Button_Up.setBounds(5, 160, 75, 20);
		Frame_ColorPriority.getContentPane().add(Button_Up);

		JButton Button_Down = new JButton("DOWN");
		Button_Down.setFont(new Font("", Font.BOLD, 10));
		Button_Down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String temp = color_priority.get(List_CPT.getSelectedIndex());
				int tmpIndex = List_CPT.getSelectedIndex() + 1;
				tmpIndex = Math.min(tmpIndex, 6); //Avoid index error
				color_priority.set(List_CPT.getSelectedIndex(), color_priority.get(tmpIndex));
				color_priority.set(tmpIndex, temp);
				List_CPT.setSelectedIndex(tmpIndex);
			}
		});
		Button_Down.setBounds(85, 160, 75, 20);
		Frame_ColorPriority.getContentPane().add(Button_Down);

		JButton Button_CP_OK = new JButton("OK");
		Button_CP_OK.setFont(new Font("", Font.BOLD, 10));
		Button_CP_OK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Frame_ColorPriority.setVisible(false);
				Frame_MainMenu.setVisible(true);
				hashMap_AllInfo.put("Color Priority List", new HashMap<String, String>() {
					{
						put("StoredAsList", color_priority.toString());
					}
				});

				// Closing Previous Window
				closeOutputImage();
				calculateAndShowOutput_threader();
			}
		});
		Button_CP_OK.setBounds(5, 185, 75, 20);
		Frame_ColorPriority.getContentPane().add(Button_CP_OK);

		JButton Button_CP_Cancel = new JButton("CANCEL");
		Button_CP_Cancel.setFont(new Font("", Font.BOLD, 10));
		Button_CP_Cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				color_priority.removeAllElements();
				for (int i = 0; i < Array_InitialList_Colors.length; i++)
					color_priority.addElement(Array_InitialList_Colors[i]);
				Frame_ColorPriority.setVisible(false);
				Frame_MainMenu.setVisible(true);
			}
		});
		Button_CP_Cancel.setBounds(85, 185, 75, 20);
		Frame_ColorPriority.getContentPane().add(Button_CP_Cancel);
	}

	
	public void BatchProcessing() {
		getHashMapChannelsAndValues();
		batchProcessingDialog = new BatchProcessingDialog();
		addEventListenersForBatchProcessing();
		batchProcessingDialog.setVisible(true);
		//Restore main menu on clicking X of this JFrame
		batchProcessingDialog.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				  Frame_MainMenu.setVisible(true);
				  }
				});
	}

	
	public void deleteAllElements() {
		hashMap_AllInfo.clear();
		initializeHashMapMetaData();
		nElements = 0;
		updateOutputImageHashmapMetadata();
		MainMenu_Update_Structure_Values();
		// Call output image to be displayed
		calculateAndShowOutput_threader();	
	}
	
	
	public void zProjectionOptions() {
		Frame_MainMenu.setVisible(false);

		final JFrame Frame_ZProjection = new JFrame("Z Projection");
		//Restore main menu on clicking X of this JFrame
		Frame_ZProjection.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				  Frame_MainMenu.setVisible(true);
				  }
				});

		Frame_ZProjection.setBounds(0, 0, 220, 460);
		Frame_ZProjection.getContentPane().setLayout(null);
		int x = (int) Rect.getMaxX() - Frame_ZProjection.getWidth();
		int y = (int) Rect.getMaxY() - Frame_ZProjection.getHeight();
		Frame_ZProjection.setLocation(x, y / 2);
		Frame_ZProjection.setVisible(true);

		// Z-Projection Output
		JLabel Label_ZP_Output = new JLabel("Output");
		Label_ZP_Output.setFont(new Font("", Font.BOLD, 12));
		Label_ZP_Output.setBounds(30, 20, 200, 20);
		Frame_ZProjection.getContentPane().add(Label_ZP_Output);

		JRadioButton RadioButton_ZP_StackOnly = new JRadioButton("Stack Only");
		RadioButton_ZP_StackOnly.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_StackOnly.setBounds(30, 40, 200, 20);
		RadioButton_ZP_StackOnly.setActionCommand("stack_only");
		if (hashMap_AllInfo.get("Z Projection Options").get("output").equals("stack_only"))
			RadioButton_ZP_StackOnly.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_StackOnly);

		JRadioButton RadioButton_ZP_ZProjectionOnly = new JRadioButton("Z-Projection Only");
		RadioButton_ZP_ZProjectionOnly.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_ZProjectionOnly.setBounds(30, 60, 200, 20);
		RadioButton_ZP_ZProjectionOnly.setActionCommand("projection_only");
		if (hashMap_AllInfo.get("Z Projection Options").get("output").equals("projection_only"))
			RadioButton_ZP_ZProjectionOnly.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_ZProjectionOnly);

		JRadioButton RadioButton_ZP_StackAndZProjection = new JRadioButton("Stack And Z-Projection");
		RadioButton_ZP_StackAndZProjection.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_StackAndZProjection.setBounds(30, 80, 200, 20);
		RadioButton_ZP_StackAndZProjection.setActionCommand("both");
		if (hashMap_AllInfo.get("Z Projection Options").get("output").equals("both"))
			RadioButton_ZP_StackAndZProjection.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_StackAndZProjection);

		final ButtonGroup BG_Output = new ButtonGroup();
		BG_Output.add(RadioButton_ZP_StackOnly);
		BG_Output.add(RadioButton_ZP_ZProjectionOnly);
		BG_Output.add(RadioButton_ZP_StackAndZProjection);

		// Z-Projection Projection Method
		JLabel Label_ZP_Method = new JLabel("Projection Method");
		Label_ZP_Method.setFont(new Font("", Font.BOLD, 12));
		Label_ZP_Method.setBounds(30, 110, 200, 20);
		Frame_ZProjection.getContentPane().add(Label_ZP_Method);

		JRadioButton RadioButton_ZP_Average = new JRadioButton("Average");
		RadioButton_ZP_Average.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_Average.setBounds(30, 130, 200, 20);
		RadioButton_ZP_Average.setActionCommand("0");
		if (hashMap_AllInfo.get("Z Projection Options").get("method").equals("0"))
			RadioButton_ZP_Average.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_Average);

		JRadioButton RadioButton_ZP_Maximum = new JRadioButton("Maximum");
		RadioButton_ZP_Maximum.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_Maximum.setBounds(30, 150, 200, 20);
		RadioButton_ZP_Maximum.setActionCommand("1");
		if (hashMap_AllInfo.get("Z Projection Options").get("method").equals("1"))
			RadioButton_ZP_Maximum.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_Maximum);

		JRadioButton RadioButton_ZP_Minimum = new JRadioButton("Minimum");
		RadioButton_ZP_Minimum.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_Minimum.setBounds(30, 170, 200, 20);
		RadioButton_ZP_Minimum.setActionCommand("2");
		if (hashMap_AllInfo.get("Z Projection Options").get("method").equals("2"))
			RadioButton_ZP_Minimum.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_Minimum);

		JRadioButton RadioButton_ZP_Sum = new JRadioButton("Sum");
		RadioButton_ZP_Sum.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_Sum.setBounds(30, 190, 200, 20);
		RadioButton_ZP_Sum.setActionCommand("3");
		if (hashMap_AllInfo.get("Z Projection Options").get("method").equals("3"))
			RadioButton_ZP_Sum.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_Sum);

		JRadioButton RadioButton_ZP_StandardDeviation = new JRadioButton("StandardDeviation");
		RadioButton_ZP_StandardDeviation.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_StandardDeviation.setBounds(30, 210, 200, 20);
		RadioButton_ZP_StandardDeviation.setActionCommand("4");
		if (hashMap_AllInfo.get("Z Projection Options").get("method").equals("4"))
			RadioButton_ZP_StandardDeviation.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_StandardDeviation);

		JRadioButton RadioButton_ZP_Median = new JRadioButton("Median");
		RadioButton_ZP_Median.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_Median.setBounds(30, 230, 200, 20);
		RadioButton_ZP_Median.setActionCommand("5");
		if (hashMap_AllInfo.get("Z Projection Options").get("method").equals("5"))
			RadioButton_ZP_Median.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_Median);

		final ButtonGroup BG_Method = new ButtonGroup();
		BG_Method.add(RadioButton_ZP_Average);
		BG_Method.add(RadioButton_ZP_Maximum);
		BG_Method.add(RadioButton_ZP_Minimum);
		BG_Method.add(RadioButton_ZP_Sum);
		BG_Method.add(RadioButton_ZP_StandardDeviation);
		BG_Method.add(RadioButton_ZP_Median);

		// Z-Projection Overlap Priority
		JLabel Label_ZP_zMapMode = new JLabel("Overlap Priority");
		Label_ZP_zMapMode.setFont(new Font("", Font.BOLD, 12));
		Label_ZP_zMapMode.setBounds(30, 260, 200, 20);
		Frame_ZProjection.getContentPane().add(Label_ZP_zMapMode);

		JRadioButton RadioButton_ZP_TopPriority = new JRadioButton("Top Priority");
		RadioButton_ZP_TopPriority.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_TopPriority.setBounds(30, 280, 200, 20);
		RadioButton_ZP_TopPriority.setActionCommand("topPriority");
		if (hashMap_AllInfo.get("Z Projection Options").get("zMapMode").equals("topPriority"))
			RadioButton_ZP_TopPriority.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_TopPriority);

		JRadioButton RadioButton_ZP_BottomPriority = new JRadioButton("Bottom Priority");
		RadioButton_ZP_BottomPriority.setFont(new Font("", Font.BOLD, 11));
		RadioButton_ZP_BottomPriority.setBounds(30, 300, 200, 20);
		RadioButton_ZP_BottomPriority.setActionCommand("botPriority");
		if (hashMap_AllInfo.get("Z Projection Options").get("zMapMode").equals("botPriority"))
			RadioButton_ZP_BottomPriority.setSelected(true);
		Frame_ZProjection.getContentPane().add(RadioButton_ZP_BottomPriority);

		final ButtonGroup BG_zMapMode = new ButtonGroup();
		BG_zMapMode.add(RadioButton_ZP_TopPriority);
		BG_zMapMode.add(RadioButton_ZP_BottomPriority);

		// OK Button
		JButton Button_OK_ZProjection = new JButton("Apply");
		Button_OK_ZProjection.setBounds(30, 380, 75, 20);
		Button_OK_ZProjection.setFont(new Font("", Font.BOLD, 12));
		Frame_ZProjection.getContentPane().add(Button_OK_ZProjection);
		Button_OK_ZProjection.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				Frame_ZProjection.setVisible(false);
				Frame_MainMenu.setVisible(true);

				// HashMap_AllInfo.get("Z Projection
				// Options").put("Enable", "True");
				hashMap_AllInfo.get("Z Projection Options").put("output",
						BG_Output.getSelection().getActionCommand());
				hashMap_AllInfo.get("Z Projection Options").put("method",
						BG_Method.getSelection().getActionCommand());
				hashMap_AllInfo.get("Z Projection Options").put("zMapMode",
						BG_zMapMode.getSelection().getActionCommand());
				calculateAndShowOutput_threader();
			}
		});
		
		// Cancel Button
		JButton Button_Cancel_ZProjection = new JButton("Cancel");
		Button_Cancel_ZProjection.setBounds(110, 380, 75, 20);
		Button_Cancel_ZProjection.setFont(new Font("", Font.BOLD, 12));
		Button_Cancel_ZProjection.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				Frame_ZProjection.setVisible(false);
				Frame_MainMenu.setVisible(true);
			}
		});
		Frame_ZProjection.getContentPane().add(Button_Cancel_ZProjection);
		//Restore main menu on clicking X of this JFrame
		Frame_ZProjection.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				  Frame_MainMenu.setVisible(true);
				  }
				});
	}
	
	
	
		
	
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	// ADD ELEMENT WIZARD METHODS
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////

	private void stopWizard() {
		nElements--;
		Frame_MainMenu.setVisible(true);
		impInput.show();
	}

	
	private void finishBinaryWizard() {
		//Add contrast/threshold values set during preview if selected
		if (BinaryElement.reuseButton.isSelected() && AdvancedOptionsBinary.reuseButton.isSelected()) {
			for (String key : valsSetDuringPreview.keySet()) {
				hashMap_AllInfo.get("Threshold Values").put(key, valsSetDuringPreview.get(key));
			}
		}
		// Put this and other data from wizard into HashMap_AllInfo
		hashMap_AllInfo.put(Integer.toString(nElements), new HashMap<String, String>() {
			{
				put("Type", Wizard.globalMap.get(Keys.wizardVar_Type));
				put("Output_Channel", Wizard.globalMap.get(Keys.wizardVar_Output_Channel));
				put("All Selected Channels", Wizard.globalMap.get(Keys.wizardVar_AND_Channels)
						+ Wizard.globalMap.get(Keys.wizardVar_NOT_Channels));
				put("Color", Wizard.globalMap.get(Keys.wizardVar_Color));
				put("Threshold_methods", Wizard.globalMap.get(Keys.wizardVar_Threshold_methods));
				put("Outline", Wizard.globalMap.get(Keys.wizardVar_Outline));
				put("NOT_Channels", Wizard.globalMap.get(Keys.wizardVar_NOT_Channels));
				put("AND_Channels", Wizard.globalMap.get(Keys.wizardVar_AND_Channels));
				put("rmv_smaller_than", Wizard.globalMap.get(Keys.wizardVar_rmv_smaller_than));
				put("unit", Wizard.globalMap.get(Keys.wizardVar_unit));
				put("rmv_zproj", Wizard.globalMap.get(Keys.wizardVar_rmv_zproj));
				put("rmv_stack", Wizard.globalMap.get(Keys.wizardVar_rmv_stack));
				put("rmv_nonstack", Wizard.globalMap.get(Keys.wizardVar_rmv_nonstack));
				put("Filters_nonstack", Wizard.globalMap.get(Keys.wizardVar_Filters_nonstack));
				put("Filters_stack", Wizard.globalMap.get(Keys.wizardVar_Filters_stack));
				put("Filters_zproj", Wizard.globalMap.get(Keys.wizardVar_Filters_zproj));
				put("Filters", Wizard.globalMap.get(Keys.wizardVar_Filters));
				//3d filters
				put("3dRmvEnable", Wizard.globalMap.get(Keys.wizardVar_3dRmvEnable));
				put("3dRmvMin", Wizard.globalMap.get(Keys.wizardVar_3dRmvMin));
				put("3dRmvMax", Wizard.globalMap.get(Keys.wizardVar_3dRmvMax));
				put("3dRmvExclude", Wizard.globalMap.get(Keys.wizardVar_3dRmvExclude));
				put("3dRmvWhen", Wizard.globalMap.get(Keys.wizardVar_3dRmvWhen));
			}
		});
		updateOutputImageHashmapMetadata();
		getUnadjustedChannels();
		if (List_Temp_ThresholdedChannels.size() > 0) { // If new channels
														// selected
			Input_Threshold();
		} else { // No new channels selected
			Frame_MainMenu.setVisible(true);
			MainMenu_Update_Structure_Values();
			calculateAndShowOutput_threader();	
		}
	}

	
	private void finishGrasyscaleWizard() {
		if (GrayscaleElement.reuseButton.isSelected()) {
			for (String key : valsSetDuringPreview.keySet()) {
				hashMap_AllInfo.get("Contrast Values").put(key, valsSetDuringPreview.get(key));
			}
		}
		// Get a string data-structure which specifies any channel that was used
		// (ACS_Final (AllChannelsSelected_Final))
		String red = Wizard.globalMap.get(Keys.wizardVar_Red).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Red) + ",";
		String green = Wizard.globalMap.get(Keys.wizardVar_Green).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Green) + ",";
		String blue = Wizard.globalMap.get(Keys.wizardVar_Blue).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Blue) + ",";
		String yellow = Wizard.globalMap.get(Keys.wizardVar_Yellow).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Yellow) + ",";
		String cyan = Wizard.globalMap.get(Keys.wizardVar_Cyan).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Cyan) + ",";
		String magenta = Wizard.globalMap.get(Keys.wizardVar_Magenta).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Magenta) + ",";
		String grays = Wizard.globalMap.get(Keys.wizardVar_Grays).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Grays) + ",";
		StringBuilder sb = new StringBuilder(red).append(green).append(blue).append(yellow).append(cyan).append(magenta)
				.append(grays);
		String ACS_Final = sb.toString();
		// Put this and other data from wizard into HashMap_AllInfo
		hashMap_AllInfo.put(Integer.toString(nElements), new HashMap<String, String>() {
			{
				put("Type", Wizard.globalMap.get(Keys.wizardVar_Type));
				put("Output_Channel", Wizard.globalMap.get(Keys.wizardVar_Output_Channel));
				put("All Selected Channels", ACS_Final);
				// put("Color", CSC_Final); //Deprecated
				put("Red", Wizard.globalMap.get(Keys.wizardVar_Red));
				put("Green", Wizard.globalMap.get(Keys.wizardVar_Green));
				put("Blue", Wizard.globalMap.get(Keys.wizardVar_Blue));
				put("Grays", Wizard.globalMap.get(Keys.wizardVar_Grays));
				put("Cyan", Wizard.globalMap.get(Keys.wizardVar_Cyan));
				put("Magenta", Wizard.globalMap.get(Keys.wizardVar_Magenta));
				put("Yellow", Wizard.globalMap.get(Keys.wizardVar_Yellow));
				put("Filters", Wizard.globalMap.get(Keys.wizardVar_Filters));
				put("Filters_nonstack", Wizard.globalMap.get(Keys.wizardVar_Filters_nonstack));
				put("Filters_stack", Wizard.globalMap.get(Keys.wizardVar_Filters_stack));
				put("Filters_zproj", Wizard.globalMap.get(Keys.wizardVar_Filters_zproj));
			}
		});
		updateOutputImageHashmapMetadata();
		getUnadjustedChannels();
		if (List_Temp_ContrastedChannels.size() > 0) { // No new channels
														// selected
			Input_Contrast();
		} else {
			Frame_MainMenu.setVisible(true);
			MainMenu_Update_Structure_Values();
			calculateAndShowOutput_threader();
		}
	}
	
	
	private void getUnadjustedChannels() {
		// This methods goes through all elements, and checks if there are any
		// channels in use for which
		// the user has not set min-max values (either threshold or contrast).
		// Unset channels are added to the List_Temp_ThresholdedChannels and
		// List_Temp_ContrastedChannels
		List_Temp_ThresholdedChannels.removeAll(List_Temp_ThresholdedChannels);
		List_Temp_ContrastedChannels.removeAll(List_Temp_ContrastedChannels);

		for (int i = 0; i < nElements; i++) {
			HashMap<String, String> element = hashMap_AllInfo.get(Integer.toString(i + 1));
			// Get channels used in this element
			String elementChannels = element.get("All Selected Channels");
			String[] elementChannels_String = elementChannels.substring(0, elementChannels.length() - 1).split(",");
			int[] elementChannels_Int = Arrays.stream(elementChannels_String).mapToInt(Integer::parseInt).toArray();

			for (int channel : elementChannels_Int) {
				// Threshold elements:
				if (element.get("Type").equals("Threshold Element")) {
					if (!List_Temp_ThresholdedChannels.contains(channel)) {
						// Ignore channels with automatic threshold method:
						String methods = element.get("Threshold_methods");
						String[] methodsArray = methods.split(",");
						List<Integer> manual_chns = new ArrayList<Integer>();
						for (int ii = 0; ii < methodsArray.length; ii++) {
							if (methodsArray[ii].equals("Manual"))
								manual_chns.add(ii + 1);
						}
						if (manual_chns.contains(channel)) {
							// Check if HashMap_AllInfo contains user input for
							// this
							// channel
							String Min = hashMap_AllInfo.get("Threshold Values").get("Channel" + (channel) + " Min");
							String Max = hashMap_AllInfo.get("Threshold Values").get("Channel" + (channel) + " Max");
							if (Min.equals("-1") || Max.equals("-1")) {
								List_Temp_ThresholdedChannels.add(channel);
							}
						}
					}
				}
				// Contrast elements:
				if (element.get("Type").equals("Contrast Element")) {
					if (!List_Temp_ContrastedChannels.contains(channel)) {
						// Check if HashMap_AllInfo contains user input for this
						// channel
						String Min = hashMap_AllInfo.get("Contrast Values").get("Channel" + (channel) + " Min");
						String Max = hashMap_AllInfo.get("Contrast Values").get("Channel" + (channel) + " Max");
						if (Min.equals("-1") || Max.equals("-1")) {
							// add to list of channels that need user input
							List_Temp_ContrastedChannels.add(channel);
						}
					}
				}

			}
		}
	}

	
	private void addEventListenersForWizard() {
		// Handle wizard stop
		FirstPage.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopWizard();
			}
		});

		BinaryElement.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopWizard();
			}
		});

		AdvancedOptionsBinary.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopWizard();
			}
		});

		GrayscaleElement.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopWizard();
			}
		});

		AdvancedOptionsGrayscale.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopWizard();
			}
		});
		
		//Handle binary preview
		BinaryElement.previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (BinaryElement.validateForm()) {
					setTemporaryPreviewData();
					//CalculateAndShowOutput is called in finishBinaryWizard().
					//CalculateAndShowOutput also restores the original HashMap_AllInfo and nElements
					finishBinaryWizard(); 
				}
			}
		});
		
		//Handle advanced binary preview
		AdvancedOptionsBinary.previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AdvancedOptionsBinary.fillChosenOptions();
				setTemporaryPreviewData();
				//CalculateAndShowOutput is called in finishBinaryWizard().
				//CalculateAndShowOutput also restores the original HashMap_AllInfo and nElements
				finishBinaryWizard(); 
			}
		});
		
		//Handle grayscale preview
		GrayscaleElement.previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (GrayscaleElement.validateForm()) {	
					//checkTooManyGrayscaleWithinElement and checkTooManyGrayscale sets the global variables
					//isManyGrayscaleInOneElement and outChannelsWithMoreThanOneGrayscaleElement
					checkTooManyGrayscale();
					checkTooManyGrayscaleWithinElement();
					//Set the ImageJ macro settings:
					GrayscaleElement.fillChosenOptions();
					setTemporaryPreviewData();
					//CalculateAndShowOutput is called in finishGrasyscaleWizard().
					//CalculateAndShowOutput also restores the original HashMap_AllInfo and nElements
					finishGrasyscaleWizard();
				}
			}
		});
		
		//Handle advanced grayscale preview
		AdvancedOptionsGrayscale.previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AdvancedOptionsGrayscale.fillChosenOptions();
				setTemporaryPreviewData();
				//CalculateAndShowOutput is called in finishGrasyscaleWizard().
				//CalculateAndShowOutput also restores the original HashMap_AllInfo and nElements
				finishGrasyscaleWizard();
			}
		});
		
		// Handle wizard finish
		BinaryElement.finishButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (BinaryElement.validateForm()) {
					finishBinaryWizard();
				}
			}
		});

		AdvancedOptionsBinary.finishButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AdvancedOptionsBinary.fillChosenOptions();
				finishBinaryWizard();
			}
		});

		GrayscaleElement.finishButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (GrayscaleElement.validateForm()) {
					//checkTooManyGrayscaleWithinElement and checkTooManyGrayscale sets the global variables
					//isManyGrayscaleInOneElement and outChannelsWithMoreThanOneGrayscaleElement
					checkTooManyGrayscale();
					checkTooManyGrayscaleWithinElement();
					//Set the ImageJ macro settings:
					GrayscaleElement.fillChosenOptions();
					finishGrasyscaleWizard();
				}
			}
		});

		AdvancedOptionsGrayscale.finishButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AdvancedOptionsGrayscale.fillChosenOptions();
				finishGrasyscaleWizard();
			}
		});
	}
	
	
	private void checkTooManyGrayscale() {
		/*
		 * Warning 1: More than 1 grayscale element per output channel.
		 * Sets the global variable outChannelsWithMoreThanOneGrayscaleElement
		 */
		//First we find info on the hashmap before the current wizard selections.
		//How many output channels:
		n_outputChannels = 0;
		for (int i = 1; i <= nElements-1; i++) {
			if (Integer.parseInt(hashMap_AllInfo.get(Integer.toString(i)).get("Output_Channel")) > n_outputChannels) {
				n_outputChannels = Integer.parseInt(hashMap_AllInfo.get(Integer.toString(i)).get("Output_Channel"));
			}
		}
		//How many grayscale elements per output channel:
		int[] nContrastElemsPrChn = new int[n_outputChannels+2]; //+2 to accomodate for if first element, and create new.
		for (int i = 1; i <= nElements-1; i++) {
			int outChn = Integer.parseInt(hashMap_AllInfo.get(Integer.toString(i)).get("Output_Channel"));
			String type = hashMap_AllInfo.get(Integer.toString(i)).get("Type");
			if (type.equals("Contrast Element")) {
				nContrastElemsPrChn[outChn-1] = nContrastElemsPrChn[outChn-1] + 1;
			}
		}
		//Add the current output channel to the array
		int outChn = Integer.parseInt(Wizard.globalMap.get(Keys.wizardVar_Output_Channel));
		//length == 0 if this is the first element previewed.
		//In that case, there will never be too many grayscale elements per output channel
		nContrastElemsPrChn[outChn-1] = nContrastElemsPrChn[outChn-1] + 1;	
		outChannelsWithMoreThanOneGrayscaleElement.clear();
		for(int i = 0; i < nContrastElemsPrChn.length; i++) {
			if (nContrastElemsPrChn[i] > 1) {
				outChannelsWithMoreThanOneGrayscaleElement.add(i+1);
			}
		}
	}
	
	
	private void checkTooManyGrayscaleWithinElement() {
		/*
		 * Warning 2: More than 1 input channel for a single grayscale element
		 */
		String red = Wizard.globalMap.get(Keys.wizardVar_Red).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Red) + ",";
		String green = Wizard.globalMap.get(Keys.wizardVar_Green).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Green) + ",";
		String blue = Wizard.globalMap.get(Keys.wizardVar_Blue).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Blue) + ",";
		String yellow = Wizard.globalMap.get(Keys.wizardVar_Yellow).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Yellow) + ",";
		String cyan = Wizard.globalMap.get(Keys.wizardVar_Cyan).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Cyan) + ",";
		String magenta = Wizard.globalMap.get(Keys.wizardVar_Magenta).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Magenta) + ",";
		String grays = Wizard.globalMap.get(Keys.wizardVar_Grays).equals("*None*") ? ""
				: Wizard.globalMap.get(Keys.wizardVar_Grays) + ",";
		StringBuilder sb = new StringBuilder(red).append(green).append(blue).append(yellow).append(cyan).append(magenta)
				.append(grays);
		String ACS_Final = sb.toString();
		ACS_Final = ACS_Final.replace(",", "");
		String[] ary = ACS_Final.split("");
		Set<String> mySet = new HashSet<String>(Arrays.asList(ary));
		if (mySet.size() > 1) {
			isManyGrayscaleInOneElement = true;
		} else {
			isManyGrayscaleInOneElement = false;
		}
	}
	
	
	public void setTemporaryPreviewData() {
		//Temporarily set HashMap_AllInfo to the currently selected values by user,
		//send this to CalculateAndShowOutput, and restore the original HashMap_AllInfo after preview,
		//Restoration is done in CalculateAndShowOutput.
		//nElements is also temporarily set to 1, and restored after.
		previewMode = true;
		//Backup HashMap_AllInfo and nElements
		hashMap_AllInfo_Tmp_Backup.clear();
		Set<String> keys = hashMap_AllInfo.keySet();
		for (String key : keys) {
			hashMap_AllInfo_Tmp_Backup.put(key, new HashMap<>(hashMap_AllInfo.get(key)));
		}
		nElements_tmpBackup = nElements;
		//Temporarily clear all elements from HashMap_AllInfo, and set nElements to 1.
		for (int i = 1; i < nElements; i++) {
			hashMap_AllInfo.remove(Integer.toString(i));
		}		
		//HashMap_AllInfo.clear(); //can remove if new preview implementation works
		//initializeHashMapMetaData(); //can remove if new preview implementation works
		nElements = 1; //preview is always only 1 element
		impInput.show();
		impInput.getWindow().toFront();	 
	}

	
	
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	// BATCH PROCESSING METHODS
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////

	private void addEventListenersForBatchProcessing() {
		batchProcessingDialog.useCurrentElementProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				ChannelTable table = batchProcessingDialog.table;
				if (table != null) {
					int dialogResult = JOptionPane.showConfirmDialog(batchProcessingDialog,
							"This will clear all current table data. Are you sure?", "Please confirm",
							JOptionPane.YES_NO_OPTION);
					if (dialogResult == JOptionPane.NO_OPTION) {
						return;
					}
				}

				Set<String> keys = hashMap_AllInfo.keySet();
				nElements = 0;
				for (String key : keys) {
					for (int i = 0; i < 1000; i++) {
						if (key.equals(Integer.toString(i))) {
							nElements++;
						}
					}
				}
				if (nElements == 0) {
					IJ.error("Profile did not contain any elements");
					return;
				}
				printHashMap();
				getHashMapChannelsAndValues();
				if (HashMap_thresholdChns.isEmpty() && HashMap_contrastChns.isEmpty()) {
					IJ.error("No channels found");
					return;
				}
				batchProcessingDialog.loadTableData(null, HashMap_thresholdChns, HashMap_thresholdVals,
						HashMap_contrastChns, HashMap_contrastVals);
				batchProcessingDialog.inputFolderTextField.setText("");
			}
		});

		batchProcessingDialog.loadElementProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				ChannelTable table = batchProcessingDialog.table;
				if (table != null) {
					int dialogResult = JOptionPane.showConfirmDialog(batchProcessingDialog,
							"This will clear all current table data. Are you sure?", "Please confirm",
							JOptionPane.YES_NO_OPTION);
					if (dialogResult == JOptionPane.NO_OPTION) {
						return;
					}
				}

				try {
					if (!LoadSettings()) {
						return;
					}
					if (nElements > 0) {
						printHashMap();
					}
					getHashMapChannelsAndValues();
					batchProcessingDialog.loadTableData(null, HashMap_thresholdChns, HashMap_thresholdVals,
							HashMap_contrastChns, HashMap_contrastVals);
					batchProcessingDialog.inputFolderTextField.setText("");
				} catch (ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		batchProcessingDialog.inputFolderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * Leads to changed lookandfeel:
				DirectoryChooser dirChooser = new DirectoryChooser(
						BatchProcessingConstants.INPUT_FOLDER_FILE_CHOOSER_TITLE);
				DirectoryChooser.setDefaultDirectory(OpenDialog.getLastDirectory());
				String inputDir = dirChooser.getDirectory();
				*/
				
				JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("chose input folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String defaultDir = OpenDialog.getLastDirectory();
                if (defaultDir!=null) {
                    File f = new File(defaultDir);
                    chooser.setSelectedFile(f);
                }
                chooser.setApproveButtonText("Select");
                String inputDir = null;
                if (chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    inputDir = file.getAbsolutePath();
                    OpenDialog.setDefaultDirectory(inputDir);
                }
				

				if (inputDir != null && !"".equals(inputDir)) {

					batchProcessingDialog.inputFolderTextField.setText(inputDir);
					File selectedFolder = new File(inputDir);
					File[] listOfFiles = selectedFolder.listFiles();
					List<String> fileNames = new ArrayList<>();
					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile()) {
							fileNames.add(listOfFiles[i].getName());
						}
					}
					getHashMapChannelsAndValues();
					batchProcessingDialog.loadTableData(fileNames, HashMap_thresholdChns, HashMap_thresholdVals,
							HashMap_contrastChns, HashMap_contrastVals);
				}
			}
		});

		batchProcessingDialog.outputFolderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * Leads to changed lookandfeel:
				DirectoryChooser dirChooser = new DirectoryChooser(
						BatchProcessingConstants.OUTPUT_FOLDER_FILE_CHOOSER_TITLE);
				DirectoryChooser.setDefaultDirectory(OpenDialog.getLastDirectory());
				String outputDir = dirChooser.getDirectory();
				*/
				
				JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("chose output folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String defaultDir = OpenDialog.getLastDirectory();
                if (defaultDir!=null) {
                    File f = new File(defaultDir);
                    chooser.setSelectedFile(f);
                }
                chooser.setApproveButtonText("Select");
                String outputDir = null;
                if (chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    outputDir = file.getAbsolutePath();
                    OpenDialog.setDefaultDirectory(outputDir);
                }
				
				
				batchProcessingDialog.outputFolderTextField.setText(outputDir);
			}
		});

		batchProcessingDialog.removeSelectedFilesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ChannelTable table = batchProcessingDialog.table;
				if (table != null) {
					List<Point> selectedFiles = table.getSelectedFiles();
					DefaultTableModel model = (DefaultTableModel) batchProcessingDialog.table.getModel();
					for (int i = 0; i < selectedFiles.size(); i++) {
						model.removeRow(table.convertRowIndexToModel(selectedFiles.get(i).x - i));
					}
				}
				table.deselectAllCells();
			}
		});

		batchProcessingDialog.filterByFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTable table = batchProcessingDialog.table;
				if (table != null) {
					String filterString = batchProcessingDialog.filterTextField.getText();
					batchProcessingDialog.filterByExtension(filterString);
				}
			}
		});

		batchProcessingDialog.selectAllFilesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ChannelTable table = batchProcessingDialog.table;
				if (table != null) {
					table.selectAllCells();
				}
			}
		});

		batchProcessingDialog.selectNoneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ChannelTable table = batchProcessingDialog.table;
				if (table != null) {
					table.deselectAllCells();
				}
			}
		});

		batchProcessingDialog.manualSetValuesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ChannelTable table = batchProcessingDialog.table;
				if (table == null) {
					IJ.error("Table is empty");
					return;
				}
				List<Point> selected = table.getSelectedChannelCells();
				if (selected.isEmpty()) {
					IJ.error("Please select at least one cell");
					return;
				}
				CellItem item = (CellItem) table.getValueAt(selected.get(0).x, selected.get(0).y);
				MinMaxSetDialog minMaxSetDialog = new MinMaxSetDialog(item.getMin(), item.getMax());
				minMaxSetDialog.setVisible(true);
				minMaxSetDialog.cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						minMaxSetDialog.setVisible(false);
					}
				});
				minMaxSetDialog.saveButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int min = (int) minMaxSetDialog.minSpinner.getValue();
						int max = (int) minMaxSetDialog.maxSpinner.getValue();
						for (Point point : selected) {
							table.setValueAt(new CellItem(min, max), point.x, point.y);
						}
						minMaxSetDialog.setVisible(false);
					}
				});
			}
		});

		batchProcessingDialog.adjustValuesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				ChannelTable table = batchProcessingDialog.table;
				if (table != null) {
					Map<String, List<IndexObject>> selections = table.getSelections();
					if (selections.keySet().isEmpty()) {
						IJ.error("Please select at least one file and channel");
						return;
					}
					List<String> filenames = new ArrayList<>(selections.keySet());
					WindowManager.closeAllWindows();
					impInput = null;
					IJ.resetEscape();
					adjustThresholdAndContrastValues(filenames, selections, 0, 0);
				}
			}
		});

		batchProcessingDialog.saveValueSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (batchProcessingDialog.table == null) {
					IJ.error("No table items found");
					return;
				}
				SaveDialog sd = new SaveDialog("Please select output file", OpenDialog.getLastDirectory(), ".json");
				if (sd.getFileName() == null || "".equals(sd.getFileName())) {
					return;
				}
				String filepath = sd.getDirectory() + sd.getFileName();
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					JTable table = batchProcessingDialog.table;
					List<JSonTableObject> dataToWrite = new ArrayList<>();
					for (int i = 0; i < batchProcessingDialog.table.getRowCount(); i++) {
						List<JSonChannel> chList = new ArrayList<>();
						for (int j = 1; j < batchProcessingDialog.table.getColumnCount(); j++) {
							String headerName = batchProcessingDialog.translateOut(table.getColumnName(j));
							CellItem cellItem = (CellItem) table.getValueAt(i, j);
							JSonChannel ch = new JSonChannel(headerName, cellItem.getMin(), cellItem.getMax());
							chList.add(ch);
						}
						JSonTableObject jto = new JSonTableObject(table.getValueAt(i, 0).toString(), chList);
						dataToWrite.add(jto);
					}
					objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filepath), dataToWrite);
					IJ.error("Exporting successful.");
				} catch (Exception ee) {
					IJ.error("Exporting failed, please try again");
					ee.printStackTrace();
				}
			}
		});

		batchProcessingDialog.loadValueSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getHashMapChannelsAndValues();
				if (HashMap_contrastChns.isEmpty() && HashMap_thresholdChns.isEmpty()) {
					IJ.error("No elements in current profile. Please load an element profile");
					return;
				}
				if ("".equals(batchProcessingDialog.inputFolderTextField.getText())) {
					IJ.error("Please specify input folder first.");
					return;
				}
				ChannelTable table = batchProcessingDialog.table;
				if (table != null) {
					int dialogResult = JOptionPane.showConfirmDialog(batchProcessingDialog,
							"This will clear all current table data. Are you sure?", "Please confirm",
							JOptionPane.YES_NO_OPTION);
					if (dialogResult == JOptionPane.NO_OPTION) {
						return;
					}
				}
				OpenDialog fileChooser = new OpenDialog("Please select file to load", OpenDialog.getLastDirectory(),
						"*.json");

				String filepath = fileChooser.getPath();
				if (filepath == null || "".equals(filepath)) {
					return;
				}
				ObjectMapper om = new ObjectMapper();
				try {
					List<JSonTableObject> data = om.readValue(new File(filepath),
							new TypeReference<List<JSonTableObject>>() {
					});
					// Check if loaded settings has all channels the current
					// element profile uses
					List<Integer> loadedSetting_thresholdChns = new ArrayList<Integer>();
					List<Integer> loadedSetting_contrastChns = new ArrayList<Integer>();
					for (JSonChannel ch : data.get(0).getChannels()) {
						int channelInt = Integer.parseInt(ch.getChannelName().replaceAll("\\D+", ""));
						boolean isThreshold = ch.getChannelName().contains("Threshold");
						if (isThreshold) {
							loadedSetting_thresholdChns.add(channelInt);
						} else {
							loadedSetting_contrastChns.add(channelInt);
						}
					}
					Collections.sort(loadedSetting_contrastChns);
					Collections.sort(loadedSetting_thresholdChns);
					if (!loadedSetting_contrastChns.containsAll(HashMap_contrastChns)
							|| !loadedSetting_thresholdChns.containsAll(HashMap_thresholdChns)) {
						IJ.error("ERROR: Loaded settings does not contain enough min/max channel information." + "\n \n"
								+ "Non-automatic threshold channels in element profile: " + HashMap_thresholdChns + "\n"
								+ "Non-automatic threshold channels in loaded settings: " + loadedSetting_thresholdChns
								+ "\n \n" + "Contrast channels in element profile: " + HashMap_contrastChns + "\n"
								+ "Contrast channels in loaded settings: " + loadedSetting_contrastChns);
						return;
					}
					if (!HashMap_contrastChns.equals(loadedSetting_contrastChns)
							|| !HashMap_thresholdChns.equals(loadedSetting_thresholdChns)) {
						IJ.error(
								"WARING: Loaded settings does not match element profile. But sufficient min/max info is present"
										+ "\n \n" + "Non-automatic threshold channels in element profile: "
										+ HashMap_thresholdChns + "\n"
										+ "Non-automatic threshold channels in loaded settings: "
										+ loadedSetting_thresholdChns + "\n \n"
										+ "Contrast channels in element profile: " + HashMap_contrastChns + "\n"
										+ "Contrast channels in loaded settings: " + loadedSetting_contrastChns);

					}
					loadedSetting_thresholdChns.clear();
					loadedSetting_contrastChns.clear();
					batchProcessingDialog.loadTableData(data);
				} catch (IOException e1) {
					IJ.error("Cannot import the file");
					e1.printStackTrace();
				}
			}
		});

		batchProcessingDialog.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				batchProcessingDialog.dispose();
				Frame_MainMenu.setVisible(true);
			}
		});

		batchProcessingDialog.startBatchingProcessingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prepareLogger();
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						startBatchProcess();
					}
				});
				t.start();
			}
		});
	}

	
	private void prepareLogger() {
		IJ.log("\\Clear");
	}

	
	private void startBatchProcess() {
		displayGrayscaleError1 = false;
		displayGrayscaleError2 = false;
		LocalTime hour = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS);
		if (batchProcessingDialog.table == null) {
			IJ.selectWindow("Log");
			IJ.run("Close");
			IJ.error("No items found in the table");
			return;
		}
		if (batchProcessingDialog.outputFolderTextField.getText() == null
				|| batchProcessingDialog.outputFolderTextField.getText().equals("")) {
			IJ.selectWindow("Log");
			IJ.run("Close");
			IJ.error("Please select output directory");
			return;
		}
		JTable table = batchProcessingDialog.table;
		IJ.resetEscape();

		IJ.log("\\Clear"); // Clears log window
		hour = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS);
		IJ.log(hour + " - " + "Batch processing started " + "\n");
		IJ.log(" ");
		for (int row = 0; row < table.getRowCount(); row++) {
			if (IJ.escapePressed()) {
				hour = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS);
				IJ.log("\n" + hour + " - " + "Espace pressed. Aborting processing...");
				return;
			}
			//Close any open image window (not log)
			int[] openImages = WindowManager.getIDList();
			if (openImages != null) {
				for (int id : openImages) {
					WindowManager.getImage(id).close();
				}
			}
			IJ.freeMemory();
			for (int column = 1; column < table.getColumnCount(); column++) {
				CellItem item = (CellItem) table.getValueAt(row, column);
				updateValuesInHashMap(row, column, item.getMin(), item.getMax());
			}
			String filename = batchProcessingDialog.table.getValueAt(row, 0).toString();
			if (filename == null || "".equals(filename)) {
				hour = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS);
				IJ.log(hour + " - " + "Filename on line " + row + 1 + " not found! Continue forward..." + "\n");
				continue;
			}
			String name = filename.substring(0, filename.lastIndexOf("."));
			String ext = filename.substring(filename.lastIndexOf(".") + 1);
			String inputPath = batchProcessingDialog.inputFolderTextField.getText() + "\\" + filename;
			String path = batchProcessingDialog.outputFolderTextField.getText() + "\\" + name + "_output." + ext;

			IJ.log(hour + " - " + "Opening file: " + filename);
			Opener opener = new Opener();
			impInput = batchProcessingDialog.useBioformatsCheckbox.isSelected() ? Opener.openUsingBioFormats(inputPath)
					: opener.openImage(inputPath);
			if (impInput == null) {
				hour = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS);
				IJ.log(hour + " - " + "ERROR: Could not open file (*try disabling/enabling opeing with bioformats*): "
						+ filename + "\n\n");
				continue;
			}
			impInput.show();
			hour = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS);
			IJ.log(hour + " - " + "Processing file: " + filename);
			try {
				calculateAndShowOutput(); //Calling main calculate method since we are already in a thread
				FileSaver fileSaver = new FileSaver(impFinalOutput);
				boolean bioformatSave = batchProcessingDialog.useBioformatsSaveCheckbox.isSelected();
				if (bioformatSave) {
					IJ.run("Bio-Formats Exporter", "save=[" + path + "] compression=LZW");
				} else {
					fileSaver.saveAsTiff(path);
				}
				hour = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS);
				IJ.log(hour + " - " + "File saved as: " + path);
				IJ.log("");
				impInput.close();
			} catch (Exception e) {
				IJ.log(hour + " - " + "ERROR: " + e.toString());
				IJ.log("");
			}
		}
		hour = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS);
		IJ.log(hour + " - " + "Batch processing completed.");
		IJ.showMessage(hour + " - " + "Batch processing completed.");
		displayGrayscaleError1 = true;
		displayGrayscaleError2 = true;
	}

	
	
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	// OTHER METHODS
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	
	private void setTempLutFromHashmap() {
		// Set color as was selected in wizard during adjustment
		// This is to get visually the same output as is seen during adjusment
		// There might be conflict if a channel is contrasted to several colors
		// but we naively take the last color that matches the channel
		// NB impInput.getProcessor().setLut(color); sets lut temporarily
		// since applylut is not called. This is desirable behaviour
		String input_channel_to_contrast = Integer.toString(impInput.getChannel());
		LUT color = null;
		for (int i = 1; i <= nElements; i++) {
			HashMap<String, String> element = hashMap_AllInfo.get(Integer.toString(i));
			if (element.get("Type").equals("Contrast Element")) {
				// Check all colors
				for (int j = 0; j < 7; j++) {
					// If current element contains a contrast element from color
					// has an output channel
					String current_color_channel = element.get(color_priority.get(j));
					if (input_channel_to_contrast.equals(current_color_channel)) {
						if (color_priority.get(j).equals("Red"))
							color = ij.process.LUT.createLutFromColor(new java.awt.Color(255, 0, 0));
						if (color_priority.get(j).equals("Green"))
							color = ij.process.LUT.createLutFromColor(new java.awt.Color(0, 255, 0));
						if (color_priority.get(j).equals("Blue"))
							color = ij.process.LUT.createLutFromColor(new java.awt.Color(0, 0, 255));
						if (color_priority.get(j).equals("Cyan"))
							color = ij.process.LUT.createLutFromColor(new java.awt.Color(0, 255, 255));
						if (color_priority.get(j).equals("Yellow"))
							color = ij.process.LUT.createLutFromColor(new java.awt.Color(255, 255, 0));
						if (color_priority.get(j).equals("Magenta"))
							color = ij.process.LUT.createLutFromColor(new java.awt.Color(255, 0, 255));
						if (color_priority.get(j).equals("Grays"))
							color = ij.process.LUT.createLutFromColor(new java.awt.Color(255, 255, 255));
						impInput.getProcessor().setLut(color);
					}
				}
			}
		}
	}

	
	private void updateOutputImageHashmapMetadata() {
		// How many output channels:
		n_outputChannels = 0;
		for (int i = 0; i < nElements; i++) {
			if (Integer.parseInt(
					hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel")) > n_outputChannels) {
				n_outputChannels = Integer
						.parseInt(hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel"));
			}
		}
	}

	public void closeOutputImage() {
		// Close the output window if open
		for (Integer id : outputImageIDlist) {
			if (WindowManager.getImage(id) != null) {
				WindowManager.getImage(id).changes = false;
				WindowManager.getImage(id).close();
			}
		}
	}

	private void printHashMap() {
		// Print HashMap_AllInfo. Printing metadata first, then element entries
		IJ.log("\\Clear");
		IJ.log("##########PROFILE LOAD SUCCESS##########");
		Set<String> keys = hashMap_AllInfo.keySet();
		boolean not_integer;
		IJ.log("##########Metadata##########");
		for (String key : keys) {
			not_integer = true;
			for (int i = 0; i < 1000; i++) {
				if (key.equals(Integer.toString(i))) {
					not_integer = false;
				}
			}
			if (not_integer) {
				IJ.log(key + ":");
				IJ.log(hashMap_AllInfo.get(key).toString());
			}
		}
		for (String key : keys) {
			for (int i = 0; i < 1000; i++) {
				if (key.equals(Integer.toString(i))) {
					IJ.log("____________________________________");
					IJ.log("Element#" + key + ":");
					IJ.log("____________________________________");
					HashMap<String, String> map_inner = hashMap_AllInfo.get(key);
					Set<String> keys_inner = map_inner.keySet();
					for (String key_inner : keys_inner) {
						IJ.log(key_inner + ":" + map_inner.get(key_inner));
					}
				}
			}
		}
	}

	private void getHashMapChannelsAndValues() {
		// Used in Batch processing, to get from HashMap_AllInfo which channels
		// are used for threshold and contrast
		// and their values. Used to initialize the channels/values panel.
		HashMap_thresholdChns.clear();
		HashMap_contrastChns.clear();
		HashMap_thresholdVals.clear();
		HashMap_contrastVals.clear();

		// Fill HashMap_thresholdChns and HashMap_contrastChns
		for (int i = 0; i < nElements; i++) {
			HashMap<String, String> element = hashMap_AllInfo.get(Integer.toString(i + 1));
			// Get channels used in this element
			String elementChannels = element.get("All Selected Channels");
			String[] elementChannels_String = elementChannels.substring(0, elementChannels.length() - 1).split(",");
			int[] elementChannels_Int = Arrays.stream(elementChannels_String).mapToInt(Integer::parseInt).toArray();

			for (int channel : elementChannels_Int) {
				// Threshold elements:
				if (element.get("Type").equals("Threshold Element")) {
					// Ignore channels with automatic threshold method:
					String methods = element.get("Threshold_methods");
					String[] methodsArray = methods.split(",");
					List<Integer> manual_chns = new ArrayList<Integer>();
					for (int ii = 0; ii < methodsArray.length; ii++) {
						if (methodsArray[ii].equals("Manual"))
							manual_chns.add(ii + 1);
					}
					if (!HashMap_thresholdChns.contains(channel) && manual_chns.contains(channel))
						HashMap_thresholdChns.add(channel);
				}
				// Contrast elements:
				if (element.get("Type").equals("Contrast Element")) {
					if (!HashMap_contrastChns.contains(channel))
						HashMap_contrastChns.add(channel);
				}
			}
		}
		Collections.sort(HashMap_thresholdChns);
		Collections.sort(HashMap_contrastChns);
		// Fill HashMap_thresholdVals and HashMap_contrastVals
		for (int channel : HashMap_thresholdChns) {
			int min = Integer.parseInt(hashMap_AllInfo.get("Threshold Values").get("Channel" + (channel) + " Min"));
			int max = Integer.parseInt(hashMap_AllInfo.get("Threshold Values").get("Channel" + (channel) + " Max"));
			int[] tmp = { min, max };
			HashMap_thresholdVals.add(tmp);
		}
		for (int channel : HashMap_contrastChns) {
			int min = Integer.parseInt(hashMap_AllInfo.get("Contrast Values").get("Channel" + (channel) + " Min"));
			int max = Integer.parseInt(hashMap_AllInfo.get("Contrast Values").get("Channel" + (channel) + " Max"));
			int[] tmp = { min, max };
			HashMap_contrastVals.add(tmp);
		}
	}


	private void updateValuesInHashMap(int row, int column, int min, int max) {
		JTable table = batchProcessingDialog.table;
		String columnName = table.getColumnName(column);
		String channelNumber = columnName.substring(7, columnName.indexOf(" "));
		String type = batchProcessingDialog.translateOut(columnName.substring(columnName.lastIndexOf(" ") + 1));
		hashMap_AllInfo.get(type + " Values").put(("Channel" + channelNumber + " Min"), String.valueOf(min));
		hashMap_AllInfo.get(type + " Values").put(("Channel" + channelNumber + " Max"), String.valueOf(max));
	}

	private void adjustThresholdAndContrastValues(List<String> filenames, Map<String, List<IndexObject>> selections,
			int row, int column) {
		if (IJ.escapePressed()) {
			IJ.error("Escape pressed. Returning to batch menu");
			WindowManager.closeAllWindows();
			batchProcessingDialog.setVisible(true);
			return;
		}

		if (row == filenames.size()) {
			batchProcessingDialog.setVisible(true);
			IJ.showMessage("Adjust values complete");
			return;
		}
		if (column == selections.get(filenames.get(row)).size()) {
			WindowManager.closeAllWindows();
			adjustThresholdAndContrastValues(filenames, selections, row + 1, 0);
		} else {
			// WindowManager.closeAllWindows(); //Don't close since we're on the
			// same image
			String inputDir = batchProcessingDialog.inputFolderTextField.getText() + "\\";
			String filepath = inputDir + filenames.get(row);
			Opener opener = new Opener();
			IJ.freeMemory();

			// Only open image from disk if it is not already opened:
			boolean alreadyOpen = true;
			// impInput is always null at the first iteration
			if (impInput == null) {
				alreadyOpen = false;
			} else if (!impInput.getTitle().equals(filenames.get(row))) {
				alreadyOpen = false;
			}
			if (!alreadyOpen) {
				impInput = batchProcessingDialog.useBioformatsCheckbox.isSelected()
						? Opener.openUsingBioFormats(filepath) : opener.openImage(filepath);
			}
			if (impInput == null) { // ImageJ couldnt read image
				IJ.error("ERROR: ImageJ could not open file: \n'" + filenames.get(row) + "'\n"
						+ "Try enabling/disabling 'use bioformats for opening files' \n or remove file from list.");
				WindowManager.closeAllWindows();
				batchProcessingDialog.setVisible(true);
				return;
			}
			impInput.show();
			batchProcessingDialog.setVisible(false);
			// If file is RGB:
			if (impInput.getBitDepth() == 24 && impInput.getNChannels() == 1) {
				IJ.showMessage("RGB input detected",
						"Input image is RGB with no channels. Converting to RedGreenBlue to channels.");
				RGBto8bitChannels();
			} else if (impInput.getBitDepth() == 24 && impInput.getNChannels() > 1) {
				IJ.showMessage("RGB input detected", "Input image is RGB with channels. Converting channels to 8-bit.");
				IJ.run(impInput, "8-bit", "");
			}
			// CheckFile();
			String channelName = selections.get(filenames.get(row)).get(column).getChannelName();
			String channelNumber = channelName.substring(7, channelName.indexOf(" "));
			String channelType = channelName.substring(channelName.lastIndexOf(" ") + 1);

			if ("Binary".equals(channelType)) {
				JDialog Frame_ThresholdNotifier = new JDialog(batchProcessingDialog,
						"Set Threshold for channel " + channelNumber);
				Frame_ThresholdNotifier.setBounds(0, 0, 250, 150);
				Frame_ThresholdNotifier.getContentPane().setLayout(null);
				int x = (int) Rect.getMaxX() - Frame_ThresholdNotifier.getWidth();
				int y = (int) Rect.getMaxY() - Frame_ThresholdNotifier.getHeight();
				Frame_ThresholdNotifier.setLocation(x, y / 2);
				final JLabel Label = new JLabel(
						"<html>Adjust the UPPER SLIDER <br>for channel" + channelNumber + " and press OK</html>");
				Label.setBounds(20, 25, 190, 30);
				Frame_ThresholdNotifier.getContentPane().add(Label);
				final JButton Button_AddThresholdValues = new JButton("OK");
				Button_AddThresholdValues.setBounds(85, 66, 60, 20);
				Frame_ThresholdNotifier.getContentPane().add(Button_AddThresholdValues);
				// Go to the middle slice of the respective channel
				impInput.setC(Integer.parseInt(channelNumber));
				impInput.setZ(impInputMiddleSlice);
				// Set LUT temporarily to grays, since its best to see weak
				// signal
				impInput.getProcessor().setLut(ij.process.LUT.createLutFromColor(new java.awt.Color(255, 255, 255)));
				IJ.run("Threshold...");
				double min = impInput.getProcessor().getMinThreshold();
				double max = 999999999;
				IJ.setThreshold(min, max);
				Button_AddThresholdValues.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int min = (int) impInput.getProcessor().getMinThreshold();
						int max = (int) impInput.getProcessor().getMaxThreshold();
						IJ.selectWindow("Threshold");
						IJ.run("Close");
						Frame_ThresholdNotifier.setVisible(false);
						IndexObject io = selections.get(filenames.get(row)).get(column);
						batchProcessingDialog.adjustValues(
								batchProcessingDialog.table.convertRowIndexToView(io.getRowIndex()),
								batchProcessingDialog.table.convertColumnIndexToView(io.getColumnIndex()),
								new CellItem(min, max));
						adjustThresholdAndContrastValues(filenames, selections, row, column + 1);
					}
				});
				Frame_ThresholdNotifier.setVisible(true);
			} else {
				final JDialog Frame_ContrastNotifier = new JDialog(batchProcessingDialog, "Set Contrast");
				Frame_ContrastNotifier.setBounds(0, 0, 250, 150);
				Frame_ContrastNotifier.getContentPane().setLayout(null);
				int x = (int) Rect.getMaxX() - Frame_ContrastNotifier.getWidth();
				int y = (int) Rect.getMaxY() - Frame_ContrastNotifier.getHeight();
				Frame_ContrastNotifier.setLocation(x, (int) (y / 2) + 200);
				final JLabel Label = new JLabel(
						"<html>Adjust MINIMUM and/or MAXIMUM for channel" + channelNumber + " and press OK</html>");
				Label.setBounds(20, 5, 190, 50);
				Frame_ContrastNotifier.getContentPane().add(Label);
				final JButton Button_AddContrastValue = new JButton("OK");
				Button_AddContrastValue.setBounds(85, 66, 60, 20);
				Frame_ContrastNotifier.getContentPane().add(Button_AddContrastValue);
				// Go to the middle slice of the respective channel
				impInput.setC(Integer.parseInt(channelNumber));
				impInput.setZ(impInputMiddleSlice);
				// Just for visualization, so color matches that of output
				// channel. No permanent change
				setTempLutFromHashmap();
				IJ.run("Brightness/Contrast...");
				Window bc = WindowManager.getWindow("B&C");
				Rectangle bcBounds = bc.getBounds();
				// Set B&C just left to the screen
				bc.setBounds((int) Rect.getMaxX() - bcBounds.width, bcBounds.y, bcBounds.width, bcBounds.height);
				Button_AddContrastValue.addMouseListener(new MouseAdapter() {

					public void mouseClicked(MouseEvent arg0) {
						// Step 1- Update the values once the OK button is
						// clicked.
						int min = (int) impInput.getProcessor().getMin();
						int max = (int) impInput.getProcessor().getMax();
						// Close the Contrast windows and the frame for the
						// first channel
						IJ.selectWindow("B&C");
						IJ.run("Close");
						Frame_ContrastNotifier.setVisible(false);
						IndexObject io = selections.get(filenames.get(row)).get(column);
						batchProcessingDialog.adjustValues(
								batchProcessingDialog.table.convertRowIndexToView(io.getRowIndex()),
								batchProcessingDialog.table.convertColumnIndexToView(io.getColumnIndex()),
								new CellItem(min, max));
						adjustThresholdAndContrastValues(filenames, selections, row, column + 1);
					}

				});
				Frame_ContrastNotifier.setVisible(true);
			}
		}
	}

	
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	// IMAGE PROCESSING METHODS:
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////

	
	public void calculateAndShowOutput_threader() {
	Thread t = new Thread(new Runnable() { //Running this in a thread speeds up processing ca 10X
		@Override
		public void run() {
			calculateAndShowOutput();
		}
	});
	t.start();
	}
	
	public void calculateAndShowOutput() {
		
		
		// Close the output window if open
		closeOutputImage();

		// The plugin expects certain preferences, and can fail if not set
		// correctly
		IJ.run("Misc...", "divide=Infinity hide run reverse");
		IJ.run(impInput, "Options...", "iterations=1 count=1 black");
		IJ.run("Colors...", "foreground=white background=black");

		// If file is RGB - force convert:
		if (impInput.getBitDepth() == 24 && impInput.getNChannels() == 1) {
			RGBto8bitChannels();
		} else if (impInput.getBitDepth() == 24 && impInput.getNChannels() > 1) {
			IJ.run(impInput, "8-bit", "");
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Get some info from HashMap_AllInfo
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		Set<String> keys;
		// How many elements in this HashMap_AllInfo?
		keys = hashMap_AllInfo.keySet();
		nElements = 0;
		for (String key : keys) {
			for (int i = 0; i < 1000; i++) {
				if (key.equals(Integer.toString(i))) {
					nElements++;
				}
			}
		}

		// Get the color priority list
		String colorOrderString = hashMap_AllInfo.get("Color Priority List").get("StoredAsList");
		colorOrderString = colorOrderString.substring(1, colorOrderString.length() - 1);
		colorOrderString = colorOrderString.replaceAll("\\s", "");
		colorOrderString = colorOrderString.replace("White", "Grays");
		String[] ss = colorOrderString.split(",");
		color_priority.removeAllElements();
		for (int i = 0; i < ss.length; i++)
			color_priority.addElement(ss[i]);

		zProjectionOutputMode = hashMap_AllInfo.get("Z Projection Options").get("output"); 
		// returns "stack_only", "projection_only", or "both"

		// How many output channels
		n_outputChannels = 0;
		for (int i = 0; i < nElements; i++) {
			if (Integer.parseInt(hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel")) > n_outputChannels) {
				n_outputChannels = Integer.parseInt(hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel"));
			}
		}

		// How many slices in final output image:
		if (zProjectionOutputMode.equals("projection_only")) {
			finalOutputSlices = 1;
		}
		if (zProjectionOutputMode.equals("stack_only")) {
			finalOutputSlices = impInput.getNSlices();
		}
		if (zProjectionOutputMode.equals("both")) {
			finalOutputSlices = impInput.getNSlices();
			finalOutputSlices += 1;
		}
		// This one overrides any previous setting, 'cause non-stack input has no
		// possibility for zProjection:
		if (impInput.getNSlices() == 1) {
			finalOutputSlices = 1;
		}

		// Reset any threshold on the input image, that might have been set
		// during "add element" (just a cosmetic)
		IJ.resetThreshold(impInput);
		IJ.run(impInput, "Select None", ""); // Clear any selection

		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Initialize impFinalOutput by creating an all black impInput, with
		//appropriate number of channels and slices
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		impFinalOutput = IJ.createHyperStack("Output image", impInput.getWidth(), impInput.getHeight(),
				n_outputChannels, finalOutputSlices, impInput.getNFrames(), 24);

		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// MAIN LOOP: Loops through each output channel.
		// For each channel/iteration we make impAllContrastElements,
		//impAllThresholdElements, and impZProjection, and paste them onto appropriate impFinalOutput channel
		//////////////////////////////////////////////////////////////////////////////////////////////////////// 

		//loop through as many output channels as we have (n_outputChannels)
		for (int curOutChnIterator = 1; curOutChnIterator <= n_outputChannels; curOutChnIterator++) {
			// Make stack, or single image if input is non-stack:
			if (zProjectionOutputMode.equals("both") || zProjectionOutputMode.equals("stack_only")
					|| impInput.getNSlices() == 1) {
				includeOutlineElements = true;
				doOutlineConversion = true;
				String calledFrom = "stack";

				// Make current output channel contrast elements:
				// Returns black image if no valid elements in hashmap. Returns RGB image in all cases
				impAllContrastElements = makeAllContrastElements(curOutChnIterator, calledFrom); 
				impFinalOutput = insertStackInHyperstack(impFinalOutput, impAllContrastElements, curOutChnIterator);
				impAllContrastElements.close();

				// Make current output channel threshold elements:
				// Returns black image if no valid elements in hashmap. Returns RGB image in all cases
				impAllThresholdElements = makeAllThresholdElements(curOutChnIterator, includeOutlineElements,
						doOutlineConversion, calledFrom); 
				impFinalOutput = insertStackInHyperstack(impFinalOutput, impAllThresholdElements, curOutChnIterator);
				impAllThresholdElements.close();
			}

			// Make current output channel Z-projection:
			// Returns black image if no valid elements in hashmap. Returns RGB image in all cases
			if ((zProjectionOutputMode.equals("both") || zProjectionOutputMode.equals("projection_only"))
					&& impInput.getNSlices() != 1) {
				impZProjection = makeZProjection(curOutChnIterator); 
				impFinalOutput = insertStackInHyperstack(impFinalOutput, impZProjection, curOutChnIterator);
				impZProjection.close();
			}
			
			//3D filter doesnt work if this imp is closed too soon! Probably some processor object in the imp is used for the main image. 
			//Thats why we close them here instead. If fixed could improve memory consumption.
			/* NOW FIXED. CAN REMOVE BELOW.
			for (String title : WindowManager.getImageTitles()) { 			
				if (title.contains("Objects map of")) {
					WindowManager.getImage(title).close();
				}
			}
			*/
				
		
		}
		impFinalOutput.copyScale(impInput); //Copy metadata from input image
		impFinalOutput.show();
		impInput.setC(1); // To reset display of impInput
		outputImageIDlist.add(impFinalOutput.getID());
		impFinalOutput.changes = false;
		
		//At this point, the whole output image has been generated (impFinalOutput)
		//Below is stuff related to preview and helper methods for image processing.
		
		
		//Stuff related to "re-use preview values".
		//Set metadata for contrast/threshold values to -1 if they are not used by any elements,
		//so that main menu is drawn correctly (the adjust values buttons)
		//Convert All Selected Channels from string to array
		String StrAll = "";
		if (nElements != 0) {
			StrAll = hashMap_AllInfo.get(Integer.toString(nElements)).get("All Selected Channels"); // "[1,2]"
		}
		if (!StrAll.equals("")) {
			StrAll = StrAll.substring(0, StrAll.length() - 1); // "1,2"
			String[] ArrayAll = StrAll.split(","); // "1" "2"
			int[] Array_ChannelAll = new int[ArrayAll.length];
			for (int x = 0; x < ArrayAll.length; x++) {
				Array_ChannelAll[x] = Integer.parseInt(ArrayAll[x]); // 1 2
			}
			//set threshold values in HashMap_AllInfo to -1 if they are in valsSetDuringPreview, 
			//but not if they are used in the current element.
			for (String key : valsSetDuringPreview.keySet()) {
				int channelOfPreviewVal = Integer.parseInt(key.replaceAll("\\D+",""));
				boolean foundMatch = false;
				for (int i : Array_ChannelAll) {
					if (i == channelOfPreviewVal) {
						foundMatch = true;
					}
				}
				if (!foundMatch) {
					if (hashMap_AllInfo.get(Integer.toString(nElements)).get("Type").equals("Threshold Element")) {
						hashMap_AllInfo.get("Threshold Values").put(key, "-1");
					} else if (hashMap_AllInfo.get(Integer.toString(nElements)).get("Type").equals("Contrast Element")) {
						hashMap_AllInfo.get("Contrast Values").put(key, "-1");
					}
		}}}
		valsSetDuringPreview.clear();
		
		if (previewMode) {
			//Find which values were set during preview mode, by comparing to the backup
			for (String key : hashMap_AllInfo.get("Threshold Values").keySet()) {
				String setBeforeWizard = hashMap_AllInfo_Tmp_Backup.get("Threshold Values").get(key);
				String setDuringWizard = hashMap_AllInfo.get("Threshold Values").get(key);
				if (!setBeforeWizard.equals(setDuringWizard)) {
					valsSetDuringPreview.put(key, setDuringWizard);
				}
			}
			for (String key : hashMap_AllInfo.get("Contrast Values").keySet()) {
				String setBeforeWizard = hashMap_AllInfo_Tmp_Backup.get("Contrast Values").get(key);
				String setDuringWizard = hashMap_AllInfo.get("Contrast Values").get(key);
				if (!setBeforeWizard.equals(setDuringWizard)) {
					valsSetDuringPreview.put(key, setDuringWizard);
				}
			}			
			//Restore nElements and HashMap_AllInfo, which contain the non-preview data
			nElements = nElements_tmpBackup;
			hashMap_AllInfo.clear();
			Set<String> keysBackup = hashMap_AllInfo_Tmp_Backup.keySet();
			for (String key : keysBackup) {
				hashMap_AllInfo.put(key, new HashMap<>(hashMap_AllInfo_Tmp_Backup.get(key)));
			}
			previewMode = false;
			//after preview user is in add element wizard, and main menu shouldn't be shown
			Frame_MainMenu.setVisible(false);
			//Hide the impInput, since it comes on top of the preview.
			//Also avoids that the user exits the impInput
			impInput.hide();
			impFinalOutput.show();
			impFinalOutput.getWindow().toFront();
			impFinalOutput.setTitle("PREVIEW CURRENT ELEMENT");
			
		} else {
			impInput.show();
			impFinalOutput.getWindow().toFront();
			MainMenu_Update_Structure_Values();
		}
		String newline = System.getProperty("line.separator");
		if (isManyGrayscaleInOneElement && displayGrayscaleError2) {
			String msg = "WARNING: Max 1 input channel of grayscale elements is recommended for colocalization analysis." + newline + newline + "Consider deleting the problematic element with \"Delete Last Element\" (unless this is preview mode)" + newline + newline + "(Press cancel to stop showing this error message in the future.)";
			displayGrayscaleError2 = IJ.showMessageWithCancel("Several grayscale input channels in single element detected", msg);
		}
		isManyGrayscaleInOneElement = false;
		if (outChannelsWithMoreThanOneGrayscaleElement.size() > 0 && displayGrayscaleError1) {
			String msg = "WARNING: Max 1 grayscale element per ouput channel is recommended for colocalization analysis." + newline + "The following output channels have more than 1 grayscale elements in them:" + newline + newline + outChannelsWithMoreThanOneGrayscaleElement.toString() + newline + newline + "Consider deleting the problematic element with \"Delete Last Element\" (unless this is preview mode)" + newline + newline + "(Press cancel to stop showing this error message in the future.)";
			displayGrayscaleError1 = IJ.showMessageWithCancel("Several grayscale elements in single output channel detected", msg);
		}
		outChannelsWithMoreThanOneGrayscaleElement.clear();
		
		
	}
	
	// End of CalculateAndShowOutput
	// End of CalculateAndShowOutput
	// End of CalculateAndShowOutput
	// Below are methods used in CalculateAndShowOutput and some other utility methods

	
	
	public ImagePlus makeAllContrastElements(int curOutChnIterator, String calledFrom) {
		// Figure out which elements should be processed:
		List<Integer> elementsToBeProcessed = new ArrayList<Integer>();
		for (int i = 1; i <= nElements; i++)
			if (hashMap_AllInfo.get(Integer.toString(i)).get("Type").equals("Contrast Element"))
				if (hashMap_AllInfo.get(Integer.toString(i)).get("Output_Channel")
						.equals(Integer.toString(curOutChnIterator)))
					elementsToBeProcessed.add(i);

		// Initialize impAllContrastElements with black image:
		impAllContrastElements = IJ.createHyperStack("impAllContrastElements", impInput.getWidth(),
				impInput.getHeight(), 1, impInput.getNSlices(), impInput.getNFrames(), 24);
		if (elementsToBeProcessed.isEmpty())
			return impAllContrastElements;

		// Process element by element
		for (int i : elementsToBeProcessed) {
			// Initialize impOneContrastElement with black image:
			impOneContrastElement = IJ.createHyperStack("impOneContrastElement", impInput.getWidth(),
					impInput.getHeight(), 1, impInput.getNSlices(), impInput.getNFrames(), 24);
			// Add by RGB rules any channels with any color in this element (to
			// impOneContrastElement)
			if (!hashMap_AllInfo.get(Integer.toString(i)).get("Red").equals("*None*"))
				impOneContrastElement = makeContrastOneChannel(impOneContrastElement, i, "Red");
			if (!hashMap_AllInfo.get(Integer.toString(i)).get("Green").equals("*None*"))
				impOneContrastElement = makeContrastOneChannel(impOneContrastElement, i, "Green");
			if (!hashMap_AllInfo.get(Integer.toString(i)).get("Blue").equals("*None*"))
				impOneContrastElement = makeContrastOneChannel(impOneContrastElement, i, "Blue");
			if (!hashMap_AllInfo.get(Integer.toString(i)).get("Yellow").equals("*None*"))
				impOneContrastElement = makeContrastOneChannel(impOneContrastElement, i, "Yellow");
			if (!hashMap_AllInfo.get(Integer.toString(i)).get("Cyan").equals("*None*"))
				impOneContrastElement = makeContrastOneChannel(impOneContrastElement, i, "Cyan");
			if (!hashMap_AllInfo.get(Integer.toString(i)).get("Magenta").equals("*None*"))
				impOneContrastElement = makeContrastOneChannel(impOneContrastElement, i, "Magenta");
			if (!hashMap_AllInfo.get(Integer.toString(i)).get("Grays").equals("*None*"))
				impOneContrastElement = makeContrastOneChannel(impOneContrastElement, i, "Grays");

			impOneContrastElement.setCalibration(impInput.getCalibration());
			// Apply macros/filter
			boolean calledFromZ = calledFrom.equals("zProjection") ? true : false;
			String macroString = hashMap_AllInfo.get(Integer.toString(i)).get("Filters");
			// We handle stacks ourselves
			macroString = macroString.replace("stack", "").replace("Stack", "");
			boolean doFilterZproj = hashMap_AllInfo.get(Integer.toString(i)).get("Filters_zproj").equals("1") ? true
					: false;
			// These two are basically the same (nonstack is if impInput is not a stack)
			boolean doFilterNonstack = hashMap_AllInfo.get(Integer.toString(i)).get("Filters_nonstack").equals("1")
					? true : false;
			boolean doFilterStack = hashMap_AllInfo.get(Integer.toString(i)).get("Filters_stack").equals("1") ? true
					: false;

			boolean doFilter = false;
			if (!calledFromZ && (doFilterStack || doFilterNonstack))
				if (!macroString.equals(""))
					doFilter = true;
			if (calledFromZ && doFilterZproj)
				if (!macroString.equals(""))
					doFilter = true;
			if (doFilter)
				for (int ii = 1; ii <= impOneContrastElement.getNSlices(); ii++)
					applyMacroToSliceInImp(impOneContrastElement, macroString, ii);

			// After filters has been applied, add to all elements:
			impAllContrastElements = ic.run("Add create stack", impAllContrastElements, impOneContrastElement);
		}
		impOneContrastElement.close();
		return impAllContrastElements;
	}

	
	
	// makeContrastOneChannel (Not the same as one element. One element can have many channels)
	public ImagePlus makeContrastOneChannel(ImagePlus impOneContrastElement, int i, String Color) {
		int ChannelNumber, Channel_Min, Channel_Max;
		ImagePlus SingleCChannel = null;
		ChannelNumber = Integer.parseInt(hashMap_AllInfo.get(Integer.toString(i)).get(Color));
		Channel_Min = Integer.parseInt(hashMap_AllInfo.get("Contrast Values").get("Channel" + ChannelNumber + " Min"));
		Channel_Max = Integer.parseInt(hashMap_AllInfo.get("Contrast Values").get("Channel" + ChannelNumber + " Max"));
		SingleCChannel = new Duplicator().run(impInput, ChannelNumber, ChannelNumber, 1, impInput.getNSlices(), 1,
				impInput.getNFrames());
		IJ.setMinAndMax(SingleCChannel, Channel_Min, Channel_Max);
		// ImageJ throws an error if values are 0-255 for 8 and 24 bit images.
		// This means that there was no change to the contrast anyway
		int min = (int) SingleCChannel.getDisplayRangeMin();
		int max = (int) SingleCChannel.getDisplayRangeMax();
		int depth = SingleCChannel.getBitDepth();
		if (!((depth == 8 || depth == 24) && min == 0 && max == 255)) {
			IJ.run(SingleCChannel, "Apply LUT", "stack");
		}
		IJ.run(SingleCChannel, Color, "");
		IJ.run(SingleCChannel, "RGB Color", "");
		impOneContrastElement = ic.run("Add create stack", impOneContrastElement, SingleCChannel);
		SingleCChannel.close();
		return impOneContrastElement;
	}

	

	// makeAllThresholdElements
	// Returns black RGB image if no valid elements in hashmap. 
	// Returns RGB image in all cases
	// calledFrom can be "stack" or "zProjection"
	public ImagePlus makeAllThresholdElements(int curOutChnIterator, boolean includeOutlineElements,
			boolean doOutlineConversion, String calledFrom) {
		
		// Initialize impAllThresholdElements with black image:
		impAllThresholdElements = IJ.createHyperStack("impAllThresholdElements", impInput.getWidth(),
				impInput.getHeight(), 1, impInput.getNSlices(), impInput.getNFrames(), 24);
		
		// Loop_Color makes sure we process elements according to the specified
		// color-priority order (threshold elements with highest priority first).
		for (int Loop_Color = 6; Loop_Color >= 0; Loop_Color--) {
			for (int i = 0; i < nElements; i++) { // loop through all elements
				if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Type").equals("Threshold Element")) {
				if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Color").equals(color_priority.get(Loop_Color))) { 
				if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel").equals(Integer.toString(curOutChnIterator))) { 
	
				// If this line is reached, we have found a valid element
				if (includeOutlineElements) { // Do any threshold element
					impOneThresholdElement = makeOneThresholdElement(i, doOutlineConversion, calledFrom);
				} else { // Restrict to non-outline elements
					if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Outline").equals("OutlineNotSelected")) {
						impOneThresholdElement = makeOneThresholdElement(i, doOutlineConversion, calledFrom);
					}
				}
				// Paste impOneThresholdElement on top of impAllThresholdElements:
				impAllThresholdElements = ic.run("Transparent-zero create stack", impAllThresholdElements, impOneThresholdElement); 
				impOneThresholdElement.close();
					}
				}
			}
		}
	}
	return impAllThresholdElements;
	}


	
	// makeOneThresholdElement 
	// Returns black RGB image if no valid elements in hashmap. Returns RGB image in all cases
	// calledFrom can be "stack" or "zProjection"
	public ImagePlus makeOneThresholdElement(int i, boolean doOutlineConversion, String calledFrom) { 
		// Initialize impOneContrastElement with WHITE image (AND operation
		// later will remove white which is not also in first AND channel):
		impOneThresholdElement = IJ.createHyperStack("impOneThresholdElement", impInput.getWidth(),
				impInput.getHeight(), 1, impInput.getNSlices(), impInput.getNFrames(), 8);
		IJ.run(impOneThresholdElement, "Invert", "stack"); // Make white, see above comment.		
		// Parsing string to array
		String StrAnd = hashMap_AllInfo.get(Integer.toString(i + 1)).get("AND_Channels"); // "[1,2]"
		if (!StrAnd.equals("")) {
			StrAnd = StrAnd.substring(0, StrAnd.length() - 1); // "1,2"
			String[] ArrayAnd = StrAnd.split(","); // "1" "2"
			int[] Array_ChannelAnd = new int[ArrayAnd.length];
			for (int x = 0; x < ArrayAnd.length; x++) {
				Array_ChannelAnd[x] = Integer.parseInt(ArrayAnd[x]); // 1 2
			}
			// For all AND channels:
			for (int j = 0; j < ArrayAnd.length; j++) { 
				// Process one channel(threshold) here:
				impSingleThresholdChannel = new Duplicator().run(impInput, Array_ChannelAnd[j], Array_ChannelAnd[j], 1,
						impInput.getNSlices(), 1, impInput.getNFrames());
				// Check for manual or automatic thresholding:
				String methods = hashMap_AllInfo.get(Integer.toString(i + 1)).get("Threshold_methods");
				String[] methodsArray = methods.split(",");
				String method = methodsArray[Array_ChannelAnd[j] - 1];
				if (method.equals("Manual")) {
					IJ.setThreshold(impSingleThresholdChannel,
							Integer.parseInt(hashMap_AllInfo.get("Threshold Values")
									.get("Channel" + Array_ChannelAnd[j] + " Min")),
							Integer.parseInt(hashMap_AllInfo.get("Threshold Values")
									.get("Channel" + Array_ChannelAnd[j] + " Max")));
				} else {
					boolean darkBackground = true;
					impSingleThresholdChannel.getProcessor().setAutoThreshold(method, darkBackground, 0);
				}
				IJ.run(impSingleThresholdChannel, "Make Binary", "stack black");
				impOneThresholdElement = ic.run("AND create stack", impOneThresholdElement, impSingleThresholdChannel);
			}
		}
		// For all NOT Channels
		String StrNot = hashMap_AllInfo.get(Integer.toString(i + 1)).get("NOT_Channels"); // "[1,2]"
		if (!StrNot.equals("")) {
			StrNot = StrNot.substring(0, StrNot.length() - 1); // "1,2"
			String[] ArrayNot = StrNot.split(","); // "1" "2"
			int[] Array_ChannelNot = new int[ArrayNot.length];
			for (int x = 0; x < ArrayNot.length; x++) {
				Array_ChannelNot[x] = Integer.parseInt(ArrayNot[x]); // 1 2
			}
			for (int j = 0; j < ArrayNot.length; j++) {
				// Process One Channel(Thresholding) here:
				impSingleThresholdChannel = new Duplicator().run(impInput, Array_ChannelNot[j], Array_ChannelNot[j], 1,
						impInput.getNSlices(), 1, impInput.getNFrames());
				// Check of manual or automatic thresholding:
				String methods = hashMap_AllInfo.get(Integer.toString(i + 1)).get("Threshold_methods");
				String[] methodsArray = methods.split(",");
				String method = methodsArray[Array_ChannelNot[j] - 1];
				if (method.equals("Manual")) {
					IJ.setThreshold(impSingleThresholdChannel,
							Integer.parseInt(hashMap_AllInfo.get("Threshold Values")
									.get("Channel" + Array_ChannelNot[j] + " Min")),
							Integer.parseInt(hashMap_AllInfo.get("Threshold Values")
									.get("Channel" + Array_ChannelNot[j] + " Max")));
				} else {
					boolean darkBackground = true;
					impSingleThresholdChannel.getProcessor().setAutoThreshold(method, darkBackground, 0);
				}
				IJ.run(impSingleThresholdChannel, "Make Binary", "stack black");
				impOneThresholdElement = ic.run("Subtract create stack", impOneThresholdElement, impSingleThresholdChannel);
			}
		}
		impSingleThresholdChannel.close();
		
		
		//Potential 3d-filter step (depends on when user wants it to be done)
		impOneThresholdElement.setCalibration(impInput.getCalibration());
		if (hashMap_AllInfo.get(Integer.toString(i + 1)).containsKey("3dRmvEnable")) { //Older versions doesnt have 3d filter info in hashMap
			if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("3dRmvWhen").equals("0") ) {
				impOneThresholdElement = apply3dFilter(impOneThresholdElement, i);
			}
		}
	
		// Apply macros/filter
		impOneThresholdElement.setCalibration(impInput.getCalibration());
		boolean calledFromZ = calledFrom.equals("zProjection") ? true : false;
		String macroString = hashMap_AllInfo.get(Integer.toString(i + 1)).get("Filters");
		// We handle slices in stacks ourselves
		macroString = macroString.replace("stack", "").replace("Stack", "");
		boolean doFilterZproj = hashMap_AllInfo.get(Integer.toString(i + 1)).get("Filters_zproj").equals("1") ? true : false;
		// These two are basically the same (nonstack is if impInput is not a stack)
		boolean doFilterNonstack = hashMap_AllInfo.get(Integer.toString(i + 1)).get("Filters_nonstack").equals("1") ? true : false;
		boolean doFilterStack = hashMap_AllInfo.get(Integer.toString(i + 1)).get("Filters_stack").equals("1") ? true : false;
		boolean doFilter = false;
		if (!calledFromZ && (doFilterStack || doFilterNonstack))
			if (!macroString.equals(""))
				doFilter = true;
		if (calledFromZ && doFilterZproj)
			if (!macroString.equals(""))
				doFilter = true;
		if (doFilter)
			for (int ii = 1; ii <= impOneThresholdElement.getNSlices(); ii++)
				applyMacroToSliceInImp(impOneThresholdElement, macroString, ii);
		
		
		//Potential 3d-filter step (depends on when user wants it to be done)
		impOneThresholdElement.setCalibration(impInput.getCalibration());
		if (hashMap_AllInfo.get(Integer.toString(i + 1)).containsKey("3dRmvEnable")) { //Older versions doesnt have 3d filter info in hashMap
			if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("3dRmvWhen").equals("1") ) {
				impOneThresholdElement = apply3dFilter(impOneThresholdElement, i);
			}
		}
		
		// Remove any particles below size set by user, if enabled:
		impOneThresholdElement.setCalibration(impInput.getCalibration());
		impOneThresholdElement.setTitle("impOneThresholdElement");
		boolean rmv_stack_bool = hashMap_AllInfo.get(Integer.toString(i + 1)).get("rmv_stack").equals("1");
		boolean rmv_nonstack_bool = hashMap_AllInfo.get(Integer.toString(i + 1)).get("rmv_nonstack").equals("1");
		boolean rmv_zproj_bool = hashMap_AllInfo.get(Integer.toString(i + 1)).get("rmv_zproj").equals("1");
		boolean doRmvParticles = false;
		// Only these two conditions enables removal of small particles
		if (calledFromZ == false && (rmv_nonstack_bool || rmv_stack_bool))
			doRmvParticles = true;
		if (calledFromZ && rmv_zproj_bool)
			doRmvParticles = true;

		if (doRmvParticles) {
			String tmpRmvValue = hashMap_AllInfo.get(Integer.toString(i + 1)).get("rmv_smaller_than");
			String tmpRmvUnit = hashMap_AllInfo.get(Integer.toString(i + 1)).get("unit");
			if (!impInput.getCalibration().scaled()) {
				IJ.run(impOneThresholdElement, "Analyze Particles...",
						"size=0-" + tmpRmvValue + " pixel show=Masks stack");
			} else {
				if (tmpRmvUnit.toLowerCase().equals("pixel") || tmpRmvUnit.toLowerCase().equals("pixels") ) {
					IJ.run(impOneThresholdElement, "Analyze Particles...",
							"size=0-" + tmpRmvValue + " pixel show=Masks stack");
				} else {
					IJ.run(impOneThresholdElement, "Analyze Particles...",
							"size=0-" + tmpRmvValue + " show=Masks stack");
				}
			}
			ImagePlus impOneThresholdElement_mask = WindowManager.getImage("Mask of impOneThresholdElement");	
			impOneThresholdElement = ic.run("Subtract create stack", impOneThresholdElement,
					impOneThresholdElement_mask); // Deleting small particles
			impOneThresholdElement_mask.close();
		}


		
		//Potential 3d-filter step (depends on when user wants it to be done)
		impOneThresholdElement.setCalibration(impInput.getCalibration());
		if (hashMap_AllInfo.get(Integer.toString(i + 1)).containsKey("3dRmvEnable")) { //Older versions doesnt have 3d filter info in hashMap
			if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("3dRmvWhen").equals("2") ) {
				impOneThresholdElement = apply3dFilter(impOneThresholdElement, i);
			}
		}
		

		// Do outline conversion
		if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Outline").equals("OutlineThick") && doOutlineConversion)
			IJ.run(impOneThresholdElement, "Find Edges", "stack");
		if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Outline").equals("OutlineThin") && doOutlineConversion)
			IJ.run(impOneThresholdElement, "Outline", "stack");

		// Color the Image
		IJ.run(impOneThresholdElement, hashMap_AllInfo.get(Integer.toString(i + 1)).get("Color"), "");
		IJ.run(impOneThresholdElement, "RGB Color", "");
		return impOneThresholdElement;
	}


	private ImagePlus apply3dFilter(ImagePlus inputImp, int i) {
		inputImp.changes = false;
		if (!hashMap_AllInfo.get(Integer.toString(i + 1)).containsKey("3dRmvEnable")) { //Older versions doesnt have 3d filer info in hashMap
			return inputImp;
		}
		boolean do3dFilter = hashMap_AllInfo.get(Integer.toString(i + 1)).get("3dRmvEnable").equals("1") ? true : false;
		if (do3dFilter) {
			/*
			if (!ij.Menus.getCommands().toString().toLowerCase().contains("3d objects counter")) {
				IJ.log("ERROR! Element number " + Integer.toString(i+1) + " contains has enabled a 3D-filter. \nThis requires the'3D Objects Counter plugin'\n Please install the plugin, or use the FIJI version of ImageJ.\n NB! The element was prossesed without the 3D filter.");
				return inputImp;
			}
			*/
			
			String min = hashMap_AllInfo.get(Integer.toString(i + 1)).get("3dRmvMin");
			String max = hashMap_AllInfo.get(Integer.toString(i + 1)).get("3dRmvMax");
			boolean exclude = hashMap_AllInfo.get(Integer.toString(i + 1)).get("3dRmvExclude").equals("1") ? true : false;
			
			/*
			String cmd = "threshold=1 slice=1 min.=" + min + " max.=" + max; //threshold can be anywhere from 2 to 254. Image is already binary. Slice is for display and not important.
			if (exclude) {
				cmd = cmd + " exclude_objects_on_edges objects";
			} else {
				cmd = cmd + " objects"; //Show binary output with preserved objects (filtered image).
			}
			
			IJ.run("3D OC Options", "  dots_size=5 font_size=10 redirect_to=none");
			IJ.run(inputImp, "3D Objects Counter", cmd); //Creates filtered image.
			*/
			
		    /**
		     * Creates a new instance of Counter3D.
		     *
		     * @param img specifies the image to convert into an Counter3D.
		     * @param thr specifies the threshold value (should be an Integer).
		     * @param min specifies the MIN size threshold to be used (should be an Integer).
		     * @param max specifies the MAX size threshold to be used (should be an Integer).
		     * @param exclude specifies if the objects on the edges should be excluded (should be a boolean).
		     * @param redirect specifies if intensities measurements should be redirected to another image defined within the options window (should be a boolean).
		     */
			//inputImp.show();
	    	IJ.log("");
	    	IJ.log("Detecting 3D objects for image: " + impInput.getTitle());
	        
			Counter3D OC=new Counter3D(inputImp, 2, Integer.parseInt(min), Integer.parseInt(max), exclude, false); //imp, thr, minSize, maxSize, excludeOnEdges, redirect
			inputImp = OC.getObjMap();
			//inputImp.show();			
			
			System.out.println(impOneThresholdElement.getBitDepth()); //For debug
			//IJ.log(" in func: " + String.valueOf(impOneThresholdElement.getBitDepth())); //For debug
		
			
			
			inputImp.changes = false;
			//String originalTitle = inputImp.getTitle();
			inputImp.changes = false;
			//ImagePlus output = WindowManager.getImage("Objects map of " + originalTitle);
			
			//output.show();
			
			ImageConverter.setDoScaling(true);
			//inputImp.show();
			IJ.run(inputImp, "8-bit", ""); 
			//inputImp.show();
			
			//Set all non-0 pixels to 255
			for (int n=1; n<=inputImp.getNSlices(); n++) {
				ImageProcessor ip = inputImp.getImageStack().getProcessor(n);
				int xmin = 0, ymin = 0, xmax = ip.getWidth(), ymax = ip.getHeight();
				double v;
				for (int y = ymin; y < ymax; y++) {
					for (int x = xmin; x < xmax; x++) {
						if (ip.getPixel(x, y) > 0) {
							ip.putPixel(x, y, 255);
				}}}
				inputImp.getImageStack().setProcessor(ip,  n);
			}
			//inputImp.show();
			//output.show();
			////inputImp.setStack(output.getStack());
			inputImp.changes = false;
			//output.changes = false;
			//output.close(); //DO NOT CLOSE! Somehow this method doesnt work when output is closed. They are closed at end of processing instead.
			//output = null;
		}
		return inputImp;
	}


	// insertStackInHyperstack
	// Puts a stack inside a hyperstack at the spesified channel position
	// Automatically puts Z-projection at the correct position
	// Always used transparent-zero method
	public ImagePlus insertStackInHyperstack(ImagePlus impHyperstack, ImagePlus impStack, int channel) {
		int zPosOffset;
		// Calculate where to put slices:
		// returns "stack_only", "projection_only", or "both":
		zProjectionOutputMode = hashMap_AllInfo.get("Z Projection Options").get("output"); 
		zPosOffset = 0; // for stack_only or projection_only
		if (zProjectionOutputMode.equals("both")) {
			if (impStack.getNSlices() == 1) {
				zPosOffset = 0; // impStack is zProjection. Will be inserted to  top slice.
			} else {
				zPosOffset = 1; // impStack is a stack, avoids pasting anything to top slice
			}
		}
		IJ.setPasteMode("Transparent-zero");
		for (int i = 1; i <= impStack.getNSlices(); i++) {
			impStack.setZ(i);
			IJ.run(impStack, "Copy", "");
			impHyperstack.setC(channel);
			impHyperstack.setZ(i + zPosOffset);
			IJ.run(impHyperstack, "Paste", "");
		}
		IJ.setPasteMode("Copy");
		IJ.run(impHyperstack, "Select None", "");
		IJ.run(impStack, "Select None", "");
		impHyperstack.setZ(1);
		impStack.setZ(1);
		return impHyperstack;

	}

	
	
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	// Z-PROJECTION IMAGE PROCESSING METHOD(S)
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////


	// makeZProjection
	// This is the main zProjection function that calls all other related functions,
	// and returns the final z-projection (for a particular output channel)
	public ImagePlus makeZProjection(int curOutChnIterator) {
		//The following ImagePlus objects are temporarily used during zProjection creation.
		ImagePlus impA, impB, impC, impD, impE, impF;
		ImagePlus impB_projection, impA_zmap, impAllThreshold, impAllThresholdZmap, impAOutlineContents,
				impBOutlineContents, impCombinedABOutlineContents;
		ImageProcessor ipE;
		ImageStack isE;
		ImageCalculator ic = new ImageCalculator();
		ZProjector projector = new ZProjector();

		impA = null;
		impB = null;
		impC = null;
		impD = null;
		impE = null;
		impF = null;
		impA_zmap = null;
		impB_projection = null;

		String zMapMode = hashMap_AllInfo.get("Z Projection Options").get("zMapMode");
		int zMethod = Integer.parseInt(hashMap_AllInfo.get("Z Projection Options").get("method"));

		// If there are no elements in this output channel, return a black RGB image:
		boolean atLeastOneElemt = false;
		for (int i = 0; i < nElements; i++) {
			if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel")
					.equals(Integer.toString(curOutChnIterator))) { // Has to be current output channel
				atLeastOneElemt = true;
			}
		}
		if (atLeastOneElemt == false) {
			impZProjection = IJ.createHyperStack("impZProjection", impInput.getWidth(), impInput.getHeight(), 1, 1,
					impInput.getNFrames(), 24);
			return impZProjection;
		}

		// Does this output channel contain any impA image (at least one binary elements that is not outlined)?
		boolean containsImpA = false;
		for (int i = 0; i < nElements; i++) {
			if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Type").equals("Threshold Element")) {
				if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel")
						.equals(Integer.toString(curOutChnIterator))) { // Has to be current output channel
					if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Outline").equals("OutlineNotSelected")) {
						containsImpA = true;
					}
				}
			}
		}
		// Does this output channel contain any impB image (at least one grayscale element)?
		boolean containsImpB = false;
		for (int i = 0; i < nElements; i++) {
			if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Type").equals("Contrast Element")) {
				if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel")
						.equals(Integer.toString(curOutChnIterator))) { // Has to be current output channel
					containsImpB = true;
				}
			}
		}
		// Does this output channel contain at least one outlined binary element?
		boolean containsOutlineElem = false;
		for (int i = 0; i < nElements; i++) {
			if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Type").equals("Threshold Element")) {
				if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel")
						.equals(Integer.toString(curOutChnIterator))) { // Has to be current output channel
					if (!hashMap_AllInfo.get(Integer.toString(i + 1)).get("Outline").equals("OutlineNotSelected")) {
						containsOutlineElem = true;
					}
				}
			}
			// INITIALIZATION 1: (If there are no outline elements, the impC is all we need to make the final zProjection
			// i.e. the zProjection method will return after this initialization.
			// Create image A, B and C
			// Make impA
			if (containsImpA) {
				includeOutlineElements = false;
				doOutlineConversion = false;
				calledFrom = "zProjection";
				impA = makeAllThresholdElements(curOutChnIterator, includeOutlineElements, doOutlineConversion,
						calledFrom); // Returns RGB image. Black if no elements included
				impA_zmap = assymetricZMap(impA, zMapMode);
			} else {
				impA_zmap = null;
				impA = null;
			}
			// Make impB
			if (containsImpB) {
				calledFrom = "zProjection";
				impB = makeAllContrastElements(curOutChnIterator, calledFrom);
				// create a Zprojection:
				projector.setImage(impB);
				projector.setMethod(zMethod);
				projector.doRGBProjection();
				impB_projection = projector.getProjection();
			} else {
				impB = null;
				impB_projection = null;
			}
		}
		// At this point we have either impB_projection, impA_zmap, both or none
		// (none if outline elements are the only elements of this output channel)
		if (impB_projection != null && impA_zmap != null) { // if we have both, put any non-black threshold pixel on top of the contrast image
			impC = ic.run("Transparent-zero create stack", impB_projection, impA_zmap); // Pastes impA_zmap on top of impB_projection
			impB_projection.close();
			impA_zmap.close();
		} else if (impB_projection != null) {
			impC = impB_projection;
			impB_projection.close();
		} else if (impA_zmap != null) {
			impC = impA_zmap;
			impA_zmap.close();
		}
		// Checking if there are at least one outline element (in the current
		// curOutChnIterator), else we are done (and impC from above is returned
		// as the Zprojection)
		if (!containsOutlineElem)
			return impC;


		// Main (outline element loop): 
		//Only started if there is at least 1 outline element in this output channel, 
		//else we are done (impC from above is the returned result)

		// INITIALIZATION 2: - done once, before loop below
		// Get new image with all threshold elements, including outline elements,
		// but don't do outline conversion on any of them!
		// Then we zMap/zProject this and discard the input stack
		// We call this "impAllThresholdZmap"
		// This object is used at the start of every iteration 
		//(number of iterations = number of outline elements)

		// This object is used to determine which pixels in the projection image
		// should belong to which threshold element 
		//(which are always prioritized over contrast elements).
		// We don't convert to outline, because we need to know which pixels 
		//belong to which threshold element/color

		// Then, in the iteration, we calculate the contents of outline elements (taking whole stack into acocunt),
		// do the outline conversion, and paint in the contents in the outline 
		//at the end of each iteration
		// Generate impAllThresholdZmap, showing which pixels belong to which
		//elements in the zProjection (without outline conversion)
		
		includeOutlineElements = true;
		doOutlineConversion = false;
		calledFrom = "zProjection";
		impAllThreshold = makeAllThresholdElements(curOutChnIterator, includeOutlineElements, doOutlineConversion,
				calledFrom); // Returns RGB image. Black if no elements included
		impAllThresholdZmap = assymetricZMap(impAllThreshold, zMapMode);
		impAllThreshold.close();


		// LOOP STARTS HERE (after some initialization):
		// NB! Loop through each OUTLINED THREHSOLD ELEMENT, in prioritized color order
		// Outlined threshold elements for the current channels are sorted by color priority first
		// This list contains strings according to element number, in the correct order, e.g. ["6","3","2"]
		// Variable name is currentOutChnOutlineElements.
		// Making currentOutChnOutlineElements
		boolean iscurOutChnIterator;
		boolean isOutlineElement;
		boolean isCurrentColor;
		// This is the list of outline elements we want to generate:
		List<String> currentOutChnOutlineElements = new ArrayList<String>(); 
		for (int Loop_Color = 6; Loop_Color >= 0; Loop_Color--) {
			for (int i = 0; i < nElements; i++) { // loop through all elements
				iscurOutChnIterator = false;
				isOutlineElement = false;
				isCurrentColor = false;
				// If all three booleans above are true, we have a match for the
				// correct outline element (in correct color priority)
				// (Non-threshold elements will give type error for below conditionals)
				if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Type").equals("Threshold Element")) { 
					if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Output_Channel")
							.equals(Integer.toString(curOutChnIterator)))
						iscurOutChnIterator = true;
					if (!hashMap_AllInfo.get(Integer.toString(i + 1)).get("Outline").equals("OutlineNotSelected"))
						isOutlineElement = true;
					if (hashMap_AllInfo.get(Integer.toString(i + 1)).get("Color").equals(color_priority.get(Loop_Color)))
						isCurrentColor = true;
					// If all 3 statements above are true:
					if (iscurOutChnIterator && isOutlineElement && isCurrentColor) {
						currentOutChnOutlineElements.add(Integer.toString(i + 1));
					}
				}
			}
		}
		// At this point the list currentOutChnOutlineElements will look like
		// this e.g.: currentOutChnOutlineElements = ["5","1","3"];
		// loop through all outline elements:
		for (int i = 0; i < currentOutChnOutlineElements.size(); i++) { 
			impCombinedABOutlineContents = null;
			impAOutlineContents = null;
			impBOutlineContents = null;
			// 1st step: Manipulate impC to have outline added and inner
			// contents deleted. No steps here involves stacks
			// Parse current element color from hashmap:
			String currentColor = hashMap_AllInfo.get(currentOutChnOutlineElements.get(i)).get("Color"); 
			// create impE, which is RGB but black and white:
			impE = extractSingleColor(impAllThresholdZmap, currentColor); 
			// Remove any particles below size set by user, if enabled:
			//DISABLED. DONE IN MAKE SINGLE THRESHOLD ELEMENT IN STACK.
			//(If enabled in Zproj by user, and calledfrom = zProjection)
			/*
			if (HashMap_AllInfo.get(currentOutChnOutlineElements.get(i)).get("rmv_zproj").equals("1")) {
				IJ.run(impE, "8-bit", ""); // Is already white RGB, no need to threshold and convert to mask.
				impE.setTitle("impE");
				String tmpRmvValue = HashMap_AllInfo.get(currentOutChnOutlineElements.get(i)).get("rmv_smaller_than");
				String tmpRmvUnit = HashMap_AllInfo.get(currentOutChnOutlineElements.get(i)).get("unit");
				if (!impInput.getCalibration().scaled()) {
					IJ.run(impE, "Analyze Particles...", "size=0-" + tmpRmvValue + " pixel show=Masks");
				} else {
					if (tmpRmvUnit.equals("pixel")) {
						IJ.run(impE, "Analyze Particles...", "size=0-" + tmpRmvValue + " pixel show=Masks");
					} else {
						IJ.run(impE, "Analyze Particles...", "size=0-" + tmpRmvValue + " show=Masks");
					}
				}
				ImagePlus impE_mask = WindowManager.getImage("Mask of impE");
				impE = ic.run("Subtract create stack", impE, impE_mask); // Deleting small particles from impE
				impE_mask.close();
				IJ.run(impE, "RGB Color", "");
			}
			*/
			impF = new Duplicator().run(impE);
			IJ.run(impF, "8-bit", ""); // Color outline appropriately
			if (hashMap_AllInfo.get(currentOutChnOutlineElements.get(i)).get("Outline").equals("OutlineThick"))
				IJ.run(impF, "Find Edges", "stack");// This is not a stack, but  the method will work
			if (hashMap_AllInfo.get(currentOutChnOutlineElements.get(i)).get("Outline").equals("OutlineThin"))
				IJ.run(impF, "Outline", "stack");// This is not a stack, but the  method will work
			IJ.run(impF, currentColor, ""); // Color outline appropriately
			IJ.run(impF, "RGB Color", ""); // Color outline appropriately		
			
			if (containsImpA || containsImpB) {
				// Step 2: Create the inner contents of outline, either from
				// impA, impB or both (both if at least one threshold and one contrast element exists)
				// However, any threshold element will be pasted on top of any contrast element

				// Create impD.
				// This is the current color element (an outline element), but not converted to outline
				// The output has to be white RGB on black background
				doOutlineConversion = false;
				// Have to subtract 1 from element argument, cause the function uses i+1.
				String calledFrom = "zProjection";
				// Returns black RGB image if no valid elements in hashmap. Returns colored RGB image in all other cases:
				impD = makeOneThresholdElement(Integer.parseInt(currentOutChnOutlineElements.get(i)) - 1,
						doOutlineConversion, calledFrom);
				// below is making it white RGB.
				IJ.run(impD, "8-bit", "");
				IJ.setRawThreshold(impD, 1, 255, null);
				IJ.run(impD, "Convert to Mask", "method=Default background=Dark black");
				
				

				//Algorithm below to remove pixels in Z-proj if there is a black (no-signal) 
				//gap in the Z-direction (topwards/bottomwards depending on user setting) between two cells/pixels.
				ImageStack impDstack = impD.getStack();
				ImageProcessor ipPixelHistory = new ij.process.ByteProcessor(impD.getWidth(),impD.getHeight());
				ImageProcessor currentSlice = null;
				int xmin = 0, ymin = 0, xmax = impD.getWidth(), ymax = impD.getHeight();
				int phVal, impDVal;
				for (int slice = 1; slice <= impD.getStackSize(); slice++) {
					if (zMapMode.equals("topPriority")) {
						currentSlice = impDstack.getProcessor(slice); //start with top slice
					} else if (zMapMode.equals("botPriority")) {
						currentSlice = impDstack.getProcessor((impD.getStackSize() +1 ) - slice); //start with bottom slice
					}					
					for (int y = ymin; y < ymax; y++) {
						for (int x = xmin; x < xmax; x++) {
							phVal = ipPixelHistory.get(x,y);
							impDVal = currentSlice.get(x,y);
							
							if (phVal == 0 && impDVal == 255) {//A signal is been seen at this xy position. Record it to the ipPixelHistory.
								ipPixelHistory.set(x, y, 100); 
							} 
							
							if (phVal == 100 && impDVal == 0) {//A signal has been seen at this xy position, but now we see a lack of signal here. This means a gap is starting between this and the next slices.
								ipPixelHistory.set(x, y, 200); 
							} 
							
							if (phVal == 200 && impDVal == 255) {//A signal is seen at this xy position. But it occured after a gap. Clear the impD pixel.
								currentSlice.set(x, y, 0);
							}
				}}} //Algorithm finished.

				
				IJ.run(impD, "RGB Color", "stack");
				// Transform impE to a stack with duplicated impE images with
				int nSlices = impInput.getNSlices(); // This variable can be gotten from several of the imps
				ipE = impE.getProcessor();
				for (int inner_i = 1; inner_i < nSlices; inner_i++) { // Add slices to impE
					IJ.run(impE, "Add Slice", "");
				}
				isE = impE.getStack();
				// Set all slices the same pixels (imageProcessor). Important to loop while inner_i<nSlices+1:
				for (int inner_i = 1; inner_i < nSlices + 1; inner_i++) { 
					isE.setProcessor(ipE, inner_i);
				}
				// if we have impA (at least 1 non-outlined threhsold element
				// of current output channel):
				if (containsImpA) { // This is "2nd step - 1st stream"
					// Create threshold outline contents (from impA)
					impAOutlineContents = ic.run("AND create stack", impE, impD);
					impAOutlineContents = ic.run("AND create stack", impAOutlineContents, impA);
					impAOutlineContents = assymetricZMap(impAOutlineContents, zMapMode);
				}
				// if we have impB (at least one contrast element of current output channel):
				if (containsImpB) { // This is "2nd step - 2nd stream"
					// Create contrast outline contents (from impB)
					impBOutlineContents = ic.run("AND create stack", impE, impD);
					impBOutlineContents = ic.run("AND create stack", impBOutlineContents, impB);
					// create a Zprojection:
					projector.setImage(impBOutlineContents);
					// This should be an option from the z-menu:
					// AVG_METHOD, MAX_METHOD, MIN_METHOD, SUM_METHOD, SD_METHOD, MEDIAN_METHOD
					projector.setMethod(zMethod); 
					projector.doRGBProjection();
					impBOutlineContents = projector.getProjection();
				}
				// if we have both impA and impB:
				if (containsImpA && containsImpB) {
					impCombinedABOutlineContents = ic.run("Transparent-zero create stack", impBOutlineContents,
							impAOutlineContents); // Pastes impAOutlineContents on top of impBOutlineContents
				} else if (containsImpB) { // Only impB
					impCombinedABOutlineContents = impBOutlineContents;
				} else if (containsImpA) { // Only impA
					impCombinedABOutlineContents = impAOutlineContents;
				}
				
				//Clear pixels in impC where impCombinedABOutlineContents will be pasted 
				impC = ic.run("Subtract create stack", impC, impE); 
				
				// Close unnecessary imps
				impD.close();
				impE.close();

				/////////////////////////////////////////////////////
				// Final steps (last of 2nd and 3rd)
				/////////////////////////////////////////////////////
				// Add outline contents to impC:
				// Pastes impCombinedABOutlineContents on top of impC:
				impC = ic.run("Add create stack", impC, impCombinedABOutlineContents); 
				impCombinedABOutlineContents.close();
			} 
			// End of "if (containsImpA || containsImpB)"
			// Add outline to impC and complete:
			if (containsImpA == false && containsImpB == false) { 
				// If no impA or impB for this output channel, we create a black mock image.
				if (i == 0) { // If there are more than 1 outline elements, only
								// do this the first time. Subsequently, we use impC from this iteration.
					impC = IJ.createHyperStack("impC", impInput.getWidth(), impInput.getHeight(), 1, 1,
							impInput.getNFrames(), 24);
				}
			}
			// Pastes impF on top of impC:
			impC = ic.run("Transparent-zero create stack", impC, impF); 
			impF.close();
			impC.setTitle("impC");
		} 
		// Main for loop end
		return impC; 
	}

	
	
	// extractSingleColor
	public ImagePlus extractSingleColor(ImagePlus imp, String color) {
		double color_hex;
		ImageProcessor ip;
		ImagePlus singleColor;
		color_hex = 0xffffff; // dummy initialization value
		singleColor = new Duplicator().run(imp);
		if (color.equals("Red"))
			color_hex = 0xff0000;
		else if (color.equals("Green"))
			color_hex = 0x00ff00;
		else if (color.equals("Blue"))
			color_hex = 0x0000ff;
		else if (color.equals("Cyan"))
			color_hex = 0x00ffff;
		else if (color.equals("Magenta"))
			color_hex = 0xff00ff;
		else if (color.equals("Yellow"))
			color_hex = 0xffff00;
		else if (color.equals("White"))
			color_hex = 0xffffff;
		else if (color.equals("Grays"))
			color_hex = 0xffffff;
		color_hex = (int) color_hex & 0xffffff;
		ip = singleColor.getChannelProcessor();
		int xmin = 0, ymin = 0, xmax = ip.getWidth(), ymax = ip.getHeight();
		double v;
		for (int y = ymin; y < ymax; y++) {
			for (int x = xmin; x < xmax; x++) {
				v = ip.getPixel(x, y) & 0xffffff;
				if (v == color_hex) {
					ip.putPixel(x, y, (int) 0xffffff);
				} else {
					ip.putPixel(x, y, (int) 0x000000);
				}
			}
		}
		singleColor.setProcessor(ip);
		return singleColor;
	}
	
	

	// assymetricZMap: 
	//This function maps a stack to a single slice where stack order of pixel color is prioritized. 
	//Only supposed to get thresholded images as input. If a non stack is the input it is simply returned
	public ImagePlus assymetricZMap(ImagePlus imp1, String mode) {
		int slice, size;
		ImagePlus impTmp1 = null;
		ImagePlus impTmp2 = null;
		ImageCalculator ic = new ImageCalculator();

		size = imp1.getStackSize();
		if (size == 1)
			return new Duplicator().run(imp1);

		for (slice = 1; slice < size; slice++) {
			if (slice == 1)
				impTmp1 = new Duplicator().run(imp1, slice, slice);
			impTmp2 = new Duplicator().run(imp1, slice + 1, slice + 1);
			if (mode.equals("topPriority"))
				impTmp1 = ic.run("Transparent-zero create", impTmp2, impTmp1);
			else if (mode.equals("botPriority"))
				impTmp1 = ic.run("Transparent-zero create", impTmp1, impTmp2);
		}
		impTmp2.close();
		return impTmp1;
	}


	
	// applyMacroToSliceInImp 
	public ImagePlus applyMacroToSliceInImp(ImagePlus imp, String macroString, int slicePos) {
		imp.setSlice(slicePos);
		WindowManager.setTempCurrentImage(imp);
		Interpreter interp = new Interpreter();
		try {
			imp = interp.runBatchMacro(macroString, imp);
		} catch (Throwable e) {
			interp.abortMacro();
			String msg = e.getMessage();
			if (!(e instanceof RuntimeException && msg != null))
				IJ.handleException(e);
		} finally {
			WindowManager.setTempCurrentImage(null);

		}
		return imp;
	}

}
