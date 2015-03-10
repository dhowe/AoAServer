
JAVAC=/usr/bin/javac
SERVER_SRC=/Users/dhowe/Documents/eclipse-workspace/AoAServer/src
SERVER_BIN=/Users/dhowe/Documents/eclipse-workspace/AoAServer/bin


cd $SERVER_SRC/aoa/server
$JAVAC -d $SERVER_BIN -classpath ".:../../lib/jetty-6.1.3.jar:../../lib/jetty-util-6.1.3.jar:../../lib/servlet-api-2.5-6.1.3.jar" *.java
