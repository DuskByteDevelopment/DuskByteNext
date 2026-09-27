@echo off
title DuskByte ZKM Obfuscation
color 0A

echo ============================================================
echo   DuskByte ZKM Obfuscation Script
echo ============================================================
echo.

set ZKM_JAR=%~dp0zkm\zkm27.jar
set BUILD_DIR=%~dp0build\libs
set TEMP_JAR=%BUILD_DIR%\duskbyte_obf.jar

if not exist "%ZKM_JAR%" (
    echo [ERROR] ZKM not found: %ZKM_JAR%
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

echo [1/5] Collecting classpath from Gradle...
cd /d "%~dp0"
call gradlew.bat -q printClasspath > "%TEMP%\db_cp.txt" 2>nul
if not exist "%TEMP%\db_cp.txt" (
    echo [ERROR] printClasspath failed
    pause
    exit /b 1
)

echo [2/5] Stripping nested jars (fixes ZKM mixin crash)...
if exist "%INPUT_JAR%.bak" del "%INPUT_JAR%.bak">nul
copy "%INPUT_JAR%" "%INPUT_JAR%.bak" >nul
copy "%INPUT_JAR%" "%TEMP_JAR%" >nul
if exist "%~dp0zkm_nested" rmdir /s /q "%~dp0zkm_nested"
powershell -ExecutionPolicy Bypass -File "%~dp0zkm_strip_nested.ps1" -Jar "%TEMP_JAR%" -NestedDir "%~dp0zkm_nested"

echo [3/5] Generating ZKM script...
powershell -ExecutionPolicy Bypass -Command "$libs = Get-Content '%TEMP%\db_cp.txt' -Raw | %% { $_.Trim().Split(';') } | Where-Object { $_ -and (Test-Path $_) } | %% { ('-libraryjars \"{0}\"' -f $_.Replace('\','/')) }; $head = @('-injars \"build/libs/duskbyte_obf.jar\"','-outjars \"build/libs/duskbyte_obf.jar\"','-libraryjars \"<java.home>/lib/jrt-fs.jar\"'); $tail = (Get-Content '%~dp0obfuscate.pro' -Raw).Replace('\','/'); ($head + $libs + $tail) | Set-Content -Path '%TEMP%\zkm_obf.pro' -Encoding ascii"

echo [4/5] Running ZKM (MAX obfuscation)...
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

echo [5/5] Restoring nested jars...
powershell -ExecutionPolicy Bypass -File "%~dp0zkm_restore_nested.ps1" -Jar "%TEMP_JAR%" -NestedDir "%~dp0zkm_nested"
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
