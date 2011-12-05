/* Translated by the Edison Design Group C++/C front end (version 2.38) */
/* Mon May  2 22:21:44 2005 */
int __EDGCPFE__2_38;
#line 1 "mid.c"
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
#line 9 "/usr/include/stdio_tag.h"
#ident "@(#)stdio_tag.h\t1.3\t98/04/20 SMI"
#line 9 "/usr/include/stdio_impl.h"
#ident "@(#)stdio_impl.h\t1.13\t01/11/16 SMI"
#line 179 "/usr/include/iso/stdio_iso.h"
extern int printf(const char *, ...);

extern int scanf(const char *, ...);
#line 23 "mid.c"
extern int main(); extern int IPF_st_init(char *, char *, int); extern int IPF_st_set_statement(int, char *); extern int IPF_st_term(char *); int main()
{
auto int __1468_5_x; auto int __1468_8_y; auto int __1468_11_z; auto int __1468_14_m; IPF_st_init("mid.c.tr", "main", 30); IPF_st_set_statement(4, "main");
printf(((const char *)"Enter the 3 numbers \n")); IPF_st_set_statement(6, "main");
scanf(((const char *)"%d%d%d"), (&__1468_5_x), (&__1468_8_y), (&__1468_11_z)); IPF_st_set_statement(8, "main");

__1468_14_m = __1468_11_z;

if ((IPF_st_set_statement(9, "main")) && (__1468_8_y < __1468_11_z))
{
if ((IPF_st_set_statement(11, "main")) && (__1468_5_x < __1468_8_y)) { IPF_st_set_statement(13, "main");
__1468_14_m = __1468_8_y; } else  {
if ((IPF_st_set_statement(14, "main")) && (__1468_5_x < __1468_11_z)) { IPF_st_set_statement(16, "main");
__1468_14_m = __1468_5_x; } }
} else  {

if ((IPF_st_set_statement(19, "main")) && (__1468_5_x > __1468_8_y)) { IPF_st_set_statement(21, "main");
__1468_14_m = __1468_8_y; } else  {
if ((IPF_st_set_statement(22, "main")) && (__1468_5_x > __1468_11_z)) { IPF_st_set_statement(24, "main");
__1468_14_m = __1468_5_x; } } } IPF_st_set_statement(28, "main");

printf(((const char *)"Middle number is %d\n"), __1468_14_m); IPF_st_term("mid.c.tr");

return 0; }
