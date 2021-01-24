package cs455.hadoop.census.jobs.rooms;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PercentileReducer extends Reducer<DoubleWritable, Text, NullWritable, Text> {
	
	private List<Double> lst = new ArrayList<Double>();
	private List<String> sts = new ArrayList<String>();
	
    @Override
    protected void reduce(DoubleWritable av, Iterable<Text> states, Context context) throws IOException, InterruptedException {
        
    	for (Text t: states)
    	{
    		lst.add(av.get());
    		sts.add(t.toString());
    	}
    	
    }
    
    protected void cleanup(Context context) throws IOException, InterruptedException
    {
    	int index = (int) (0.95 * lst.size());
    	
        String result = sts.get(index);
        
        context.write(NullWritable.get(), new Text(result));
    }
}
