@echo off

::  Batch file to start the EMF Client

set EMF_HOME=C:\Users\Public\EMFClient

set JAVA_EXE=C:\Program Files\Java\jre1.8.0_102\bin\java

set TOMCAT_SERVER=http://ec2-52-54-42-63.compute-1.amazonaws.com

set USER_PREFERENCES=%EMF_HOME%/EMFPrefs.txt

:: set needed jar files in CLASSPATH

SETLOCAL ENABLEDELAYEDEXPANSION

set CLASSPATH=%EMF_HOME%\emf-client.jar
for %%J in ("%EMF_HOME%\lib\*.jar") do set CLASSPATH=%%J;!CLASSPATH!
for %%J in ("%EMF_HOME%\lib\axis-1.4\*.jar") do set CLASSPATH=%%J;!CLASSPATH!
for %%J in ("%EMF_HOME%\lib\xerces-2.10.0\*.jar") do set CLASSPATH=%%J;!CLASSPATH!

@echo on

"%JAVA_EXE%" -Xmx1024M -DUSER_PREFERENCES="%USER_PREFERENCES%" -DEMF_HOME="%EMF_HOME%" -classpath "%CLASSPATH%" gov.epa.emissions.framework.client.EMFClient %TOMCAT_SERVER%/emf/services
