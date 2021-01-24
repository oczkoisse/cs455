package cs455.hadoop.census.jobs.elderly;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;

public class ElderlyCombiner extends Reducer<Text, LongPair, Text, LongPair> {
    @Override
    protected void reduce(Text state, Iterable<LongPair> popWithElderly, Context context) throws IOException, InterruptedException {
        long population = 0, elderly = 0;
        
        for(LongPair p : popWithElderly){
        	population += p.getFirst();
        	elderly += p.getSecond();
            
        }   
        context.write(state, new LongPair(population, elderly));
    }
}
