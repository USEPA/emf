#!/bin/bash

## Script to start the EMF Client

## Top of the EMF client directory tree
EMF_HOME=~/EMFClient


## java 
JAVA_HOME=/usr/java/jre1.6.0_01
JAVA_EXE=$JAVA_HOME/bin/java

## Input/Output preferences file
PREF_FILE=~/EMFPrefs.txt

## tomcat apache server info (and port)
TOMCAT_SERVER=http://localhost:8080

##----------------------------------------------------------------##
## add needed jar files to CLASSPATH
CLASSPATH=$EMF_HOME/lib/activation.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/analysis-engine-0.1.1.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/analysis-engine.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/antlr-2.7.5H3.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/asm-attrs.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/asm.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/axis-ant.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/axis.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/c3p0-0.9.0.2.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/cglib-2.1.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/cleanimports.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/colt.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/commons-collections-3.1.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/commons-discovery-0.2.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/commons-logging-1.0.4.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/commons-primitives-1.0.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/concurrent.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/connector.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/cosu.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/dom4j-1.6.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/ehcache-1.1.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/epa-commons.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/hibernate3.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/java_cup.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/jaxrpc.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/jh.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/jlfgr-1_0.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/jnlp.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/jta.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/log4j-1.2.9.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/mail.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/oscache-2.1.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/proxool-0.8.3.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/saaj-api.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/saaj-impl.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/saaj.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/soap.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/swarmcache-1.0rc2.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/versioncheck.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/weka.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/wsdl4j-1.5.1.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/xercesSamples.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/xmlParserAPIs.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/lib/xstream-1.2.1.jar
CLASSPATH=$CLASSPATH:$EMF_HOME/emf-client.jar


## Start the client
$JAVA_EXE -Xmx400M -DUSER_PREFERENCES=$PREF_FILE -DEMF_HOME=$EMF_HOME -classpath $CLASSPATH gov.epa.emissions.framework.client.EMFClient $TOMCAT_SERVER/emf/services

