
SRCDIR = .
OBJDIR = ../database/
BINDIR = ../bin/
HDRDIR = ../headers/
LIBDIR = ../lib/

CC = gcc
CFLAGS = -I$(HDRDIR) -c
LIBFLAGS = -L$(LIBDIR) -ldbh -lglobalfunc

OBJS = $(OBJDIR)/coverage.o
HDRS = $(HDRDIR)/dbh_th.h $(HDRDIR)/dbh_tr.h
SRC = 
EXES = $(BINDIR)/coverage

default:
	@echo 
	@echo   Make options:
	@echo
	@echo   build:   build coverage 
	@echo 

build: begin $(EXES) end

begin:
	@echo "Building coverage tool..."

end:
	@echo "Coverage tool finished"

$(BINDIR)/coverage: $(LIBDIR)/libdbh.a(dbh_th.o) $(LIBDIR)/libdbh.a(dbh-common.o) $(LIBDIR)/libglobalfunc.a $(SRCDIR)/coverage.c $(HDRS)
	@ echo "          compiling coverage"
	@ $(CC) $(CFLAGS) -o $(OBJDIR)/coverage.o $(SRCDIR)/coverage.c
	@ $(CC) -o $(BINDIR)/coverage $(OBJDIR)/coverage.o $(LIBFLAGS)
