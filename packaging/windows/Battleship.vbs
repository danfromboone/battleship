' Battleship Windows Silent Launcher
' Launches the Java Swing GUI without showing a command prompt window

Set WshShell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

strDir = fso.GetParentFolderName(WScript.ScriptFullName)
jarPath = strDir & "\lib\battleship-1.0.0.jar"

If Not fso.FileExists(jarPath) Then
    MsgBox "Application JAR not found at: " & vbCrLf & jarPath, vbCritical, "Battleship Error"
    WScript.Quit 1
End If

On Error Resume Next
ret = WshShell.Run("javaw -jar """ & jarPath & """", 0, False)
If Err.Number <> 0 Then
    ' Fallback to java command if javaw failed
    Err.Clear
    ret = WshShell.Run("java -jar """ & jarPath & """", 0, False)
    If Err.Number <> 0 Then
        MsgBox "Java 21 or newer is required to run Battleship." & vbCrLf & vbCrLf & _
               "Please download and install Java from:" & vbCrLf & _
               "https://adoptium.net", vbCritical, "Battleship - Java Required"
        WScript.Quit 1
    End If
End If
