package cs455.hadoop.census.jobs.analysis;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import cs455.hadoop.census.io.LongTriple;
import cs455.hadoop.census.data.Field;
import cs455.hadoop.census.data.FieldExtractor;
import cs455.hadoop.census.except.SegmentMismatchException;

public class AnalysisMapper extends Mapper<LongWritable, Text, Text, LongTriple> {
	
	private Text state = new Text();
	private LongTriple popWithSepMalesFemales = new LongTriple();
		
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
        	
        	long maleSeparated = (int) FieldExtractor.extract(record, Field.MALE_SEPARATED);
        	long femaleSeparated = (int) FieldExtractor.extract(record, Field.FEMALE_SEPARATED);
        	long population = (int) FieldExtractor.extract(record, Field.POPULATION);
        	
        	popWithSepMalesFemales.setFirst(maleSeparated);
        	popWithSepMalesFemales.setSecond(femaleSeparated);
        	popWithSepMalesFemales.setThird(population);
        
        	context.write(state, popWithSepMalesFemales);
        }
        catch(SegmentMismatchException e)
        {
        	// Continue with the next segment if segment does not have at least one of the needed fields
        	// Should improve so as to be able to combine fields across segments
        }
    }
}
