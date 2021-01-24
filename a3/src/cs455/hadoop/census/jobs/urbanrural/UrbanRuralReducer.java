package cs455.hadoop.census.jobs.urbanrural;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongTriple;
import cs455.hadoop.census.io.DoublePair;

public class UrbanRuralReducer extends Reducer<Text, LongTriple, Text, Text> {
    @Override
    protected void reduce(Text state, Iterable<LongTriple> housesWithUrbanRural, Context context) throws IOException, InterruptedException {
        long houses = 0, urban = 0, rural = 0;
        
        for(LongTriple p : housesWithUrbanRural){
        	houses += p.getFirst();
        	urban += p.getSecond();
        	rural += p.getThird();
            
        }  
        
        context.write(state, new Text("\n" +
				  					  "Urban%: " + urban * 100.0 / houses + "\n" +
									  "Rural%: " + rural * 100.0 / houses + "\n"
									  ));
    }
}