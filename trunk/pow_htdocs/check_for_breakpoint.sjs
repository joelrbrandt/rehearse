<?sjs
	var found = false;
	for(var i = 0; i < getBrowser().browsers.length; i++) {
		var context =
			window.TabWatcher.getContextByWindow(getBrowser().browsers[i].contentWindow);
		if(context != null && context.stopped) {
			var result = window.Firebug.CommandLine.evaluate("_rehearse_uid", context);
			document.write(result);
			found = true;
			break;
		}
	}
	if(!found)
		document.write(-1);
?>