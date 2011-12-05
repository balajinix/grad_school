import java.io.DataInputStream;
import java.io.IOException;

/*---------------------------------------------------------------------------*/
/*   
		  Program file name : Secant.java
		  Functions         : main, ABS, F 
*/
/*---------------------------------------------------------------------------*/

public class Secant {

static double F(double z) { return ( z * z - x ); }

static double x;

/*---------------------------------------------------------------------------*/
/*   
	  Function         : main
	  Parameters       : none
	  Input            : real number, real epsilon value
	  Output           : square root of number 
	  Description      : computes square root of number using
                                     the secant method
          From Applied Numerical Analysis, 3rd Ed
          Curtis F. Gerald, Patrick O. Wheatley;  Addison Wesley, 1985 

          chapter 1. pg 11. -- Secant Method 
*/
/*---------------------------------------------------------------------------*/


public static void main(String argv[])
{
	double x1, x2, x3,x4, x5,  eps;
	double retval;

	String str1, str2;
	DataInputStream in = new DataInputStream(System.in);

	System.out.print ("enter x and epsilon ");
	System.out.flush();
	try {
		str1 = in.readLine();
		str2 = in.readLine();
	} catch (IOException e) {
		str1 = "0";
		str2 = "0";
	}
	x = (new Double(str1)).doubleValue();
	eps = (new Double(str2)).doubleValue();

	if ( x <= 0.0 || eps <= 0.0)
		System.out.println (" x and eps need to be greater than 0.0");
	else
	{
		x1 = eps;
		x2 = x;

		x3 = x2 - F(x2)*(x2-x1)/(F(x2)-F(x1));
		x1 = x3;
		x4 = ABS(x2 - x1);
		x5 = ABS(F(x3));
		while (( x4 >= eps ) &&( x5 >= eps ))
		{
			x3 = x2 - F(x2)*(x2-x1)/(F(x2)-F(x1));
			x1 = x3;
			x4 = ABS(x2 - x1);
			x5 = ABS(F(x3));
		}

		System.out.println ("sqrt(" + x + ") = " + x3 + " +- " + eps);
	}
}


/*---------------------------------------------------------------------------*/
/*   
		  Function         : ABS
		  Parameters       : real number x 
		  Output           : absolute value of x 
*/
/*---------------------------------------------------------------------------*/



static double ABS(double x)
{
	double retval = 0.0;
	if (x<0.0)
		retval = (-x);
	else 
		retval =  (x);

	return retval;
}

}//end class Secant
