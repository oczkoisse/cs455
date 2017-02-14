package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskInitiate implements Event {
	
	private int rounds;
	
	public TaskInitiate(int rounds)
	{
		this.rounds = rounds;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] bytes = null;
		
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bout)))
		{
			dout.writeInt(this.getType().ordinal());
			
			dout.writeInt(rounds);
			
			dout.flush();
			
			bytes = bout.toByteArray();
		}
		
		return bytes;
	}
	
	public int getRounds()
	{
		return rounds;
	}

	@Override
	public EventType getType() {
		// TODO Auto-generated method stub
		return EventType.TASK_INITIATE;
	}

}
