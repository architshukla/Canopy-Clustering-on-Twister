#! /usr/bin/env bash

java -cp dist/*.jar dataops.SplitData $1 $2 $3 $4

OUTPUTFOLDER=output
FILEFILTER=input

if [ $3 ]; then
	OUTPUTFOLDER=$3
fi

if [ $4 ]; then
	FILEFILTER=$4
fi

DIR=`pwd`

cd $TWISTER_HOME/bin
./twister.sh rmdir cc
./twister.sh mkdir cc

echo "Copying to dfs..."
echo ./twister.sh put $DIR/$OUTPUTFOLDER cc $FILEFILTER 8
./twister.sh put $DIR/$OUTPUTFOLDER cc $FILEFILTER 8

echo "Removing existing partifion file..."
rm cc.pf

echo "Creating partition file..."
./create_partition_file.sh cc $FILEFILTER cc.pf

cd $DIR

echo "Removing local files in '$OUTPUTFOLDER'..."
rm -rf $OUTPUTFOLDER

echo Done.