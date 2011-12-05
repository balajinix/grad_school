/* Copyright (C) -- The Ohio State University */

#ifndef DBH_MAP_H
#define DBH_MAP_H
/* ------------------------------------------------------------------------- */
/* dbh_map.h

   Prototypes & structure definitions for mapping information

   Rev History:  3-15-94  J Michael Smith  -     Created
*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

#define DBH_NODE_LABEL_LENGTH 7


typedef struct dbh_map_data {
  int node_id;
  char node_label[DBH_NODE_LABEL_LENGTH];
  int node_type;
  int sub_type;
  int start_stmt_number;
  int end_stmt_number;
  int first_rtl_code_stmt;
  int last_rtl_code_stmt;
} DBH_MAP_DATA;

typedef struct dbh_map_info {
  int highest_node_number;
  int number_of_nodes;
  DBH_MAP_DATA *map_dat;
} DBH_MAP_INFO;

#ifdef DBH_MAP_MOD
DB_FILE *dbh_map_begin( char *program_name );
int      dbh_map_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_MAP_INFO **map_info_struct );
int      dbh_map_end( DB_FILE *prog_file );
DB_FILE *dbh_map_create( char *program_name );
int      dbh_map_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_MAP_INFO *map_info_struct );
void     dbh_map_free( DBH_MAP_INFO *map_info_struct );
int      dbh_map_pred_node( DBH_MAP_INFO *map_info_struct, int nodeid );
int      dbh_map_stmt_node( DBH_MAP_INFO *map_info_struct, int nodeid );
#else
extern DB_FILE *dbh_map_begin( char *program_name );
extern int      dbh_map_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_MAP_INFO **map_info_struct );
extern int      dbh_map_end( DB_FILE *prog_file );
extern DB_FILE *dbh_map_create( char *program_name );
extern int      dbh_map_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_MAP_INFO *map_info_struct );
extern void     dbh_map_free( DBH_MAP_INFO *map_info_struct );
extern int      dbh_map_pred_node( DBH_MAP_INFO *map_info_struct, int nodeid );
extern int      dbh_map_stmt_node( DBH_MAP_INFO *map_info_struct, int nodeid );
#endif

#endif
