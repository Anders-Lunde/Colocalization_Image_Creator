package batchprocessing;

public class Channel {

	private String type;
	private int number;
	private int min;
	private int max;

	public Channel(String type, int number, int min, int max) {
		this.type = type;
		this.number = number;
		this.min = min;
		this.max = max;
	}

	public String getType() {
		return type;
	}

	public int getNumber() {
		return number;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}
	
	public String getName() {
		return "Channel" + this.number + " " + this.type;
	}
}
