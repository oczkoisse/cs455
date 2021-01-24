package cs455.hadoop.census.jobs.rent;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SortedMapWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Map.Entry;

public class HouseRentCombiner extends Reducer<Text, SortedMapWritable, Text, SortedMapWritable> {
	
	private SortedMapWritable combinedHouseRentsTable = new SortedMapWritable();
	
	@Override
    protected void reduce(Text state, Iterable<SortedMapWritable> houseRentsTables, Context context) throws IOException, InterruptedException {
		
		for(SortedMapWritable houseRentsTable: houseRentsTables)
		{
			// Construct our aggregate table for the current state
			// The entries below will be in order of key which is instance number
			for(Entry<WritableComparable, Writable> houseRentGroup: houseRentsTable.entrySet())
			{
				IntWritable rentGroup = (IntWritable) houseRentGroup.getKey();
				long houseCount = ((LongWritable) houseRentGroup.getValue()).get();
				
				LongWritable prevHouseCounts = (LongWritable) combinedHouseRentsTable.get(rentGroup);
				
				if (prevHouseCounts == null)
				{
					combinedHouseRentsTable.put(rentGroup, new LongWritable(houseCount));
				}
				else
				{
					prevHouseCounts.set(prevHouseCounts.get() + houseCount);
				}
			}
		}
		
		context.write(state, combinedHouseRentsTable);
	}
}