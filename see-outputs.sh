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
