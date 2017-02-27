package cs455.scaling.works;

import java.nio.channels.SelectionKey;

public class DeregisterWork extends Work {

	public DeregisterWork(SelectionKey key)
	{
		super(key);
	}
	
	@Override
	public WorkType getType() {
		// TODO Auto-generated method stub
		return WorkType.DEREGISTER;
	}

}
