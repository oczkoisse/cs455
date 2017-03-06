package cs455.scaling.util;

import java.util.*;

/**
 * A queue that blocks on dequeuing unless it has something to dequeue.
 * This class is thread-safe
 * @author Rahul Bangar
 *
 * @param <T>	the type of the elements to be stored in the queue
 */
public class BlockingQueue<T> {
	
	/**
	 * Internal dequeue that does not have blocking capability
	 */
	private ArrayDeque<T> q;
	
	/**
	 * Creates a new {@link cs455.scaling.util.BlockingQueue} instance
	 */
	public BlockingQueue()
	{
		q = new ArrayDeque<T>();
	}
	
	/**
	 * Enqueues an element
	 * @param elem
	 */
	public void enqueue(T elem)
	{
		synchronized(q)
		{
			q.addLast(elem);
			//printContents();
			q.notify();
		}
	}
	
	/**
	 * Dequeues an element, and blocks if the queue is empty.
	 * Resumes as soon as at least one element is enqueued
	 * @return
	 */
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
			T r = q.removeFirst();
			//printContents();
			return r;
		}
	}
	
	/**
	 * Prints the contents of the queue
	 * The format is elem - elem - ... - elem -
	 */
	public void printContents()
	{
		synchronized(q)
		{
			synchronized(System.out)
			{
				for(T e: q)
				{
					System.out.print(e + " - ");
				}
				System.out.println();
			}
		}
	}

}
