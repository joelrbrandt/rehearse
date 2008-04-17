<?sjs
	var uid = pow_server.POST['rehearse_uid'];
	
	//var currWindow = getCurrentWindow(uid);
	var currWindow = getBrowser().browsers[0].contentWindow.window;
	
	if(currWindow) {
		var url = currWindow.document.URL;
		url = url.substring(0, url.lastIndexOf('/'));
		url += "/rehearse_client.js";
		document.writeln(url);
		window.Firebug.Debugger.setBreakpoint(url, 30);
	}
	
	//doesnt work right now
	function getCurrentWindow(uid) {
		for(var i = 0; i < getBrowser().browsers.length; i++) {
			var b = getBrowser().browsers[i];
			if (b.contentWindow) {
				//document.writeln(b.contentWindow.window.document.URL);
				//document.writeln(b.contentWindow.window.pow_server_loc);
				if (b.contentWindow.window._rehearse_uid == uid) {
					return b.contentWindow.window;
				}
			}
		}
		return null;
	}

?>