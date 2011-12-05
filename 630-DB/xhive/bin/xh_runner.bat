@echo off
REM Runner for X-hive tools

REM *** init environment ***

set OLD_JAVA_HOME=%JAVA_HOME%
set OLD_CLASSPATH=%CLASSPATH%
set JAVA_HOME=C:\Program Files\Java\jdk1.5.0_03\
set XHIVE_HOME=C:\xhive
set JMEM_MIN=32M
set JMEM_MAX=128M

REM *** Propagate other variables ***

set XHL=%XHIVE_HOME%\lib

if exist %JAVA_HOME%\lib\tools.jar set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%CLASSPATH%
if exist %JAVA_HOME%\lib\classes.zip set CLASSPATH=%JAVA_HOME%\lib\classes.zip;%CLASSPATH%

set CLASSPATH=%XHL%\antlr.jar;%XHL%\fop.jar;%XHL%\icu4j.jar;%XHL%\jsr173_api.jar;%XHL%\lucene.jar;%XHL%\mx4j.jar;%XHL%\retroweaver-rt.jar;%XHL%\serializer.jar;%XHL%\w3c.jar;%XHL%\xalan.jar;%XHL%\xbean.jar;%XHL%\xercesImpl.jar;%XHL%\xml-apis.jar;%XHL%\xhive.jar

set JVMARGS= -Dxhive.bootstrap="xhive://localhost:1235" -Xms%JMEM_MIN% -Xmx%JMEM_MAX%

REM *** execute command ***

REM Note that %1 will be the class-name
"%JAVA_HOME%\bin\java" %JVMARGS% -classpath "%CLASSPATH%" %*


REM *** Clean up ***

set JAVA_HOME=%OLD_JAVA_HOME%
set CLASSPATH=%OLD_CLASSPATH%
set OLD_JAVA_HOME=
set OLD_CLASSPATH=
set XHIVE_HOME=
set JMEM_MIN=
set JMEM_MAX=
