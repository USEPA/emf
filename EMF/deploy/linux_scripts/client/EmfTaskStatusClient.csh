#!/bin/csh 

## Tests for status of tasks (jobs, exports, imports) and persisted


## top of EMF directory for jar files
setenv EMF_HOME ~/emf/EMFClient

## java 
setenv JAVA_HOME /usr/lib/jvm/java-6-sun
setenv JAVA_EXE $JAVA_HOME/bin/java

## tomcat apache server info (and port)
#setenv TOMCAT_SERVER http://orchid.nesc.epa.gov:8080
setenv TOMCAT_SERVER http://localhost:8080

## Input/Output preferences file
setenv PREF_FILE $EMF_HOME/EMFPrefs.txt

## add needed jar files to CLASSPATH


setenv CLASSPATH $EMF_HOME/lib/activation.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/analysis-engine-0.1.1.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/analysis-engine.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/antlr-2.7.5H3.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/asm-attrs.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/asm.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/axis-ant.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/axis.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/c3p0-0.9.0.2.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/cglib-2.1.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/cleanimports.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/colt.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/commons-collections-3.1.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/commons-discovery-0.2.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/commons-logging-1.0.4.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/commons-primitives-1.0.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/concurrent.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/connector.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/cosu.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/dom4j-1.6.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/ehcache-1.1.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/epa-commons.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/hibernate3.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/java_cup.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/jaxrpc.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/jh.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/jlfgr-1_0.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/jnlp.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/jta.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/log4j-1.2.9.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/mail.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/oscache-2.1.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/proxool-0.8.3.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/saaj-api.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/saaj-impl.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/saaj.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/soap.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/swarmcache-1.0rc2.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/versioncheck.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/weka.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/wsdl4j-1.5.1.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/xercesImpl.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/xercesSamples.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/xml-apis.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/xmlParserAPIs.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/lib/xstream-1.2.1.jar
setenv CLASSPATH ${CLASSPATH}:$EMF_HOME/emf-client.jar

## cmd client
$JAVA_EXE -Xmx400M -DEMF_HOME=$EMF_HOME -classpath $CLASSPATH gov.epa.emissions.framework.client.EMFTaskManagerStatusClient $TOMCAT_SERVER/emf/services 

