; Inno Setup Script for Fleet Strike
#define MyAppName "Fleet Strike"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Dan"
#define MyAppURL "https://github.com/danfromboone/fleetstrike"
#define MyAppExeName "FleetStrike.vbs"

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
OutputBaseFilename=FleetStrike-{#MyAppVersion}-Setup
SetupIconFile=fleetstrike.ico
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: "..\..\build\windows\FleetStrike\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "wscript.exe"; Parameters: """{app}\FleetStrike.vbs"""; WorkingDir: "{app}"; IconFilename: "{app}\icon\fleetstrike.ico"
Name: "{autodesktop}\{#MyAppName}"; Filename: "wscript.exe"; Parameters: """{app}\FleetStrike.vbs"""; WorkingDir: "{app}"; IconFilename: "{app}\icon\fleetstrike.ico"; Tasks: desktopicon

[Run]
Filename: "wscript.exe"; Parameters: """{app}\FleetStrike.vbs"""; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
