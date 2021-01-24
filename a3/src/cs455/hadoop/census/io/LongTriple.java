package cs455.hadoop.census.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class LongTriple implements Writable {
	
	private long first, second, third;
	
	public LongTriple(long first, long second, long third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public LongTriple() 
	{
		this.first = 0;
		this.second = 0;
		this.third = 0;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		first = in.readLong();
		second = in.readLong();
		third = in.readLong();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(first);
		out.writeLong(second);
		out.writeLong(third);
	}
	
	public String toString()
	{
		return "(" + first + " , " + second + " , " + third + ")";
	}
	
	public long getFirst()
	{
		return first;
	}
	
	public long getSecond()
	{
		return second;
	}
	
	public long getThird()
	{
		return third;
	}
	
	public void setFirst(long first)
	{
		this.first = first;
	}
	
	public void setSecond(long second)
	{
		this.second = second;
	}
	
	public void setThird(long third)
	{
		this.third = third;
	}

}
