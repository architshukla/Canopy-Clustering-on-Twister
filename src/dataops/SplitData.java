package dataops;

/**
  * @author Archit Shukla
  */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

public class SplitData {
	/**
	  * Holds number of lines in file to split.
	  */
	public static long count = 0;
	/**
	  * Number of file splits needed. Given as parameter to file.
	  */
	public static long numberOfFileSplits = 0;

	public static String outputFolder = "output";
	public static String filePrefix = "input_";

	public static BufferedReader reader;
	public static BufferedWriter writer;

	/**
	  * Main function, splits data into a give number of files
	  * @param args
	  */
	public static void main(String args[]) {
		// Check number of arguments
		if(args.length < 2) {
			System.err.println("Insufficient arguments given.");
			System.out.println("Usage: java SplitData INPUT_FILE NUMBER_OF_SPLITS [OUTPUT_FOLDER] [FILE_PREFIX]");
			System.exit(-1);
		}

		// Parse number of file splits
		numberOfFileSplits = Integer.parseInt(args[1]);

		// Check if optional parameters are passed
		if(args.length >= 3) {
			outputFolder = args[2];
			if(args.length >= 4)
				filePrefix = args[3];
		}

		// Check if output folder exists
		try {
			File dir = new File(outputFolder);
			if(dir.exists() && dir.isDirectory()) {
				throw new Exception("Output folder \"" + outputFolder + "\" already exists. Please specify an empty folder.");
			}
			dir.mkdir();

			// Count number of lines
			reader = new BufferedReader(new FileReader(args[0]));
			while(reader.readLine() != null)
				count++;
			reader.close();

			if(count < numberOfFileSplits)
				throw new Exception("Number of splits given is greater than number of lines in file");

			System.out.println("Number of lines in file: " + count);

			long quotient = count / numberOfFileSplits;
			long remainder = count % numberOfFileSplits;

			// Reopen the file
			reader = new BufferedReader(new FileReader(args[0]));
			// Split contents into files
			for(long filenumber = 0; filenumber < numberOfFileSplits; filenumber++) {
				writer = new BufferedWriter(new FileWriter(outputFolder + "/" + filePrefix + filenumber));
				for(long i = 0; i < quotient; i++) {
					writer.write(reader.readLine());
					writer.newLine();
				}
				if(remainder > 0) {
					writer.write(reader.readLine());
					writer.newLine();
					remainder--;
				}
				writer.close();
			}
			reader.close();
			System.out.println("File split complete.");
			System.out.println(numberOfFileSplits + " files created in \"" + outputFolder +"/\" folder");
		}
		catch(Exception e) {
			System.out.println("An exception has occurred. " + e);
			e.printStackTrace();
		}
	}
}