package cs455.overlay.node;
import cs455.overlay.wireformats.EventType;

public interface Node {
	
	void onEvent(EventType ev);

}
