
/*---------------------------------------------------------------------------*/
/*   
		  Program file name : bisect.c
		  Functions         : main
*/
/*---------------------------------------------------------------------------*/

/* include files */
#include <stdio.h>

/* macros */
#define F(z) ((z)*(z) - x)

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
*/
/*---------------------------------------------------------------------------*/




main()
{
	double x, x1, x2, x3, eps;

	printf ("enter x and epsilon ");
	scanf ("%lf %lf", &x, &eps);

	if (( x <= 0.0) || (eps <= 0.0) )
	{
		printf (" x and eps need to be greater than 0.0\n");
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

		printf ("sqrt(%f) = %f +- %f\n", x, x3, eps);
	}
}

