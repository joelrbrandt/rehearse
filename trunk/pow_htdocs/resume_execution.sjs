<?sjs
	var uid = pow_server.POST['rehearse_uid'];
	var trycount = 0;
	var context = getContextById(uid);
	while(context == null && trycount < 500) {
		context = getContextById(uid);
		trycount++;
	}
	window.Firebug.Debugger.resume(context);
	document.write(1);
	
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