package cs455.hadoop.census.jobs.age;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import cs455.hadoop.census.io.LongList;
import cs455.hadoop.census.data.Field;
import cs455.hadoop.census.data.FieldExtractor;
import cs455.hadoop.census.except.SegmentMismatchException;

public class AgeMapper extends Mapper<LongWritable, Text, Text, LongList> {
	
	private Text state = new Text();
	private LongList ageCounts = new LongList(null, 8);
		
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
        	
        	long group_male_18 = 0, group_male_19_29 = 0, group_male_30_39 = 0, group_male_rest = 0;
        	for(int i = 1; i <= Field.MALE_HISP_AGE_GRP.getInstances(); i++)
        	{
        		long group_count = (Integer) FieldExtractor.extractAt(record, Field.MALE_HISP_AGE_GRP, i);
        		if (i <= 13)
        			group_male_18 += group_count;
        		else if(i > 13 && i <= 18)
        			group_male_19_29 += group_count;
        		else if(i > 18 && i <= 20)
        			group_male_30_39 += group_count;
        		else
        			group_male_rest += group_count;
        	}
        	
        	ageCounts.set(0, group_male_18);
        	ageCounts.set(1, group_male_19_29);
        	ageCounts.set(2, group_male_30_39);
        	ageCounts.set(3, group_male_18 + group_male_19_29 + group_male_30_39 + group_male_rest);
        	
        	long group_female_18 = 0, group_female_19_29 = 0, group_female_30_39 = 0, group_female_rest = 0;
        	for(int i = 1; i <= Field.FEMALE_HISP_AGE_GRP.getInstances(); i++)
        	{
        		long group_count = (Integer) FieldExtractor.extractAt(record, Field.FEMALE_HISP_AGE_GRP, i);
        		if (i <= 13)
        			group_female_18 += group_count;
        		else if(i > 13 && i <= 18)
        			group_female_19_29 += group_count;
        		else if(i > 18 && i <= 20)
        			group_female_30_39 += group_count;
        		else
        			group_female_rest += group_count;
        	}
        	
        	ageCounts.set(4, group_female_18);
        	ageCounts.set(5, group_female_19_29);
        	ageCounts.set(6, group_female_30_39);
        	ageCounts.set(7, group_female_18 + group_female_19_29 + group_female_30_39 + group_female_rest);
        	
        	context.write(state, ageCounts);
        }
        catch(SegmentMismatchException e)
        {
        	// Continue with the next segment if segment does not have any of the needed fields
        }
    }
}
