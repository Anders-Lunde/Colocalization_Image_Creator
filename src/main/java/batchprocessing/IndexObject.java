package batchprocessing;

public class IndexObject {
	
	String channelName;
	int rowIndex;
	int columnIndex;
	
	public IndexObject(String channelName, int rowIndex, int columnIndex) {
		this.channelName = channelName;
		this.rowIndex = rowIndex;
		this.columnIndex = columnIndex;
	}
	public String getChannelName() {
		return channelName;
	}
	
	public int getRowIndex() {
		return rowIndex;
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}

}
