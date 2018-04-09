OUTPUT_PATH=$1
ENCRYPTED_PASSWORD=$2
NUMBER_OF_SPLIT=$3

hdfs dfs -rm -r ${OUTPUT_PATH}*
hadoop jar PasswordCrackerInHadoop.jar PasswordCracker.CrackerDriver ${OUTPUT_PATH} ${ENCRYPTED_PASSWORD} ${NUMBER_OF_SPLIT}
