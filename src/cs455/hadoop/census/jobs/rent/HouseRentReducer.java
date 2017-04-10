package cs455.hadoop.census.jobs.rent;

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
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class HouseRentReducer extends Reducer<Text, SortedMapWritable, Text, DoubleWritable> {
	
	// Lower and upper bounds indexed by instance number from 1 to 20
	// For example, instance 1 has bounds groupBounds[0] and groupBounds[1]
	// ... groupBounds[n-1] and groupBounds[n]
	private static List<Double> groupBounds = Arrays.asList(0.5, 100.5, 149.5, 199.5, 249.5, 299.5, 349.5, 399.5, 449.5, 499.5,
															549.5, 599.5, 649.5, 699.5, 749.5, 999.5, Double.MAX_VALUE);
    
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
		
		double median = 0.0;
		
		// Calculate Median index
		// Will compare it to instance bounds
		double medianIndex = totalHouses / 2.0;
		long cumulativeHouseCount = 0,
		     prevCumulativeHouseCount = 0;
		
		// Iterate over TreeMap to find the group that contains the median index,
		// and hence the median value. Guaranteed to be ordered by instance number
		for(Entry<Integer, Long> entry: combinedHouseRentsTable.entrySet())
		{
			int instance = entry.getKey();
			long houseCount = entry.getValue();
			
			cumulativeHouseCount += houseCount;
			
			double lInstance = groupBounds.get(instance - 1);
			double hInstance = groupBounds.get(instance);
			
			if (prevCumulativeHouseCount <= medianIndex && medianIndex < cumulativeHouseCount)
			{
				// This is the median group
				median = lInstance + (medianIndex - prevCumulativeHouseCount) * ((hInstance -lInstance) / houseCount);
				break;
			}
			
			prevCumulativeHouseCount = cumulativeHouseCount;
		}
        
        context.write(state, new DoubleWritable(median));
    }
}