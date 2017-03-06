package cs455.scaling.works;

import java.nio.channels.SelectionKey;

public class ReadWork extends Work {
		
	public ReadWork(SelectionKey key)
	{
		super(key, WorkType.READ);
	}

	
	public String toString()
	{
		return "Read work";
	}

}
