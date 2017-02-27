package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;


import cs455.scaling.works.*;
import cs455.scaling.util.Hasher;

class Worker implements Runnable {
	
	private Work currentWork;
	private final Object currentWorkLock;
	private volatile boolean idle;
	
	private ThreadPoolManager manager;
	
	// Local read buffer for reading data from a channel
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	public Worker(ThreadPoolManager manager)
	{
		this.manager = manager;
		
		currentWork = null;
		currentWorkLock = new Object();
		idle = true;
	}
	
	private void handleRead() throws IOException
	{
		SelectionKey selKey = currentWork.getSelectionKey();
		SocketChannel selChannel = (SocketChannel) selKey.channel();
		
		readBuffer.clear();
		
		int readCount;
		while(readBuffer.hasRemaining())
		{
			readCount = selChannel.read(readBuffer);
			if (readCount == -1)
			{
				// Other side gracefully terminated the connection
				// Post DeregisterWork to the selecting thread
				Server.getInstance().addWork(new DeregisterWork(selKey));
				return;
			}
		}
		
		Server.getInstance().addWork(new HashWork(selKey, readBuffer));
	}
	
	private void handleWrite() throws IOException
	{
		SelectionKey selKey = currentWork.getSelectionKey();
		SocketChannel selChannel = (SocketChannel) selKey.channel();
		
		WriteWork ww = (WriteWork) currentWork;
		
		ByteBuffer writeBuffer = ww.getData();
		
		while(writeBuffer.hasRemaining())
			selChannel.write(writeBuffer);
		
		// Post ReadWork
		Server.getInstance().addWork(new ReadWork(selKey));
	}
	
	
	private void handleHash()
	{
		SelectionKey selKey = currentWork.getSelectionKey();
		
		HashWork hw = (HashWork) currentWork;
		
		Hasher hasher = new Hasher();
		ByteBuffer hash = hasher.hash(hw.getData());
		
		Server.getInstance().addWork(new WriteWork(selKey, hash));
	}
	
	private void finishWork() throws IOException
	{
		synchronized(currentWorkLock)
		{
			switch(currentWork.getType())
			{
			case READ: 
				handleRead();
				break;
			case WRITE: 
				handleWrite();
				break;
			case HASH: 
				handleHash();
				break;
			default:
				System.out.println("Unrecognized work type");
				System.exit(0);
			}
		}
		
		manager.signalDone(this);
	}

	@Override
	public void run()
	{
		System.out.println("Worker started");
		synchronized(currentWorkLock)
		{
			while (true)
			{
				while(currentWork == null)
				{
					System.out.println("Waiting");
					try
					{
						currentWorkLock.wait();
					}
					catch(InterruptedException e)
					{
						System.out.println(e.getMessage());
						break;
					}
				}
				System.out.println("Got some work");
				idle = false;
				try
				{
					finishWork();
				}
				catch(IOException e)
				{
					System.out.println(e.getMessage());
					System.exit(0);
				}
				currentWork = null;
				idle = true;
			}
		}
	}
	
	public void setWork(Work w)
	{
		synchronized(currentWorkLock)
		{
			this.currentWork = w;
			this.currentWorkLock.notify();
		}
	}
	
	public boolean getStatus()
	{
		return idle;
	}
}