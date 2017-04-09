package cs455.hadoop.census.jobs.tenure;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;

public class TenureCombiner extends Reducer<Text, LongPair, Text, LongPair> {
    @Override
    protected void reduce(Text state, Iterable<LongPair> tenureCounts, Context context) throws IOException, InterruptedException {
        long ownedCount = 0, rentedCount = 0;
        
        for(LongPair p : tenureCounts){
        	ownedCount += p.getFirst();
        	rentedCount += p.getSecond();
            
        }   
        context.write(state, new LongPair(ownedCount, rentedCount));
    }
}
