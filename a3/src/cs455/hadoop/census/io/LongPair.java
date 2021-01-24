package cs455.hadoop.census.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class LongPair implements Writable {
	
	private long first, second;
	
	public LongPair(long first, long second)
	{
		this.first = first;
		this.second = second;
	}
	
	public LongPair() 
	{
		this.first = 0;
		this.second = 0;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		first = in.readLong();
		second = in.readLong();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(first);
		out.writeLong(second);
	}
	
	public String toString()
	{
		return "(" + first + " , " + second + ")";
	}
	
	public long getFirst()
	{
		return first;
	}
	
	public long getSecond()
	{
		return second;
	}
	
	public void setFirst(long first)
	{
		this.first = first;
	}
	
	public void setSecond(long second)
	{
		this.second = second;
	}

}
