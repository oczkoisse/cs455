package cs455.hadoop.census.jobs.housevalue;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SortedMapWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Map.Entry;

public class HouseValueCombiner extends Reducer<Text, SortedMapWritable, Text, SortedMapWritable> {
	
	private SortedMapWritable combinedHouseValuesTable = new SortedMapWritable();
	
	@Override
    protected void reduce(Text state, Iterable<SortedMapWritable> houseValuesTables, Context context) throws IOException, InterruptedException {
		
		for(SortedMapWritable houseValuesTable: houseValuesTables)
		{
			// Construct our aggregate table for the current state
			// The entries below will be in order of key which is instance number
			for(Entry<WritableComparable, Writable> houseValueGroup: houseValuesTable.entrySet())
			{
				IntWritable valueGroup = (IntWritable) houseValueGroup.getKey();
				long houseCount = ((LongWritable) houseValueGroup.getValue()).get();
				
				LongWritable prevHouseCounts = (LongWritable) combinedHouseValuesTable.get(valueGroup);
				
				if (prevHouseCounts == null)
				{
					combinedHouseValuesTable.put(valueGroup, new LongWritable(houseCount));
				}
				else
				{
					prevHouseCounts.set(prevHouseCounts.get() + houseCount);
				}
			}
		}
		
		context.write(state, combinedHouseValuesTable);
	}
}