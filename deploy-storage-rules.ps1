# PowerShell Script to Help Deploy Firebase Storage Rules
# This script will open Firebase Console in your browser

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Firebase Storage Rules Deployment Helper" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Opening Firebase Console in your browser..." -ForegroundColor Yellow
Write-Host ""

# Open Firebase Console Storage Rules page
Start-Process "https://console.firebase.google.com/project/my-bank-e08a3/storage/rules"

Write-Host "✅ Browser opened!" -ForegroundColor Green
Write-Host ""
Write-Host "INSTRUCTIONS:" -ForegroundColor Yellow
Write-Host "1. You should see the Firebase Console in your browser" -ForegroundColor White
Write-Host "2. Go to the 'Rules' tab (should already be selected)" -ForegroundColor White
Write-Host "3. Delete ALL existing rules" -ForegroundColor White
Write-Host "4. Copy the rules from storage.rules file in this project" -ForegroundColor White
Write-Host "5. Paste into the Firebase Console editor" -ForegroundColor White
Write-Host "6. Click the 'Publish' button" -ForegroundColor White
Write-Host "7. Wait 5-10 seconds for deployment to complete" -ForegroundColor White
Write-Host ""
Write-Host "Want to see the rules now? (Y/N): " -ForegroundColor Cyan -NoNewline

$response = Read-Host

if ($response -eq "Y" -or $response -eq "y") {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "Copy these rules to Firebase Console:" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    
    $rules = @"
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {
    
    // Allow anyone to upload customer profile images during registration
    match /customer_profile_images/{imageId} {
      allow read: if true;
      allow write: if request.resource.size < 1 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
    
    // Allow authenticated users to upload/read their own profile images
    match /profile_images/{userId}/{imageId} {
      allow read: if true;
      allow write: if request.auth != null 
                   && request.auth.uid == userId
                   && request.resource.size < 1 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
    
    // Admin profile images
    match /admin_profile_images/{imageId} {
      allow read: if true;
      allow write: if request.resource.size < 1 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
    
    // Staff profile images
    match /staff_profile_images/{imageId} {
      allow read: if true;
      allow write: if request.resource.size < 1 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
    
    // Default: deny all other access
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
"@
    
    Write-Host $rules -ForegroundColor White
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Rules copied to clipboard? Opening clipboard copy helper..." -ForegroundColor Yellow
    
    # Copy to clipboard
    $rules | Set-Clipboard
    Write-Host "✅ Rules copied to clipboard!" -ForegroundColor Green
    Write-Host "Now just paste (Ctrl+V) in Firebase Console!" -ForegroundColor Green
}

Write-Host ""
Write-Host "After deploying rules, press any key to test the app..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host ""
Write-Host "Testing APK location..." -ForegroundColor Yellow
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"

if (Test-Path $apkPath) {
    Write-Host "✅ APK found: $apkPath" -ForegroundColor Green
    Write-Host ""
    Write-Host "Install with: adb install -r `"$apkPath`"" -ForegroundColor Cyan
} else {
    Write-Host "❌ APK not found. Run: .\gradlew assembleDebug" -ForegroundColor Red
}

Write-Host ""
Write-Host "Done! Your app should now upload images successfully! 🎉" -ForegroundColor Green
Write-Host ""
