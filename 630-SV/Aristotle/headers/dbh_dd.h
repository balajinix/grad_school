/* Copyright (C) -- The Ohio State University */

#ifndef DBH_DD_H
#define DBH_DD_H
/* ------------------------------------------------------------------------- */
/* dbh_dd.h

   Prototypes & structure definitions for data dependence database routines.

   Rev History:  10-10-94  G. Rothermel -     Created
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct dd_data {
  int     node_id;
  N_LIST  *fd_preds;        /* flow dependence predecessors */
  N_LIST  *fd_succs;        /* flow dependence successors */
  N_LIST  *od_preds;        /* output dependence predecessors */
  N_LIST  *od_succs;        /* output dependence successors */
} DD_DATA;

typedef struct dbh_dd_info {
  int highest_node_number;
  int number_of_nodes;
  int root_node_id;
  DD_DATA *dd_arr;
} DBH_DD_INFO;

#ifdef DBH_DD_MOD
DB_FILE *dbh_dd_begin( char *program_name );
int      dbh_dd_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_DD_INFO **dd_info_struct );
int      dbh_dd_end( DB_FILE *prog_file );
DB_FILE *dbh_dd_create( char *program_name );
int      dbh_dd_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_DD_INFO *dd_info_struct );
void     dbh_dd_free( DBH_DD_INFO *dd_info_struct );
#else
extern DB_FILE *dbh_dd_begin( char *program_name );
extern int      dbh_dd_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_DD_INFO **dd_info_struct );
extern int      dbh_dd_end( DB_FILE *prog_file );
extern DB_FILE *dbh_dd_create( char *program_name );
extern int      dbh_dd_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_DD_INFO *dd_info_struct );
extern void     dbh_dd_free( DBH_DD_INFO *dd_info_struct );
#endif

#endif
