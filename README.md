# PhotoEditPro — GitHub Actions APK Build

## Phone-only build

1. Create a GitHub repository.
2. Upload the contents of this ZIP so that `PhotoEditPro/settings.gradle` exists.
3. Upload `.github/workflows/build-apk.yml` from this package.
4. Open **Actions → Build PhotoEditPro APK → Run workflow**.
5. When the workflow finishes, open the run and download the **PhotoEditPro-debug-apk** artifact.
6. Extract the artifact ZIP and install the `.apk` on your Android phone.

Note: GitHub Actions builds the project in the cloud; the APK is not produced by the phone itself.
