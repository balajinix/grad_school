/*---------------------------------------------------------------------------*/
/*   
		  Program file name : euclid.c
		  Functions         : main
*/
/*---------------------------------------------------------------------------*/

/* include files */
#include<stdio.h>

/*---------------------------------------------------------------------------*/
/*   
		  Function         : main
		  Parameters       : none
		  Input            : integers a and b
		  Output           : GCD of a and b  
		  Description      : uses euclid's famous algo for gcd computation 
*/
/*---------------------------------------------------------------------------*/


main()
{
	int e, a, b;
	int div, rem, d;
	scanf("%d%d", &a, &b);

	rem = 1;

	while(rem > 0)
	{
		rem = a%b;
		a = b;
		b = rem;
	}

	printf("%d\n", a);
}
