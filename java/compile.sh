cd org.cohorte.herald.api
mvn clean install -Dmaven.test.skip=true
P1=$?
cp target/org.cohorte.herald.api-*.jar $COHORTE_HOME/repo

cd ..
cd org.cohorte.herald.core
mvn clean install -Dmaven.test.skip=true
P2=$?
cp target/org.cohorte.herald.core-*.jar $COHORTE_HOME/repo

cd ..
cd org.cohorte.herald.http
mvn clean install -Dmaven.test.skip=true
P3=$?
cp target/org.cohorte.herald.http-*.jar $COHORTE_HOME/repo

cd ..
cd org.cohorte.herald.mqtt
mvn clean install -Dmaven.test.skip=true
P4=$?
cp target/org.cohorte.herald.mqtt-*.jar $COHORTE_HOME/repo

cd ..
cd org.cohorte.herald.rpc
mvn clean install -Dmaven.test.skip=true
P5=$?
cp target/org.cohorte.herald.rpc-*.jar $COHORTE_HOME/repo

cd ..
cd org.cohorte.herald.shell
mvn clean install -Dmaven.test.skip=true
P6=$?
cp target/org.cohorte.herald.shell-*.jar $COHORTE_HOME/repo

cd ..
cd org.cohorte.herald.xmpp
mvn clean install -Dmaven.test.skip=true
P7=$?
cp target/org.cohorte.herald.xmpp-*.jar $COHORTE_HOME/repo
cd ..

echo ""
echo "***********************"
echo api   $P1 
echo core  $P2
echo http  $P3 
echo mqtt  $P4
echo rpc   $P5 
echo shell $P6
echo xmpp  $P7
echo "***********************"
