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
public class DataPoint implements Value, Key
{
	/**
	  * Attributes for the class: year and temperature.
	  */
	public int year, temperature;

	public int count;

	/**
	  * T1 and T2 thresholds for this Data Set.
	  */
	public final static double T1 = 10, T2 = 6;
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
	  * @param dataPointString String representation of an object of the class.
	  *
	  * Parses the input string and sets the appropriate fields of the object. Inverse of toString() method.
	  */
	public DataPoint(String dataPointString) {
		int commaPosition = dataPointString.indexOf(",");
		year = Integer.parseInt(dataPointString.substring(0, commaPosition));
		temperature = Integer.parseInt(dataPointString.substring(commaPosition + 1));
		count = 0;
	}

	/**
	  * Copy Constructor (DataPoint).
	  * @param dataPoint Reference Data Point whose fields are to be copied into this object <br>
	  *
	  * Sets this object's fields to the corresponding fields of the passed object.
	  */
	public DataPoint(DataPoint dataPoint)
	{
		year = dataPoint.year;
		temperature = dataPoint.temperature;
		count = dataPoint.count;
	}

	/**
	  * compareTo method of.
	  * @param dataPoint The object to compare this object to <br>
	  * @return	one among -1, 0 and 1
	  *
	  * Compares the fields of two objects to obtain an ordering.
	  */
	public int compareTo(DataPoint dataPoint)
	{
		return (temperature < dataPoint.temperature ? -1 : 
			(temperature == dataPoint.temperature ? (year < dataPoint.year ? -1 : (year == dataPoint.year ? 0 : 1)) : 1));
	}

	/**
	  * Checks if the distance between two Data Points is within T1.
	  * @param dataPoint The object to compare this object to. <br>
	  * @return boolean
	  *
	  * Compares this object with the passed object to check if they are within T1 distance of each other.
	  * Returns true if they are within T1 distance, false otherwise.
	  */
	public boolean withinT1(DataPoint dataPoint)
	{
		return (simpleDistance(dataPoint) < T1);
	}

	/**
	  * Checks if the distance between two Data Points is within T1.
	  * @paramdataPoint The object to compare this object to. <br>
	  * @return boolean
	  *
	  * Compares this object with the passed object to check if they are within T2 distance of each other.
	  * It returns true if they are within T2 distance, false otherwise.
	  */
	public boolean withinT2(DataPoint dataPoint)
	{
		return (simpleDistance(dataPoint) < T2);
	}

	/**
	  * Simple and inexpensive distance metric.
	  * @param dataPoint The object to compare this object to.
	  * @return simple distance value.
	  *
	  * Finds a simple, cheap distance between two Data Points.
	  * Used in Canopy Generation phase.
	  */
	public long simpleDistance(DataPoint dataPoint)
	{
		return Math.abs(temperature - dataPoint.temperature);
	}

	/**
	  * Expensive distance metric for clustering.
	  * @param dataPoint The object to compare this object to.
	  * @return double A complex distance value.
	  *
	  * Finds a complex, more expensive distance between two Data Points.
	  * Used in Clustering phase.
	  */
	public double complexDistance(DataPoint dataPoint)
	{
		return Math.abs((year - dataPoint.year) * (year - dataPoint.year) 
				+ (temperature - dataPoint.temperature) * (temperature - dataPoint.temperature));
	}

	/**
	  * Converts the Data Point to a String.
	  * @return String
	  *
	  * Returns a string representation of this object.
	  */
	public String toString()
	{
		return year + "," + temperature;
	}

	/**
	  * Overridden equals method of Object.
	  * @param object The passed object to check for equality <br>
	  * @return boolean
	  *
	  * Returns true if this object and the passed object are the same.
	  * Similarity conditions depend on the Data Set.
	  */
	@Override
	public boolean equals(Object object)
	{
		if(object == null)
			return false;
		DataPoint dataPoint = (DataPoint) object;
		if(year ==  dataPoint.year && temperature == dataPoint.temperature)
			return true;
		return false;
	}

	/**
	  * Overridden hashCode method of Object Class.
	  * @return int, the hash code.
	  *
	  * Returns a user defined hash code for the object.
	  */
	@Override
	public int hashCode()
	{
		return (17 * (int)year + 31 * (int)temperature);
	}

	/**
	  * Function to convert DataPoint into a byte array.
	  * @throws SerializationException
	  * @return - A byte array corresponding to this object.
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
	  * @param dOutputStream
	  * - DataOutputStream to write the object to.
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
	  * @param bytes
	  * - Byte array to be parsed to the DataPoint.
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
	  * @param dInputStream
	  * - DataInputStream to read the object from.
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
	  * @param dataPoint
	  * - Other DataPoint to add to this DataPoint.
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