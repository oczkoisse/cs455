package cs455.overlay.wireformats;

public interface Event {
	
	byte[] getBytes();
	
	EventType getType();

}