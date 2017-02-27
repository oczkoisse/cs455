package cs455.scaling.server;

import cs455.scaling.works.*;

class ThreadPoolManager implements Runnable {
	
	private BlockingQueue<Work> pendingWorks;
	private BlockingQueue<Worker> availableWorkers;
	private int size;
	public ThreadPoolManager(int poolSize)
	{
		pendingWorks = new BlockingQueue<Work>();
		availableWorkers = new BlockingQueue<Worker>();
		size = poolSize;
	}
	
	@Override
	public void run() {
		
		System.out.println("Starting threads");

		for(int i=0; i<this.size; i++)
		{
			Worker wkr = new Worker(this);
			availableWorkers.enqueue(wkr);
			new Thread(wkr).start();
		}
		System.out.println("Threads started");
		while(true)
		{
			Work w = pendingWorks.dequeue();
			Worker wkr = availableWorkers.dequeue();
			
			// We have a work as well as a worker
			wkr.setWork(w);	
		}
	}
	
	public void signalDone(Worker w)
	{
		availableWorkers.enqueue(w);
	}
	
	public void addWork(Work w)
	{
		pendingWorks.enqueue(w);
	}

	public static void main(String[] args)
	{
		ThreadPoolManager tpm = new ThreadPoolManager(10);
		tpm.run();
	}
}