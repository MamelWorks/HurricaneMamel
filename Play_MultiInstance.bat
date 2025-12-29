@echo off
set INSTANCE=%1
if "%INSTANCE%"=="" set INSTANCE=1

if not exist "jdk-21.0.6+7\bin\javaw.exe" (
    echo ERROR: Java 21 not found at jdk-21.0.6+7\bin\javaw.exe
    echo Please make sure JDK is installed in the correct location.
    pause
    exit /b 1
)

if not exist "bin\hafen.jar" (
    echo ERROR: bin\hafen.jar not found!
    echo Please run the build first.
    pause
    exit /b 1
)

echo Starting Hurricane client instance %INSTANCE%...
cd bin
start "Hurricane Instance %INSTANCE%" ..\jdk-21.0.6+7\bin\javaw -Dsun.java2d.uiScale.enabled=false -Dsun.java2d.win.uiScaleX=1.0 -Dsun.java2d.win.uiScaleY=1.0 -Xss8m -Xms1024m -Xmx4096m --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED -DrunningThroughDiscord=true -jar hafen.jar
