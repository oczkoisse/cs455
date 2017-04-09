package cs455.hadoop.census.jobs;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;
import cs455.hadoop.census.io.DoublePair;

public class TenureReducer extends Reducer<Text, LongPair, Text, DoublePair> {
    @Override
    protected void reduce(Text state, Iterable<LongPair> tenureCounts, Context context) throws IOException, InterruptedException {
        long ownedCount = 0, rentedCount = 0;
        
        for(LongPair p : tenureCounts){
        	ownedCount += p.getFirst();
        	rentedCount += p.getSecond();
            
        }
        
        long total = ownedCount + rentedCount;
        
        context.write(state, new DoublePair(ownedCount / (double) total, rentedCount / (double) total));
    }
}
