package cs455.hadoop.census.jobs.urbanrural;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import cs455.hadoop.census.io.LongTriple;
import cs455.hadoop.census.data.Field;
import cs455.hadoop.census.data.FieldExtractor;
import cs455.hadoop.census.except.SegmentMismatchException;

public class UrbanRuralMapper extends Mapper<LongWritable, Text, Text, LongTriple> {
	
	private Text state = new Text();
	private LongTriple housesWithUrbanRural = new LongTriple();
		
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
        	
        	long urbanInside = (int) FieldExtractor.extract(record, Field.HOUSES_URBAN_INSIDE);
        	long urbanOutside = (int) FieldExtractor.extract(record, Field.HOUSES_URBAN_OUTSIDE);
        	long rural = (int) FieldExtractor.extract(record, Field.HOUSES_RURAL);
        	long other= (int) FieldExtractor.extract(record, Field.HOUSES_OTHER);
        	
        	housesWithUrbanRural.setFirst(urbanInside + urbanOutside + rural + other);
        	housesWithUrbanRural.setSecond(urbanInside + urbanOutside);
        	housesWithUrbanRural.setThird(rural);
        
        	context.write(state, housesWithUrbanRural);
        }
        catch(SegmentMismatchException e)
        {
        	// Continue with the next segment if segment does not have at least one of the needed fields
        	// Should improve so as to be able to combine fields across segments
        }
    }
}
