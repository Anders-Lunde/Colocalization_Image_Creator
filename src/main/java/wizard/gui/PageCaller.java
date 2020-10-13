package wizard.gui;

import wizard.enums.PageEnum;

public class PageCaller {

	public static void showPage(PageEnum page) {

		switch (page) {
		case FIRST_PAGE:
			FirstPage firstPage = new FirstPage();
			firstPage.setVisible(true);
			break;
		case BINARY_ELEMENT:
			BinaryElement binaryElementPage = new BinaryElement();
			binaryElementPage.setVisible(true);
			break;
		case GRAYSCALE_ELEMENT:
			GrayscaleElement grayscaleElementPage = new GrayscaleElement();
			grayscaleElementPage.setVisible(true);
			break;
		case ADVANCED_OPTIONS_BINARY:
			AdvancedOptionsBinary advancedOptionsBinaryPage = new AdvancedOptionsBinary();
			advancedOptionsBinaryPage.setVisible(true);
			break;
		case ADVANCED_OPTIONS_GRAYSCALE:
			AdvancedOptionsGrayscale advancedOptionsGrayscalePage = new AdvancedOptionsGrayscale();
			advancedOptionsGrayscalePage.setVisible(true);
			break;
		}
	}
}
