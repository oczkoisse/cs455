package cs455.overlay.node;
import cs455.overlay.wireformats.Event;
import java.net.*;

public interface Node {
	
	void onEvent(Event ev);
	
	default String getOwnIpAddress()
	{
		try
		{
			return InetAddress.getLocalHost().getHostAddress();
		}
		catch(UnknownHostException e)
		{
			System.out.println(e.getMessage());
			return null;
		}
	}

}
