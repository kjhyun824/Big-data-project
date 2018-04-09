#!/bin/bash

for (( i=1; i<=$1; i++))
do
	hdfs dfs -get output${i}
	hdfs dfs -rm -r output${i}
done

hdfs dfs -get /tmp/logs/bd20175052/logs
hdfs dfs -rm -r /tmp/logs/bd20175052/logs
