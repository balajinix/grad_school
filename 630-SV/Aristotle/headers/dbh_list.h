/* Copyright (C) -- The Ohio State University */

#ifndef LISTS
#define LISTS

/*----------------------------------------------------------------------*/
/*    
          dbh_list.h
          generic list manipulation package header file
          original authors: amar & rama
                            modified:  kanu tewary 
			    modified:  m smith
			    modified:  Clark Crawford
*/
/*----------------------------------------------------------------------*/

/*----------------------------------------------------------------------*/
/* Include files */

#include <stdio.h>
#include <string.h>
#include <ctype.h>


/*---------------------------------------------------------------------*/
/* constants */

#define TRUE 1
#define FALSE 0

/*---------------------------------------------------------------------*/
/* generic list elements */

#define LIST_ELEMENT struct list_element
LIST_ELEMENT
{
    void               *info_ptr;  /* This is a pointer to any kind of
                                      structure like
                                      MAP_INFO,CF_INFO,
                                      DEF_USE_INFO etc. Whenever we need
                                      to access a particular structure
                                      we have to typecast 'info' to the
                                      type of the structure. */
    LIST_ELEMENT       *next_element_ptr;
};


#define LIST_HEADER struct list_header
LIST_HEADER
{
    LIST_ELEMENT       *first_element_ptr;  /* Pointer to the first
                                               element in a list */
    LIST_ELEMENT       *last_element_ptr;  /* Pointer to the last
                                              element in a list */
};


/*-------------------------------------------------------------------------*/
/* Function prototypes */


void dbh_list_insert_end(LIST_HEADER *,void *);
void dbh_list_delete_end(LIST_HEADER *);
void dbh_list_free(LIST_HEADER *);
LIST_HEADER *dbh_list_new(void);
void dbh_list_delete(LIST_HEADER *);
void dbh_list_init(LIST_HEADER *);
int  dbh_list_empty(LIST_HEADER );
void dbh_list_cat(LIST_HEADER *,LIST_HEADER *);
int dbh_list_length(LIST_HEADER *);

#endif

