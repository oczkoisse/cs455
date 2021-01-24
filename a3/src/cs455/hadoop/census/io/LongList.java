package cs455.hadoop.census.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.io.Writable;

public class LongList implements Writable, Iterable<Long> {
	
	private List<Long> backingList = new ArrayList<Long>();
	
	public LongList()
	{
		
	}
	
	/**
	 * A fixed size list of Longs
	 * @param initializingList List with which to initializing this LongList
	 * @param size If size is greater than the size of initializingList, then 0L will be filled in
	 * If size is less than the size of initializingList, then only first size elements are considered
	 */
	public LongList(Iterable<Long> initializingList, int size)
	{	
		if (initializingList != null)
		{
			Iterator<Long> it = initializingList.iterator();
			while (it.hasNext() && backingList.size() < size)
			{
				backingList.add(it.next());
			}
		}
		
		while(backingList.size() < size)
		{
			backingList.add(0L);
		}
		
	}
	
	public int size()
	{
		return backingList.size();
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int sz = in.readInt();
		
		while (sz > backingList.size())
		{
			backingList.add(0L);
		}
		
		while (sz < backingList.size())
		{
			backingList.remove(backingList.size() - 1);
		}
		
		for(int i=0; i<backingList.size(); i++)
		{
			backingList.set(i, in.readLong());
		}
	}
	
	public void set(int index, Long val)
	{
		backingList.set(index, val);
	}
	
	public Long get(int index)
	{
		return backingList.get(index);
	}
	
	public void reset()
	{
		for(int i=0; i<backingList.size(); i++)
		{
			backingList.set(i, 0L);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(backingList.size());
		for(Long e: backingList)
		{
			out.writeLong(e);
		}
	}
	
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append("[");
		for(Long e: backingList)
		{
			s.append(" " + e.longValue() + ",");
		}
		s.append("]");
		
		return s.toString();
	}

	@Override
	public Iterator<Long> iterator() {
		return backingList.iterator();
	}
}