@echo off
echo ===================================
echo   CRM-OC Android - Build ^& Deploy
echo ===================================
echo.

cd /d "%~dp0"

echo [1/3] Building debug APK...
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED!
    pause
    exit /b 1
)
echo.

echo [2/3] Installing on device...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% NEQ 0 (
    echo INSTALL FAILED! Is the phone connected with USB debugging?
    pause
    exit /b 1
)
echo.

echo [3/3] Launching app...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" shell am start -n com.ossadkowski.app/.MainActivity
echo.

echo ===================================
echo   Done! App is running on device.
echo ===================================
pause
