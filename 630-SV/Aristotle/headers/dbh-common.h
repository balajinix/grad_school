/* Copyright (C) -- The Ohio State University */

#ifndef DBH_COM_H
#define DBH_COM_H
/* -------------------------------------------------------------------- */
/* dbh_common.h

   This file contains the structure definitions and prototypes for generic
   routines used by all database handlers.

   Rev History:     3-15-94  J Michael Smith     -    Created
		    11-10-94 David A. Nedved     -    Added constatnts for
						      max line and filename
						      lengths
   Rev History       2-3-97  G.Rothermel         -  Added code supplied
                                                    by Sujatha and Ning
                     08-12-97   Rui Wu   -- Add declaration of 
                                      dbh_com_proc_unosorted_list();
		     2-19-98  Ning Ci -- Added declaration of
		     dbh_com_free_proc_list(DBH_PROC_NAMES* proc_list);
             9-10-98  S. Sinha -- added routines to free N_LIST and NODE_LIST
                                  search N_LIST and NODE_LIST
                      2-21-99 jlaw added missing function prototype.
*/
/* -------------------------------------------------------------------- */

#include <stdio.h>

/* define error codes */
#ifndef STATUS_OK
#define STATUS_OK              0
#endif

#define FILE_NOT_FOUND         1
#define PROCEDURE_NOT_FOUND    2
#define BAD_FILE_NAME          3
#define BAD_CONTROL_BLOCK      4
#define OUT_OF_MEMORY          5
#define BAD_PARAMETER          6
#define END_OF_FILE            7
#define BAD_FILE_FORMAT        8

#define MAX_LINE_LENGTH        1024
#define MAX_FILENAME_LENGTH    256

/* modified macro here  -- Ning Ci */
#ifndef DBH_PROC_NAME_SIZE
#define DBH_PROC_NAME_SIZE         80
#endif

/* structure used for neighbor lists */
typedef struct n_list {
  int node_id;
  char *edge_label;
  struct n_list *next;
} N_LIST;

/* structure used to represent files */
typedef struct db_file {
   char *program_name;
   FILE *db_file_ptr;
   int  mode;          /* 0=unopened  1=read  2=write */
} DB_FILE;

/* structure used to return all of the proedure names present in a file */
typedef struct proc_names {
  int number_of_names;
  char **name_array; /* note! names start at 1 not 0! */
} DBH_PROC_NAMES;

/* structure used for neighbor lists for interprocedural analysis*/
typedef struct node_list {
         int          proc_id;/* procedure id to which the node belongs to */
         int          node_id;/* id of the node */
         char         *edge_label;/* label on the edge to this node */
         struct node_list        *next;/* pointer to the next node */
} NODE_LIST;

/* structure used for call site lists for interprocedural analysis*/
typedef struct call_list {
         int          proc_id;/* procedure id to which the node belongs to */
         int          node_id;/* id of the node */
         int          call_proc_id; /* proc_id of the called procedure */
         char         flag;  /* flag to indicate if a particular call site is 
                                a dnrc or pnrc or not*/
         struct call_list        *next;/* pointer to the next node */
} CALL_LIST;

/* The structure UNREACHABLE is a list all unreachable nodes in a particular 
   procedure */

typedef struct unreachable{
   int        node_id;
   struct unreachable *next;
} UNREACHABLE;


/* prototypes */
#ifdef DBH_COM
DB_FILE *dbh_com_open_file( char *program_name, char *extension );
DB_FILE *dbh_com_create_file_block();
int      dbh_com_get_next_line( DB_FILE *f_ptr, char *buffer, int max );
int      dbh_com_close_file( DB_FILE *f_ptr );
DB_FILE *dbh_com_create_file( char *program_name, char *extension );
N_LIST  *dbh_com_add_to_nlist( N_LIST *old_nlist, int node_id, char *label);
N_LIST  *dbh_com_delete_from_nlist( N_LIST *old_nlist, int node_id);
NODE_LIST  *dbh_com_add_to_node_list( NODE_LIST *old_nlist,int proc_id,
                                      int node_id,char *label);
DBH_PROC_NAMES *dbh_com_proc_list( DB_FILE *f_ptr );
DBH_PROC_NAMES *dbh_com_proc_unsorted_list( DB_FILE *f_ptr );
DBH_PROC_NAMES *dbh_com_proc_frame_list( DB_FILE *f_ptr );
CALL_LIST  *dbh_com_add_to_call_list(CALL_LIST *calls,int call_proc_id,
                                     int proc_id,int node_id,int flag);
UNREACHABLE  *dbh_com_add_to_unreachable_list(UNREACHABLE *old_list,int node_id);
int dbh_com_search_proc_list(char **names, int count, char *name);
int db_error_code;
void dbh_com_free_proc_list(DBH_PROC_NAMES* proc_list);
void dbh_com_free_nlist( N_LIST *nlist );
void dbh_com_free_node_list( NODE_LIST *node_list );
int dbh_com_search_nlist( N_LIST *nlist, int node_id, char *label );
int dbh_com_search_node_list( NODE_LIST *nlist, int proc_id, int node_id,
                              char *label );
void dbh_com_sort_proc_names(char **names,int number_of_names); /*jlaw*/
#else
extern DB_FILE *dbh_com_open_file( char *program_name, char *extension );
extern DB_FILE *dbh_com_create_file_block();
extern int      dbh_com_get_next_line( DB_FILE *f_ptr, char *buffer, int max );
extern int      dbh_com_close_file( DB_FILE *f_ptr );
extern DB_FILE *dbh_com_create_file( char *program_name, char *extension );
extern N_LIST  *dbh_com_add_to_nlist( N_LIST *old_nlist, int node_id, char *label);
extern N_LIST  *dbh_com_delete_from_nlist( N_LIST *old_nlist, int node_id);
extern NODE_LIST  *dbh_com_add_to_node_list( NODE_LIST *old_nlist,int proc_id,
                                             int node_id,char *label);
extern DBH_PROC_NAMES *dbh_com_proc_list( DB_FILE *f_ptr );
extern DBH_PROC_NAMES *dbh_com_proc_unsorted_list( DB_FILE *f_ptr );
extern DBH_PROC_NAMES *dbh_com_proc_frame_list( DB_FILE *f_ptr );
extern CALL_LIST  *dbh_com_add_to_call_list(CALL_LIST *calls,int call_proc_id,
                                            int proc_id,int node_id,int flag);
extern UNREACHABLE  *dbh_com_add_to_unreachable_list(UNREACHABLE *old_list,
                                                     int node_id);
extern int dbh_com_search_proc_list(char **names, int count, char *name);
extern int db_error_code;
extern void dbh_com_free_proc_list(DBH_PROC_NAMES* proc_list);
extern void dbh_com_free_nlist( N_LIST *nlist );
extern void dbh_com_free_node_list( NODE_LIST *node_list );
extern int dbh_com_search_nlist( N_LIST *nlist, int node_id, char *label );
extern int dbh_com_search_node_list( NODE_LIST *nlist, int proc_id, int node_id,
                                     char *label );
extern void dbh_com_sort_proc_names(char **names,int number_of_names);/*jlaw*/
#endif /* DBH_COM */

#endif /* DBH_COM_H */
