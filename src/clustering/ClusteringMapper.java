package clustering;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashMap;

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
import cgl.imr.types.StringKey;

import types.DataPoint;
import types.DataPointVector;

public class ClusteringMapper implements MapTask {

	private static final int CANOPY_CENTER = 0;
	private static final int DATA_POINT = 1;

	private FileData fileData;
	private DataPointVector [] data;
	private DataPointVector canopyCenters;

	public void close() throws TwisterException {
	}

	public void configure(JobConf jobConf, MapperConf mapConf)	throws TwisterException {
		// Allocate memory for Data Set and Canopy Centers
		data = new DataPointVector[2];
		data[CANOPY_CENTER] = new DataPointVector();
		data[DATA_POINT] = new DataPointVector();

		canopyCenters = new DataPointVector();
		fileData = (FileData) mapConf.getDataPartition();

		String line;
		BufferedReader reader;

		try {
			// Load Data Set
			String filename = fileData.getFileName();
			reader = new BufferedReader(new FileReader(filename));
			line = null;
			while((line = reader.readLine()) != null) {
				int tabPosition = line.indexOf("\t");
				// Canopy Center
				data[CANOPY_CENTER].add(new DataPoint(line.substring(0, tabPosition)));
				// Data Point
				data[DATA_POINT].add(new DataPoint(line.substring(tabPosition + 1)));
			}

			// Load Canopy Centers
			int dataPosition = filename.indexOf("data");
			String canopyCentersFile = filename.substring(0, dataPosition) + "data/" + ClusteringDriver.canopyCentersFileLocation;
			reader = new BufferedReader(new FileReader(canopyCentersFile));
			line = null;
			while((line = reader.readLine()) != null) {
				canopyCenters.add(new DataPoint(line));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TwisterException(e);
		}
	}

	public void map(MapOutputCollector collector, Key key, Value val)
	throws TwisterException {
		// Allocate memory for k-Means Centroids and HashMap
		DataPointVector kCentroids = new DataPointVector();
		HashMap<DataPoint, ArrayList<DataPoint>> canopyCenterKCentroidsMap = new HashMap<DataPoint, ArrayList<DataPoint>>();

		try {
			// Read broadcasted k-Centroids
			kCentroids.fromBytes(val.getBytes());
			
			// Set up the HashMap
			for(int i = 0; i < canopyCenters.size(); i++)
			{
				// For each Canopy Center, create an ArrayList of all Data Points within this Canopy
				ArrayList<DataPoint> centroidList = new ArrayList<DataPoint>();
	
				for(int j = 0; j < kCentroids.size(); j++)
				{
					// If a k-Means Centroid is within this Canopy, add it to the ArrayList
					if(canopyCenters.get(i).withinT1(kCentroids.get(j))) {
						centroidList.add(kCentroids.get(j));
					}
						
				}
				// Add all the k-Means Centroids in this Canopy to the HashMap as this Canopy Center's value
				if(centroidList.size() > 0)
					canopyCenterKCentroidsMap.put(canopyCenters.get(i), centroidList);
			}

			DataPointVector newCentroids = new DataPointVector(kCentroids.size());

			for(int k = 0; k < data[CANOPY_CENTER].size(); k++) {
				ArrayList<DataPoint> centroids = canopyCenterKCentroidsMap.get(data[CANOPY_CENTER].get(k));
				DataPoint dataPoint = data[DATA_POINT].get(k);
				if(centroids != null)
				{
					// Set the minimum distance to the maximum value a double can hold and create
					double minDistance = Double.MAX_VALUE;
					int offset = -1;

					for(int i = 0; i < centroids.size(); i++)
					{
						DataPoint centroid = centroids.get(i);
						double distance = dataPoint.complexDistance(centroid);

						// Check if the distance is less than the minimum distance found so far
						if(distance < minDistance)
						{
							minDistance = distance;
							offset = i;
						}
					}

					for(int j = 0; j < newCentroids.size(); j++) {
						if(centroids.get(offset).equals(kCentroids.get(j))) {
							newCentroids.sumDataPointToElement(j, dataPoint);
							newCentroids.get(j).incrementCounter();
						}
					}
				}
			}

			collector.collect(new StringKey("kmeans-map-to-reduce-key"),
					new BytesValue(newCentroids.getBytes()));

		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
	}
}
