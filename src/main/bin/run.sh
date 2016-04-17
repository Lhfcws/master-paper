#!/bin/sh
print_usage ()
{
     echo "Usage: sh run.sh COMMAND"
     echo "where COMMAND is one of the follows:"
     exit 1
   }

if [ $# = 0 ] || [ $1 = "help" ]; then
  print_usage
fi

COMMAND=$1
shift

if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi


JAVA=$JAVA_HOME/bin/java
HEAP_OPTS="-Xmx6000m -XX:PermSize=128m -XX:MaxPermSize=512m"

CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar
CLASSPATH=${CLASSPATH}:conf
CLASSPATH=${CLASSPATH}:`ls |grep jar |grep dependencies`

for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

# echo $CLASSPATH

# Sentiment
if [ "$COMMAND" = "sntLRTrain" ]; then
  CLASS=com.datatub.hornbill.mllib.text.sentiment.shorttext.tools.LinearRegressionTrainerCli
elif [ "$COMMAND" = "hdfs" ]; then
  CLASS=paper.test.OnlineHDFS
elif [ "$COMMAND" = "community" ]; then
  CLASS=paper.CommunityRunner
else
  CLASS=$COMMAND
fi

params=$@
"$JAVA" -Djava.awt.headless=true $HEAP_OPTS -classpath "$CLASSPATH" $CLASS $params
