#include <stdio.h>
main()
{ int x,y,z,m;
  printf("Enter 1st number:\n");
  scanf("%d",&x);
  printf("Enter 2st number:\n");
  scanf("%d",&y);
  printf("Enter 3st number:\n");
  scanf("%d",&z);
  m = z;
  if (y<z)
  {	if (x<y)
  		m = y;
	else if (x<z)
		m = y;}
  else
  {	if (x>y)
		m = y;
	else if (x>z)
		 m = x;}
printf("Middle number is %d: ", m) ;
return 1;}

