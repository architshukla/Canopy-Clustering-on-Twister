#! /usr/bin/env bash

if [ $# -ne 1 ]; then
	echo "Usage: configure.sh NUMBER_OF_FILE_SPLITS"
	exit
fi

DIR=`pwd`

echo "Getting canopy centers..."
hadoop dfs -get /tmp/maxT/output1/part* 1.txt
echo "Removing leading tabs..."
./removeLeadingTabs.py 1.txt
rm 1.txt
mv 1.txt_out canopycenters.txt

cd $TWISTER_HOME/bin
./twister.sh rmdir cccenters
./twister.sh mkdir cccenters
echo "Copying canopy centers..."
./twister.sh put $DIR cccenters canopycenters.txt 1

cd $DIR
rm canopycenters.txt

echo "Getting data..."
hadoop dfs -get /tmp/maxT/output2/part* 1.txt
./loadData.sh 1.txt $1
rm 1.txt
