# sample make file
SHELL = sh
VPATH = cpp tests
#NOGC = 1

OBJ = obj
DEP = dep

#VISUALC_UNDER_WINDOWS = 1
ifeq ($(OSTYPE),msys)
  MINGW_UNDER_WINDOWS = 1
endif

ifeq ($(OSTYPE),darwin)
  DARWIN = 1
endif

ifeq ($(OS),Windows_NT)
ifdef VISUALC_UNDER_WINDOWS
  VISUALC = 1
else
ifdef MINGW_UNDER_WINDOWS
  MINGW = 1
else
  CYGWIN = 1
endif
endif
else
  UNIX = 1
endif

ifeq ($(OS),SunOS)
NOGC = 1
endif

ifdef NOGC
GENOPT =-D__NOGC__
endif

ifneq ($(OS),Windows_NT)
  IFNEQ_TEST = 0
endif

TESTS = tests/tests-bundle

P = cfserver
#COPT =-O3 -DNO_ASSERT2 -Wall -fomit-frame-pointers  -march=athlon-xp

ifeq ($(OS),SunOS)

#options for Sun compiler
COPT =-xO2 -features=zla -DSUN_OS -library=stlport4 $(GENOPT)
DEP_OPT =-xM1
SYNTAX_OPT =-xe +w2

else

#options for GNU compiler
COPT =-O2 -Wall $(GENOPT) # -march=athlon-xp
#COPT = -O0 -Wall $(GENOPT)
DEP_OPT =-MM
SYNTAX_OPT =-Wall -fsyntax-only -Wno-deprecated
endif

#COPT =-O2 -D WITH_MYGC -Wall # -march=athlon-xp
#COPT =-O1 -Wall -fno-inline $(GENOPT)
#COPT =-O2 -fno-inline -Wall
#COPT =-O0 -D WITH_MYGC -fno-inline -Wall
#COPT =-O0 -fno-inline -Wall
#COPT =-O1 -Wall
WC = wc -l -w -c -L

######################################################################
ifdef VISUALC

CXX = cl /nologo
OUTFLAG = /Fo
O = obj
A = lib

INCLUDES = /I..\\gc\\include /I..\\tcl\\generic
LIBS = ..\\gc\\gc.$(A) ..\\tcl\\win\\release\\tcl84.lib imagehlp.lib user32.lib

CXXFLAGS = /GR /Zi /Yd /Zc:forScope /wd4355 /EHsc $(INCLUDES) -D__WIN__ /O2
#CXXFLAGS = /GR /Zi /Yd /Zc:forScope /wd4355 /EHsc $(INCLUDES) -D__WIN__ /O2 -DDEBUG_LOG
#CXXFLAGS = /GR /O2 /Zi /Yd /Zc:forScope /wd4355 /EHsc $(INCLUDES) -D__WIN__

LDFLAGS =  /DEBUG /Zi /F0x800000
#LDFLAGS = /DEBUG /Zi /F0x800000

######################################################################
else
ifdef MINGW

CXX = mingw32-g++
OUTFLAG = -o
O = o
A = a

INCLUDES = -I../gc/include -I../tcl/generic
ifdef NOGC
LIBS = -ltcl84s
else
LIBS = -ltcl84s -lgc
endif

#PF = -pg
CXXFLAGS = $(COPT) $(INCLUDES) -D__MINGW__ -D__WIN__ -DSTATIC_BUILD $(PF) -g
LDFLAGS = -L../gc/.libs -L../tcl/win $(PF) -g -Wl,--stack,96000000

######################################################################
else
ifdef UNIX

ifndef CXX
ifeq ($(OS),SunOS)
CXX = CC
else
CXX = g++
endif
endif
OUTFLAG = -o
O = o
A = a

INCLUDE = -I../gc/include -I../Tcl/generic
ifeq ($(OS),SunOS)
INCLUDE += -I/usr/sfw/include
endif

#PF = -pg -fprofile-arcs -ftest-coverage
#PF = -pg
#CXXFLAGS = -Wall -O2 -march=athlon-xp  $(PF) -g -Wno-deprecated
CXXFLAGS = $(COPT) $(INCLUDE) $(PF) -g
#LDFLAGS =  $(PF) -g -static
#LDFLAGS =  $(PF) -g
LDFLAGS =  $(PF) -g

ifdef NOGC
LIBS = -ldl ../Tcl/unix/libtcl8.4.a
else
LIBS = -ldl -lpthread ../gc/.libs/libgc.a ../Tcl/unix/libtcl8.4.a
#LIBS = -lpthread -ldl ../gc/libgc.a ../Tcl/unix/libtcl8.4.a
#LIBS = -ldl  /usr/local/lib/libgc.a -ltcl /usr/lib/libc.a
#LIBS = -lpthread -ltcl -ldl -lgc
endif

ifdef DARWIN
  CXXFLAGS += -DDARWIN
  LIBS += -framework Carbon
endif

######################################################################
else
ifdef CYGWIN

CXX = g++
OUTFLAG = -o
O = o
A = a

INCLUDE = -I../gc/include -I../Tcl/generic
ifdef NOGC
LIBS = -ltcl
else
LIBS = -lgc -ltcl
endif

#CXXFLAGS = $(COPT) -D__WIN__ -g -DDEBUG_LOG
CXXFLAGS = $(COPT) -D__WIN__ -g
LDFLAGS = -Wl,--stack,64000000

######################################################################
else

error "Unsupported target"

endif
endif
endif
endif

MODS = bpt common about bitset char pack builder auxfunc filename init \
       vector tuple queue list \
       mp_int allocate mem str log range msg text text-scan \
       includes strhash pp_env pp_tree \
       pp_evaluate pp_prepare pp_reusable pp_symbols cache src ops lexer \
       context program-context context-cache \
       ast ast.service ast.decl ast.declarator ast.expr ast.stmt ast.type \
       hash mode inh-table \
       cenv sym-path symbols symbols.search symbols.parents-tab cparser infile protocol \
       emacs-writer eclipse-writer far-writer std-writer msvc-writer \
       options module project set coor cfproject analyzer \
       util sort errors errors-builder \
       cfserver cfserver.usages cfserver.names cfserver.outline cfserver.list-inh-par \
       cfserver.rename tclinterp stacktrace gc-init

HDRS = $(MODS:%=%.h)
SRCS = $(MODS:%=%.cpp)
OBJS = $(MODS:%=$(OBJ)/%.$(O))

DIR = CF-C
ARC = $(DIR)
DISTR = cf-c.distr

all: $(P) .cf-modules cfcat

run: all
	$(MAKE) -C ../UI-ML run

test: $(TESTS)
	./$<

tests::
	make -C TESTS | grep errors

%.i:%.cpp
	$(CXX) -E (CXXFLAGS) $< -o $@



#----------------------------------------------------------

ifdef VISUALC

$(P): $(OBJS) $(OBJ)/cf-main.$(O)
	@echo Linking...
	@$(CXX) $(LDFLAGS) $^ $(LIBS) -o $@
#	@$(CXX) $(LDFLAGS) $^ $(LIBS) -o $@ /link /NODEFAULTLIB:LIBC user32.lib

else

$(P): $(OBJS) cf-main.$(O)
	@echo Linking...
	@$(CXX) $(LDFLAGS) -L. $^ $(LIBS) -o $@

static: $(OBJS) cf-main.$(O)
	@echo Linking...
	@$(CXX) $(LDFLAGS) -static -L. $^ $(LIBS) -o $@

endif

cfcat:
	@$(CXX) $(LDFLAGS) cfcat.cpp -o $@

#----------------------------------------------------------

$(TESTS): tests-main.cpp queue-test.cpp vector-test.cpp $(OBJS)
	@$(CXX) $(CPPFLAGS) $(CXXFLAGS) $(LDFLAGS) $^ $(LIBS) -o $@

#----------------------------------------------------------

syntax:
	$(CXX) $(SYNTAX_OPT) $(SRCS)

#----------------------------------------------------------

cleancov:
	$(RM) *.gcov *.da

clean: cleancov
	$(RM) -r core.* *.ilk *.pdb *.exe *.bak *.o $(OBJ)/*.o $(OBJ)/*.obj $(DEP)/* *~ $(P) cfserver cfemacs core *.cache *.ast gmon.out x.x *.a *.lib *.bb *.bbg cfserver.log.out *.elc $(TESTS)
	$(RM) -f `find . -name gmon.out`
	$(MAKE) -C TESTS clean

arc: clean
	@echo archiving...
	@strip cfserver.work || echo "stripping failed"
	@rm -f `find . -name \*~`
	@cd ..; rm -f $(ARC).tar.bz2; tar -c --exclude CVS $(DIR)/* $(DIR)/msvc/* $(DIR)/.cf-project | bzip2 >$(ARC).tar.bz2
	@ls -l ../$(ARC).tar.bz2
	@cp ../$(ARC).tar.bz2 ../$(ARC)-`date +%y-%b-%d-at-%H-%M`.tar.bz2

.cf-modules: Makefile
	@rm -f /tmp/mods
	@for mod in $(SRCS); do echo "module $$mod" >>/tmp/mods; done
	@echo "module cf-main.cpp" >>/tmp/mods
	@sort /tmp/mods >$@
	@rm -f /tmp/mods

#	rm -f $@
#	for path in $(VPATH) .; do \
#	   echo user-include-path $$path >>$@; \
#	for mod in $(SRCS); do ls 2>/dev/null $$path/$$mod | sed "s/\(.*\)/module \1/" >>$@; done; \
#	done

update: $(P)
	cp $< $<.work

idea-update:
	cp cfserver ~/SOFTW/IDEA/idea-6148/plugins/CppTools/lib/

flash: arc
	cp ../$(ARC).tar.bz2 /mnt/flash

floppy: arc
	@echo copying $(ARC).tar.bz2
	@mcopy -o ../$(ARC).tar.bz2 a:
	@mcopy -o ../$(ARC).tar.bz2 a:copy

wc:
	@echo "Emacs:" ; $(WC) emacs/*.el | tail -n 1
	@echo "Far:"   ; $(WC) far/*.h far/*.cpp 2>/dev/null | tail -n 1
	@echo "MS-VC:" ; $(WC) msvc/*.h msvc/*.cpp 2>/dev/null | tail -n 1
	@echo "CF-C:"  ; $(WC) *.h *.cpp *.inc | tail -n 1
	@echo "Tests:" ; $(WC) tests/*.h tests/*.cpp | tail -n 1
	@echo "All:"   ; $(WC) *.h *.cpp *.inc emacs/*.el far/*.h far/*.cpp msvc/*.h msvc/*.cpp tests/*.h tests/*.cpp 2>/dev/null | tail -n 1

distrib: static
	@if (ls CF-C) then rm CF-C -r; fi
	@mkdir CF-C
	@mkdir CF-C/emacs
	@cd emacs; make all; cd ..
	@cp emacs/*.el* CF-C/emacs
	@cd HTML; make all; cd ..
	@cp HTML/howto.html CF-C
	@cp static CF-C/cfserver.work
	@cp profile.tcl lib.tcl CF-C
	@tar -c CF-C/* | bzip2 > ../$(DISTR)-`date +%y-%b-%d`.tar.bz2

.suffixes: .d .$(O)

$(OBJ)/%.$(O):%.cpp
	@echo Compiling $(basename $<)...
	@if ! test -e $(OBJ); then mkdir $(OBJ); fi
	@$(CXX) -c $(CPPFLAGS) $(CXXFLAGS) $< $(OUTFLAG)$@

ifndef VISUALC
%.s:%.cpp
	$(CXX) -S $(CPPFLAGS) $(CXXFLAGS) $< $(OUTFLAG)$@
endif


$(DEP)/%.d: %.cpp
	@echo Building dependencies for $(basename $<)...
	@if ! test -e $(DEP); then mkdir $(DEP); fi
	@$(CXX) $(DEP_OPT) $(CPPFLAGS) $(CXXFLAGS) $< > $@.xxx
	@sed <$@.xxx s!^$(basename $<).o!obj/$(basename $<).o! >$@
	@rm $@.xxx

#	@$(CXX) -MM $(CPPFLAGS) $(CXXFLAGS) $< | sed s!$(basename $<).o!obj/$(basename $<).o! >$@

%.i: %.cpp
	@echo Preprocesing $(basename $<)...
	@$(CXX) -E $(CPPFLAGS) $(CXXFLAGS) $< >$@

ifndef VISUALC
include $(SRCS:%.cpp=$(DEP)/%.d) $(DEP)/$(P).d
endif
