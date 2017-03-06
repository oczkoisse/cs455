package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;


import cs455.scaling.works.*;
import cs455.scaling.util.Hasher;

/**
 * Worker thread that does jobs allocated by the {@link cs455.scaling.server.ThreadPoolManager}
 * @author rahul
 *
 */
class Worker implements Runnable {
	
	/**
	 * The most current work that needs to be done
	 * If null, then this worker is free.
	 */
	private Work currentWork;
	
	/**
	 * Lock to be used for synchronizing on {@link #currentWork}
	 */
	private final Object currentWorkLock;
	
	/**
	 * Status of the worker
	 */
	private volatile boolean idle;
	
	/**
	 * Parent thread pool manager which manages this worker thread
	 */
	private final ThreadPoolManager manager;
	
	/**
	 * The name of this worker thread
	 */
	private String threadName;
	
	/**
	 * Instantiates a new worker thread, marks it as idle but does not start it.
	 * The initial name is 'Unknown', which changes to the name of the thread in which this worker thread runs.
	 * @param manager	the parent {@link cs455.scaling.server.ThreadPoolManager} instance
	 */
	public Worker(ThreadPoolManager manager)
	{
		this.manager = manager;
		
		currentWork = null;
		currentWorkLock = new Object();
		idle = true;
		
		this.threadName = "Unknown";
	}
	

	/**
	 * Handles a read operation requested by the parent {@link cs455.scaling.server.ThreadPoolManager} instance
	 * @throws IOException
	 */
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
	
	/**
	 * Handles a write operation requested by the parent {@link cs455.scaling.server.ThreadPoolManager} instance
	 * @throws IOException
	 */
	private void handleWrite() throws IOException
	{
		// System.out.println(threadName + " writing");
		
		SocketChannel selChannel = (SocketChannel) currentWork.getSelectionKey().channel();
		
		ByteBuffer writeBuffer = ((WriteWork) currentWork).getData();
		
		while(writeBuffer.hasRemaining())
			selChannel.write(writeBuffer);	
		
		Server.getInstance().notifyMessageProcessed();
	}
	
	/**
	 * Handles a hash operation requested by the parent {@link cs455.scaling.server.ThreadPoolManager} instance
	 */
	private void handleHash()
	{
		// System.out.println(threadName + " hashing");
		
		SelectionKey selKey = currentWork.getSelectionKey();
		
		HashWork hw = (HashWork) currentWork;
		
		ByteBuffer hash = Hasher.hash(hw.getData());
		
		Server.getInstance().addWork(new WriteWork(selKey, hash));
	}
	
	/**
	 * Identifies the type of work, and dispatches it to the relevant method
	 * @throws IOException
	 */
	private void finishWork()
	{
		try
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
				System.out.println("Unknown work type");
				System.exit(0);
			}
		}
		catch(IOException e)
		{
			Server.getInstance().addWork(new DeregisterWork(currentWork.getSelectionKey()));
			return;
		}
	}

	/**
	 * Begins by instantiating its name using the thread's name
	 * Waits until a work is allocated. After finishing the allocated work,
	 * signals the parent thread pool manager of availability.
	 */
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
				
				finishWork();
				
				currentWork = null;
				idle = true;
				
				manager.signalDone(this);
			}
		}
	}
	
	/**
	 * Allocates a work to this worker thread
	 * @param w
	 */
	public void setWork(Work w)
	{
		synchronized(currentWorkLock)
		{
			if (w==null)
			{
				throw new IllegalStateException("Attempt to deallocate work from a busy thread");
			}
			else
			{
				if (this.currentWork == null)
				{
					this.currentWork = w;
					this.currentWorkLock.notify();
				}
				else
				{
					throw new IllegalStateException("Attempt to allocate work to a busy thread");
				}
			}
		}
	}
	
	/**
	 * Returns a string representation of the worker thread
	 * which is its name
	 */
	public String toString()
	{
		return threadName;
	}
	
	/**
	 * Get the status of the worker
	 * @return	true if idle otherwise false
	 */
	public boolean getStatus()
	{
		return idle;
	}
}
