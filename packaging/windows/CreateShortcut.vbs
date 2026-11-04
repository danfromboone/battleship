' Creates a Windows Desktop shortcut to Battleship with custom icon

Set WshShell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

strDir = fso.GetParentFolderName(WScript.ScriptFullName)
strDesktop = WshShell.SpecialFolders("Desktop")
strShortcut = strDesktop & "\Battleship.lnk"

Set oShortcut = WshShell.CreateShortcut(strShortcut)
oShortcut.TargetPath = "wscript.exe"
oShortcut.Arguments = """" & strDir & "\Battleship.vbs"""
oShortcut.WorkingDirectory = strDir
oShortcut.WindowStyle = 1
oShortcut.IconLocation = strDir & "\icon\battleship.ico, 0"
oShortcut.Description = "Battleship - Classic Naval Tactical Simulation"
oShortcut.Save

MsgBox "Desktop shortcut created successfully on your Desktop!", vbInformation, "Battleship"
