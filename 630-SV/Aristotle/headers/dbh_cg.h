/* Copyright (C) -- The Ohio State University */

#ifndef DBH_CG_H
#define DBH_CG_H
/* ------------------------------------------------------------------------- */
/* dbh_cg.h

   This file contains the structure definitions and prototypes for the call
   graph handler routines.

   Rev. History:  12-12-96 Clark Crawford          - created

*/
/*-------------------------------------------------------------------------- */



#include "dbh-common.h"
#include "dbh_list.h"



typedef struct cg_node
{
  char *proc_name;                             /* name of the node procedure */
  int node_id;                                          /* unique integer id */
  int defined;                       /* true --> proc defined in source file */
  int number_of_edges;                               /* length of edge_array */
  int *edge_array;                         /* outbound edges, store node ids */
}
DBH_CG_INFO;            /* structures manipulated singly or in generic lists */
DBH_CG_INFO *dbh_cg_new_node (const char *proc_name, int node_id,
			      int defined, int number_of_edges);
DBH_CG_INFO *dbh_cg_find_node (LIST_HEADER *cg, const char *proc_name,
			       int node_id);
LIST_HEADER *dbh_cg_read_all (DB_FILE *prog_file);
DB_FILE *dbh_cg_begin (char *program_name);
int dbh_cg_read (DB_FILE *prog_file, char *procedure_name,
		 DBH_CG_INFO **cg_info_struct);
int dbh_cg_end (DB_FILE *prog_file);
DB_FILE *dbh_cg_create (char *program_name);
int dbh_cg_write (DB_FILE *prog_file, DBH_CG_INFO *cg_info_struct);
void dbh_cg_free (DBH_CG_INFO *cg_info_struct);
void dbh_cg_delete_list (LIST_HEADER *cg);
#endif                                                   /* #ifndef DBH_CG_H */
