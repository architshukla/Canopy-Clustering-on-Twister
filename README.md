Canopy Clustering on Twister (Java)
===================================

This project implements the canopy clustering algorithm on [Twister - Iterative MapReduce](http://www.iterativemapreduce.org/).

## Pre-requisites
* Twister environment, including TWISTER_HOME environment variable
* Narada Brokering or ActiveMQ with their corresponding environment variables
* Hadoop environment
* [Canopy Clustering (Java)](http://www.bitbucket.org/architshukla/canopy-clustering-on-hadoop-java)

## Building the Project
The project comes with a Makefile.
#### To build the project type in
> make
#### To clean the build
> make clean

## Running the Project
The project is designed to be used as a fast alternative for the step 3 of the MapReduce algorithm for canopy clustering already implemented on Hadoop.
#### Configuring the project
This step is to import files from HDFS to Twister's Distributed File System
> configure.sh SIZE_OF_FILE_SPLITS
To run
> run.sh NUMBER_OF_MAPPERS