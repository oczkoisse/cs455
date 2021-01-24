package cs455.hadoop.census.jobs.elderly;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;
import cs455.hadoop.census.data.Field;
import cs455.hadoop.census.data.FieldExtractor;
import cs455.hadoop.census.except.SegmentMismatchException;

public class ElderlyMapper extends Mapper<LongWritable, Text, Text, LongPair> {
	
	private Text state = new Text();
	private LongPair popWithElderly = new LongPair();
		
    @Override
    protected void map(LongWritable offset, Text segment, Context context) throws IOException, InterruptedException {
        String record = segment.toString();
        
        // Preliminary checks        
        try
        {
            int sl = (int) FieldExtractor.extract(record, Field.SUMMARY_LEVEL);
            
            if (sl != 100)
            	return;
            
        	state.set((String) FieldExtractor.extract(record, Field.STATE));

        	popWithElderly.setFirst((int) FieldExtractor.extract(record, Field.POPULATION));
        	popWithElderly.setSecond((int) FieldExtractor.extractAt(record, Field.AGE_GRP, Field.AGE_GRP.getInstances()));
        
        	context.write(state, popWithElderly);
        }
        catch(SegmentMismatchException e)
        {
        	// Continue with the next segment if segment does not have any of the needed fields
        }
    }
}
