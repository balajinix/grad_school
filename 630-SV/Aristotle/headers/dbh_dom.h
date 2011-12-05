/* Copyright (C) -- The Ohio State University */

#ifndef DBH_DOM_H
#define DBH_DOM_H
/* ------------------------------------------------------------------------- */
/* dbh_dom.h

   Prototypes & structure definitions for domintator info routines.

   Rev History:  1-25-95  J Michael Smith  -     Created
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct dom_data {
  int     node_id;
  N_LIST  *dominates;
  N_LIST  *dominatedby;
  N_LIST  *dtree_parent;
  N_LIST  *dtree_children;
  N_LIST  *pdominates;
  N_LIST  *pdominatedby;
  N_LIST  *pdtree_parent;
  N_LIST  *pdtree_children;
} DOM_DATA;

typedef struct dbh_dom_info {
  int highest_node_number;
  int number_of_nodes;
  int dtree_root_node_id;
  int pdtree_root_node_id;
  DOM_DATA *dom_arr;
} DBH_DOM_INFO;

#ifdef DBH_DOM_MOD
DB_FILE *dbh_dom_begin( char *program_name );
int      dbh_dom_read( DB_FILE *prog_file, char *procedure_name,
                       DBH_DOM_INFO **dom_info_struct );
int      dbh_dom_end( DB_FILE *prog_file );
DB_FILE *dbh_dom_create( char *program_name );
int      dbh_dom_write( DB_FILE *prog_file, char *procedure_name,
                        DBH_DOM_INFO *dom_info_struct );
void     dbh_dom_free( DBH_DOM_INFO *dom_info_struct );
#else
extern DB_FILE *dbh_dom_begin( char *program_name );
extern int      dbh_dom_read( DB_FILE *prog_file, char *procedure_name,
                              DBH_DOM_INFO **dom_info_struct );
extern int      dbh_dom_end( DB_FILE *prog_file );
extern DB_FILE *dbh_dom_create( char *program_name );
extern int      dbh_dom_write( DB_FILE *prog_file, char *procedure_name,
                               DBH_DOM_INFO *dom_info_struct );
extern void     dbh_dom_free( DBH_DOM_INFO *dom_info_struct );
#endif

#endif
