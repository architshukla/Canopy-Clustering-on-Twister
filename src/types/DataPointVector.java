/**
  * @author Archit Shukla
  */

package types;

import java.io.IOException;
import java.io.EOFException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.util.ArrayList;

import cgl.imr.base.Value;
import cgl.imr.base.SerializationException;

import types.DataPoint;

public class DataPointVector implements Value {
	private ArrayList<DataPoint> dataPoints;

	public DataPointVector() {
		dataPoints = new ArrayList<DataPoint>();
	}

	public DataPointVector(int length) {
		dataPoints = new ArrayList<DataPoint>();
		for(int i = 0; i < length; i++) {
			this.add(new DataPoint());
		}
	}

	public DataPointVector(DataPointVector dataPointVector) {
		dataPoints = new ArrayList<DataPoint>();
		for(int i = 0; i < dataPointVector.size(); i++) {
			this.add(dataPointVector.get(i));
		}
	}

	public byte[] getBytes() 
	throws SerializationException {
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();

		DataOutputStream dOutputStream = new DataOutputStream(bOutputStream);
		byte[] marshalledBytes = null;

		try {
			for(int i = 0; i < dataPoints.size(); i++) {
				this.get(i).writeBytesToDataOutputStream(dOutputStream);
			}	
			dOutputStream.flush();
			marshalledBytes = bOutputStream.toByteArray();
			bOutputStream = null;
			dOutputStream = null;
		} catch (IOException ioe) {
			throw new SerializationException(ioe);
		}
		return marshalledBytes;
	}

	public void fromBytes(byte[] bytes) 
	throws SerializationException {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(bytes);
		DataInputStream din = new DataInputStream(baInputStream);

		try {
			try {
				while(true) {
					this.add(DataPoint.readFromDataInputStream(din));
				}
			}
			catch(EOFException e){}

			din.close();
			baInputStream.close();
		} catch (IOException ioe) {
			throw new SerializationException(ioe);
		}
	}

	public void add(DataPoint data) {
		dataPoints.add(data);
	}

	public DataPoint get(int index) {
		return dataPoints.get(index);
	}

	public String toString() {
		String output = "[";
		for(int i = 0; i < this.size(); i++) {
			output += "[" + this.get(i).toString() + "],";
		}
		return output + "]";
	}

	public int size() {
		return dataPoints.size();
	}

	public void sumDataPointToElement(int index, DataPoint dataPoint) {
		this.get(index).sumDataPoint(dataPoint);
	}
}