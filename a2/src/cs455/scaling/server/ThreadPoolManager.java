package cs455.scaling.server;

import cs455.scaling.util.BlockingQueue;
import cs455.scaling.works.*;

/**
 * A thread pool manager implementation.
 * Can run in its own thread.
 * @author Rahul Bangar
 *
 */
class ThreadPoolManager implements Runnable {
	
	/**
	 * List of pending works in a blocking queue
	 */
	private BlockingQueue<Work> pendingWorks;
	
	/**
	 * List of free threads that can be allocated some pending work in a blocking queue
	 */
	private BlockingQueue<Worker> availableWorkers;
	
	/**
	 * The size of the thread pool
	 */
	private int size;
	
	/**
	 * Instantiates a new thread pool manager
	 * @param poolSize	the size of the thread pool
	 */
	public ThreadPoolManager(int poolSize)
	{
		pendingWorks = new BlockingQueue<Work>();
		availableWorkers = new BlockingQueue<Worker>();
		size = poolSize;
	}
	
	/**
	 * Starts up the threads in this thread pool, and enqueues them as available.
	 * Each thread is named 'Worker x' where x is an integer identifier.
	 * Sequentially allocates pending works to worker threads as they become available,
	 * or blocks if there is no work or worker or both.
	 */
	@Override
	public void run() {
		for(int i=0; i<this.size; i++)
		{
			Worker wkr = new Worker(this);
			availableWorkers.enqueue(wkr);
			new Thread(wkr, "Worker " + i).start();
		}
		while(true)
		{
			Work w = pendingWorks.dequeue();
			Worker wkr = availableWorkers.dequeue();
			
			// We have a work as well as a worker
			wkr.setWork(w);	
		}
	}
	
	/**
	 * Signal that a worker thread has finished its allocated work
	 * Meant to be used by a {@link cs455.scaling.server.Worker}
	 * Such a worker is re-enqueued as a available worker
	 * @param wkr	the worker thread that is finished with its allocated work
	 */
	void signalDone(Worker wkr)
	{
		availableWorkers.enqueue(wkr);
	}
	
	/**
	 * Add a work to the pending works for thread pool manager
	 * @param w	new work that needs to be done
	 */
	public void addWork(Work w)
	{
		pendingWorks.enqueue(w);
	}
}
