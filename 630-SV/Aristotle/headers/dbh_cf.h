/* Copyright (C) -- The Ohio State University */

#ifndef DBH_CF_H
#define DBH_CF_H
/* ------------------------------------------------------------------------- */
/* dbh_cf.h

   Prototypes & structure definitions for control-flow database routines.

   Rev History:  3-15-94  J Michael Smith  -     Created
                 5-15-94  G Rothermel  -  added E_LIST information
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct dbh_cf_edge_info {
   int pred_node;
   int succ_node;
   char *edge_label;
} DBH_CF_EDGE_INFO;

/* Structure used for edge list - this gives us a mapping
   of edges to unique integer edge identifiers; where a 
   edge is a labelled cfg edge.  This is needed to obtain
   edge execution traces in a concise format.  */
typedef struct e_list {
  int node_id;
  int edge_id;
  char *edge_label;
  struct e_list *next;
} E_LIST;

typedef struct cf_data {
  int     node_id;
  N_LIST  *cf_preds;
  N_LIST  *cf_succs;
  E_LIST  *e_list;
} CF_DATA;

typedef struct dbh_cf_info {
  int highest_node_number;
  int number_of_nodes;
  int number_of_edges;
  int root_node_id;
  int is_reachable; /* -- mavrick 09/06/95 added field for reachability info */
  CF_DATA *cf_arr;
} DBH_CF_INFO;

#ifdef DBH_CF_MOD
DB_FILE *dbh_cf_begin( char *program_name );
int      dbh_cf_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_CF_INFO **cf_info_struct );
int      dbh_cf_end( DB_FILE *prog_file );
DB_FILE *dbh_cf_create( char *program_name );
int      dbh_cf_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_CF_INFO *cf_info_struct );
void     dbh_cf_free( DBH_CF_INFO *cf_info_struct );
void     dbh_cf_free_from_fe( DBH_CF_INFO *cf_info_struct );
int      dbh_cf_get_edge_list( DBH_CF_INFO *cf_info_struct, DBH_CF_EDGE_INFO **edge_list );
int dbh_cf_get_edge_number( DBH_CF_INFO *cf_info_struct, int sourcenode, int sinknode, char *label);
#else
extern DB_FILE *dbh_cf_begin( char *program_name );
extern int      dbh_cf_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_CF_INFO **cf_info_struct );
extern int      dbh_cf_end( DB_FILE *prog_file );
extern DB_FILE *dbh_cf_create( char *program_name );
extern int      dbh_cf_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_CF_INFO *cf_info_struct );
extern void     dbh_cf_free( DBH_CF_INFO *cf_info_struct );
extern void     dbh_cf_free_from_fe( DBH_CF_INFO *cf_info_struct );
extern int      dbh_cf_get_edge_list( DBH_CF_INFO *cf_info_struct, DBH_CF_EDGE_INFO **edge_list );
extern int dbh_cf_get_edge_number( DBH_CF_INFO *cf_info_struct, int sourcenode, int sinknode, char *label);
#endif

#endif
