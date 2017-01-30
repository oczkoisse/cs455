package cs455.overlay.node;

import cs455.overlay.wireformats.EventType;
import java.io.*;
import java.lang.IllegalArgumentException;
import cs455.util.Interpreter;

public class Registry implements Node {
	
	private static int portnum;
	
	private static final Registry instance = new Registry(Registry.portnum);
	
	private Registry(int portnum)
	{
		Registry.portnum = portnum;
	}
	
	public static Registry getInstance()
	{
			return instance;
	}
	
	public static void init(int portnum)
	{
		Registry.portnum = portnum;
	}
	
	@Override
	public void onEvent(EventType ev) {

	}
	
	
	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			int portnum = Integer.parseInt(args[0]);
			Registry.getInstance().init(portnum);
			
			RegistryInterpreter p = new RegistryInterpreter(">> ", 3);
			p.run();
			System.out.println("Session finished");
			System.exit(0);
		}
		else
		{
			System.out.println("Usage: java cs455.overlay.node.Registry <portnum>");
			System.exit(0);
		}
	}

}


class RegistryInterpreter extends Interpreter
{
	
	public RegistryInterpreter(String prompt, int maxTries)
	{
		super(prompt, maxTries);
	}
	
	private boolean handleSingleWordCommands(String[] words)
	{
		boolean isValid = true;
		
		if (words.length != 1)
		{		
			isValid = false;
		}
		
		if (!isValid)
			System.out.println("Usage: " + words[0].trim());
		
		return isValid;
	}
	
	private boolean handleListMessagingNodes(String[] words)
	{
		return this.handleSingleWordCommands(words);
	}
	
	private boolean handleListWeights(String[] words)
	{
		return this.handleSingleWordCommands(words);
	}
	
	private boolean handleSetupOverlay(String[] words)
	{
		boolean isValid = true;
		
		if (words.length == 2)
		{
			try{
				int numCon = Integer.parseInt(words[1]);
				// Registry should take over from here
			}
			catch(NumberFormatException e)
			{
				isValid = false;
			}
		}
		else 
			return false;
		
		if (!isValid)
			System.out.println("Usage: setup-overlay <number-of-connected-nodes>");
		
		return isValid;
	}
	
	private boolean handleSendOverlayLinkWeights(String[] words)
	{
		return this.handleSingleWordCommands(words);
	}
	
	private boolean handleStart(String[] words)
	{
		boolean isValid = true;
		
		if (words.length == 2)
		{
			try{
				int numRounds = Integer.parseInt(words[1]);
				// Registry should take over from here
			}
			catch(NumberFormatException e)
			{
				isValid = false;
			}
		}
		else 
			return false;
		
		if (!isValid)
			System.out.println("Usage: start <number-of-rounds>");
		
		return isValid;
	}
	
	public boolean handleCommand(String cmd)
	{
		String[] words = cmd.split("\\s+");
		
		boolean isValid = true;
		
		if (words.length > 0 && words[0].trim().length() > 0)
		{
			switch(words[0])
			{
			case "list-messaging-nodes":
				isValid = this.handleListMessagingNodes(words);
				break;
			case "list-weights":
				isValid = this.handleListWeights(words);
				break;
			case "setup-overlay":
				isValid = this.handleSetupOverlay(words);
				break;
			case "send-overlay-link-weights":
				isValid = this.handleSendOverlayLinkWeights(words);
				break;
			case "start":
				isValid = this.handleStart(words);
				break;
			default: 
				System.out.println("Unknown command: " + words[0]);
				isValid = false;
				break;
			}
		}
		
		return isValid;
	}
}
