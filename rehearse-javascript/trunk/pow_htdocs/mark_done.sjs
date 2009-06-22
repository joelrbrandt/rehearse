<?sjs
	var uid = pow_server.POST['rehearse_uid'];
	var functionNum = pow_server.POST['function_num'];
	var trycount = 0;
	var context = getContextById(uid);
	while(context == null && trycount < 500) {
		context = getContextById(uid);
		trycount++;
	}
	if(context != null) {
		window.Firebug.CommandLine.evaluate("markDone(" + functionNum + ");", context);
		document.writeln("Marked done!");
	}
	document.writeln("Error: couldn't mark done");
	
	function getContextById(uid) {
		for(var i = 0; i < getBrowser().browsers.length; i++) {
			var b = getBrowser().browsers[i];
			var context = window.TabWatcher.getContextByWindow(b.contentWindow);
			if(context != null) {
				try {
					var result = window.Firebug.CommandLine.evaluate("_rehearse_uid", context);
					if (result == uid) {
						return context;
					}
				} catch (exc) {}
			}
		}
		return null;
	}
?>