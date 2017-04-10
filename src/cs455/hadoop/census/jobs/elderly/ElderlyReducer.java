package cs455.hadoop.census.jobs.elderly;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;

public class ElderlyReducer extends Reducer<Text, LongPair, NullWritable, Text> {

	private Text maxState = new Text();
	private double maxStatePercentage = 0.0;
	
    @Override
    protected void reduce(Text state, Iterable<LongPair> popWithElderly, Context context) throws IOException, InterruptedException {
		long population = 0, elderly = 0;
        
        for(LongPair p : popWithElderly)
        {
        	population += p.getFirst();
        	elderly += p.getSecond();
        }
        
        double curStatePercentage = (elderly / (double) population);
        
        if (curStatePercentage > maxStatePercentage)
        {
        	maxState.set(state.toString());
        	maxStatePercentage = curStatePercentage;
        }
   }
    
   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException
   {
	   context.write(NullWritable.get(), maxState);
   }
}
