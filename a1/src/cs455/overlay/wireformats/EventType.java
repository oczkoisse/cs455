package cs455.overlay.wireformats;

public enum EventType {
	// Use ordinal() to convert these to int starting with 0
	// Note that changing order will also change ordinal values
	REGISTER_REQUEST,
	REGISTER_RESPONSE,
	DEREGISTER_REQUEST,
	MESSAGING_NODES_LIST,
	LINK_WEIGHTS,
	TASK_INITIATE,
	MESSAGE,
	TASK_COMPLETE,
	PULL_TRAFFIC_SUMMARY,
	TRAFFIC_SUMMARY
}