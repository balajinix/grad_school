/* Copyright (C) -- The Ohio State University */

/*
   This file is the header file used in a program instrumented
   to report statement trace information.
   December 1, 2000 -- Alexey Malishevsky -- added tracefile argument to IPF_st_init(...)
*/

#include <stdlib.h>    
#include <stdio.h>    
#include <string.h>   
#include <ctype.h>   
#define IPF_ST_FUNCNAMEMAX 256
#define IPF_ST_FILE "tr"
#define IPF_ST_PROGNAMEMAX 256
#define IPF_ST_FUNCNAMEMAX 256

#ifdef IPF_st_INIT
#define EXTERN /* */
#else
#define EXTERN extern
#endif

EXTERN char  ipf_st_program[IPF_ST_PROGNAMEMAX]; 
EXTERN FILE  *ipf_st_trace;

#undef    EXTERN

typedef struct function_list *function_list_ptr;

typedef struct function_list {
	char name[IPF_ST_FUNCNAMEMAX];
	int num_nodes;
	unsigned char *state_vect;
	function_list_ptr next;
} Name_List;

function_list_ptr function_list_head; 

/* function prototypes */

void free_function_list();
void insert_function_list(char *name, int num_statements);
void print_function_list();

int IPF_st_init(char * tracefile, char *proc_name, int num_statements);
int IPF_st_set_statement( int statement_number, char *func_name );
int IPF_st_term(char *trace_file);
