JC = javac
JFLAGS = -g -Xlint
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES := $(wildcard *.java)

default : classes

classes : $(CLASSES:.java=.class)

.PHONY: clean
clean :
	-rm -f *.class

