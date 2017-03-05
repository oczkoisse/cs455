package cs455.scaling.server;

import java.util.*;

class BlockingQueue<T> {
	
	private ArrayDeque<T> q;
	
	public BlockingQueue()
	{
		q = new ArrayDeque<T>();
	}
	
	public void enqueue(T elem)
	{
		synchronized(q)
		{
			q.addLast(elem);
			q.notify();
		}
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
			return q.removeFirst();
		}
	}

}
