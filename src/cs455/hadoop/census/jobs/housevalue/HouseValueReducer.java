package cs455.hadoop.census.jobs.housevalue;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SortedMapWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class HouseValueReducer extends Reducer<Text, SortedMapWritable, Text, Text> {
	
	private static HashMap<Integer, String> instanceToClass = new HashMap<Integer, String>();
	
	static {
		instanceToClass.put(1, "Less than $15,000");
		instanceToClass.put(2, "$15,000 - $19,999");
		instanceToClass.put(3, "$20,000 - $24,999");
		instanceToClass.put(4, "$25,000 - $29,999");
		instanceToClass.put(5, "$30,000 - $34,999");
		instanceToClass.put(6, "$35,000 - $39,999");
		instanceToClass.put(7, "$40,000 - $44,999");
		instanceToClass.put(8, "$45,000 - $49,999");
		instanceToClass.put(9, "$50,000 - $59,999");
		instanceToClass.put(10, "$60,000 - $74,999");
		instanceToClass.put(11, "$75,000 - $99,999");
		instanceToClass.put(12, "$100,000 - $124,999");
		instanceToClass.put(13, "$125,000 - $149,999");
		instanceToClass.put(14, "$150,000 - $174,999");
		instanceToClass.put(15, "$175,000 - $199,999");
		instanceToClass.put(16, "$200,000 - $249,999");
		instanceToClass.put(17, "$250,000 - $299,999");
		instanceToClass.put(18, "$300,000 - $399,999");
		instanceToClass.put(19, "$400,000 - $499,999");
		instanceToClass.put(20, "$500,000 or more");
	}
	
	private TreeMap<Integer, Long> combinedHouseValuesTable = new TreeMap<Integer, Long>();
	
	@Override
    protected void reduce(Text state, Iterable<SortedMapWritable> houseValuesTables, Context context) throws IOException, InterruptedException {
		
		long totalHouses = 0;
		
		for(SortedMapWritable houseValuesTable: houseValuesTables)
		{
			// Construct our aggregate table for the current state
			// The entries below will be in order of key which is instance number
			for(Entry<WritableComparable, Writable> houseValueGroup: houseValuesTable.entrySet())
			{
				int valueGroup = ((IntWritable) houseValueGroup.getKey()).get();
				long houseCount = ((LongWritable) houseValueGroup.getValue()).get();
				
				totalHouses += houseCount;
				
				Long prevHouseCounts = combinedHouseValuesTable.get(valueGroup);
				
				if (prevHouseCounts == null)
				{
					combinedHouseValuesTable.put(valueGroup, (long) houseCount);
				}
				else
				{
					combinedHouseValuesTable.put(valueGroup, prevHouseCounts.longValue() + houseCount);
				}
			}
		}
		
		int median = 0;
		
		// Calculate Median index
		// Will compare it to instance bounds
		long medianIndex = totalHouses / 2L;
		long cumulativeHouseCount = 0,
		     prevCumulativeHouseCount = 0;
		
		// Iterate over TreeMap to find the group that contains the median index,
		// and hence the median value. Guaranteed to be ordered by instance number
		for(Entry<Integer, Long> entry: combinedHouseValuesTable.entrySet())
		{
			int instance = entry.getKey();
			long houseCount = entry.getValue();
			
			cumulativeHouseCount += houseCount;
			
			if (prevCumulativeHouseCount <= medianIndex && medianIndex < cumulativeHouseCount)
			{
				// This is the median group
				median = instance;
				break;
			}
			
			prevCumulativeHouseCount = cumulativeHouseCount;
		}
        
        context.write(state, new Text(instanceToClass.get(median)));
    }
}