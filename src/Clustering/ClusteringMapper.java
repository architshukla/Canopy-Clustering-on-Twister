package Clustering;

import cgl.imr.base.Key;
import cgl.imr.base.MapOutputCollector;
import cgl.imr.base.MapTask;
import cgl.imr.base.SerializationException;
import cgl.imr.base.TwisterException;
import cgl.imr.base.Value;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.data.file.FileData;
import cgl.imr.types.BytesValue;
import cgl.imr.types.DoubleVectorData;
import cgl.imr.types.StringKey;

/**
 * Map task for the K-Means clustering.
 * 
 * @author Jaliya Ekanayake (jaliyae@gmail.com)
 * 
 */
public class ClusteringMapper implements MapTask {

	private FileData fileData;
	private DoubleVectorData vectorData;

	public void close() throws TwisterException {
		// TODO Auto-generated method stub
	}

	/**
	 * Loads the vector data from a file. Since the map tasks are cached
	 * across iterations, we only need to load this data  once for all
	 * the iterations.
	 */
	public void configure(JobConf jobConf, MapperConf mapConf)	throws TwisterException {
		this.vectorData = new DoubleVectorData();
		fileData = (FileData) mapConf.getDataPartition();
		try {
			vectorData.loadDataFromTextFile(fileData.getFileName());
		} catch (Exception e) {
			throw new TwisterException(e);
		}
	}

	public double getEuclidean2(double[] v1, double[] v2, int vecLen) {
		double sum = 0;
		for (int i = 0; i < vecLen; i++) {
			sum += ((v1[i] - v2[i]) * (v1[i] - v2[i]));
		}
		return sum; // No need to use the sqrt.
	}

	/**
	 * Map function for the K-means clustering. Calculates the Euclidean
	 * distance between the data points and the given cluster centers. Next it
	 * calculates the partial cluster centers as well.
	 */
	
	public void map(MapOutputCollector collector, Key key, Value val)
			throws TwisterException {

		double[][] data = vectorData.getData();
		DoubleVectorData cData = new DoubleVectorData();

		try {
			cData.fromBytes(val.getBytes());
			double[][] centroids = cData.getData();

			int numCentroids = cData.getNumData();
			int numData = vectorData.getNumData();
			int vecLen = vectorData.getVecLen();
			double newCentroids[][] = new double[numCentroids][vecLen + 1];

			for (int i = 0; i < numData; i++) {
				double min = 0;
				double dis = 0;
				int minCentroid = 0;
				for (int j = 0; j < numCentroids; j++) {
					dis = getEuclidean2(data[i], centroids[j], vecLen);
					if (j == 0) {
						min = dis;
					}
					if (dis < min) {
						min = dis;
						minCentroid = j;
					}
				}

				for (int k = 0; k < vecLen; k++) {
					newCentroids[minCentroid][k] += data[i][k];
				}
				newCentroids[minCentroid][vecLen] += 1;
			}

			/**
			 * additional location carries the number of partial points to a
			 * particular centroid.
			 */
			DoubleVectorData newCData = new DoubleVectorData(newCentroids,
					numCentroids, vecLen + 1);
			// This algorithm uses only one reduce task, so we only need one
			// key.
			collector.collect(new StringKey("kmeans-map-to-reduce-key"),
					new BytesValue(newCData.getBytes()));

		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
	}
}
