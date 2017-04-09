package cs455.hadoop.census.jobs.marital;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongTriple;
import cs455.hadoop.census.io.DoublePair;

public class MaritalReducer extends Reducer<Text, LongTriple, Text, DoublePair> {
    @Override
    protected void reduce(Text state, Iterable<LongTriple> popWithMaritalCounts, Context context) throws IOException, InterruptedException {
        long population = 0, maleNeverMarried = 0, femaleNeverMarried = 0;
        
        for(LongTriple p : popWithMaritalCounts){
        	population += p.getFirst();
        	maleNeverMarried += p.getSecond();
        	femaleNeverMarried += p.getThird();
            
        }  
        
        context.write(state, new DoublePair(maleNeverMarried * 100.0 / population, femaleNeverMarried * 100.0 / population));
    }
}
