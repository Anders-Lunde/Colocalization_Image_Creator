package batchprocessing.json;

public class JSonChannel {

	String channelName;
	int min;
	int max;

	public JSonChannel() {
		
	}
	
	public JSonChannel(String channelName, int min, int max) {
		this.channelName = channelName;
		this.min = min;
		this.max = max;
	}

	public String getChannelName() {
		return channelName;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public void setMax(int max) {
		this.max = max;
	}

}
