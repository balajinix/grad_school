int fetch()
{
    int value;
    scanf ("%d",&value);
    return value;
}
main()
{
    int red, green, blue, yellow,sweet,sour,salty,bitter,i;
    printf("Enter value for red\n");
    red = fetch();
    printf("Enter value for blue\n");
    blue = fetch();
    printf("Enter value for green\n");
    green = fetch();
    printf("Enter value for yellow\n");
    yellow = fetch();
    red = 2*red;
    sweet = red*green;
    sour = 0;
    for (i = 0; i < red; i++)
        sour += green;
    salty = blue + yellow;
    green = green + 1;
    bitter = yellow + green;
    printf ("\nThe value of sweet ( 2* red * blue) = %d\n",sweet);
    printf("%d %d %d\n",sour,salty,bitter);
}
