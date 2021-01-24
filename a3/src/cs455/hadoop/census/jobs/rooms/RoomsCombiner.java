package cs455.hadoop.census.jobs.rooms;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;

public class RoomsCombiner extends Reducer<Text, LongPair, Text, LongPair> {
    @Override
    protected void reduce(Text state, Iterable<LongPair> roomHouseCounts, Context context) throws IOException, InterruptedException {
    	
    	long houseCount = 0, roomCount = 0;
        
        for(LongPair p : roomHouseCounts){
        	roomCount += p.getFirst();
        	houseCount += p.getSecond();
            
        }   
        context.write(state, new LongPair(roomCount, houseCount));
    }
}
