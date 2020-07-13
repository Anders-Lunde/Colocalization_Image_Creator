package batchprocessing;

import java.awt.Font;

public class BatchProcessingConstants {
	
	public static final String TITLE = "Batch Processing";
	
	public static final String BINARY = "Binary";
	public static final String GRAYSCALE = "Grayscale";
	
	public static final int MAIN_DIALOG_WIDTH = 1200;
	public static final int MAIN_DIALOG_HEIGHT = 800;
	public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 11);
	
	public static final String USE_CURRENT_ELEMENT_PROFILE_BUTTON_LABEL = "Use current element profile";
	public static final String LOAD_ELEMENT_PROFILE_BUTTON_LABEL = "Load element profile";
	public static final String INPUT_FOLDER_BUTTON_LABEL = "Input folder...";
	public static final String OUPUT_FOLDER_BUTTON_LABEL = "Output folder...";
	public static final String USE_BIOFORMATS_CHECK_BOX_LABEL = "Use bioformats for opening files";
	public static final String USE_BIOFORMATS_SAVE_CHECK_BOX_LABEL = "Use bioformats for saving TIF (with LZW compression)";
	
	
	public static final String REMOVE_SELECTED_FILES_BUTTON_LABEL = "<html><center>Remove selected files from list<center></html>";
	public static final String FILTER_BY_FILE_EXTENSION_BUTTON_LABEL = "<html><center>Filter by file extension<center></html>";
	public static final String SELECT_ALL_FILES_BUTTON_LABEL = "Select all files";
	public static final String SELECT_NONE_BUTTON_LABEL = "Select none";
	public static final String ADJUST_VALUES_BUTTON_LABEL = "<html><center>Adjust values for selected files and channels<center></html>";
	public static final String MANUAL_SET_VALUES_BUTTON_LABEL = "Manual set values";
	
	public static final String LOAD_VALUE_SET_BUTTON_LABEL = "Load value set";
	public static final String SAVE_VALUE_SET_BUTTON_LABEL = "Save value set";
	public static final String CANCEL_BUTTON_LABEL = "Cancel";
	public static final String START_BATCH_PROCESSING_BUTTON_LABEL = "Start batch processing";
	public static final String COLUMN_TOOLTIP_LABEL = "Double click column header to select column";
	
	
	public static final String FILENAME_TABLE_HEADER = "<html><center>Filename<center><br><br></html>";
	
	public static final String MIN_LABEL = "Min:";
	public static final String MAX_LABEL = "Max:";
	public static final String MIN_MAX_DIALOG_TITLE = "Set min and max";
	public static final String SAVE_BUTTON_LABEL = "Save";
	
	public static final String INPUT_FOLDER_FILE_CHOOSER_TITLE = "Choose input folder";
	public static final String OUTPUT_FOLDER_FILE_CHOOSER_TITLE = "Choose output folder";

}
