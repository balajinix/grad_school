/* Copyright (C) -- The Ohio State University */

#ifndef DBH_DU_H
#define DBH_DU_H
/* ------------------------------------------------------------------------- */
/* dbh_du.h

   Prototypes & structure definitions for def-use database routines.

   Rev History:  3-15-94  J Michael Smith  -     Created
		 5-27-94  David A. Nedved  -     Added typedefs for occurences
						 Added prototypes for has_use
								  abd has_def
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct du_data {
  int     node_id;

  int     num_defs;
  int    *def_list_id;
  int    *def_list_occ_type;

  int     num_uses;
  int    *use_list_id;
  int    *use_list_occ_type;

} DBH_DU_DATA;

typedef struct dbh_du_info {
  int highest_node_number;
  int number_of_nodes;
  DBH_DU_DATA *du_dat;
} DBH_DU_INFO;

/*
The following are bitstreams defined for variable occurence type.
These types determine what types of assignment or usage of a variable
occurs in a node.
*/

#define DBH_DU_AU       0x00000001    /* address use                     */
#define DBH_DU_SVU      0x00000002    /* scalar variable use             */
#define DBH_DU_SVA      0x00000004    /* scalar variable assignment      */
#define DBH_DU_PVU      0x00000008    /* pointer variable use            */
#define DBH_DU_PVA      0x00000010    /* pointer variable assignment     */
#define DBH_DU_IVA      0x00000020    /* indirect variable use           */
#define DBH_DU_IVU      0x00000040    /* indirect variabble assignment   */
#define DBH_DU_IIVA     0x00000080    /* double-indirect variable use    */
#define DBH_DU_IIVU     0x00000100    /* double-indirect var. assignment */

/*
These types are derivative types of the occurence types defined above.
These simply specify that a variable of the type appears somewhere in
the node in question, either as a use or an assignment.  These values
are combinations of assignment and usage of the same varieties.
These values are easily found by bitwise OR'ing together the values
given above.  These are given only for the convenience of the programmer
of applications.
*/

#define DBH_DU_SV  DBH_DU_SVU | DBH_DU_SVA   /* all scalar variable      */
#define DBH_DU_PV  DBH_DU_PVU | DBH_DU_PVA   /* all pointer variable     */
#define DBH_DU_IV  DBH_DU_IVU | DBH_DU_IVA   /* all indirect variable    */
#define DBH_DU_IIV DBH_DU_IIVU | DBH_DU_IIVA /* all double indirect var. */
#define DBH_DU_ALL DBH_DU_AU | DBH_DU_SVU | DBH_DU_SVA | DBH_DU_PVU |    \
		   DBH_DU_PVA | DBH_DU_IVA | DBH_DU_IVU | DBH_DU_IIVA |  \
		   DBH_DU_IIVU               /* all variables            */


#ifdef DBH_DU_MOD
DB_FILE *dbh_du_begin( char *program_name );
int      dbh_du_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_DU_INFO **du_info_struct );
int      dbh_du_end( DB_FILE *prog_file );
DB_FILE *dbh_du_create( char *program_name );
int      dbh_du_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_DU_INFO *du_info_struct );
void     dbh_du_free( DBH_DU_INFO *du_info_struct );
int      dbh_du_has_use(unsigned int use_type, unsigned int bitstring);
int      dbh_du_has_def(unsigned int def_type, unsigned int bitstring);
#else
extern DB_FILE *dbh_du_begin( char *program_name );
extern int      dbh_du_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_DU_INFO **du_info_struct );
extern int      dbh_du_end( DB_FILE *prog_file );
extern DB_FILE *dbh_du_create( char *program_name );
extern int      dbh_du_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_DU_INFO *du_info_struct );
extern void     dbh_du_free( DBH_DU_INFO *du_info_struct );
extern int      dbh_du_has_use(unsigned int use_type, unsigned int bitstring);
extern int      dbh_du_has_def(unsigned int def_type, unsigned int bitstring);
#endif

#endif
