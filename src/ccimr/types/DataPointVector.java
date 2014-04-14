/**
  * @author Archit Shukla
  */
package ccimr.types;

import java.io.IOException;
import java.io.EOFException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.util.ArrayList;

import cgl.imr.base.Value;
import cgl.imr.base.SerializationException;

import ccimr.types.DataPoint;

/**
  * A generic Vector of Data Points.
  */
public class DataPointVector implements Value {

	/** 
	  *ArrayList of dataPoints encapsulated by the Vector.
	  */
	private ArrayList<DataPoint> dataPoints;

	/**
	  * Default Constructor.
	  * Allocates memory for the dataPoints ArrayList.
	  */
	public DataPointVector() {
		dataPoints = new ArrayList<DataPoint>();
	}

	/**
	  * Parameterized Constructor.
	  * @param length Initial size of the dataPoints ArrayList.
	  *
	  * Allocates the dataPoints ArrayList of size length, the parameter passed.
	  */
	public DataPointVector(int length) {
		dataPoints = new ArrayList<DataPoint>();
		for(int i = 0; i < length; i++) {
			this.add(new DataPoint());
		}
	}

	/**
	  * Copy Constructor.
	  * @param dataPointVector The source vector to copy.
	  *
	  * Creates a deep copy of the vector passed.
	  */
	public DataPointVector(DataPointVector dataPointVector) {
		dataPoints = new ArrayList<DataPoint>();
		for(int i = 0; i < dataPointVector.size(); i++) {
			this.add(dataPointVector.get(i));
		}
	}

	/**
	  * Converts vector to bytes.
	  * @return byte[] The array of bytes from the object.
	  *
	  * Function converts the DataPointVector into an array of bytes.
	  */
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


	/**
	  * Converts bytes array to this DataPointVector object.
	  * @param byte[] byte array to convert to the DataPointVector.
	  *
	  * Function converts an array of bytes to this DataPointVector object.
	  */
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

	/**
	  * Appends a DataPoint object to the DataPointVector.
	  * @param dataPoint The DataPoint object to add to the DataPointVector.
	  */
	public void add(DataPoint dataPoint) {
		dataPoints.add(dataPoint);
	}

	/**
	  * Returns the DataPoint object at a given index of the DataPointVector.
	  * @param index Index of the DataPoint to return in the DataPointVector.
	  * @return DataPoint The DataPoint at given index of the DataPointVector.
	  */
	public DataPoint get(int index) {
		return dataPoints.get(index);
	}

	/**
	  * Returns a string representation of the vector.
	  * @return String The String representation of the vector.
	  */
	public String toString() {
		String output = "[";
		for(int i = 0; i < this.size(); i++) {
			output += "[" + this.get(i).toString() + "],";
		}
		return output + "]";
	}

	/**
	  * Returns the size or length of the DataPointVector.
	  * Size refers to the number of DataPoint objects in the DataPointVector.
	  * @return int Current size of the DataPointVector.
	  */
	public int size() {
		return dataPoints.size();
	}

	/**
	  * Performs the addition operation on the passed DataPoint object and the DataPoint at a given offset.
	  * @param index Index of the DataPoint in the DataPointVector.
	  * @param dataPoint The DataPoint object to add.
	  */
	public void sumDataPointToElement(int index, DataPoint dataPoint) {
		this.get(index).sumDataPoint(dataPoint);
	}
}