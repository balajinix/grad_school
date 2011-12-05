import java.io.DataInputStream;
import java.io.IOException;

/*---------------------------------------------------------------------------*/
/*   
                  Program file name : Bisect.java
                  Functions         : main
*/
/*---------------------------------------------------------------------------*/

public class Bisect {

static double x;

static double F(double z) { return (z * z - x); }


/*---------------------------------------------------------------------------*/
/*   
       Function         : main
       Parameters       : none
       Input            : real number, real epsilon value
       Output           : square root 
       Description      : finds square root of number using 
                                     method of interval bisection

       From Applied Numerical Analysis, 3rd Ed
           Curtis F. Gerald, Patrick O. Wheatley;  Addison Wesley, 1985 

       Chapter 1. pg 5. -- Method of halving the interval 

       Revision history
           9-96             Jim Jones		Created
*/
/*---------------------------------------------------------------------------*/

public static void main(String argv[]) {
        double x1, x2, x3=0.0, eps=0.0;
	String str = new String();
	DataInputStream in = new DataInputStream(System.in);

        System.out.print ("enter x -->");
	System.out.flush();
	try {
		str = in.readLine();
	} catch (IOException e) {
		str = "0";
	}
	x = (new Double(str)).doubleValue();

	System.out.print ("enter epsilon -->");
	System.out.flush();
	try {
		str = in.readLine();
	} catch (IOException e) {
		str = "0";
	}
	eps = (new Double(str)).doubleValue();
	
        if (( x <= 0.0) || (eps <= 0.0) )
        {
                System.out.println (" x and eps need to be greater than 0.0\n");
                /* removed return from here and added the else clause
                  to keep Aristotle happy */
        }
        else
        {
                if ( x < 1 )
                {
                        x1 = x;
                        x2 = 1;
                }
                else 
                {
                        x1 = eps;
                        x2 = x;
                }

                while ( (x2 - x1) >= 2.0*eps )
                {
                        x3 = (x1+x2)/2.0;

                        if ((F(x3))*(F(x1)) < 0)
                                /* F(x3) and F(x1) have different signs */
                                x2 = x3;
                        else
                                x1 = x3;
                }

                System.out.println ("sqrt(" + x + ") = " + x3 + " +- " 
			+ eps + "\n");
        }
}

}
