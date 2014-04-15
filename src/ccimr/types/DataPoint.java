/**
  * @author Archit Shukla
  */
package ccimr.types;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import cgl.imr.base.Value;
import cgl.imr.base.Key;
import cgl.imr.base.SerializationException;

/**
  * A class to model a simple [year, temperature] Data Set.
  */
public class DataPoint implements Value, Key {
	/**
	  * Attributes for the class: year and temperature.
	  */
	public int year, temperature;

	/**
	  * Counter used in average calculation for clustering.
	  */
	public int count;

	/**
	  * T1 threshold for this Data Set.
	  */
	public final static double T1 = 10;
	public final static double T2 = 6;
	/**
	  * Threshold for convergence. A distance value below the specified value denotes the point has converged.
	  */
	public final static double CONVERGENCE_THRESHOLD = 1.0;

	/**
	 * The number of iterations so far.
	 */
	public static long NUM_ITERATIONS = 0;

	/**
	  * Default Constructor.
	  * Sets the year and temperature values to 0.
	  */
	public DataPoint() {
		year = temperature = count = 0;
	}

	/**
	  * Parameterized Constructor (String).
	  * Parses the input string and sets the appropriate fields of the object. Inverse of toString() method.
	  *
	  * @param dataPointString String representation of an object of the class.
	  */
	public DataPoint(String dataPointString) {
		int commaPosition = dataPointString.indexOf(",");
		year = Integer.parseInt(dataPointString.substring(0, commaPosition));
		temperature = Integer.parseInt(dataPointString.substring(commaPosition + 1));
		count = 0;
	}

	/**
	  * Copy Constructor (DataPoint).
	  * Sets this object's fields to the corresponding fields of the passed object.
	  *
	  * @param dataPoint Reference Data Point whose fields are to be copied into this object <br>
	  */
	public DataPoint(DataPoint dataPoint) {
		year = dataPoint.year;
		temperature = dataPoint.temperature;
		count = dataPoint.count;
	}

	/**
	  * compareTo method of.
	  * Compares the fields of two objects to obtain an ordering.
	  *
	  * @param dataPoint The object to compare this object to
	  * @return	one among -1, 0 and 1
	  */
	public int compareTo(DataPoint dataPoint) {
		return (temperature < dataPoint.temperature ? -1 : 
			(temperature == dataPoint.temperature ? (year < dataPoint.year ? -1 : (year == dataPoint.year ? 0 : 1)) : 1));
	}

	/**
	  * Checks if the distance between two Data Points is within T1.
	  * Compares this object with the passed object to check if they are within T1 distance of each other.
	  * Returns true if they are within T1 distance, false otherwise.
	  *
	  * @param dataPoint The object to compare this object to.
	  * @return boolean
	  */
	public boolean withinT1(DataPoint dataPoint) {
		return (simpleDistance(dataPoint) < T1);
	}

	/**
	  * Checks if the distance between two Data Points is within T1.
	  * Compares this object with the passed object to check if they are within T2 distance of each other.
	  * It returns true if they are within T2 distance, false otherwise.
	  *
	  * @param dataPoint The object to compare this object to.
	  * @return boolean
	  */
	public boolean withinT2(DataPoint dataPoint) {
		return (simpleDistance(dataPoint) < T2);
	}

	/**
	  * Simple and inexpensive distance metric.
	  * Finds a simple, cheap distance between two Data Points.
	  * Used in Canopy Generation phase.
	  *
	  * @param dataPoint The object to compare this object to.
	  * @return simple distance value.
	  */
	public long simpleDistance(DataPoint dataPoint) {
		return Math.abs(temperature - dataPoint.temperature);
	}

	/**
	  * Expensive distance metric for clustering.
	  * Finds a complex, more expensive distance between two Data Points.
	  * Used in Clustering phase.
	  *
	  * @param dataPoint The object to compare this object to.
	  * @return double A complex distance value.
	  */
	public double complexDistance(DataPoint dataPoint) {
		return Math.abs((year - dataPoint.year) * (year - dataPoint.year) 
				+ (temperature - dataPoint.temperature) * (temperature - dataPoint.temperature));
	}

	/**
	  * Converts the Data Point to a String.
	  * Returns a string representation of this object.
	  *
	  * @return String
	  */
	public String toString() {
		return year + "," + temperature;
	}

	/**
	  * Overridden equals method of Object.
	  * Returns true if this object and the passed object are the same.
	  * Similarity conditions depend on the Data Set.
	  *
	  * @param object The passed object to check for equality.
	  * @return boolean.
	  */
	@Override
	public boolean equals(Object object) {
		if(object == null)
			return false;
		DataPoint dataPoint = (DataPoint) object;
		if(year ==  dataPoint.year && temperature == dataPoint.temperature)
			return true;
		return false;
	}

	/**
	  * Overridden hashCode method of Object Class.
	  * Returns a user defined hash code for the object.
	  *
	  * @return int, the hash code.
	  */
	@Override
	public int hashCode() {
		return (17 * (int)year + 31 * (int)temperature);
	}

	/**
	  * Function to convert DataPoint into a byte array.
	  *
	  * @return A byte array corresponding to this object.
	  * @throws SerializationException
	  */
	public byte[] getBytes() 
	throws SerializationException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			DataOutputStream dout = new DataOutputStream(outputStream);
			dout.writeInt(year);
			dout.writeInt(temperature);
			dout.writeInt(count);
			dout.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return outputStream.toByteArray();
	}

	/**
	  * Function to write out the object to a DataOutputStream.
	  *
	  * @param dOutputStream DataOutputStream to write the object to.
	  * @throws IOException
	  */
	public void writeBytesToDataOutputStream(DataOutputStream dOutputStream) 
	throws IOException {
		dOutputStream.writeInt(year);
		dOutputStream.writeInt(temperature);
		dOutputStream.writeInt(count);
	}

	/**
	  * Function to parse the object from an array of bytes.
	  *
	  * @param bytesByte array to be parsed to the DataPoint.
	  * @throws SerializationException
	  */
	public void fromBytes(byte[] bytes)
	throws SerializationException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		try {
			DataInputStream din = new DataInputStream(inputStream);
			year = din.readInt();
			temperature = din.readInt();
			count = din.readInt();
			din.close();
			inputStream.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	  * Function to read in the object from a DataInputStream.
	  *
	  * @param dInputStream DataInputStream to read the object from.
	  * @throws IOException
	  */
	public static DataPoint readFromDataInputStream(DataInputStream dInputStream) 
	throws IOException {
		int y = dInputStream.readInt();
		int t = dInputStream.readInt();
		int c = dInputStream.readInt();
		DataPoint dataPoint = new DataPoint();
		dataPoint.year = y;
		dataPoint.temperature = t;
		dataPoint.count = c;
		return dataPoint;
	}

	/**
	  * Function to sum values of two DataPoint objects.
	  *
	  * @param dataPoint Other DataPoint to add to this DataPoint.
	  */
	public void sumDataPoint(DataPoint dataPoint) {
		year += dataPoint.year;
		temperature += dataPoint.temperature;
	}

	/**
	  * Function to find average of the value of this DataPoint (by dividing it by the current count).
	  */
	public void averageDataPoint() {
		if(count == 0) {
			return;
		}
		year /= count;
		temperature /= count;
	}

	/**
	  * Function to increment count.
	  */
	public void incrementCounter() {
		count++;
	}
}