//*****************************************************************************************************
//
//	Project		: Association Mining using XQuery
//  Authors		: Balaji Ganesan (balaji*)
//				  Richard Nikhil Martin (rnmartin*)
//			 	  Satya Narayan Thiruvallur Selvakumar (satya*)
//	Date   		: December 14th
//	Program		: Input
//	Description : Helper program to get the user input for minimum support and confidence
//
//*****************************************************************************************************

import java.io.*;
import java.io.File;

public class input
{

	private static BufferedReader stdin =
	new BufferedReader( new InputStreamReader( System.in ) );

	public static void main(String args[]) throws Exception
	{
		// Open input/output and setup variables
		PrintWriter pw = new PrintWriter(System.out, true);
		char c[] = new char[4096];
		int read = 0;
		double value = 0.0,value1 = 0.0;

		// Get the values

		System.out.print( "\nEnter the value for minimum support (Ex: 0.5)\n" );


		String input = stdin.readLine();
		value = Double.parseDouble(input);

		System.out.print( "\nEnter the value for minimum confidence (Ex: 1.0)\n" );

		input = stdin.readLine();
		value1 = Double.parseDouble(input);


		// Populate the support file
		File file_support = new File("C:\\xhive\\data\\support.txt");

		PrintWriter myOut = null;
		try
		{
			myOut = new PrintWriter(new FileOutputStream(file_support));
		}
		catch (IOException io)
		{
			System.out.println("FILE ERROR");
			//io.printStackTrace();
		}

		if (myOut != null)
		{
			myOut.println(value);
			myOut.flush();
		}

		myOut.close();



		// Populate the confidence file

		File file_confidence = new File("C:\\xhive\\data\\confidence.txt");

		myOut = null;
		try
		{
			myOut = new PrintWriter(new FileOutputStream(file_confidence));

		}
		catch (IOException io)
		{
			System.out.println("FILE ERROR");
			io.printStackTrace();
		}

		if (myOut != null)
		{
			myOut.println(value1);
			myOut.flush();
		}

		myOut.close();



	}
}