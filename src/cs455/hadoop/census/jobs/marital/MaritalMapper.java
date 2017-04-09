package cs455.hadoop.census.jobs.marital;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;
import cs455.hadoop.census.io.LongTriple;
import cs455.hadoop.census.data.Field;
import cs455.hadoop.census.data.FieldExtractor;
import cs455.hadoop.census.except.SegmentMismatchException;

public class MaritalMapper extends Mapper<LongWritable, Text, Text, LongTriple> {
	
	private Text state = new Text();
	private LongTriple popWithMaritalCounts = new LongTriple();
		
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
        	
        	popWithMaritalCounts.setFirst((int) FieldExtractor.extract(record, Field.POPULATION));
        	popWithMaritalCounts.setSecond((int) FieldExtractor.extract(record, Field.MALE_NEVER_MARRIED));
        	popWithMaritalCounts.setThird((int) FieldExtractor.extract(record, Field.FEMALE_NEVER_MARRIED));
        
        	context.write(state, popWithMaritalCounts);
        }
        catch(SegmentMismatchException e)
        {
        	// Continue with the next segment if segment does not have at least one of the needed fields
        	// Should improve so as to be able to combine fields across segments
        }
    }
}
