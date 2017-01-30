package cs455.util;

import java.io.*;

/**
 * A simple whitespace separated command prompt interpreter
 * @author Rahul Bangar
 *
 */
public abstract class Interpreter
{
	private int maxTries;
	
	private String prompt;
	
	public Interpreter(String prompt, int maxTries)
	{
		this.prompt = prompt;
		
		if (maxTries < 0)
			throw new IllegalArgumentException("Invalid value for maximum tries");
		else
			this.maxTries = maxTries;
	}
	
	public void run()
	{	
		try(BufferedReader buf = new BufferedReader(new InputStreamReader(System.in)))
		{
			int tries = 0;
			Prompter:
			while(true)
			{
				System.out.print(prompt);
				String cmd = buf.readLine();
				if (!handleCommand(cmd))
					tries++;
				else
					tries = 0;
				
				if (this.maxTries > 0 && tries == this.maxTries)
					break Prompter;
				
			}
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
	
	/**
	 * Should handle each line of command at the prompt
	 * Returns a boolean representing if the command was a valid one
	 * The return value is used by the run() method to count the number of tries
	 * before giving up
	 */
	abstract public boolean handleCommand(String cmd);
}