export HADOOP_CONF_DIR=$HOME/client-config/

echo -e "Configuration directory: ${HADOOP_CONF_DIR}\n"

CENSUS_DIR=/data/census

# Clean
echo -e "Cleaning previous outputs\n"
$HADOOP_HOME/bin/hdfs dfs -rm -r -f /home/outputs/*

# Run jobs one after the other
echo -e "Running job for question 1\n"
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.tenure.TenureJob $CENSUS_DIR /home/outputs/output1 &

echo -e "Running job for question 2\n"
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.marital.MaritalJob $CENSUS_DIR /home/outputs/output2 &

echo -e "Running job for question 3\n" &
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.age.AgeJob $CENSUS_DIR /home/outputs/output3 &

echo -e "Running job for question 4\n" &
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.urbanrural.UrbanRuralJob $CENSUS_DIR /home/outputs/output4 &

echo -e "Running job for question 5\n" &
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.housevalue.HouseValueJob $CENSUS_DIR /home/outputs/output5 &

echo -e "Running job for question 6\n" &
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.rent.HouseRentJob $CENSUS_DIR /home/outputs/output6 &

echo -e "Running job for question 7\n" &
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.rooms.RoomsJob $CENSUS_DIR /home/outputs/output7 /home/outputs/temp &

echo -e "Running job for question 8\n" &
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.elderly.ElderlyJob $CENSUS_DIR /home/outputs/output8 &

echo -e "Running job for question 9\n" &
$HADOOP_HOME/bin/hadoop jar dist/job.jar cs455.hadoop.census.jobs.analysis.AnalysisJob $CENSUS_DIR /home/outputs/output9 &
 
echo -e "Waiting\n"
wait

# Show the outputs
echo -e "Question 1 output\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output1/part-r-00000
read -s -n 1

echo -e "Question 2 output\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output2/part-r-00000
read -s -n 1

echo -e "Question 3 output\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output3/part-r-00000
read -s -n 1

echo -e "Question 4 output\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output4/part-r-00000
read -s -n 1

echo -e "Question 5 output\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output5/part-r-00000
read -s -n 1

echo -e "Question 6 output\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output6/part-r-00000
read -s -n 1

echo -e "Question 7 output (2 files)\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/temp/part-r-00000
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output7/part-r-00000
read -s -n 1

echo -e "Question 8 output\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output8/part-r-00000
read -s -n 1

echo -e "Question 9 output\n"
$HADOOP_HOME/bin/hdfs dfs -cat /home/outputs/output9/part-r-00000
read -s -n 1
