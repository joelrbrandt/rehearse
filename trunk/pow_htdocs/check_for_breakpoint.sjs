<?sjs
	var contentWindow = getBrowser().browsers[0].contentWindow;
	var context = window.TabWatcher.getContextByWindow(contentWindow);
	if(context.stopped)
		document.write(1);
	else
		document.write(2);
?>