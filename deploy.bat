@echo off
echo ===================================
echo   CRM-OC Android - Build ^& Deploy
echo ===================================
echo.

cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [1/3] Building debug APK...
call gradlew.bat clean assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED!
    pause
    exit /b 1
)
echo.

set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo [2/3] Installing on device/emulator...
%ADB% install -r "app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% NEQ 0 (
    echo INSTALL FAILED! Is the phone connected or emulator running?
    pause
    exit /b 1
)
echo.

echo [3/3] Launching app...
%ADB% shell am start -n com.ossadkowski.crm.mobile/.MainActivity
echo.

echo ===================================
echo   Done! App is running.
echo ===================================
pause
