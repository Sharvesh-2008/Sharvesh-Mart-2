@echo off
setlocal
REM =====================================================================
REM SharveshMart - repeatable local run script (Windows).
REM
REM Builds the war, deploys it to the portable Tomcat in tools/, and starts
REM Tomcat with the working directory set to the repository root so the H2
REM file database at ./data/sharvesh_mart is created inside the repository
REM (retro Sprint 1 "next change": pin the Tomcat/H2 setup to a script).
REM =====================================================================

set "ROOT=%~dp0.."
cd /d "%ROOT%"

echo [1/4] Building war (mvn -B clean package)...
call "tools\apache-maven-3.9.16\bin\mvn.cmd" -B -q clean package
if errorlevel 1 (
    echo Build failed. See output above for details.
    exit /b 1
)

echo [2/4] Deploying sharvesh_mart.war to Tomcat...
if exist "tools\apache-tomcat-9.0.120\webapps\sharvesh_mart" (
    rmdir /s /q "tools\apache-tomcat-9.0.120\webapps\sharvesh_mart"
)
copy /y "target\sharvesh_mart.war" "tools\apache-tomcat-9.0.120\webapps\sharvesh_mart.war" >nul
if errorlevel 1 (
    echo Failed to copy war to the Tomcat webapps directory.
    exit /b 1
)

echo [3/4] Starting Tomcat from "%CD%"
echo         H2 file DB will be created at %CD%\data\sharvesh_mart.mv.db
set "CATALINA_HOME=%CD%\tools\apache-tomcat-9.0.120"
set "CATALINA_BASE=%CD%\tools\apache-tomcat-9.0.120"
call "%CATALINA_HOME%\bin\startup.bat"

echo [4/4] Done.
echo        App:     http://localhost:8080/sharvesh_mart/
echo        Health:  http://localhost:8080/sharvesh_mart/api/v1/health
endlocal