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
import java.util.TreeMap;
import java.util.HashMap;

public class HouseRentReducer extends Reducer<Text, SortedMapWritable, Text, Text> {
	
	private static HashMap<Integer, String> instanceToClass = new HashMap<Integer, String>();
	
	static {
		instanceToClass.put(1, "Less than $100");
		instanceToClass.put(2, "$100 - $149");
		instanceToClass.put(3, "$150 - $199");
		instanceToClass.put(4, "$200 - $249");
		instanceToClass.put(5, "$250 - $299");
		instanceToClass.put(6, "$300 - $349");
		instanceToClass.put(7, "$350 - $399");
		instanceToClass.put(8, "$400 - $449");
		instanceToClass.put(9, "$450 - $499");
		instanceToClass.put(10, "$500 - $549");
		instanceToClass.put(11, "$550 - $599");
		instanceToClass.put(12, "$600 - $649");
		instanceToClass.put(13, "$650 - $699");
		instanceToClass.put(10, "$700 - $749");
		instanceToClass.put(11, "$750 - $999");
		instanceToClass.put(12, "$1000 or more");
		instanceToClass.put(13, "No Cash Rent");
	}
    
	private TreeMap<Integer, Long> combinedHouseRentsTable = new TreeMap<Integer, Long>();
	
	@Override
    protected void reduce(Text state, Iterable<SortedMapWritable> houseRentsTables, Context context) throws IOException, InterruptedException {
		
		long totalHouses = 0;
		
		for(SortedMapWritable houseRentsTable: houseRentsTables)
		{
			// Construct our aggregate table for the current state
			// The entries below will be in order of key which is instance number
			for(Entry<WritableComparable, Writable> houseRentGroup: houseRentsTable.entrySet())
			{
				int rentGroup = ((IntWritable) houseRentGroup.getKey()).get();
				long houseCount = ((LongWritable) houseRentGroup.getValue()).get();
				
				totalHouses += houseCount;
				
				Long prevHouseCounts = combinedHouseRentsTable.get(rentGroup);
				
				if (prevHouseCounts == null)
				{
					combinedHouseRentsTable.put(rentGroup, (long) houseCount);
				}
				else
				{
					combinedHouseRentsTable.put(rentGroup, prevHouseCounts.longValue() + houseCount);
				}
			}
		}
		
		
		// Calculate Median index
		// Will compare it to instance bounds
		long medianIndex = totalHouses / 2L;
		int median = 0;
		long cumulativeHouseCount = 0,
		     prevCumulativeHouseCount = 0;
		
		// Iterate over TreeMap to find the group that contains the median index,
		// and hence the median value. Guaranteed to be ordered by instance number
		for(Entry<Integer, Long> entry: combinedHouseRentsTable.entrySet())
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