' Creates a Windows Desktop shortcut to Fleet Strike with custom icon

Set WshShell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

strDir = fso.GetParentFolderName(WScript.ScriptFullName)
strDesktop = WshShell.SpecialFolders("Desktop")
strShortcut = strDesktop & "\Fleet Strike.lnk"

Set oShortcut = WshShell.CreateShortcut(strShortcut)
oShortcut.TargetPath = "wscript.exe"
oShortcut.Arguments = """" & strDir & "\FleetStrike.vbs"""
oShortcut.WorkingDirectory = strDir
oShortcut.WindowStyle = 1
oShortcut.IconLocation = strDir & "\icon\fleetstrike.ico, 0"
oShortcut.Description = "Fleet Strike - Tactical Naval Warfare Simulation"
oShortcut.Save

MsgBox "Desktop shortcut created successfully on your Desktop!", vbInformation, "Fleet Strike"
