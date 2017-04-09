package cs455.hadoop.census.jobs.marital;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongTriple;

public class MaritalCombiner extends Reducer<Text, LongTriple, Text, LongTriple> {
    @Override
    protected void reduce(Text state, Iterable<LongTriple> popWithMaritalCounts, Context context) throws IOException, InterruptedException {
        long population = 0, maleNeverMarried = 0, femaleNeverMarried = 0;
        
        for(LongTriple p : popWithMaritalCounts){
        	population += p.getFirst();
        	maleNeverMarried += p.getSecond();
        	femaleNeverMarried += p.getThird();
            
        }   
        context.write(state, new LongTriple(population, maleNeverMarried, femaleNeverMarried));
    }
}
