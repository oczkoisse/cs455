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
	
	private String threadName;
	
	public Worker(ThreadPoolManager manager)
	{
		this.manager = manager;
		
		currentWork = null;
		currentWorkLock = new Object();
		idle = true;
		
		this.threadName = "Unknown";
	}
	
	private void handleRead() throws IOException
	{
		//System.out.println(threadName + " reading");
		
		SelectionKey selKey = currentWork.getSelectionKey();
		SocketChannel selChannel = (SocketChannel) selKey.channel();
		
		ByteBuffer readBuffer = ByteBuffer.allocate(8192);
		
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

		readBuffer.flip();
		Server.getInstance().addWork(new HashWork(selKey, readBuffer));
	}
	
	private void handleWrite() throws IOException
	{
		System.out.println(threadName + " writing");
		
		SocketChannel selChannel = (SocketChannel) currentWork.getSelectionKey().channel();
		
		ByteBuffer writeBuffer = ((WriteWork) currentWork).getData();
		
		while(writeBuffer.hasRemaining())
			selChannel.write(writeBuffer);	
		
		Server.getInstance().notifyMessageProcessed();
	}
	
	
	private void handleHash()
	{
		System.out.println(threadName + " hashing");
		
		SelectionKey selKey = currentWork.getSelectionKey();
		
		HashWork hw = (HashWork) currentWork;
		
		ByteBuffer hash = Hasher.hash(hw.getData());
		
		Server.getInstance().addWork(new WriteWork(selKey, hash));
	}
	
	private void finishWork() throws IOException
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
			System.out.println("Rogue work type");
			System.exit(0);
		}
	}

	@Override
	public void run()
	{
		this.threadName = Thread.currentThread().getName();
		
		synchronized(currentWorkLock)
		{
			while (true)
			{
				while(currentWork == null)
				{
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
				
				manager.signalDone(this);
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
