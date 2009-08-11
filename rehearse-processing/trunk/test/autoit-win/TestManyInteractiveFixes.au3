#include <Array.au3>
#include <String.au3>

; start processing, paste broken text, run interactively, fix text, run again, close without saving
Local $brokenArray[1]
Local $fixedArray[1]
;read script File 
$fscript = FileOpen("script.txt",0);
If $fscript = -1 Then
    MsgBox(0, "Error", "Unable to open file.")
    Exit
EndIf
; Read in lines of text until the EOF is reached
While 1
    $line = FileReadLine($fscript)
    If @error = -1 Then ExitLoop
    $stripped = StringStripWS($line,1)
	If StringCompare(StringLeft($stripped,1),"#") <> 0  Then
		$array = _StringExplode($stripped,",")
		_ArrayAdd($brokenArray,$array[0])
		_ArrayAdd($fixedArray,$array[1])
	EndIf	
Wend

;get rid of our placeholders
_ArrayDelete($brokenArray,0)
_ArrayDelete($fixedArray,0)

_ArrayDisplay($brokenArray,"These are the broken filenames we read")
_ArrayDisplay($fixedArray,"These are the fied filenames we read")
For $i = 1 To _ArrayMaxIndex($brokenArray) Step 1
	;run the whole shebang in here
Next