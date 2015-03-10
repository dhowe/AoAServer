
SERVER_DIR=/Users/dhowe/Documents/eclipse-workspace/AoAServer/bin
cp $0 "/Users/dhowe/Documents/Flex Builder 3/ArchOfAssoc/src/bin/"
cd $SERVER_DIR
java -classpath ".:lib/jetty-6.1.3.jar:lib/jetty-util-6.1.3.jar:lib/servlet-api-2.5-6.1.3.jar" aoa.server.AoACommandServer $1 $2 
#&
