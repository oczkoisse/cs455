package cs455.hadoop.census.jobs.rooms;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;
import cs455.hadoop.census.data.Field;
import cs455.hadoop.census.data.FieldExtractor;
import cs455.hadoop.census.except.SegmentMismatchException;

public class RoomsMapper extends Mapper<LongWritable, Text, Text, LongPair> {
	
	private Text state = new Text();
	private LongPair roomHouseCounts = new LongPair();
		
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
        	
        	long houseCount = 0, roomCount = 0;
        	for(int i=1; i<=Field.HOUSE_ROOMS_GRP.getInstances(); i++)
        	{
        		int houseCountInGrp = (int) FieldExtractor.extractAt(record, Field.HOUSE_ROOMS_GRP, i);
        		roomCount += i * houseCountInGrp;
        		houseCount += houseCountInGrp;
        	}
        	
        	roomHouseCounts.setFirst(roomCount);
        	roomHouseCounts.setSecond(houseCount);
        
        	context.write(state, roomHouseCounts);
        }
        catch(SegmentMismatchException e)
        {
        	// Continue with the next segment if segment does not have any of the needed fields
        }
    }
}