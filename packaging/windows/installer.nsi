; NSIS Modern User Interface Installer Script for Fleet Strike

!include "MUI2.nsh"

Name "Fleet Strike"
OutFile "..\..\build\distributions\FleetStrike-1.0.0-Setup.exe"
InstallDir "$PROGRAMFILES64\FleetStrike"
RequestExecutionLevel admin

!define MUI_ICON "fleetstrike.ico"
!define MUI_UNICON "fleetstrike.ico"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

Section "Fleet Strike Core" SecCore
    SetOutPath "$INSTDIR"
    File /r "..\..\build\windows\FleetStrike\*.*"
    
    CreateDirectory "$SMPROGRAMS\Fleet Strike"
    CreateShortcut "$SMPROGRAMS\Fleet Strike\Fleet Strike.lnk" "$WINDIR\System32\wscript.exe" '"$INSTDIR\FleetStrike.vbs"' "$INSTDIR\icon\fleetstrike.ico"
    CreateShortcut "$DESKTOP\Fleet Strike.lnk" "$WINDIR\System32\wscript.exe" '"$INSTDIR\FleetStrike.vbs"' "$INSTDIR\icon\fleetstrike.ico"
    
    WriteUninstaller "$INSTDIR\uninstall.exe"
SectionEnd

Section "Uninstall"
    RMDir /r "$INSTDIR"
    Delete "$SMPROGRAMS\Fleet Strike\Fleet Strike.lnk"
    Delete "$DESKTOP\Fleet Strike.lnk"
    RMDir "$SMPROGRAMS\Fleet Strike"
SectionEnd
