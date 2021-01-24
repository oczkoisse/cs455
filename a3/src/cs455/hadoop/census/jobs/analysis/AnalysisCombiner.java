package cs455.hadoop.census.jobs.analysis;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongTriple;

public class AnalysisCombiner extends Reducer<Text, LongTriple, Text, LongTriple> {
    @Override
    protected void reduce(Text state, Iterable<LongTriple> popWithSepMalesFemales, Context context) throws IOException, InterruptedException {
    	long sepMales = 0, sepFemales = 0, population = 0;
        
        for(LongTriple p : popWithSepMalesFemales){
        	sepMales += p.getFirst();
        	sepFemales += p.getSecond();
        	population += p.getThird();
            
        }  
        
        context.write(state, new LongTriple(sepMales, sepFemales, population));
    }
}
