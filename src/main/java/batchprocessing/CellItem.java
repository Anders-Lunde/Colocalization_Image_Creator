package batchprocessing;

public class CellItem {
	
	private int min;
	private int max;
	
	public CellItem(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}
}
