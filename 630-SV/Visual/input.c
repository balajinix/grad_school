main()
{
int i,j,m,p,n,a[100],res;
i = 2;
p = 1;
printf("Enter n\n");
scanf("%d",&n);
for(j=1;j<=n;j++)
{
	printf("Enter number %d\n",j);
	scanf("%d",&a[j]);
}
m = a[p];
while (i<n)
{
	if (a[p] <= a[i])
		p = i;
	a[i] = a[i] + a[i -1];
	i = i + 1;
}
m = a[p];
printf("Minimum is %d\n",m);
res = a[n];
printf("Sum is %d\n",res);
exit(0);
}
