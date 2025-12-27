@echo off
if not exist "jdk-21.0.6+7\bin\java.exe" (
    echo ERROR: Java 21 not found at jdk-21.0.6+7\bin\java.exe
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

echo Starting Hurricane client...
cd bin
..\jdk-21.0.6+7\bin\java -Dsun.java2d.uiScale.enabled=false -Dsun.java2d.win.uiScaleX=1.0 -Dsun.java2d.win.uiScaleY=1.0 -Xss8m -Xms1024m -Xmx4096m --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED -DrunningThroughSteam=true -DrunningThroughDiscord=true -jar hafen.jar > ..\client.log 2>&1

REM Log exit code and close automatically
echo Client exited with code: %ERRORLEVEL% >> ..\client.log
exit