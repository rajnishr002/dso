@echo off

rem
rem  All content copyright (c) 2003-2006 Terracotta, Inc.,
rem  except as may otherwise be noted in a separate copyright notice.
rem  All rights reserved.
rem

setlocal
set topdir=%~d0%~p0..\..\..
cd %~d0%~p0
call "%topdir%\bin\dso-env.bat" -q tc-config.xml
start call "%topdir%\bin\tc-functions.bat" tc_java %tc_java_opts% -Dcom.sun.management.jmxremote -cp "classes;lib\org.mortbay.jetty-4.2.20.jar;lib\javax.servlet.jar" demo.sharedqueue.Main %*
endlocal
