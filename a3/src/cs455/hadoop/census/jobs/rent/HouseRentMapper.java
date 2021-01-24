package cs455.hadoop.census.jobs.rent;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SortedMapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

import cs455.hadoop.census.data.Field;
import cs455.hadoop.census.data.FieldExtractor;
import cs455.hadoop.census.except.SegmentMismatchException;

public class HouseRentMapper extends Mapper<LongWritable, Text, Text, SortedMapWritable>  {
	
	private Text state = new Text();
		
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
        	
        	SortedMapWritable houseRentGroups = new SortedMapWritable();
        	        			
        	for(int i = 1; i <= Field.HOUSE_RENT_GRP.getInstances(); i++)
        	{
        		LongWritable houseCount = new LongWritable( (int) FieldExtractor.extractAt(record, Field.HOUSE_RENT_GRP, i));
        		houseRentGroups.put(new IntWritable(i), houseCount);
        	}
        	
        	context.write(state, houseRentGroups);
        }
        catch(SegmentMismatchException e)
        {
        	// Continue with the next segment if segment does not have any of the needed fields
        }
    }
}
