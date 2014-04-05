package Clustering;

import java.util.Iterator;
import java.util.Map;

import cgl.imr.base.Combiner;
import cgl.imr.base.Key;
import cgl.imr.base.SerializationException;
import cgl.imr.base.TwisterException;
import cgl.imr.base.Value;
import cgl.imr.base.impl.JobConf;
import cgl.imr.types.BytesValue;
import cgl.imr.types.DoubleVectorData;

/**
 * Convert the set of bytes representing the Value object into a ByteValue.
 * 
 * @author Jaliya Ekanayake (jaliyae@gmail.com)
 * 
 */
public class ClusteringCombiner implements Combiner {

	DoubleVectorData results;

	public ClusteringCombiner() {
		results = new DoubleVectorData();
	}

	public void close() throws TwisterException {
		// TODO Auto-generated method stub
	}

	/***
	 * Combines the reduce outputs to a single value.
	 */
	public void combine(Map<Key, Value> keyValues) throws TwisterException {
		assert (keyValues.size() == 1);// There should be a single value here.
		Iterator<Key> ite = keyValues.keySet().iterator();
		Key key = ite.next();
		BytesValue val = (BytesValue) keyValues.get(key);
		try {
			this.results.fromBytes(val.getBytes());
		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
	}

	public void configure(JobConf jobConf) throws TwisterException {
		// TODO Auto-generated method stub

	}

	public DoubleVectorData getResults() {
		return results;
	}
}
