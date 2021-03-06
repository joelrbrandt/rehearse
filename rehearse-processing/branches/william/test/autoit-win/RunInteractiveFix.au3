#include <ScreenCapture.au3>
#include <Array.au3>
#include <String.au3>

; start processing, paste broken text, run interactively, fix text, run again, close without saving
; if second parameter is not supplied, only query and snapshot, don't fix
Func RunInteractiveFix( $brokenFilename,  $fixedFilename="") 
	;read broken code into VarGetType
	$fbroken = FileOpen($brokenFilename, 0)
	; Check if file opened for reading OK
	If $fbroken = -1 Then
		MsgBox(0, "Error", "Unable to open file.")
		Exit
	EndIf
	$broken = FileRead($fbroken)
	FileClose($fbroken)

	If $fixedFilename <> "" Then
		;read fixed code into VarGetType
		$ffixed = FileOpen($fixedFilename, 0)
		; Check if file opened for reading OK
		If $ffixed = -1 Then
			MsgBox(0, "Error", "Unable to open file.")
			Exit
		EndIf
		$fixed = FileRead($ffixed)
		FileClose($ffixed)
	EndIf


	AutoItSetOption("MouseCoordMode",0) ;use mouse coords relative to window
	AutoItSetOption("WinTitleMatchMode",2) ;match any substring in the title when looking for window titles


	;launch app using run.sh script
	Run("c:\cygwin\bin\bash.exe -c 'cd ~/build/rehearse/build/windows;./run.sh >log.txt'")
	WinWaitActive("Processing 0167") ;wait until it is active

	;insert code
	Sleep(100);
	ClipPut($broken);
	Send("^v");
	;Send($broken,1) ;send raw

	;show helpmeout window
	MouseClick("left",132,34) ;click on menu tools
	MouseClick("left",223,168) ;show helpmeout window

	;Make sure Processing is active again
	WinActivate("Processing 0167")
	MouseClick("left",22,60) ; click interactive run

	Sleep(5000); sleep for 5 seconds


	If $fixedFilename <> "" Then 
		;insert fixed code
		WinActivate("Processing 0167")
		Sleep(100);
		Send("^a");select everything
		Sleep(500);
		;Send($fixed,1) ;send raw
		ClipPut($fixed);
		Send("^v");
		MouseClick("left",22,60) ; click interactive run

		Sleep(5000); sleep for 5 seconds
	Else
		; Capture full screen
		_ScreenCapture_Capture($brokenFilename & ".jpg")		
	EndIf
	

	;Make sure Processing is active again
	WinActivate("Processing 0167")
	;send Ctrl+q to close
	Send("^q")
	;wait for save dialog to pop up
	WinWaitActive("Close")
	;send Alt+n to dismiss save dialog
	Send("!n")
EndFunc