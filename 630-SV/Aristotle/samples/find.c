
/*---------------------------------------------------------------------------*/
/*   
		  Program file name : find.c
		  Functions         : main
*/
/*---------------------------------------------------------------------------*/

/* include files */
#include<stdio.h>

/* constants */
#define MAX 256

/*---------------------------------------------------------------------------*/
/*   
		  Function         : main
		  Parameters       : none
          Input            : array A,  number of elements in array N, index F
          Output           : instrumented array 
          Description      : F is index into A().  After execution, all 
			     elements to the left of A(F) are less than or 
			     equal to A(F) and all elements to the right of 
                             A(F) are greater than or equal to A(F).
                             Only the first N elements are considered.
      From DeMillo, Lipton, and Sayward [DeMi78], repeated from Hoare's
      paper [Hoar70].
*/
/*---------------------------------------------------------------------------*/


main()
{
   int a[MAX+1];
   int n,f;
   int i;
   int b,m,ns,j,w;


   scanf("%d", &n);
   scanf("%d", &f);

   i = 1;
   while(i <= n)
   {
	  scanf("%d", &a[i]);
	  i++;
   }

   b = 0;
   m = 1;
   ns = n;

   while((m < ns) || b)
   {
	  if(!b)
	  {
		i = m;
		j = ns;
      }
	  else
		b = 0;

      if(i > j)
	  {
		 if(f > j)
		 {
			if(i > f)
			   m = ns;
            else
			   m = i;
         }
         else
		   ns = j;
      }
	  else
	  {
		 while(a[i]<a[f])
			 i = i+1;
         while(a[f]<a[j])
			 j = j-1;
         if(i<=j)
		 {
		   w = a[i];
		   a[i] = a[j];
		   a[j] = w;
		   i = i+1;
		   j = j-1;
         }

		 b = 1;
       }
    }

	printf("%5d\n", n);
	printf("%5d\n", f);
	i = 1;
	while(i<=n)
	{
	   printf("%5d\n", a[i]);
	   i++;
    }


}
