
/*---------------------------------------------------------------------------*/
/*   
		  Program file name : bub.c
		  Functions         : main
*/
/*---------------------------------------------------------------------------*/

/* include files */
#include<stdio.h>


/*---------------------------------------------------------------------------*/
/*   
		  Function         : main
		  Parameters       : none
		  Input            : 5 integer array elements
		  Output           : sorted array 
		  Description      : sorts array of 5 integers using bubble sort  
*/
/*---------------------------------------------------------------------------*/


main()
{
	int a[6];
	/* index 0 is not to be used */

	int i, j, n, itmp;


	/* read elements into array */

	printf("Enter  5 elements into array to be sorted \n");
	i = 1;

	while( i <= 5)
	{
		scanf("%d", &a[i]);
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

	while(n<= 5)
	{
		printf("%d\t", a[n]);
		n++;
	}
	printf("\n");
	return;
}

