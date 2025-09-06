@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ==============================================
REM mavenPublish.bat  —  Gradle publish helper
REM Modes:
REM   local   -> publishToMavenLocal
REM   central -> publishAllPublicationsToMavenCentralRepository
REM If no mode is given, defaults to "local".
REM ==============================================

pushd "%~dp0"

if not exist "gradlew.bat" (
  echo [ERROR] gradlew.bat not found. Run from the project root.
  popd & endlocal & exit /b 1
)

REM Disable iOS on non-mac builds by default; override with -PenableIos=true
set "DEFAULT_PROPS=-PenableIos=false"

set "MODE=%~1"
if "%MODE%"=="" set "MODE=local"

if /I "%MODE%"=="local" (
  shift
  echo [INFO] Publishing to mavenLocal...
  call .\gradlew.bat clean publishToMavenLocal %DEFAULT_PROPS% %*
  set ERR=%ERRORLEVEL%
  popd & endlocal & exit /b %ERR%
)

if /I "%MODE%"=="central" (
  shift
  echo [INFO] Publishing to Maven Central
  echo [HINT] Ensure these are set (env or gradle.properties):
  echo        ORG_GRADLE_PROJECT_mavenCentralUsername
  echo        ORG_GRADLE_PROJECT_mavenCentralPassword
  echo        ORG_GRADLE_PROJECT_signingInMemoryKey
  echo        ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
  call .\gradlew.bat clean publishAllPublicationsToMavenCentralRepository %DEFAULT_PROPS% %*
  set ERR=%ERRORLEVEL%
  popd & endlocal & exit /b %ERR%
)

echo Usage:
echo   %~nx0 local   [extra-gradle-args]
echo   %~nx0 central [extra-gradle-args]
echo Examples:
echo   %~nx0 local -PlocalDev
echo   %~nx0 central -x test
popd
endlocal
exit /b 1
