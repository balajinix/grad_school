
/*---------------------------------------------------------------------------*/
/*   
		  Program file name : newton.c
		  Functions         : main, ABS
*/
/*---------------------------------------------------------------------------*/

/* include files */
#include <stdio.h>

/* macros */
#define F(z) ((z)*(z) - x)
#define D(z) ((z)*2.0)


/*---------------------------------------------------------------------------*/
/*   
		  Function         : ABS 
		  Parameters       : real number 
		  Input            : none 
		  Output           : absolute value of number 
*/
/*---------------------------------------------------------------------------*/


double ABS(x)
double x;
{
int retval = 0;
	if (x<0)
		retval = -x;
	else 
        retval = x;		
return retval;
}

/*---------------------------------------------------------------------------*/
/*   
		  Function         : main
		  Parameters       : none
		  Input            : real number, real epsilon value
		  Output           : square root of number 
		  Description      : computes the square root of a number 
                                     using newton's method							 

  From Applied Numerical Analysis, 3rd Ed
  Curtis F. Gerald, Patrick O. Wheatley;  Addison Wesley, 1985 

  chapter 1. pg 16. -- Newton's Method  
*/
/*---------------------------------------------------------------------------*/



main()
{
	double x, x1, x2, x3, eps;

	printf ("enter x and epsilon ");
	scanf ("%lf %lf", &x, &eps);

	if ( x <= 0.0 || eps <= 0.0) 
	{
		printf (" x and eps need to be greater than 0.0\n");
		/*  removing return statement, since not accepted by Aristotle 
		added else clause to compensate */
		/*
		return(1);
		*/
	}
	else
	{

	x1 = x/2.0;
	x2 = eps;

	while ( ABS (x2 - x1) >= eps  ||
		ABS(F(x2)) >= eps )
	{
		x1 = x2;
		x2 = x1 - F(x1)/D(x1);
	}

	printf ("sqrt(%f) = %f\n", x, x2);
	}
}
