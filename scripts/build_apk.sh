#!/usr/bin/env bash
# ==============================================================================
# Mosque Seba (মসজিদ সেবা) — Automated Release APK Builder
# ==============================================================================

set -euo pipefail

echo "=================================================="
echo "  Mosque Seba (মসজিদ সেবা) — Release APK Builder   "
echo "  Target: com.mosqueseba.app v1.2.5+10            "
echo "=================================================="

# Check for Flutter SDK
if ! command -v flutter &> /dev/null; then
    echo "❌ Error: 'flutter' CLI is not installed or not in PATH."
    echo "💡 Please install Flutter SDK from https://flutter.dev/docs/get-started/install"
    exit 1
fi

echo "✅ Flutter found: $(flutter --version | head -n 1)"

# Step 1: Fetch dependencies
echo ""
echo "📦 Step 1: Running 'flutter pub get'..."
flutter pub get

# Step 2: Code Generation (Drift database & Riverpod providers)
echo ""
echo "⚙️ Step 2: Running code generation for Drift and Riverpod..."
dart run build_runner build --delete-conflicting-outputs

# Step 3: Build Release APK
echo ""
echo "🚀 Step 3: Building Android Release APK..."
flutter build apk --release

echo ""
echo "=================================================="
echo "🎉 SUCCESS! Mosque Seba Release APK has been built:"
echo "👉 build/app/outputs/flutter-apk/app-release.apk"
echo "=================================================="
