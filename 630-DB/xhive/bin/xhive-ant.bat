@echo off
REM CALLANT.BAT

REM *** init environment ***

set OLD_ANT_HOME=%ANT_HOME%
set OLD_JAVA_HOME=%JAVA_HOME%
set OLD_CLASSPATH=%CLASSPATH%
set OLD_PATH=%PATH%
set JAVA_HOME=C:\Program Files\Java\jdk1.5.0_03\
set XHIVE_HOME=C:\xhive

set CLASSPATH=

if exist "%JAVA_HOME%"\lib\tools.jar set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%CLASSPATH%
if exist "%JAVA_HOME%"\lib\classes.zip set CLASSPATH=%JAVA_HOME%\lib\classes.zip;%CLASSPATH%

set ANT_HOME="."
set CLASSPATH=%XHIVE_HOME%\lib\ant.jar;%XHIVE_HOME%\lib\xml-apis.jar;%XHIVE_HOME%\lib\xercesImpl.jar;%CLASSPATH%

set PATH="%JAVA_HOME%"\bin;%PATH%

REM *** call ant ***

"%JAVA_HOME%\bin\java" -mx32m -Dant.home="%ANT_HOME%" -classpath "%CLASSPATH%" org.apache.tools.ant.Main -buildfile "%XHIVE_HOME%/bin/build.xml" %1 %2 %3 %4 %5 %6 %7 %8 %9

REM *** Clean up ***

set ANT_HOME=%OLD_ANT_HOME%
set JAVA_HOME=%OLD_JAVA_HOME%
set CLASSPATH=%OLD_CLASSPATH%
set PATH=%OLD_PATH%
set OLD_ANT_HOME=
set OLD_JAVA_HOME=
set OLD_CLASSPATH=
set OLD_PATH=
set XHIVE_HOME=



