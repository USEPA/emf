set PATH=%PATH%;C:\j2sdk1.4.2_01\bin
set HOME=D:\CEP\AnalysisEngine
set CLASSPATH=%HOME%\lib\jh.jar;%HOME%\lib\jhall.jar;%HOME%\lib\jsearch.jar

java -classpath %CLASSPATH% com.sun.java.help.search.Indexer -c JavaSearchIndexConfig.txt  -db ..\JavaHelpSearch ..\html

