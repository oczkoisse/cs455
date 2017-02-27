package cs455.scaling.server;

import java.util.*;

class BlockingQueue<T> {
	
	private ArrayDeque<T> q;
	
	public BlockingQueue()
	{
		q = new ArrayDeque<T>();
	}
	
	public boolean enqueue(T elem)
	{
		boolean success = false;
		synchronized(q)
		{
			success = q.offer(elem);
			if (success)
			{
				q.notify();
			}
		}
		return success;
	}
	
	public T dequeue()
	{
		synchronized(q)
		{
			while(q.isEmpty())
			{
				try
				{
					q.wait();
				}
				catch(InterruptedException e)
				{
					break;
				}
			}
			return q.poll();
		}
	}

}
