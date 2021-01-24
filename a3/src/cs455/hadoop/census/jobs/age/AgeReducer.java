package cs455.hadoop.census.jobs.age;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongList;
import cs455.hadoop.census.io.DoubleList;


public class AgeReducer extends Reducer<Text, LongList, Text, Text> {
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
        
        double male_grp_18 = l.get(0) * 100.0 / l.get(3);
        double male_grp_19_29 = l.get(1) * 100.0 / l.get(3);
        double male_grp_30_39 = l.get(2) * 100.0 / l.get(3);
        
        double female_grp_18 = l.get(4) * 100.0 / l.get(7);
        double female_grp_19_29 = l.get(5) * 100.0 / l.get(7);
        double female_grp_30_39 = l.get(6) * 100.0 / l.get(7);
        
        context.write(state, new Text("\n" + 
        		"Male% 1 - 18: " + male_grp_18 + "\n" +
        		"Male% 19 - 29: " + male_grp_19_29 + "\n" +
        		"Male% 30 - 39: " + male_grp_30_39 + "\n" +
        		"Female% 1 - 18: " + female_grp_18 + "\n" +
        		"Female% 19 - 29: " + female_grp_19_29 + "\n" +
        		"Female% 30 - 39: " + female_grp_30_39 + "\n"
        		));
    }
}