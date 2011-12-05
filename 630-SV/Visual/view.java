//*****************************************************************************************************
//
//	Course Name :	Software Analysis, Testing and Verification
//  Project		:	Visualization Techinque using Output Influences
//	Due Date	:	May 3, 2005
//	Team		:	Balaji Ganesan (balaji*) and Aditi Noata (aditi*)
//
//*****************************************************************************************************

import java.awt.*;
import java.applet.*;
import java.io.*;


public class view extends Applet
{

    static String[] st_array = new String[100];
    static int st_count;
    static int[] st_old_pass = new int[100];
    static int[] st_old_fail = new int[100];
    static int[] st_old_cv = new int[100];
    static int[] st_new_pass = new int[100];
    static int[] st_new_fail = new int[100];
    static int[] st_new_cv = new int[100];

    Color[] spectrum;
    static int N = 120;

    public void init( )
    {

	//comment
	int i =0;
	int value;
	int pass = 0,stno = 0,test = 0;
	int test_pass = 0, test_fail = 0;
	int per_pass = 0;
	int per_fail = 0;
	// Source File

	File testFile = new File("input.c");

	// Coverage Information
	File testData1 = new File("data1.txt");
	File testData2 = new File("data2.txt");

	StringBuffer contents = new StringBuffer();

	// Read Buffer

	BufferedReader input = null;

	// Populate the color spectrum. (From Red to Green.)
	spectrum = new Color[N];
	for(i=1;i<=N;i++)
	    spectrum[ i-1 ] = new Color( i/(float)N, (N-i)/(float)N, 0 );


	i = 0;

	// Read the source file
	try
	    {

		//use buffering
		//this implementation reads one line at a time

		input = new BufferedReader( new FileReader(testFile) );

		String line = null; //not declared within while loop

		while (( line = input.readLine()) != null)
		    {
			st_array[i] = line;
			i++;
			contents.append(line);
			contents.append(System.getProperty("line.separator"));
		    }
		st_count = i;
	    }

	catch (FileNotFoundException ex)
	    {
		ex.printStackTrace();
	    }

	catch (IOException ex)
	    {
		ex.printStackTrace();
	    }

	finally
	    {

		try
		    {

			if (input!= null)
			    {
				//flush and close both "input" and its underlying FileReader
				input.close();
			    }
		    }

		catch (IOException ex)
		    {
			ex.printStackTrace();
		    }
	    }



	// Read the text file

	i = 0;

	try
	    {

		input = new BufferedReader( new FileReader(testData1) );

		String line = null; //not declared within while loop

		while (( line = input.readLine()) != null)
		    {
			if (line.equals("##"))
			    break;

			if (line.equals("#"))
			    {
				line = input.readLine();
				//test++;
				line = input.readLine();

				if (line.equals("P"))
				    {
					pass = 1;
					test_pass++;
				    }
				else
				    {
					pass = 0;
					test_fail++;
				    }
			    }
			else
			    {

				stno = Integer.parseInt(line) - 1;
				if (pass == 1)
				    st_old_pass[stno] += 1;
				else
				    st_old_fail[stno] += 1;
			    }
		    }


	    }

	catch (FileNotFoundException ex)
	    {
		ex.printStackTrace();
	    }

	catch (IOException ex)
	    {
		ex.printStackTrace();
	    }

        finally
            {

                try
		    {
                        if (input!= null)
                            {
                                //flush and close both "input" and its underlying FileReader
                                input.close();
                            }
                    }

                catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
            }


	// Calculate the coverage information and store iin array.

	//System.out.println(test_pass);
	//System.out.println(test_fail);

	for(i=0;i<st_count;i++)
	    {
		if (test_pass != 0)
		    per_pass = ((st_old_pass[i] * 100) / test_pass);
		else
			per_pass = 0;



		if (test_fail != 0)
             per_fail = ((st_old_fail[i] * 100)/ test_fail);
        else
             per_fail = 0;


    	if ((per_pass + per_fail) != 0)
			st_old_cv[i] = 120 - ((per_pass * 120)/(per_pass + per_fail));
		else
			st_old_cv[i] = 999;

	    }

	/*for (i=0;i<st_count;i++)
		  {
		 System.out.print(i);
		 	  System.out.print(" ");
		 	  System.out.print(st_array[i]);
		 	  System.out.print(" ");
		 	  System.out.print(st_old_pass[i]);
		 	  System.out.print(" per_pass ");
		 	  System.out.print(per_pass);
		 	  System.out.print(" ");
		 	  System.out.print(st_old_fail[i]);
		 	  System.out.print(" per_fail ");
		 	  System.out.print(per_fail);
		 	  System.out.print(" ");
		 	  System.out.println(st_old_cv[i]);

	  }*/

	// Read the text file

	i = 0;
	test_pass = 0;
	test_fail = 0;

	try
	    {

		input = new BufferedReader( new FileReader(testData2) );

		String line = null; //not declared within while loop

		while (( line = input.readLine()) != null)
		    {
			if (line.equals("##"))
			    break;

			if (line.equals("#"))
			    {

				line = input.readLine();
				//				test++;
				line = input.readLine();

				if (line.equals("P"))
				    {
					pass = 1;
					test_pass++;
				    }
				else
				    {
					pass = 0;
					test_fail++;
				    }
			    }
			else
			    {

				stno = Integer.parseInt(line) - 1;
				if (pass == 1)
				    st_new_pass[stno] += 1;
				else
				    st_new_fail[stno] += 1;
			    }


		    }


	    }

	catch (FileNotFoundException ex)
	    {
		ex.printStackTrace();
	    }

	catch (IOException ex)
	    {
		ex.printStackTrace();
	    }

        finally
            {

                try
		    {
                        if (input!= null)
                            {
                                //flush and close both "input" and its underlying FileReader
                                input.close();
                            }
                    }

                catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
            }

	// Calculate the coverage information and store in array.

	for(i=0;i<st_count;i++)
	    {
		if (test_pass != 0)
		    per_pass = ((st_new_pass[i] * 100) / test_pass);
		else
		    per_pass = 0;

		if (test_fail != 0)
                    per_fail = ((st_new_fail[i] * 100)/ test_fail);
                else
                    per_fail = 0;

		if ((per_pass + per_fail) != 0)
		    st_new_cv[i] = 120 - ((per_pass * 120)/(per_pass + per_fail));
		else
		    st_new_cv[i] = 999;
	    }


	/*for (i=0;i<st_count;i++)
		  {
		 System.out.print(i);
		 	  System.out.print(" ");
		 	  System.out.print(st_array[i]);
		 	  System.out.print(" ");
		 	  System.out.print(st_new_pass[i]);
		 	  System.out.print(" per_pass ");
		 	  System.out.print(per_pass);
		 	  System.out.print(" ");
		 	  System.out.print(st_new_fail[i]);
		 	  System.out.print(" per_fail ");
		 	  System.out.print(per_fail);
		 	  System.out.print(" ");
		 	  System.out.println(st_new_cv[i]);

	  }*/
	// Set the background Color
	setBackground( Color.black);

    }



    // Function Paint
    public void paint (Graphics g)
    {
	int i=0;

	// Draw vertical line to partition the applet into two.

	g.setColor(Color.white);
	g.drawLine(350,0,350,600);

	g.drawString("Without Output Influences",10,20);
	g.drawString("With Output Influences",360,20);

	for(i=0;i<st_count;i++)
	    {

				switch(st_old_cv[i])
								{
								case 0:
									g.setColor(Color.green);
									break;
								case 60:
									g.setColor(Color.yellow);
									break;
								case 120:
									g.setColor(Color.red);
									break;
								case 999:
																    g.setColor(Color.white);
								    break;
								default:
									g.setColor(spectrum[st_old_cv[i]]);
									break;
				}


		g.drawString(st_array[i],10,(50 + (i*20)));

	    }

	for(i=0;i<st_count;i++)
	    {

				switch(st_new_cv[i])
				{
				case 0:
					g.setColor(Color.green);
					break;
				case 60:
					g.setColor(Color.yellow);
					break;
				case 120:
					g.setColor(Color.red);
					break;
					case 999:
				    g.setColor(Color.white);
				    break;
				default:
					g.setColor(spectrum[st_new_cv[i]]);
					break;
				}

		g.drawString(st_array[i],360,(50 + (i*20)));

	    }



    }
}

