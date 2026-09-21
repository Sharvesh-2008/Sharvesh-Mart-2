@echo off
setlocal
REM =====================================================================
REM SharveshMart - stop the portable Tomcat instance.
REM =====================================================================

set "ROOT=%~dp0.."
cd /d "%ROOT%"

set "CATALINA_HOME=%CD%\tools\apache-tomcat-9.0.120"
set "CATALINA_BASE=%CD%\tools\apache-tomcat-9.0.120"
call "%CATALINA_HOME%\bin\shutdown.bat"
echo Stopped Tomcat (if it was running).
endlocal