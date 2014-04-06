#! /usr/bin/env bash

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

java -cp $cp:dist/*.jar clustering.ClusteringDriver $TWISTER_HOME/bin/centroids.txt 256 $TWISTER_HOME/bin/cc.pf