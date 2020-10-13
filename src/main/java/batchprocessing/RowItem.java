package batchprocessing;

import java.util.ArrayList;
import java.util.List;

public class RowItem {

	String fileName;
	List<Channel> channels = new ArrayList<>();

	public RowItem(String fileName, List<Channel> channels) {
		this.fileName = fileName;
		this.channels = channels;
	}

	public String getFileName() {
		return fileName;
	}

	public List<Channel> getChannels() {
		return channels;
	}

}
