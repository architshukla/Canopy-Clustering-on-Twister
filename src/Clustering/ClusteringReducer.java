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

import types.DataPointVector;

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

			DataPointVector newCentroids = new DataPointVector(tmpCentroids.size());
			int numMapTasks = values.size();

			for(int i = 0; i < numMapTasks; i++) {
				val = (BytesValue) values.get(i);
				mapperCentroids = new DataPointVector();
				mapperCentroids.fromBytes(val.getBytes());

				for(int j = 0; j < newCentroids.size(); j++) {
					newCentroids.sumDataPointToElement(j, mapperCentroids.get(j));
					newCentroids.get(j).count += mapperCentroids.get(j).count;
				}
			}

			for(int i = 0; i < newCentroids.size(); i++) {
				newCentroids.get(i).averageDataPoint();
			}

			collector.collect(key, new BytesValue(newCentroids.getBytes()));
			
		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
	}
}