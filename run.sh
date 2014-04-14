#! /usr/bin/env bash

if [ $# -ne 1 ]; then
	echo "Usage: run.sh NUMBER_OF_MAPPERS"
	exit
fi

DIR=`pwd`

echo $DIR

cp=$TWISTER_HOME/bin:.

for i in ${TWISTER_HOME}/lib/*.jar;
  do cp=$i:${cp}
done

for i in ${TWISTER_HOME}/apps/*.jar;
  do cp=$i:${cp}
done

cd $TWISTER_HOME/bin
./twister.sh cpj $DIR/dist/*.jar

cd $PWD

java -cp $cp:dist/*.jar ccimr.clustering.ClusteringDriver $TWISTER_HOME/bin/centroids.txt $1 $TWISTER_HOME/bin/cc.pf
