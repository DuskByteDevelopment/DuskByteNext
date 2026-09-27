@echo off
title DuskByte ZKM Obfuscation
color 0A

echo ============================================================
echo   DuskByte ZKM Obfuscation Script
echo   Zelix KlassMaster 27.0.0
echo ============================================================
echo.

set ZKM_JAR=%~dp0zkm\zkm27.jar
set BUILD_DIR=%~dp0build\libs
set TEMP_JAR=%BUILD_DIR%\duskbyte_obf.jar

if not exist "%ZKM_JAR%" (
    echo [ERROR] ZKM not found: %ZKM_JAR%
    echo Please place zkm27.jar in zkm\ folder
    pause
    exit /b 1
)

if not exist "%BUILD_DIR%" (
    echo [ERROR] build\libs not found
    pause
    exit /b 1
)

set INPUT_JAR=
for %%F in ("%BUILD_DIR%\*.jar") do (
    if /I not "%%~nxF"=="sources.jar" if /I not "%%~nxF"=="duskbyte_obf.jar" (
        set INPUT_JAR=%%F
    )
)

if "%INPUT_JAR%"=="" (
    echo [ERROR] No JAR found in build\libs\
    pause
    exit /b 1
)

echo Found: %INPUT_JAR%
echo.

echo [1/4] Getting classpath from Gradle...
cd /d "%~dp0"
set "CLASSPATH_LINE="
for /f "delims=" %%C in ('call gradlew.bat printClasspath 2^>nul') do set "CLASSPATH_LINE=%%C"

if "%CLASSPATH_LINE%"=="" (
    echo [ERROR] Failed to get classpath
    pause
    exit /b 1
)
echo       OK
echo.

echo [2/4] Generating ZKM script...
> "%TEMP%\zkm_obf.pro" (
    echo -injars build\libs\duskbyte_obf.jar
    echo -outjars build\libs\duskbyte_obf.jar
    echo -libraryjars ^<java.home^>/lib/jrt-fs.jar
)

for %%J in (%CLASSPATH_LINE:;=%) do (
    if exist "%%~J" (
        >> "%TEMP%\zkm_obf.pro" echo -libraryjars %%~J
    )
)

>> "%TEMP%\zkm_obf.pro" (
    echo -dontshrink
    echo -dontoptimize
    echo -keep public class dev.duskbyte.Main { *; }
    echo -keep class dev.duskbyte.mixin.** { *; }
    echo -keep class dev.duskbyte.imixin.** { *; }
    echo -keep class dev.duskbyte.DuskByte { public *; protected *; public static *; }
    echo -keep class dev.duskbyte.event.** { *; }
    echo -keep class dev.duskbyte.module.modules.** { public ^<init^>(); }
    echo -keep class dev.duskbyte.gui.** { public ^<init^>(...); }
    echo -keep class dev.duskbyte.managers.cloud.** { *; }
    echo -keep class dev.duskbyte.managers.AuthManager { *; }
    echo -keep class dev.duskbyte.managers.TranslationManager { *; }
    echo -keep class net.fabricmc.** { *; }
    echo -keep class com.google.gson.** { *; }
    echo -optimizationpasses 3
    echo -repackageclasses
    echo -allowaccessmodification
    echo -renamesourcefileattribute SourceFile
    echo -printmapping build\libs\mapping.txt
)
echo       OK
echo.

echo [3/4] Preparing JAR...
copy "%INPUT_JAR%" "%TEMP_JAR%" >nul
copy "%INPUT_JAR%" "%INPUT_JAR%.bak" >nul

echo [4/4] Running ZKM (MAX obfuscation)...
echo       Please wait...
echo.

java -Xmx2G -jar "%ZKM_JAR%" "%TEMP%\zkm_obf.pro"

if errorlevel 1 (
    echo.
    echo [ERROR] ZKM failed! Restoring...
    copy "%INPUT_JAR%.bak" "%INPUT_JAR%" >nul
    del "%TEMP_JAR%" >nul 2>nul
    pause
    exit /b 1
)

copy "%TEMP_JAR%" "%INPUT_JAR%" >nul
del "%TEMP_JAR%" >nul 2>nul

echo.
echo ============================================================
echo   DONE!
echo ============================================================
echo.

for %%A in ("%INPUT_JAR%.bak") do echo   Original:   %%~zA bytes
for %%A in ("%INPUT_JAR%") do echo   Obfuscated: %%~zA bytes
echo   Mapping:    build\libs\mapping.txt
echo.

pause
