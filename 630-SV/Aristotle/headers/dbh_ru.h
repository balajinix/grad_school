/* Copyright (C) -- The Ohio State University */

#ifndef DBH_RU_H
#define DBH_RU_H
/* ------------------------------------------------------------------------- */
/* dbh_ru.h

   Prototypes & structure defs for reaching uses database routines.

   Rev History:  6-01-94  David A. Nedved        -     Created

*/
/* ------------------------------------------------------------------------- */

#ifndef DBH_COM_H
#include "dbh-common.h"
#endif

typedef struct ru_data 
  {
  int     node_id;

  int     num_uses;
  int    *node_id_arr;
  int    *var_id_arr;
  } DBH_RU_DATA;

typedef struct dbh_ru_info 
  {
  int highest_node_number;
  int number_of_nodes;
  DBH_RU_DATA *ru_dat;
  } DBH_RU_INFO;


#ifdef DBH_RU_MOD
DB_FILE *dbh_ru_begin( char *program_name );
int      dbh_ru_read( DB_FILE *prog_file, char *procedure_name,
                      DBH_RU_INFO **ru_info_struct );
int      dbh_ru_end( DB_FILE *prog_file );
DB_FILE *dbh_ru_create( char *program_name );
int      dbh_ru_write( DB_FILE *prog_file, char *procedure_name,
                      DBH_RU_INFO *ru_info_struct );
void     dbh_ru_free( DBH_RU_INFO *ru_info_struct );
#else
extern DB_FILE *dbh_ru_begin( char *program_name );
extern int      dbh_ru_read( DB_FILE *prog_file, char *procedure_name,
                             DBH_RU_INFO **ru_info_struct );
extern int      dbh_ru_end( DB_FILE *prog_file );
extern DB_FILE *dbh_ru_create( char *program_name );
extern int      dbh_ru_write( DB_FILE *prog_file, char *procedure_name,
                              DBH_RU_INFO *ru_info_struct );
extern void     dbh_ru_free( DBH_RU_INFO *ru_info_struct );
#endif

#endif
