package cs455.overlay.wireformats;

public class Register implements Event 
{
	
	public EventType getType()
	{
		return EventType.REGISTER_REQUEST;
	}
	
}
