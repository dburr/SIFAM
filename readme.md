# Abandoned
I am no longer working on SIFAM and have no plans to continue it. If someone wants to take over they can

# SIFAM
SchoolIdolFestival Account Manager (SIFAM) is a simple tool for android devices for managing multiple School Idol Festival accounts.

### Editing with Android Studio
1. Create new project selecting "Add No Activity" when prompted.
2. Merge SIFAM app directory into the new project.
3. Add dependancies to gradle
4. Run Application. (Most likely wont work in default SDK emulator)

### Dependancies
Add the following the the dependancies section of build.gradle (module: app)
```
compile 'com.journeyapps:zxing-android-embedded:3.1.0@aar'
compile 'com.google.zxing:core:3.2.0'
```
