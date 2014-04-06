package clustering;

import java.util.Iterator;
import java.util.Map;

import cgl.imr.base.Combiner;
import cgl.imr.base.Key;
import cgl.imr.base.SerializationException;
import cgl.imr.base.TwisterException;
import cgl.imr.base.Value;
import cgl.imr.base.impl.JobConf;
import cgl.imr.types.BytesValue;

import types.DataPointVector;

public class ClusteringCombiner implements Combiner {

	DataPointVector results;

	public ClusteringCombiner() {
		results = new DataPointVector();
	}

	public void close() throws TwisterException {
	}

	public void combine(Map<Key, Value> keyValues) throws TwisterException {
		assert (keyValues.size() == 1); // There should be a single value here.
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
	}

	public DataPointVector getResults() {
		return results;
	}
}
