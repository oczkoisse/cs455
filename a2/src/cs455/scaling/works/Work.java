package cs455.scaling.works;

import java.nio.channels.SelectionKey;

public abstract class Work {

	private SelectionKey key;
	
	public Work(SelectionKey key)
	{
		this.key = key;
	}
	
	public SelectionKey getSelectionKey()
	{
		return key;
	}

	public abstract WorkType getType();

}

