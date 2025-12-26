@echo off
REM Build script for Hurricane client
REM Uses local JDK 21 and Apache Ant

set JAVA_HOME=%~dp0jdk-21.0.6+7
set PATH=%JAVA_HOME%\bin;%PATH%

echo Building Hurricane client...
%~dp0apache-ant-1.10.15\bin\ant.bat %*

if errorlevel 1 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Build completed successfully!
