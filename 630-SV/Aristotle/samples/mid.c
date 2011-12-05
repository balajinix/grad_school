#include<stdio.h>
main()
{
int x, y, z, m;
printf("Enter the 3 numbers \n");
scanf("%d%d%d", &x,&y,&z);
   m = z;
  if(y < z)
  {
	if(x < y)
	   m = y;
    else if(x < z)
	   m = x;
  }
  else
	if(x > y)
	  m = y;
  else if(x > z)
	  m = x;
printf("Middle number is %d\n", m);
}


