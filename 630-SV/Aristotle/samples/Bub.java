import java.io.DataInputStream;
import java.io.IOException;

/*---------------------------------------------------------------------------*/
/*   
		  Program file name : Bub.java
		  Functions         : main
*/
/*---------------------------------------------------------------------------*/

public class Bub {


/*---------------------------------------------------------------------------*/
/*   
	  Function         : main
	  Parameters       : none
	  Input            : 5 integer array elements
	  Output           : sorted array 
	  Description      : sorts array of 5 integers using bubble sort  

          Revision history
      		9-96             Jim Jones                Created
*/

/*---------------------------------------------------------------------------*/


public static void main(String argv[])
{
	int a[] = new int[6];
	/* index 0 is not to be used */

	int i, j, n, itmp;
	String str = new String();
	DataInputStream in = new DataInputStream(System.in);

	/* read elements into array */

	System.out.println("Enter  5 elements into array to be sorted");
	System.out.flush();
	i = 1;

	while( i <= 5)
	{
		try { str = in.readLine(); }
		catch (IOException e) { str = "0"; }
		a[i] = (new Integer(str)).intValue();
		i++;
	}

	n = 5;

	j = n-1;
	while(j >    0)
	{
		i = 1;
		while(i <= j)
		{
			if(a[i] > a[i+1])
			{
				itmp = a[i];
				a[i] = a[i+1];
				a[i+1] = itmp;
			}
			i++;
		}
		j--;
	}

	n = 1;

	System.out.println("Sorted:");
	while(n<= 5)
	{
		System.out.println(a[n] + "\t");
		n++;
	}
	System.out.println();
	return;
}

} //end class Bub
