package cs455.scaling.works;

import java.nio.channels.SelectionKey;

public class ReadWork extends Work {
		
	public ReadWork(SelectionKey key)
	{
		super(key);
	}

	@Override
	public WorkType getType() {
		return WorkType.READ;
	}

}
