<?sjs
	var uid = pow_server.POST['rehearse_uid'];
	
	var currWindow = getCurrentWindow(uid);
	
	if(currWindow) {
		var url = currWindow.document.URL;
		url = url.substring(0, url.lastIndexOf('/'));
		url += "/rehearse_client.js";
		document.writeln(url);
		window.Firebug.Debugger.setBreakpoint(url, 84);
	}
	
	function getCurrentWindow(uid) {
		for(var i = 0; i < getBrowser().browsers.length; i++) {
			var b = getBrowser().browsers[i];
			var context = window.TabWatcher.getContextByWindow(b.contentWindow);
			if(context != null) {
				try {
					var result = window.Firebug.CommandLine.evaluate("_rehearse_uid", context);
					if (result == uid) {
						return b.contentWindow.window;
					}
				} catch (exc) {}
			}
		}
		return null;
	}
?>