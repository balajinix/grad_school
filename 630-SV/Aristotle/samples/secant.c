
/*---------------------------------------------------------------------------*/
/*   
		  Program file name : secant.c
		  Functions         : main, ABS 
*/
/*---------------------------------------------------------------------------*/

/* include files */
#include <stdio.h>

/*  macros */
#define F(z) ((z)*(z) - x)

/* prototypes */
double ABS();

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


main()
{
	double x, x1, x2, x3,x4, x5,  eps;
	double retval;

	printf ("enter x and epsilon ");
	scanf ("%lf %lf", &x, &eps);

	if ( x <= 0.0 || eps <= 0.0)
	{
		printf (" x and eps need to be greater than 0.0\n");
		retval = (1);
	}
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

		printf ("sqrt(%f) = %f +- %f\n", x, x3, eps);
	}
}


/*---------------------------------------------------------------------------*/
/*   
		  Function         : ABS
		  Parameters       : real number x 
		  Output           : absolute value of x 
*/
/*---------------------------------------------------------------------------*/



double ABS(x)
double x;
{
	double retval = 0;
	if (x<0)
		retval = (-x);
	else 
		retval =  (x);

	return retval;
}

