package cs455.hadoop.census.jobs.age;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongList;

public class AgeCombiner extends Reducer<Text, LongList, Text, LongList> {
    @Override
    protected void reduce(Text state, Iterable<LongList> ageCountsList, Context context) throws IOException, InterruptedException {
        
    	// All zero LongList of length 8
    	LongList l = new LongList(null, 8);
    	
        for (LongList ageCounts : ageCountsList)
        {
        	assert ageCounts.size() == l.size();
        	
        	for(int i=0; i<ageCounts.size(); i++)
        	{
        		l.set(i, l.get(i) + ageCounts.get(i));
        	}
        }
        
        context.write(state, l);
    }
}
