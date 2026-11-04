; Inno Setup Script for Battleship
#define MyAppName "Battleship"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Dan"
#define MyAppURL "https://github.com/danfromboone/battleship"
#define MyAppExeName "Battleship.vbs"

[Setup]
AppId={{E6F7D8A2-3B4C-5D6E-7F8A-9B0C1D2E3F4A}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DisableProgramGroupPage=yes
OutputDir=..\..\build\distributions
OutputBaseFilename=Battleship-{#MyAppVersion}-Setup
SetupIconFile=battleship.ico
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: "..\..\build\windows\Battleship\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "wscript.exe"; Parameters: """{app}\Battleship.vbs"""; WorkingDir: "{app}"; IconFilename: "{app}\icon\battleship.ico"
Name: "{autodesktop}\{#MyAppName}"; Filename: "wscript.exe"; Parameters: """{app}\Battleship.vbs"""; WorkingDir: "{app}"; IconFilename: "{app}\icon\battleship.ico"; Tasks: desktopicon

[Run]
Filename: "wscript.exe"; Parameters: """{app}\Battleship.vbs"""; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
