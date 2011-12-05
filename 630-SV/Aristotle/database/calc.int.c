/* Translated by the Edison Design Group C++/C front end (version 2.38) */
/* Wed Apr 13 18:03:59 2005 */
int __EDGCPFE__2_38;
#line 1 "calc.c"
#line 8 "/usr/include/math.h"
#ident "@(#)math.h\t2.11\t00/09/07 SMI"
#line 8 "/usr/include/iso/math_iso.h"
#ident "@(#)math_iso.h\t1.2\t00/09/07 SMI"
#line 11 "/usr/include/floatingpoint.h"
#ident "@(#)floatingpoint.h\t2.5\t99/06/22 SMI"
#line 9 "/usr/include/stdio_tag.h"
#ident "@(#)stdio_tag.h\t1.3\t98/04/20 SMI"
#line 8 "/usr/include/sys/ieeefp.h"
#ident "@(#)ieeefp.h\t2.8 99/10/29"
#line 20 "/usr/include/stdio.h"
#ident "@(#)stdio.h\t1.79\t01/04/16 SMI"
#line 32 "/usr/include/iso/stdio_iso.h"
#ident "@(#)stdio_iso.h\t1.2\t99/10/25 SMI"
#line 13 "/usr/include/sys/feature_tests.h"
#ident "@(#)feature_tests.h\t1.18\t99/07/26 SMI"
#line 9 "/usr/include/sys/isa_defs.h"
#ident "@(#)isa_defs.h\t1.20\t99/05/04 SMI"
#line 9 "/usr/include/sys/va_list.h"
#ident "@(#)va_list.h\t1.13\t01/02/08 SMI"
#line 9 "/usr/include/stdio_impl.h"
#ident "@(#)stdio_impl.h\t1.13\t01/11/16 SMI"
#line 78 "/usr/include/math.h"
enum version { libm_ieee = (0-1), c_issue_4, ansi_1, strict_ansi};
#line 18 "/usr/include/sys/ieeefp.h"
enum fp_direction_type {
fp_nearest,
fp_tozero,
fp_positive,
fp_negative};


enum fp_precision_type {
fp_extended,
fp_single,
fp_double,
fp_precision_3};


enum fp_exception_type {
fp_inexact,
fp_division,
fp_underflow,
fp_overflow,
fp_invalid};


enum fp_trap_enable_type {
fp_trap_inexact,
fp_trap_division,
fp_trap_underflow,
fp_trap_overflow,
fp_trap_invalid};
#line 124
enum fp_class_type {
fp_zero,
fp_subnormal,
fp_normal,
fp_infinity,
fp_quiet,
fp_signaling};
#line 99 "/usr/include/floatingpoint.h"
enum decimal_form {
fixed_form,


floating_form};
#line 115
enum decimal_string_form {
invalid_form,
whitespace_form,
fixed_int_form,
fixed_intdot_form,
fixed_dotfrac_form,
fixed_intdotfrac_form,
floating_int_form,
floating_intdot_form,
floating_dotfrac_form,
floating_intdotfrac_form,
inf_form,
infinity_form,
nan_form,
nanstring_form};
#line 38 "/usr/include/stdio_impl.h"
struct __FILE;
#line 21 "/usr/include/stdio_tag.h"
typedef struct __FILE __FILE;
#line 41 "/usr/include/floatingpoint.h"
typedef __FILE FILE;
#line 22 "/usr/include/stdio_impl.h"
typedef int ssize_t;
#line 38
struct __FILE {
#line 44
ssize_t _cnt;
unsigned char *_ptr;

unsigned char *_base;
unsigned char _flag;
unsigned char _file;
unsigned int __orientation: 2;
unsigned int __ionolock: 1;
unsigned int __seekable: 1;
unsigned int __filler: 4;};
#line 177 "/usr/include/floatingpoint.h"
extern double atof(const char *);
#line 179 "/usr/include/iso/stdio_iso.h"
extern int printf(const char *, ...);
#line 205
extern int ungetc(int, FILE *);
#line 222
extern int __filbuf(FILE *);
#line 16 "calc.c"
extern int main();
#line 87
extern void push();
#line 101
extern double pop();
#line 118
extern int clear();




extern int getop(); extern void IPF_bt_init(char *, enum decimal_string_form, enum decimal_string_form, enum decimal_string_form, char *); extern void IPF_bt_term(char *); extern enum decimal_string_form IPF_bt_set_branch(char *, enum decimal_string_form, enum decimal_string_form, enum 
#line 123
decimal_string_form); extern void IPF_bt_set_switch_branch(char *, enum decimal_string_form);
#line 147 "/usr/include/iso/stdio_iso.h"
extern __FILE __iob[20];
#line 13 "calc.c"
extern int sp;
double val[100]; extern enum decimal_string_form ipf_bt_sig_switch;
#line 13
int sp = 0;


int main()
{
auto int __2116_10_type;
auto char __2117_11_s[20];
auto double __2118_13_num1; auto double __2118_19_num2; IPF_bt_init("main", ((enum decimal_string_form)75), 1, fixed_intdotfrac_form, "calc.c.tr");


__2116_10_type = (((int (*)())getop)(((char *)__2117_11_s), 20));
while (IPF_bt_set_branch("main", floating_int_form, floating_intdot_form, (__2116_10_type != 113)))
{ ipf_bt_sig_switch = 1;
switch (__2116_10_type) {


case 48: IPF_bt_set_switch_branch("main", floating_dotfrac_form);
__2118_13_num1 = (((double (*)())atof)(((char *)__2117_11_s)));
((void (*)())push)(__2118_13_num1);
break;

case 43: IPF_bt_set_switch_branch("main", 9);
__2118_13_num1 = (((double (*)())pop)());
__2118_19_num2 = (((double (*)())pop)());
((void (*)())push)((__2118_13_num1 + __2118_19_num2));
break;

case 42: IPF_bt_set_switch_branch("main", 10);
__2118_13_num1 = (((double (*)())pop)());
__2118_19_num2 = (((double (*)())pop)());
((void (*)())push)((__2118_13_num1 * __2118_19_num2));
break;

case 45: IPF_bt_set_switch_branch("main", infinity_form);
__2118_13_num1 = (((double (*)())pop)());
__2118_19_num2 = (((double (*)())pop)());
((void (*)())push)((__2118_19_num2 - __2118_13_num1));
break;

case 47: IPF_bt_set_switch_branch("main", nan_form);
__2118_13_num1 = (((double (*)())pop)());
if (IPF_bt_set_branch("main", 46, ((enum decimal_string_form)47), (__2118_13_num1 != (0.000000000000000000e+00))))
{
__2118_19_num2 = (((double (*)())pop)());
((void (*)())push)((__2118_19_num2 / __2118_13_num1));
} else  {

printf(((const char *)"zero divisor popped\n")); }
break;

case 61: IPF_bt_set_switch_branch("main", nanstring_form);
__2118_13_num1 = (((double (*)())pop)());
printf(((const char *)"\t%f\n"), __2118_13_num1);
((void (*)())push)(__2118_13_num1);
break;

case 99: IPF_bt_set_switch_branch("main", ((enum decimal_string_form)14));
((int (*)())clear)();
break;

case 57: IPF_bt_set_switch_branch("main", ((enum decimal_string_form)15));
printf(((const char *)"%.20s ... is too long\n"), ((char *)__2117_11_s));
break;

default: IPF_bt_set_switch_branch("main", ((enum decimal_string_form)16));
printf(((const char *)"unknown command %c\n"), __2116_10_type);
break;}



__2116_10_type = (((int (*)())getop)(((char *)__2117_11_s), 20));
} IPF_bt_term("calc.c.tr");
return 0; }

void push(__2186_8_f)
double __2186_8_f;
{ IPF_bt_init("push", infinity_form, 1, fixed_intdotfrac_form, "calc.c.tr");
if (IPF_bt_set_branch("push", fixed_intdot_form, fixed_dotfrac_form, (sp < 100)))
{
(val[sp++]) = __2186_8_f;
}

else  {
printf(((const char *)"error: stack full\n"));
((int (*)())clear)();
}
return; }

double pop()
{
auto double __2201_13_retval; IPF_bt_init("pop", nanstring_form, 1, fixed_intdotfrac_form, "calc.c.tr");

if (IPF_bt_set_branch("pop", fixed_dotfrac_form, fixed_intdotfrac_form, (sp > 0)))
{
__2201_13_retval = (val[(--sp)]);
}

else  {
printf(((const char *)"error: stack empty\n"));
((int (*)())clear)();
__2201_13_retval = (0.000000000000000000e+00);
}
return __2201_13_retval;
}

int clear()
{ IPF_bt_init("clear", fixed_intdot_form, 1, fixed_intdotfrac_form, "calc.c.tr");
sp = 0;
return; }

int getop(__2222_6_s, __2223_5_lim)
char *__2222_6_s;
int __2223_5_lim;

{
auto int __2226_8_i; auto int __2226_10_c; auto int __2226_12_retval; IPF_bt_init("getop", ((enum decimal_string_form)68), 1, fixed_intdotfrac_form, "calc.c.tr");

__2226_10_c = (((--((__iob[0])._cnt)) < 0) ? (__filbuf((__iob + 0))) : ((int)((*(((__iob[0])._ptr)++)))));
while (IPF_bt_set_branch("getop", floating_int_form, floating_intdot_form, (((__2226_10_c == 32) || (__2226_10_c == 9)) || (__2226_10_c == 10))))
{
__2226_10_c = (((--((__iob[0])._cnt)) < 0) ? (__filbuf((__iob + 0))) : ((int)((*(((__iob[0])._ptr)++)))));
}

if (IPF_bt_set_branch("getop", infinity_form, nan_form, ((__2226_10_c != 46) && ((__2226_10_c < 48) || (__2226_10_c > 57)))))
{
__2226_12_retval = __2226_10_c;
}

else  {
(__2222_6_s[0]) = ((char)__2226_10_c);

__2226_10_c = (((--((__iob[0])._cnt)) < 0) ? (__filbuf((__iob + 0))) : ((int)((*(((__iob[0])._ptr)++)))));
__2226_8_i = 1;
while (IPF_bt_set_branch("getop", ((enum decimal_string_form)19), 20, ((__2226_10_c >= 48) && (__2226_10_c <= 57))))
{
if (IPF_bt_set_branch("getop", ((enum decimal_string_form)22), ((enum decimal_string_form)23), (__2226_8_i < __2223_5_lim)))
{
(__2222_6_s[__2226_8_i]) = ((char)__2226_10_c);
}
__2226_10_c = (((--((__iob[0])._cnt)) < 0) ? (__filbuf((__iob + 0))) : ((int)((*(((__iob[0])._ptr)++)))));
__2226_8_i++;
}

if (IPF_bt_set_branch("getop", ((enum decimal_string_form)30), ((enum decimal_string_form)31), (__2226_10_c == 46)))
{
if (IPF_bt_set_branch("getop", ((enum decimal_string_form)33), ((enum decimal_string_form)34), (__2226_8_i < __2223_5_lim)))
{
(__2222_6_s[__2226_8_i]) = ((char)__2226_10_c);
}
__2226_8_i++;
__2226_10_c = (((--((__iob[0])._cnt)) < 0) ? (__filbuf((__iob + 0))) : ((int)((*(((__iob[0])._ptr)++)))));
while (IPF_bt_set_branch("getop", ((enum decimal_string_form)41), ((enum decimal_string_form)42), ((__2226_10_c >= 48) && (__2226_10_c <= 57))))
{
if (IPF_bt_set_branch("getop", ((enum decimal_string_form)44), ((enum decimal_string_form)45), (__2226_8_i < __2223_5_lim)))
{
(__2222_6_s[__2226_8_i]) = ((char)__2226_10_c);
}
__2226_8_i++;
__2226_10_c = (((--((__iob[0])._cnt)) < 0) ? (__filbuf((__iob + 0))) : ((int)((*(((__iob[0])._ptr)++)))));
}
}

if (IPF_bt_set_branch("getop", ((enum decimal_string_form)53), ((enum decimal_string_form)54), (__2226_8_i < __2223_5_lim)))
{
ungetc(__2226_10_c, (__iob + 0));
(__2222_6_s[__2226_8_i]) = ((char)0);
__2226_12_retval = 48;
}

else  {
while (IPF_bt_set_branch("getop", ((enum decimal_string_form)60), ((enum decimal_string_form)61), ((__2226_10_c != 10) && (__2226_10_c != 113))))
{
__2226_10_c = (((--((__iob[0])._cnt)) < 0) ? (__filbuf((__iob + 0))) : ((int)((*(((__iob[0])._ptr)++)))));
}
(__2222_6_s[__2223_5_lim - 1]) = ((char)0);
__2226_12_retval = 57;
}
}

return __2226_12_retval;
}
