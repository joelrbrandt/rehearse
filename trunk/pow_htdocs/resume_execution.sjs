<?sjs
	var contentWindow = getBrowser().browsers[0].contentWindow;
	var context = window.TabWatcher.getContextByWindow(contentWindow);
	window.Firebug.Debugger.resume(context);
	document.write(1);
?>