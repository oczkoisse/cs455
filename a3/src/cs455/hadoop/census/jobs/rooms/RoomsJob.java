package cs455.hadoop.census.jobs.rooms;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

import cs455.hadoop.census.io.LongPair;
import cs455.hadoop.census.io.DoublePair;


/**
 * This is the main class. Hadoop will invoke the main method of this class.
 */
public class RoomsJob {
    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();
            // Give the MapRed job a name. You'll see this name in the Yarn webapp.
            Job job = Job.getInstance(conf, "q7");
            // Current class.
            job.setJarByClass(RoomsJob.class);
            // Mapper
            job.setMapperClass(RoomsMapper.class);
            // Combiner.
            job.setCombinerClass(RoomsCombiner.class);
            // Reducer
            job.setReducerClass(RoomsReducer.class);
            // Outputs from the Mapper.
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(LongPair.class);
            // Outputs from Reducer. It is sufficient to set only the following two properties
            // if the Mapper and Reducer has same key and value types. It is set separately for
            // elaboration.
            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);
            // path to input in HDFS
            FileInputFormat.addInputPath(job, new Path(args[0]));
            // path to output in HDFS
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            // Block until the job is completed.
            if (job.waitForCompletion(true));
            {
	            Configuration conf2 = new Configuration();
	            // Give the MapRed job a name. You'll see this name in the Yarn webapp.
	            Job job2 = Job.getInstance(conf2, "q7_1");
	            // Current class.
	            job2.setJarByClass(RoomsJob.class);
	            // Mapper
	            job2.setMapperClass(PercentileMapper.class);
	            // Reducer
	            job2.setReducerClass(PercentileReducer.class);
	            // Outputs from the Mapper.
	            job2.setMapOutputKeyClass(DoubleWritable.class);
	            job2.setMapOutputValueClass(Text.class);
	            // Outputs from Reducer. It is sufficient to set only the following two properties
	            // if the Mapper and Reducer has same key and value types. It is set separately for
	            // elaboration.
	            job2.setOutputKeyClass(NullWritable.class);
	            job2.setOutputValueClass(Text.class);
	            
	            job2.setNumReduceTasks(1);
	            
	            // path to input in HDFS
	            FileInputFormat.addInputPath(job2, new Path(args[2]));
	            // path to output in HDFS
	            FileOutputFormat.setOutputPath(job2, new Path(args[1]));
	            // Block until the job is completed.
	            System.exit(job2.waitForCompletion(true) ? 0 : 1);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }

    }
}
