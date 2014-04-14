/**
  * @author Archit Shukla
  */
package ccimr.clustering;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;

import cgl.imr.base.TwisterMonitor;
import cgl.imr.base.impl.JobConf;
import cgl.imr.client.TwisterDriver;

import ccimr.types.DataPoint;
import ccimr.types.DataPointVector;

public class ClusteringDriver {

	public static String canopyCentersFileLocation = "cccenters/canopycenters.txt";

	/**
	 * Main program to run Canopy Clustering.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)
	throws Exception {
		if (args.length != 3) {
			String errorReport = "CanopyClustering: the Correct arguments are \n"
					+ "clustering.ClusteringDriver <centroid file> <num map tasks> <partition file>";
			System.out.println(errorReport);
			System.exit(0);
		}
		String centroidFile = args[0];
		int numMapTasks = Integer.parseInt(args[1]);
		String partitionFile = args[2];

		ClusteringDriver client;
		try {
			client = new ClusteringDriver();
			double beginTime = System.currentTimeMillis();
			client.driveMapReduce(partitionFile, numMapTasks, centroidFile);
			double endTime = System.currentTimeMillis();
			System.out
					.println("------------------------------------------------------");
			System.out.println("Canopy Clustering took "
					+ (endTime - beginTime) / 1000 + " seconds.");
			System.out
					.println("------------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void driveMapReduce(String partitionFile, int numMapTasks,
			String centroidFile) throws Exception {
		long beforeTime = System.currentTimeMillis();
		int numReducers = 1; // we need only one reducer for the above

		// JobConfigurations
		JobConf jobConf = new JobConf("canopyclustering-map-reduce");
		jobConf.setMapperClass(ClusteringMapper.class);
		jobConf.setReducerClass(ClusteringReducer.class);
		jobConf.setCombinerClass(ClusteringCombiner.class);
		jobConf.setNumMapTasks(numMapTasks);
		jobConf.setNumReduceTasks(numReducers);
		//jobConf.setFaultTolerance();

		TwisterDriver driver = new TwisterDriver(jobConf);
		driver.configureMaps(partitionFile);

		DataPointVector centroids = new DataPointVector();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(centroidFile));
			String line = null;
			while((line = reader.readLine()) != null) {
				centroids.add(new DataPoint(line));
			}
			System.out.println("Centroids: " + centroids);
		} catch (IOException e) {
			e.printStackTrace();
		}

		double totalError = 0;
		int loopCount = 0;
		TwisterMonitor monitor = null;

		//Main iteration for K-Means clustering
		boolean complete = false;
		while (!complete) {		
			monitor = driver.runMapReduceBCast(centroids);
			monitor.monitorTillCompletion();
			DataPointVector newCentroids = ((ClusteringCombiner) driver.getCurrentCombiner()).getResults();
			totalError = getError(centroids, newCentroids);
			centroids = newCentroids;
			if (totalError < DataPoint.CONVERGENCE_THRESHOLD) {
				complete = true;
				break;
			}
			loopCount++;
		}
		// Print the test statistics
		double timeInSeconds = ((double) (System.currentTimeMillis() - beforeTime)) / 1000;
		System.out.println("Selected Centroids: " + centroids);
		System.out.println("Total Time for Canopy Clustering : " + timeInSeconds);
		System.out.println("Total loop count : " + (loopCount + 1));
		// Close the TwisterDriver. This will close the broker connections.
		driver.close();
	}

	public double getError(DataPointVector cData, DataPointVector newCData) {
		double totalError = 0;
		for(int i = 0; i < cData.size(); i++) {
			totalError += cData.get(i).complexDistance(newCData.get(i));
		}
		return totalError;
	}
}