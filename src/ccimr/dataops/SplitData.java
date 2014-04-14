/**
  * @author Archit Shukla
  */
package ccimr.dataops;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.IOException;

public class SplitData {
	/**
	  * Holds number of lines in file to split.
	  */
	private static long count = 0;

	/**
	  * Name of the folder where output files are placed.
	  */
	private static String outputFolder = "output";

	/**
	  * Prefix for output files.
	  */
	private static String outputFilePrefix = "input_";

	/**
	  * Splits input file into a fixed number of smaller files. Executes serially. 
	  * @param filePath Path to the input file to be split.
	  * @param numberOfFileSplits Number of output file splits required.
	  * @throws IOException
	  *
	  * The method checks if the output directory exists. If it does, an IOException is thrown.
	  * The file is scanned once to find the number of lines in the file.
	  * An IOException is thrown if the number of lines in the file is too small to be split among the given number of file splits.
	  * Then, the number of file splits is used to calculate the number of lines tow be written to each file.
	  * The input file is read again and written to a desired number of output files serially.
	  */
	public static void fixedNumFilesSplit(String filePath, long numberOfFileSplits) 
		throws IOException {
		// Check if number of file splits is a natural number
		if(numberOfFileSplits <= 0) {
			throw new IOException("Number of file splits cannot be less than or equal to 0.");
		}

		BufferedReader reader;
		BufferedWriter writer;

		// Check if output folder exists
		File dir = new File(outputFolder);
		if(dir.exists() && dir.isDirectory()) {
			throw new IOException("Output folder \"" + outputFolder + "\" already exists. Please specify an empty folder.");
		}
		dir.mkdir();

		// Count number of lines
		reader = new BufferedReader(new FileReader(filePath));
		while(reader.readLine() != null)
			count++;
		reader.close();

		// Check if file can be split into the given number of file splits
		if(count < numberOfFileSplits)
			throw new IOException("Number of splits given is greater than number of lines in file");

		System.out.println("Number of lines in file: " + count);

		// Calculate the number of lines in each file
		long quotient = count / numberOfFileSplits;
		long remainder = count % numberOfFileSplits;

		// Reopen the file
		reader = new BufferedReader(new FileReader(filePath));

		// Split contents into files
		for(long filenumber = 0; filenumber < numberOfFileSplits; filenumber++) {
			// Create a new output file
			writer = new BufferedWriter(new FileWriter(outputFolder + "/" + outputFilePrefix + filenumber));
			for(long i = 0; i < quotient; i++) {
				// Read a line from the file and write it to the output file split
				writer.write(reader.readLine());
				writer.newLine();
			}
			// If lines cannot be divided exactly, spread the remaining lines over output files evenly
			if(remainder > 0) {
				// Read an extra line and write it to the output file split
				writer.write(reader.readLine());
				writer.newLine();
				remainder--;
			}
			// Close the file split
			writer.close();
		}
		// Close the reader
		reader.close();

		// Display completion of splitting
		System.out.println("File split complete.");
		System.out.println(numberOfFileSplits + " files created in \"" + outputFolder +"/\" folder");
	}

	/**
	  * Splits input file into a number of files each of a given size. Executes in parallel.
	  * @param filePath Path to the input file to be split.
	  * @param sizeString Size of each split as a string. Can have a trailing K or M signifying the size is in Kilobytes or Megabytes respectively.
	  * @throws IOException
	  *
	  * The method checks if the size has a valid trailing character - K or M.
	  * If a valid character is found, the size is suitably multipled by a factor.
	  * Then the overloaded function fixedSizeSplit(String, long) is called with the new size.
	  * An IOException is thrown if an invalid character is found.
	  */
	public static void fixedSizeSplit(String filePath, String sizeString)
		throws IOException {
			// Check if size is in Kilobytes
			if(sizeString.charAt(sizeString.length() - 1) == 'k' || sizeString.charAt(sizeString.length() - 1) == 'K') {
				fixedSizeSplit(filePath, Long.parseLong(sizeString.substring(0, sizeString.length() - 1)) * 1024);
				return;
			}

			// Check if size is in MegaBytes
			if(sizeString.charAt(sizeString.length() - 1) == 'm' || sizeString.charAt(sizeString.length() - 1) == 'M') {
				fixedSizeSplit(filePath, Long.parseLong(sizeString.substring(0, sizeString.length() - 1)) * 1024 * 1024);
				return;
			}

			// Throw exception if the character is invalid
			throw new IOException("Bad argument: " + sizeString + " Expected K or M, found: " + sizeString.charAt(sizeString.length() - 1));
	}

	/**
	  * Splits input file into a number of files each of a given size. Executes in parallel.
	  * @param filePath Path to the input file to be split.
	  * @param size Size of each split.
	  * @throws IOException
	  *
	  */
	public static void fixedSizeSplit(String filePath, long size)
		throws IOException {
		// Check if size is a natural number
		if(size <= 0) {
			throw new IOException("Size of each split cannot be less than or equal to 0");
		}

		// Open the file and find its size
		File file = new File(filePath);
		final long filesize = file.length();

		// Inner class to run threads
		class FixedSizeSplitter implements Runnable {
			String filePath;
			private long threadNum;
			private long size;

			FixedSizeSplitter(String path, long threadNumber, long sizeOfSplit) {
				filePath = path;
				threadNum = threadNumber;
				size = sizeOfSplit;
			}
			@Override
			public void run() {
				// Starting byte offset in input file for this thread
				long start = threadNum * size;

				// Ending byte offset in input file for this thread
				long end = start + size;
				int b;

				try {
					// Open the input file in random access mode
					RandomAccessFile file = new RandomAccessFile(filePath, "r");

					// Seek to the starting byte for this thread
					file.seek(start);

					try {
						// Find actual start byte (beginning of a new record)
						if(start != 0) {
							// Find the next newline
							while((b = file.read()) != '\n') {
								start = start + 1;
							}
							// Skip the newline character, next character is the start of the new record
							start = start + 1;
						}

						// Check if start is beyond the file's size. If so, skip this block
						if(start >= filesize) return;

						// Seek to ending byte in the input file for this thread
						file.seek(end);

						// Find the end of the current record, that is, the next newline character
						while((b = file.read()) != '\n') {
							// Check if end is beyond the file's size. If so, break out of the loop
							if(end >= filesize) {
								end = filesize;
								break;
							}
							// Move the pointer one byte forward to include the newline character
							end = end + 1;
						}

						// Check if start is beyond end. If so, return
						if(start > end) return;

						// Allocate buffer, set file pointer to start
						byte[] bytes = new byte[(int)(end - start)];
						file.seek(start);

						// Read the set number of bytes
						file.read(bytes);

						// Write to output file
						FileOutputStream outputStream = new FileOutputStream(outputFolder 
							+ "/" + outputFilePrefix + threadNum);
						outputStream.write(bytes);

						// Close the file, flush the contents
						outputStream.close();

					}
					catch (Exception except) {
						except.printStackTrace();
					}

					// Close random access file
					file.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Check if output folder exists
		File dir = new File(outputFolder);
		if(dir.exists() && dir.isDirectory()) {
			throw new IOException("Output folder \"" + outputFolder + "\" already exists. Please specify an empty folder.");
		}
		dir.mkdir();

		// Find number of file splits
		long parts = (long) Math.ceil(filesize/(double)size);

		// Display status messages
		System.out.println("File size: " + filesize);
		System.out.println("Expected number of splits of size " + size + " bytes : " + parts);

		// Invoke threads to split file
		for(long i = 0; i < parts; i++) {
			Thread thread = new Thread(new FixedSizeSplitter(filePath, i, size));
			thread.start();
		}
	}

	/**
	  * Main function, splits data into a give number of files
	  * @param args Arguments passed to main method. 
	  * args[0] is the input file. 
	  * args[1] is the choice between fixed file sizes or fixed number of files.
	  * args[2] is the number of splits if args[1] was file and size of each split in bytes if args[1] was size.
	  * args[3] (optional) is the output folder. 
	  * args[4] (optional) is the file prefix of the output files
	  */
	public static void main(String args[]) {
		// Check number of arguments
		if(args.length < 3) {
			System.err.println("Insufficient arguments given.");
			System.out.println("Usage: java SplitData INPUT_FILE size|file NUMBER_OR_SIZE_OF_SPLITS [OUTPUT_FOLDER] [FILE_PREFIX]");
			System.exit(-1);
		}

		// Check if optional parameters are passed
		if(args.length >= 4) {
			outputFolder = args[3];
			if(args.length >= 5)
				outputFilePrefix = args[4];
		}

		// Check if the file is to be split according to a given number of splts or a given size of splits
		try {
			if(args[1].toLowerCase().equals("file")) {
				fixedNumFilesSplit(args[0], Long.parseLong(args[2]));
			}
			else if (args[1].toLowerCase().equals("size")) {
				try {
					fixedSizeSplit(args[0], Long.parseLong(args[2]));
				}
				// If the size is followed by a M (Megabytes) or K (Kilobytes)
				catch(NumberFormatException e) {
					fixedSizeSplit(args[0], args[2]);
				}
				
			}
			else {
				throw new Exception("Bad argument: " + args[1] +". Expected: 'file' or 'size'");
			}
		}
		catch(Exception e) {
			System.out.println("An exception has occurred. " + e);
			e.printStackTrace();
		}
	}
}
