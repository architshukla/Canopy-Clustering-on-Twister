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
./twister.sh cpj $DIR/dist/canopyclustering_twister.jar

cd $PWD

# java -cp $cp:dist/canopyclustering_twister.jar Clustering.ClusteringDriver $TWISTER_HOME/bin/init_clusters.txt 80 $TWISTER_HOME/bin/kmeans.pf
java -cp $cp:dist/canopyclustering_twister.jar Clustering.ClusteringDriver $TWISTER_HOME/bin/centroids.txt 16 $TWISTER_HOME/bin/cc.pf