# SOURCE
APRIORI=src/apriori
TRIE=src/trie
LIST=src/list
UTILS=src/utils

APRIORI_CODE=$(addsuffix .java, AprioriDriver AprioriPass1Mapper AprioriPassKMapper AprioriReducer)
LIST_CODE=$(addsuffix .java, ItemSet Transaction)
TRIE_CODE=$(addsuffix .java, Trie TrieNode)
UTILS_CODE=$(addsuffix .java, AprioriUtils)

SOURCE_CODE=$(addprefix ${APRIORI}/, ${APRIORI_CODE}) $(addprefix ${LIST}/, ${LIST_CODE}) $(addprefix ${TRIE}/, ${TRIE_CODE}) $(addprefix ${UTILS}/, ${UTILS_CODE})

# DATA
DATASET=dataSet
DATA=$(patsubst ${DATASET}/%,%,$(wildcard ${DATASET}/*))
OUTPUT=output
OUTPUT_FILE=$(addprefix ${OUTPUT}/, ${DATA})

# DEBUG
#.IGNORE: ${DATA}

# RULES

all: add_manifest
env: add_manifest dataSet output

.PHONY: dataSet
dataSet:
	hdfs dfs -put -f $@
clean_dataSet:
	hdfs dfs -rm -f -r dataSet

classes: 
	mkdir -p classes
HadoopBasedApriori.jar: classes ${SOURCE_CODE}
	javac -classpath $$(${HADOOP_HOME}/bin/hadoop classpath) -d classes ${SOURCE_CODE}
add_manifest: HadoopBasedApriori.jar classes
	jar -cvf HadoopBasedApriori.jar -C classes .
clean:
	rm -rf classes/*
	rm -f HadoopBasedApriori.jar

.PHONY: output
output:
	mkdir -p ${OUTPUT}
	hdfs dfs -mkdir -p output

.PHONY: ${DATA}
${DATA}: add_manifest
ifndef NUM_PASS
	$(error NUM_PASS is undefined)
endif
ifndef MIN_SUP
	$(error MIN_SUP is undefined)
endif
ifndef NUM_TRANSACTION
	$(error NUM_TRANSACTION is undefined)
endif
	rm -rf ${OUTPUT}/$@/NUM_PASS_${NUM_PASS}
	hdfs dfs -rm -f -r ${OUTPUT}/$@/NUM_PASS_${NUM_PASS}
	mkdir -p ${OUTPUT}/$@/NUM_PASS_${NUM_PASS}
	hdfs dfs -mkdir ${OUTPUT}/$@/NUM_PASS_${NUM_PASS}
	hadoop jar HadoopBasedApriori.jar apriori.AprioriDriver ${DATASET}/$@ ${OUTPUT}/$@/NUM_PASS_${NUM_PASS}/$@ ${NUM_PASS} ${MIN_SUP} ${NUM_TRANSACTION}
	hdfs dfs -get ${OUTPUT}/$@/NUM_PASS_${NUM_PASS} ${OUTPUT}/$@
clean_output:
	rm -rf ${OUTPUT}
	hdfs dfs -rm -f -r output

.PHONY: ls
ls: 
	hdfs dfs -ls

.PHONY: distclean
distclean: clean clean_dataSet clean_output
