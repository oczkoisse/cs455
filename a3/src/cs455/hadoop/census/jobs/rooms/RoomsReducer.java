package cs455.hadoop.census.jobs.rooms;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;

public class RoomsReducer extends Reducer<Text, LongPair, NullWritable, Text> {
    @Override
    protected void reduce(Text state, Iterable<LongPair> roomHouseCounts, Context context) throws IOException, InterruptedException {
        
        long houseCount = 0, roomCount = 0;
        
        for(LongPair p : roomHouseCounts){
        	roomCount += p.getFirst();
        	houseCount += p.getSecond();
            
        }
        
        Double average = roomCount / (double) houseCount;
        String result = state.toString() + ", " + average.toString();
        
        context.write(NullWritable.get(), new Text(result));
    }
}
