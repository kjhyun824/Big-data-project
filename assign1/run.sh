#!/bin/bash

declare -a TEST_SET=("25ed1bcb423b0b7200f485fc5ff71c8e" "02c425157ecd32f259548b33402ff6d3" "453e41d218e071ccfb2d1c99ce23906a")
declare -a PASSWD_LEN=("2" "4" "6")
THREAD_LIST="4 8 16"

for thr in $THREAD_LIST; do
	for i in {0..2}; do
		for j in {1..5}; do
			echo "(time java -jar build/jar/PasswordCrackerMultiThread.jar ${thr} ${PASSWD_LEN[$i]} false ${TEST_SET[$i]}) &>> ./passwd_result/passwd_thr${thr}_len${PASSWD_LEN[$i]}_result.txt"
			(time java -jar build/jar/PasswordCrackerMultiThread.jar ${thr} ${PASSWD_LEN[$i]} false ${TEST_SET[$i]}) &>> ./passwd_result/passwd_thr${thr}_len${PASSWD_LEN[$i]}_result.txt
			echo &>> passwd_thr${thr}_result.txt
			sleep 5 
		done
	done
done
