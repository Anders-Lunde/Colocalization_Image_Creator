package batchprocessing.json;

import java.util.List;

public class JSonTableObject {

	String filename;
	List<JSonChannel> channels;
	
	public JSonTableObject() {
		
	}

	public JSonTableObject(String filename, List<JSonChannel> channels) {
		this.filename = filename;
		this.channels = channels;
	}

	public String getFilename() {
		return filename;
	}

	public List<JSonChannel> getChannels() {
		return channels;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setChannels(List<JSonChannel> channels) {
		this.channels = channels;
	}

}
