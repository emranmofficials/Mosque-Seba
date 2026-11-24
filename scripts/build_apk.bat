@echo off
REM ==============================================================================
REM Mosque Seba (মসজিদ সেবা) — Automated Release APK Builder (Windows)
REM ==============================================================================

echo ==================================================
echo   Mosque Seba (মসজিদ সেবা) — Release APK Builder   
echo   Target: com.mosqueseba.app v1.2.5+10            
echo ==================================================

where flutter >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Flutter CLI was not found in your PATH.
    echo Please install Flutter SDK from https://flutter.dev/docs/get-started/install
    pause
    exit /b 1
)

echo [1/3] Fetching Flutter dependencies...
call flutter pub get

echo [2/3] Running Drift & Riverpod code generation...
call dart run build_runner build --delete-conflicting-outputs

echo [3/3] Compiling Release APK...
call flutter build apk --release

echo ==================================================
echo SUCCESS! Release APK built at:
echo build\app\outputs\flutter-apk\app-release.apk
echo ==================================================
pause
