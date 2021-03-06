/**
  * @author Archit Shukla
  */
package ccimr.clustering;

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

import ccimr.types.DataPoint;
import ccimr.types.DataPointVector;

public class ClusteringReducer implements ReduceTask {

	public void close() throws TwisterException {
	}

	public void configure(JobConf jobConf, ReducerConf reducerConf)
			throws TwisterException {
	}

	public void reduce(ReduceOutputCollector collector, Key key, List<Value> values)
	throws TwisterException {
		if (values.size() <= 0) {
			throw new TwisterException("Reduce input error no values.");
		}

		try {
			DataPointVector mapperCentroids;
			BytesValue val = (BytesValue) values.get(0);
			DataPointVector tmpCentroids = new DataPointVector();
			tmpCentroids.fromBytes(val.getBytes());

			DataPointVector newCentroids = new DataPointVector();
			long temp[][] = new long[tmpCentroids.size()][3];
			for(int i = 0; i < tmpCentroids.size(); i++) {
				temp[i][0] = temp[i][1] = temp[i][2] = 0;
			}
			int numMapTasks = values.size();

			for(int i = 0; i < numMapTasks; i++) {
				val = (BytesValue) values.get(i);
				mapperCentroids = new DataPointVector();
				mapperCentroids.fromBytes(val.getBytes());

				for(int j = 0; j < tmpCentroids.size(); j++) {
					temp[j][0] += mapperCentroids.get(j).year;
					temp[j][1] += mapperCentroids.get(j).temperature;
					temp[j][2] += mapperCentroids.get(j).count;
					// newCentroids.sumDataPointToElement(j, mapperCentroids.get(j));
					// newCentroids.get(j).count += mapperCentroids.get(j).count;
				}
			}

			for(int i = 0; i < tmpCentroids.size(); i++) {
				if(temp[i][2]!=0) {
					temp[i][0] /= temp[i][2];
					temp[i][1] /= temp[i][2];
					newCentroids.add(new DataPoint(temp[i][0]+","+temp[i][1]));
				}
				// newCentroids.get(i).averageDataPoint();
			}

			collector.collect(key, new BytesValue(newCentroids.getBytes()));
			
		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
	}
}