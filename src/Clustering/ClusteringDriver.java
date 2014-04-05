package Clustering;

import java.io.IOException;

import cgl.imr.base.TwisterMonitor;
import cgl.imr.base.impl.JobConf;
import cgl.imr.client.TwisterDriver;
import cgl.imr.types.DoubleVectorData;

/**
 * Implements K-means clustering algorithm using MapReduce programming model.
 * <p>
 * <code>
 * K-means Clustering Algorithm for MapReduce
 * 	Do
 * 	Broadcast Cn 
 * 	[Perform in parallel] the map() operation
 * 	for each Vi
 * 		for each Cn,j
 * 	Dij <= Euclidian (Vi,Cn,j)
 * 	Assign point Vi to Cn,j with minimum Dij		
 * 	for each Cn,j
 * 		Cn,j <=Cn,j/K
 * 	
 * 	[Perform Sequentially] the reduce() operation
 * 	Collect all Cn
 * 	Calculate new cluster centers Cn+1
 * 	Diff<= Euclidian (Cn, Cn+1)
 * 	while (Diff <THRESHOLD)
	 * </code>
 * <p>
 * The MapReduce algorithm we used is shown below. (Assume that the input is
 * already partitioned and available in the compute nodes). In this algorithm,
 * Vi refers to the ith vector, Cn,j refers to the jth cluster center in nth
 * iteration, Dij refers to the Euclidian distance between ith vector and jth
 * cluster center, and K is the number of cluster centers.
 * 
 * @author Jaliya Ekanayake (jaliyae@gmail.com)
 */
public class ClusteringDriver {

	public static String DATA_FILE_SUFFIX = ".txt";
	public static int NUM_LOOPS = 16;
	public static String PROP_VEC_DATA_FILE = "prop_vec_data_file";
	public static int THRESHOLD = 1;

	/**
	 * Main program to run K-means clustering.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			String errorReport = "KMeansClustering: the Correct arguments are \n"
					+ "java cgl.imr.samples.kmeans.KmeansClustering "
					+ "<centroid file> <num map tasks> <partition file>";
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
			System.out.println("Kmeans clustering took "
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
		JobConf jobConf = new JobConf("kmeans-map-reduce");
		jobConf.setMapperClass(ClusteringMapper.class);
		jobConf.setReducerClass(ClusteringReducer.class);
		jobConf.setCombinerClass(ClusteringCombiner.class);
		jobConf.setNumMapTasks(numMapTasks);
		jobConf.setNumReduceTasks(numReducers);
		//jobConf.setFaultTolerance();

		TwisterDriver driver = new TwisterDriver(jobConf);
		driver.configureMaps(partitionFile);

		DoubleVectorData cData = new DoubleVectorData();
		try {
			cData.loadDataFromTextFile(centroidFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		double totalError = 0;
		int loopCount = 0;
		TwisterMonitor monitor = null;

		@SuppressWarnings("unused")
		// Use this with the while loop.
		//for (loopCount = 0; loopCount < NUM_LOOPS; loopCount++) {
		
		//Main iteration for K-Means clustering
		boolean complete = false;
		while (!complete) {		
			monitor = driver.runMapReduceBCast(cData);
			monitor.monitorTillCompletion();
			DoubleVectorData newCData = ((ClusteringCombiner) driver.getCurrentCombiner()).getResults();
			totalError = getError(cData, newCData);
			cData = newCData;
			if (totalError < THRESHOLD) {
				complete = true;
				break;
			}			
			loopCount++;
		}
		// Print the test statistics
		double timeInSeconds = ((double) (System.currentTimeMillis() - beforeTime)) / 1000;
		double[][] selectedCentroids = cData.getData();
		int numCentroids = cData.getNumData();
		int vecLen = cData.getVecLen();

		for (int i = 0; i < numCentroids; i++) {
			for (int j = 0; j < vecLen; j++) {
				System.out.print(selectedCentroids[i][j] + " , ");
			}
			System.out.println();
		}
		System.out.println("Total Time for kemeans : " + timeInSeconds);
		System.out.println("Total loop count : " + (loopCount));
		// Close the TwisterDriver. This will close the broker connections and
		driver.close();
	}

	private double getError(DoubleVectorData cData, DoubleVectorData newCData) {
		double totalError = 0;
		int numCentroids = cData.getNumData();

		double[][] centroids = cData.getData();
		double[][] newCentroids = newCData.getData();

		for (int i = 0; i < numCentroids; i++) {
			totalError += getEuclidean(centroids[i], newCentroids[i], cData
					.getVecLen());
		}
		return totalError;
	}

	/**
	 * Calculates the square value of the Euclidean distance. Although K-means
	 * clustering typically uses Euclidean distance, the use of its square value
	 * does not change the algorithm or the final results. Calculation of square
	 * root is costly. square value
	 * 
	 * @param v1
	 *            - First vector.
	 * @param v2
	 *            - Second vector.
	 * @param vecLen
	 *            - Length of the vectors.
	 * @return - Square of the Euclidean distances.
	 */
	private double getEuclidean(double[] v1, double[] v2, int vecLen) {
		double sum = 0;
		for (int i = 0; i < vecLen; i++) {
			sum += ((v1[i] - v2[i]) * (v1[i] - v2[i]));
		}
		return sum;
	}
}
