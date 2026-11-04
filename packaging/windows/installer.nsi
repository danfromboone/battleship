; NSIS Modern User Interface Installer Script for Battleship

!include "MUI2.nsh"

Name "Battleship"
OutFile "..\..\build\distributions\Battleship-1.0.0-Setup.exe"
InstallDir "$PROGRAMFILES64\Battleship"
RequestExecutionLevel admin

!define MUI_ICON "battleship.ico"
!define MUI_UNICON "battleship.ico"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

Section "Battleship Core" SecCore
    SetOutPath "$INSTDIR"
    File /r "..\..\build\windows\Battleship\*.*"
    
    CreateDirectory "$SMPROGRAMS\Battleship"
    CreateShortcut "$SMPROGRAMS\Battleship\Battleship.lnk" "$WINDIR\System32\wscript.exe" '"$INSTDIR\Battleship.vbs"' "$INSTDIR\icon\battleship.ico"
    CreateShortcut "$DESKTOP\Battleship.lnk" "$WINDIR\System32\wscript.exe" '"$INSTDIR\Battleship.vbs"' "$INSTDIR\icon\battleship.ico"
    
    WriteUninstaller "$INSTDIR\uninstall.exe"
SectionEnd

Section "Uninstall"
    RMDir /r "$INSTDIR"
    Delete "$SMPROGRAMS\Battleship\Battleship.lnk"
    RMDir "$SMPROGRAMS\Battleship"
    Delete "$DESKTOP\Battleship.lnk"
SectionEnd
