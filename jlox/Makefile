JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java


CLASSES = \
        lox/Lox.java \
        lox/Scanner.java \
        lox/Token.java \
        lox/TokenType.java \
        lox/Parser.java \
        lox/AstPrinter.java \
        lox/Expression.java \
        

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class