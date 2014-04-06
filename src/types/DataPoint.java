/**
  * @author Archit Shukla
  */

package types;

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
	public byte[] getBytes() 
	throws SerializationException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(year);
		outputStream.write(temperature);
		outputStream.write(count);
		return outputStream.toByteArray();
	}

	public void writeBytesToDataOutputStream(DataOutputStream dOutputStream) 
	throws IOException {
		dOutputStream.writeInt(year);
		dOutputStream.writeInt(temperature);
		dOutputStream.writeInt(count);
	}

	public void fromBytes(byte[] bytes)
	throws SerializationException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		year = inputStream.read();
		temperature = inputStream.read();
		count = inputStream.read();
	}

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

	public void sumDataPoint(DataPoint dataPoint) {
		year += dataPoint.year;
		temperature += dataPoint.temperature;
	}

	public void averageDataPoint() {
		if(count == 0) {
			return;
		}
		year /= count;
		temperature /= count;
	}

	public void incrementCounter() {
		count++;
	}

	/**
	  * Attributes for the class: year and temperature.
	  */
	private int year, temperature;

	public int count;

	/**
	  * T1 and T2 thresholds for this Data Set.
	  */
	public final static double T1 = 10, T2 = 5;
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
	public DataPoint()
	{
		year = temperature = count = 0;
	}

	/**
	  * Parameterized Constructor (String).
	  * @param dataPoint
	  * - StringString representation of an object of the class.
	  *
	  * Parses the input string and sets the appropriate fields of the object. Inverse of toString() method.
	  */
	public DataPoint(String dataPointString)
	{
		int commaPosition = dataPointString.indexOf(",");
		year = Integer.parseInt(dataPointString.substring(0, commaPosition));
		temperature = Integer.parseInt(dataPointString.substring(commaPosition + 1));
		count = 0;
	}

	/**
	  * Copy Constructor (DataPoint).
	  * @param dataPoint
	  * - Reference Data Point whose fields are to be copied into this object <br>
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
	  * <b>compareTo method of. </b><br>
	  * <b>Parameters:</b>	DataPoint dataPoint, the object to compare this object to <br>
	  * <b>Returns:</b>		int ,possible values are -1, 0 and 1 <br><br>
	  *
	  * Compares the fields of two objects to obtain an ordering.
	  */
	public int compareTo(DataPoint dataPoint)
	{
		return (temperature < dataPoint.temperature ? -1 : 
			(temperature == dataPoint.temperature ? (year < dataPoint.year ? -1 : (year == dataPoint.year ? 0 : 1)) : 1));
	}

	/**
	  * <b>Checks if the distance between two Data Points is within T1. </b><br>
	  * <b>Parameters:</b>	DataPoint dataPoint, the object to compare this object to. <br>
	  * <b>Returns:</b>		boolean <br>
	  * <b>Uses:</b>		int simpleDistance(DataPoint). <br><br>
	  *
	  * Compares this object with the passed object to check if they are within T1 distance of each other.
	  * Returns true if they are within T1 distance, false otherwise.
	  */
	public boolean withinT1(DataPoint dataPoint)
	{
		return (simpleDistance(dataPoint) < T1);
	}

	/**
	  * <b>Checks if the distance between two Data Points is within T1. </b><br>
	  * <b>Parameters:</b>	DataPoint dataPoint, the object to compare this object to. <br>
	  * <b>Returns:</b>		boolean <br>
	  * <b>Uses:</b>		int simpleDistance(DataPoint) <br>
	  *
	  * Compares this object with the passed object to check if they are within T2 distance of each other.
	  * It returns true if they are within T2 distance, false otherwise.
	  */
	public boolean withinT2(DataPoint dataPoint)
	{
		return (simpleDistance(dataPoint) < T2);
	}

	/**
	  * <b>Simple and inexpensive distance metric. </b><br>
	  * <b>Parameters:</b>	DataPoint dataPoint, the object to compare this object to. <br>
	  * <b>Returns:</b>		int, a simple distance value. <br><br>
	  *
	  * Finds a simple, cheap distance between two Data Points.
	  * Used in Canopy Generation phase.
	  */
	public int simpleDistance(DataPoint dataPoint)
	{
		return Math.abs(temperature - dataPoint.temperature);
	}

	/**
	  * <b>Expensive distance metric for clustering. </b><br>
	  * <b>Parameters:</b>	DataPoint dataPoint, the object to compare this object to. <br>
	  * <b>Returns:</b>		double, a complex distance value. <br>
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
	  * <b>Converts the Data Point to a String. </b><br>
	  * <b>Parameters:</b>	None <br>
	  * <b>Returns:</b>		String <br><br>
	  *
	  * Returns a string representation of this object.
	  */
	public String toString()
	{
		return year + "," + temperature;
	}

	/**
	  * <b>Overridden equals method of Object. </b><br>
	  * <b>Parameters:</b>	Object object, the passed object to check for equality <br>
	  * <b>Returns:</b>		boolean <br><br>
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
	  * <b>Overridden hashCode method of Object Class. </b><br>
	  * <b>Parameters:</b>	Nothing
	  * <b>Returns:</b>		int, the hash code. <br><br>
	  *
	  * Returns a user defined hash code for the object.
	  */
	@Override
	public int hashCode()
	{
		return (17 * year + 31 * temperature);
	}
}