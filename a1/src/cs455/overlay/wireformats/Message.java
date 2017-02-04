package cs455.overlay.wireformats;

import java.io.IOException;

public class Message implements Event {

	@Override
	public byte[] getBytes() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventType getType() {
		// TODO Auto-generated method stub
		return EventType.MESSAGE;
	}

}
