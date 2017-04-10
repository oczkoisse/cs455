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
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class HouseValueReducer extends Reducer<Text, SortedMapWritable, Text, DoubleWritable> {
	
	// Lower and upper bounds indexed by instance number from 1 to 20
	// For example, instance 1 has bounds groupBounds[0] and groupBounds[1]
	// ... groupBounds[n-1] and groupBounds[n]
	private static List<Double> groupBounds = Arrays.asList(0.5, 14999.5, 19999.5, 24999.5, 29999.5, 34999.5, 39999.5, 44999.5,
															44999.5, 49999.5, 59999.5, 74999.5, 99999.5, 124999.5, 149999.5, 174999.5,
															199999.5, 249999.5, 299999.5, 399999.5, 499999.5, Double.MAX_VALUE);
    
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
		
		double median = 0.0;
		
		// Calculate Median index
		// Will compare it to instance bounds
		double medianIndex = totalHouses / 2.0;
		long cumulativeHouseCount = 0,
		     prevCumulativeHouseCount = 0;
		
		// Iterate over TreeMap to find the group that contains the median index,
		// and hence the median value. Guaranteed to be ordered by instance number
		for(Entry<Integer, Long> entry: combinedHouseValuesTable.entrySet())
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