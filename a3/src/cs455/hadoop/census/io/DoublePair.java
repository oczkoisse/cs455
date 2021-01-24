package cs455.hadoop.census.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class DoublePair implements Writable {
	
	private double first, second;
	
	public DoublePair(double first, double second)
	{
		this.first = first;
		this.second = second;
	}
	
	public DoublePair() 
	{
		this.first = 0.0;
		this.second = 0.0;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		first = in.readDouble();
		second = in.readDouble();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(first);
		out.writeDouble(second);
	}
	
	public String toString()
	{
		return "(" + first + " , " + second + ")";
	}
	
	public double getFirst()
	{
		return first;
	}
	
	public double getSecond()
	{
		return second;
	}
	
	public void setFirst(double first)
	{
		this.first = first;
	}
	
	public void setSecond(double second)
	{
		this.second = second;
	}

}