
/* This program is a version of the reverse Polish calculator
   described in the first edition of kernighan and ritchie, pp. 74ff. */

#define MAXVAL 100
#define MAXOP 20
#define NUMBER '0'
#define DONE 'q'
#define TOOBIG '9'
#include <math.h>
#include <stdio.h>

int sp = 0;
double val[MAXVAL];

main()
{
     int type;
     char s[MAXOP];
     double num1, num2, op2, atof(), pop();
     void push();

     type = getop(s,MAXOP);
     while (type != DONE)
     {
        switch (type) 
        { 

          case NUMBER:
            num1 = atof(s);
            push(num1);
            break;

          case '+':
            num1 = pop();
            num2 = pop();
            push(num1 + num2);
            break;
         
          case '*':
            num1 = pop();
            num2 = pop();
            push(num1 * num2);
            break;
         
          case '-':
            num1 = pop();
            num2 = pop();
            push(num2 - num1);
            break;
         
          case '/':
            num1 = pop();
            if (num1 != 0.0)
            {
               num2 = pop();
               push( num2 / num1);
            }
            else
               printf("zero divisor popped\n");
            break;
         
           case '=':
            num1 = pop();
            printf("\t%f\n",num1);
            push(num1);
            break;
         
           case 'c':
            clear();
            break;

           case TOOBIG:
            printf("%.20s ... is too long\n",s);
            break;

           default:
            printf("unknown command %c\n",type);
            break;

         }

         type = getop(s,MAXOP);
     }
}

void push(f)
double f;
{
     if (sp < MAXVAL)
     {
        val[sp++] = f;
     }
     else
     {
         printf("error: stack full\n");
         clear();
     }
}

double pop()
{
     double retval;

     if (sp > 0)
     {
        retval = val[--sp];
     }
     else
     { 
        printf("error: stack empty\n");
        clear();
        retval = 0;
     }
     return(retval);
}

clear()
{
   sp = 0;
}

getop(s, lim)
char s[];
int lim;

{
   int i,c,retval;

   c = getc(stdin);
   while ( c == ' ' || c == '\t' || c == '\n' )
   {
       c = getc(stdin);
   }

   if (c != '.' && (c < '0' || c > '9') )
   {
       retval = c; 
   }
   else
   {
      s[0] = c;

      c = getc(stdin);
      i = 1;
      while (c >= '0' && c <= '9')
      {
          if (i < lim)
          {
             s[i] = c;
          }
          c = getc(stdin);
          i++;
      }

      if (c == '.')
      {
          if (i < lim)
          {
             s[i] = c;
          }
          i++;
          c = getc(stdin);
          while (c >= '0' && c <= '9')
          {
              if (i<lim)
              {
                 s[i] = c;
              }
              i++;
              c = getc(stdin);
          }
      }

      if (i < lim)
      {
         ungetc(c,stdin);
         s[i] = '\0';
         retval = NUMBER;
      }
      else
      {
         while (c!= '\n' && c != DONE)
         {
           c = getc(stdin);
         }
         s[lim-1] = '\0';
         retval = TOOBIG;
      }
   }

   return (retval);
}

