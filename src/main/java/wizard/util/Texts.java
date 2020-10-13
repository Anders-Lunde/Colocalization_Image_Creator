package wizard.util;

//import Image_Rearranger;

public class Texts {

	// Arguments page
	public static final String TOTAL_INPUT_CHANNELS_ARG = "Total input channels:";
	public static final String SCALE_UNIT_ARG = "Scale unit:";
	public static final String MAX_OUTPUT_CHANNEL_ARG = "Max output channel:";
	public static final String START_APP = "Start application";
	
	public static final String CREATES_NEW = " (Creates new)";

	// First page
	public static final String FIRST_PAGE_HEADER = "Select element type";
	public static final String BINARY_ELEMENT = "Binary element:";
	public static final String GRAYSCALE_ELEMENT = "Grayscale element:";
	public static final String BINARY_ELEMENT_TEXT = "<html><ul>"
			+ "<li>A binary element has only two levels: signal or no signal. Set manual or automatic threshold to define cutoff level.</li>"
			+ "<li>Enable the &#171;convert to outline&#187; option to simulate &#171;looking inside objects&#187; (for example looking inside DAPI/hoechst stained nuclei).</li>"
			+ "<li>(!) If multiple input channels are selected, the output will show where the channels colocalize (by performing the Boolean AND operation on them).</li>"
			+ "<li>Binary elements do not mix colors with other elements. If two binary elements are in the same position, only the color of highest priority is displayed (color priority can be adjusted in &#171;Set color priority&#187; in the main menu).</li>"
			+ "<li>Binary elements are always prioritized over grayscale elements if they occur in the same position (hint: use &#171;convert to outline&#187; to look inside binary elements).</li>"
			+ "</ul></html>";
	public static final String GRAYSCALE_ELEMENT_TEXT = "<html><ul>"
			+ "<li>Grayscale elements are like normal images, can have many shades of gray (signal intensity levels), and does not indicate signal versus no signal.</li>"
			+ "<li>A warning will be displayed if more than 1 grayscale element per output channel is used, since this is not recommended for colocalization analysis.</li>"
			+ "<li>Grayscale element should be used if input channels are difficult to threhsold, or if relative intensity of input images/channels is important, or for verification channels.</li>"
			+ "<li>Altough not recommended for colocalizaiton analysis, mixing multiple input channels lead to additive color mixing just as for normal RGB images (example: red+green=yellow).</li>"
			+ "<li>Does not mix with binary elements. Binary elements always overwrite grayscale elements.</li>"
			+ "</ul></html>";

	//Common
	public static final String REUSEBUTTON_HOVER = "Channel values used in other elements are always reused, and can only be re-set in the main menu";
	
	public static final String IMAGE_J_MACRO_TEXT_BINARY = "<html>Add ImageJ macro commands excecuted on the element, for example minimum/maximum filters.<br>"
			+ "Applied before small particle removal and Zprojection. For binary elements, avoid commands that don't work on binary images,<br>or commands that transform binary to grayscale.</html>";
	
	public static final String IMAGE_J_MACRO_TEXT_GRAYSCALE = "<html>Add ImageJ macro commands excecuted on the element, for example minimum/maximum filters.<br>"
			+ "Applied before Zprojection, and after multiple input channels merging.</html>";
	
	
	// Grayscale element page
	public static final String INPUT_CHANNELS_LABEL = "Chose the color of input channels (tip: visually brightest to faintest is white, yellow, cyan, green, magenta, red, blue)";
	public static final String CHANNEL_TO_INSERT = "Output image channel:";

	// Advanced options binary page
	public static final String ADVANCED_OPTIONS_HEADER = "Binary element - advanced options:";
	public static final String PIXELS = "Pixels";
	public static final String ENABLE_INSTACK = "Enable in individual sections";
	public static final String Z_PROJECTION = "Enable in Z-projection";

	public static final String ADD_MACRO_COMMANDS = "Add macro commands";

	// Advanced options grayscale page
	public static final String ADVANCED_OPTIONS_GRAYSCALE_HEADER = "Grayscale element - advanced options:";
	public static final String COLOR_LABEL = "Element color:";
	public static final String CHANNEL_LABEL = "Chose which input channels to threshold. Chose more than one to only get colocalized signals:";
	public static final String CHANNEL = "Channel ";
	public static final String OUTLINE_LABEL = "Convert to outline (look inside objects):";
	public static final String INSERT_LABEL = "Output image channel:";
	public static final String OVERLAP_LABEL = "Subtract from element. Signals from these channels are removed from the binary element.";
	public static final String REMOVE_LABEL = "Remove connected particles smaller than: (Useful to remove small connected particles that are not object of interest)";
	public static final String OFF_LABEL = "Off";
	public static final String THICK_OUTLINE_LABEL = "Thick outlines";
	public static final String THIN_OUTLINE_LABEL = "Thin outlines";
	
	// Buttons
	public static final String NEXT_BUTTON = "Next";
	public static final String CANCEL_BUTTON = "Cancel";
	public static final String ADVANCED_OPTIONS_BUTTON = "Advanced options";
	public static final String BACK_BUTTON = "Back";
	public static final String FINISH_BUTTON = "Finish";
	public static final String SAVE_BUTTON = "Save";
	public static final String PREVIEW_BUTTON = "Preview";
	public static final String REUSE_VALUES = "<html>Re-use preview values for<br>\"preview\" and \"finish\" buttons.</html>";

	// Colors
	public static final String COLOR_RED = "Red";
	public static final String COLOR_GREEN = "Green";
	public static final String COLOR_BLUE = "Blue";
	public static final String COLOR_YELLOW = "Yellow";
	public static final String COLOR_CYAN = "Cyan";
	public static final String COLOR_MAGENTA = "Magenta";
	public static final String COLOR_WHITE = "White";

	// Colors
	public static final String COLOR_RED_PR = "(red)";
	public static final String COLOR_GREEN_PR = "(green)";
	public static final String COLOR_BLUE_PR = "(blue)";
	public static final String COLOR_YELLOW_PR = "(yellow)";
	public static final String COLOR_CYAN_PR = "(cyan)";
	public static final String COLOR_MAGENTA_PR = "(magenta)";
	public static final String COLOR_WHITE_PR = "(white)";

	// Errors
	public static final String COLOR_NOT_SELECTED = "Color not selected!";
	public static final String CHANNEL_NOT_SELECTED = "At least one channel must be selected!";
	public static final String ALERT = "Alert";
	
}
