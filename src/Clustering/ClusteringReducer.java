package Clustering;

import java.util.List;

import cgl.imr.base.Key;
import cgl.imr.base.ReduceOutputCollector;
import cgl.imr.base.ReduceTask;
import cgl.imr.base.SerializationException;
import cgl.imr.base.TwisterException;
import cgl.imr.base.Value;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.ReducerConf;
import cgl.imr.types.BytesValue;
import cgl.imr.types.DoubleVectorData;

/**
 * Calculates the new centroids using the partial centroids.
 * 
 * @author Jaliya Ekanayake (jaliyae@gmail.com)
 * 
 */
public class ClusteringReducer implements ReduceTask {

	public void close() throws TwisterException {
		// TODO Auto-generated method stub
	}

	public void configure(JobConf jobConf, ReducerConf reducerConf)
			throws TwisterException {
	}

	public void reduce(ReduceOutputCollector collector, Key key, 
			List<Value> values) throws TwisterException {

		if (values.size() <= 0) {
			throw new TwisterException("Reduce input error no values.");
		}
		try {
			BytesValue val = (BytesValue) values.get(0);
			DoubleVectorData tmpCentroids = new DoubleVectorData();
			tmpCentroids.fromBytes(val.getBytes());

			int numData = tmpCentroids.getNumData();
			/**
			 * One additional location carries the count.
			 */
			int lenData = tmpCentroids.getVecLen() - 1;

			double[][] newCentroids = new double[numData][lenData];

			DoubleVectorData centroids = null;
			double[][] tmpCentroid;
			double[] counts = new double[numData];
			int numMapTasks = values.size();
			for (int i = 0; i < numMapTasks; i++) {
				val = (BytesValue) values.get(i);
				centroids = new DoubleVectorData();
				centroids.fromBytes(val.getBytes());
				tmpCentroid = centroids.getData();

				for (int j = 0; j < numData; j++) {
					for (int k = 0; k < lenData; k++) {
						newCentroids[j][k] += tmpCentroid[j][k];
					}
					/*
					 * Say data length is 2, then the tmpCentroid has 3 data
					 * points including the counts. from points 0,1,2 , 2 is the
					 * count.
					 */
					counts[j] += tmpCentroid[j][lenData];
				}
			}

			/**
			 * The results have already been divided by the total number of data
			 * points. So simply adding them is enough.
			 */

			for (int i = 0; i < numData; i++) {
				for (int j = 0; j < lenData; j++) {
					if (counts[i] != 0) {
						newCentroids[i][j] = (newCentroids[i][j]) / counts[i];
					}
				}
			}

			DoubleVectorData newCentroidData = 
				new DoubleVectorData(newCentroids, numData, lenData);
			collector.collect(key, new BytesValue(newCentroidData.getBytes()));
			
		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
	}
}
