#include <ScreenCapture.au3>
#include <Array.au3>
#include <String.au3>

; start processing, paste broken text, run interactively, fix text, run again, close without saving
Func RunInteractiveFix( $brokenFilename,  $fixedFilename) 
	;read broken code into VarGetType
	$fbroken = FileOpen($brokenFilename, 0)
	; Check if file opened for reading OK
	If $fbroken = -1 Then
		MsgBox(0, "Error", "Unable to open file.")
		Exit
	EndIf
	$broken = FileRead($fbroken)
	FileClose($fbroken)

	;read fixed code into VarGetType
	$ffixed = FileOpen($fixedFilename, 0)
	; Check if file opened for reading OK
	If $ffixed = -1 Then
		MsgBox(0, "Error", "Unable to open file.")
		Exit
	EndIf
	$fixed = FileRead($ffixed)
	FileClose($ffixed)



	AutoItSetOption("MouseCoordMode",0) ;use mouse coords relative to window
	AutoItSetOption("WinTitleMatchMode",2) ;match any substring in the title when looking for window titles


	;launch app using run.sh script
	Run("c:\cygwin\bin\bash.exe -c 'cd ~/build/rehearse/build/windows;./run.sh'")
	WinWaitActive("Processing 0167") ;wait until it is active

	;insert code
	;Send("{ENTER}int x;{ENTER}x++{ENTER}") ;paste our file in
	Send($broken,1) ;send raw

	;show helpmeout window
	MouseClick("left",132,34) ;click on menu tools
	MouseClick("left",223,168) ;show helpmeout window

	;Make sure Processing is active again
	WinActivate("Processing 0167")
	MouseClick("left",22,60) ; click interactive run

	Sleep(5000); sleep for 5 seconds

	;insert fixed code
	WinActivate("Processing 0167")
	Send("^a");select everything
	Send($fixed,1) ;send raw
	MouseClick("left",22,60) ; click interactive run

	Sleep(5000); sleep for 5 seconds

	; Capture full screen
	_ScreenCapture_Capture(@MyDocumentsDir & "\GDIPlus_Image1.jpg")

	;Make sure Processing is active again
	WinActivate("Processing 0167")
	;send Ctrl+q to close
	Send("^q")
	;wait for save dialog to pop up
	WinWaitActive("Close")
	;send Alt+n to dismiss save dialog
	Send("!n")
EndFunc