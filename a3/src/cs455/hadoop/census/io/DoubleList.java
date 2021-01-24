package cs455.hadoop.census.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.io.Writable;

public class DoubleList implements Writable, Iterable<Double> {
	
	private List<Double> backingList = new ArrayList<Double>();
	
	public DoubleList()
	{
		
	}
	
	/**
	 * A fixed size list of Doubles
	 * @param initializingList List with which to initializing this DoubleList
	 * @param size If size is greater than the size of initializingList, then 0L will be filled in
	 * If size is less than the size of initializingList, then only first size elements are considered
	 */
	public DoubleList(Iterable<Double> initializingList, int size)
	{	
		if (initializingList != null)
		{
			Iterator<Double> it = initializingList.iterator();
			while (it.hasNext() && backingList.size() < size)
			{
				backingList.add(it.next());
			}
		}
		
		while(backingList.size() < size)
		{
			backingList.add(0.0);
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
			backingList.add(0.0);
		}
		
		while (sz < backingList.size())
		{
			backingList.remove(backingList.size() - 1);
		}
		
		for(int i=0; i<backingList.size(); i++)
		{
			backingList.set(i, in.readDouble());
		}
	}
	
	public void set(int index, Double val)
	{
		backingList.set(index, val);
	}
	
	public Double get(int index)
	{
		return backingList.get(index);
	}
	
	public void reset()
	{
		for(int i=0; i<backingList.size(); i++)
		{
			backingList.set(i, 0.0);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(backingList.size());
		for(Double e: backingList)
		{
			out.writeDouble(e);
		}
	}
	
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append("[");
		for(Double e: backingList)
		{
			s.append(" " + e.doubleValue() + ",");
		}
		
		s.setCharAt(s.length() - 1, ' ');
		
		s.append("]");
		
		return s.toString();
	}

	@Override
	public Iterator<Double> iterator() {
		return backingList.iterator();
	}
}