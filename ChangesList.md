# Introduction #

This looks to be the last release version. Our initial list of changes was compiled for [revision #6011](https://code.google.com/p/rehearse/source/detail?r=#6011), so this is a new list of changes for [revision #5766](https://code.google.com/p/rehearse/source/detail?r=#5766)


# Details #

## Changelist for rev #6011 ##


---

Reorganized:

1. Made changes to processing.app.Editor.java so that we can add a custom textArea and a toolbar. This is because our RehearseToolbar extends processing.app.EditorToolbar, and RehearseTextArea extends processing.app.syntax.JEditTextArea.
> Added public buildToolbar() function. This returns a custom toolbar if it exists, or just returns an EditorToolbar otherwise.
> Added public buildTextArea() function that returns a custom text area if it exists, or just the default TextArea otherwise.
> Added private instance variables customToolbar and customToolbarTool.
> Added private instance variables customTextArea and customTextAreaTool.
> All four are initialized to null.
> Created methods setCustomToolbar and setCustomTextArea. Added a BuildCustomException.

2. Changed the visibility of a few variables in processing.app.EditorToolbar.java to protected so we can access it from RehearseToolbar.
> Changed visibility of title and BUTTON\_COUNT to protected, removed final modifier
> Changed visibility of BUTTON\_GAP, BUTTON\_WIDTH and BUTTON\_HEIGHT to protected
> Changed visibility of inactive, active and rollover to protected
> Changed visibility of RUN, STOP, NEW, OPEN, SAVE, EXPORT
> Changed visibility of currentRollover, buttons, buttonImages, popup, menu, buttonCount, which, state, width, height, offScreen, and stateImage to protected, used by RehearseToolbar.
> Changed visibility of x1[.md](.md), x2[.md](.md), y1[.md](.md) and y2[.md](.md) to protected
> Changed visibility of findSelection() and setState() to protected
> Lines 2372-2374 -  Added a public getBase method.
> Lines 379-381 -  added public getToolbarMenu method. Used to construct RehearseToolbar in the RehearseTool class.

3. Made a change in processing.app.syntax.TextAreaPainter.java to enable print points.
> Lines 761-763 - added getFontHeight method used by RehearseSidebarPainter

4. Made changes to processing.app.Editor.java for RehearseHandler (which was earlier called RehearseEditor)
> Line 1478 - changed visibility of setCode() to public, to be used in RehearseHandler
> Lines 2509-2511 - Added getConsole method

5. Made changes to processing.app.Editor.java so users can switch between custom and default toolbars/textarea.
> Created rebuildToolbarTextArea, called when the user changes Rehearse preferences.
> Made "upper" a global variable so we can add/remove the toolbar from it.


---


[1](1.md) processing.app.Editor.java - Lines 136-139
> Added private instance variables customToolbar and customToolbarTool.
> Added private instance variables customTextArea and customTextAreaTool.
> All four are initialized to null.
> Made "upper" a global variable so we can add/remove the toolbar from it.

[2](2.md) processing.app.Editor.java - Line 204, 210 & 364-372
> Added public buildToolbar() function. This returns a custom toolbar if it exists, or just returns an EditorToolbar otherwise.  Added public buildTextArea() function that returns a custom text area if it exists, or just the default TextArea otherwise.


[3](3.md) processing.app.Editor.java - Lines 814-816
> Added code to load the Rehearse tool more easily for development inside Eclipse
> This does NOT need to go in the deployed version

[4](4.md) processing.app.Editor.java - Line 1478
> Changed visibility of setCode() to public, to be used in RehearseHandler

[5](5.md) processing.app.Editor.java - Lines 379-381
> added public getToolbarMenu method.

[6](6.md) processing.app.Editor.java - Lines 2372-2374
> added public getBase method.

[7](7.md) processing.app.Editor.java - Lines 2509-2511
> Added getConsole method to be used in RehearseHandler

[8](8.md) processing.app.Editor.java - Lines 2403-2406
> Created methods setCustomToolbar, setCustomTextArea and rebuildToolbarTextArea. Added a BuildCustomException.

[9](9.md) processing.app.EditorToolbar.java - Lines 36-70
> Changed visibility of title and BUTTON\_COUNT to protected, removed final modifier
> Changed visibility of BUTTON\_GAP, BUTTON\_WIDTH and BUTTON\_HEIGHT to protected
> Changed visibility of inactive, active and rollover to protected
> Changed visibility of RUN, STOP, NEW, OPEN, SAVE, EXPORT
> Changed visibility of currentRollover, buttons, buttonImages, popup, menu, buttonCount, which, state, width, height, offScreen, and stateImage to protected, used by RehearseToolbar.
> Changed visibility of x1[.md](.md), x2[.md](.md), y1[.md](.md) and y2[.md](.md) to protected
> Changed visibility of findSelection() and setState() to protected

[10](10.md) processing.app.syntax.TokenMarker.java - Lines 341-348
> added methods getLineModelAt and setLineModelAt

[11](11.md) processing.app.syntax.TextAreaPainter.java - Lines 761-763
> added getFontHeight method used by RehearseSidebarPainter

## Things that must be kept in sync ##

RehearsePApplet overridden methods need to be kept in sync with PApplet methods that
are supposed to get overrridden.


---


## Changelist for rev #5766 ##

[1](1.md) processing.app.Editor.java - Lines 136-139
> Added private instance variables customToolbar and customToolbarTool.
> Added private instance variables customTextArea and customTextAreaTool.
> All four are initialized to null.
> Made "upper" a global variable so we can add/remove the toolbar from it.

[2](2.md) processing.app.Editor.java - Lines 197, 204 & 354-362
> Added public buildToolbar() function. This lets us build a custom toolbar if it exists, or just an EditorToolbar otherwise.

[3](3.md) processing.app.Editor.java - Line 1428
> Changed visibility of setCode(), to be used in RehearseHandler

[4](4.md) processing.app.Editor.java - Lines 2396-2398
> added getToolbarMenu method.

[5](5.md) processing.app.Editor.java - Lines 2398-2402
> added getBase method.

[6](6.md) processing.app.Editor.java - Lines 2403-2406
> Added getConsole() method to be used in RehearseHandler

[7](7.md) processing.app.EditorToolbar.java - Lines 36-70
> Changed visibility of title and BUTTON\_COUNT to protected, removed final modifier
> Changed visibility of BUTTON\_GAP, BUTTON\_WIDTH and BUTTON\_HEIGHT to protected
> Changed visibility of inactive, active and rollover to protected
> Changed visibility of RUN, STOP, NEW, OPEN, SAVE, EXPORT
> Changed visibility of buttons, currentRollover, buttonImages, popup, menu, buttonCount, which, state, width, height, offScreen, and stateImage to protected, used by RehearseToolbar.
> Changed visibility of x1[.md](.md), x2[.md](.md), y1[.md](.md) and y2[.md](.md) to protected
> > Changed visibility of findSelection() and setState() to protected

[8](8.md) processing.app.Editor.java - Lines 2407-2415

> Created methods setCustomToolbar, setCustomTextArea and rebuildToolbar. Added a BuildCustomException.

[9](9.md) processing.app.syntax.TokenMarker.java - Lines 341-348
> added methods getLineModelAt and setLineModelAt

[10](10.md) processing.app.syntax.TextAreaPainter.java - Lines 761-763
> added getFontHeight method used by RehearseSidebarPainter