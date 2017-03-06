package cs455.scaling.works;

import java.nio.channels.SelectionKey;

public class DeregisterWork extends Work {

	public DeregisterWork(SelectionKey key)
	{
		super(key, WorkType.DEREGISTER);
	}

	public String toString()
	{
		return "Deregister work for " + super.getSelectionKey().channel().toString();
	}
}
