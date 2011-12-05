
/*---------------------------------------------------------------------------*/
/*   
		  Program file name : search.c
		  Functions         : main
*/
/*---------------------------------------------------------------------------*/

/* include files */
#include<stdio.h>

/*---------------------------------------------------------------------------*/
/*   
		  Function         : main
		  Parameters       : none
		  Input            : 8 integer array elements, element e to search for
		  Output           : index of element e , if found in array,
				     or index of element preceeding where e 
				     should be placed
		  Description      : search array for element e , return index 
			             to e or index immediately preceeding where 
                                     e should be placed. Uses binary search
*/
/*---------------------------------------------------------------------------*/

main()
{
	int a[8];

	int j, k, i;
	int s, e;

	printf("Enter 8 array values\n");
	i = 0;
	while(i < 8)
	{
		scanf("%d", &a[i]);
		i++;
	}

	printf("Enter element\n");
	scanf("%d", &e);

	if(e < a[0])
		s = -1;

	else
	{
		i = 0;
		j = 7;

		while(i < j)
		{
			k = (i + j + 1)/2;
			if(e < a[k])
				j = k-1;
			else
				i = k;
		}

		s = i;
	}


	printf("%d\n", s);
}
