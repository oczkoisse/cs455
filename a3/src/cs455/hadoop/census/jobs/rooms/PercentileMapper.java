package cs455.hadoop.census.jobs.rooms;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;


public class PercentileMapper extends Mapper<LongWritable, Text, DoubleWritable, Text> {
	
	private Text state = new Text();
	private DoubleWritable av = new DoubleWritable();
		
    @Override
    protected void map(LongWritable offset, Text line, Context context) throws IOException, InterruptedException {
        String l = line.toString();
        
        String[] parts = l.split(",");
        
        state.set(parts[0]);
        av.set(Double.parseDouble(parts[1]));
        
        context.write(av, state);
    }
}
