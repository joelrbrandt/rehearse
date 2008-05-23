<?sjs
	var uid = pow_server.POST['rehearse_uid'];
	var functionNum = pow_server.POST['function_num'];
	var command = pow_server.POST['command'];
	var isUndo = pow_server.POST['is_undo'];
	
	var context = getContextById(uid);
	if(context != null && command != null) {
		command = command.replace(/"/g, "\\\"");
		var temp = "addCodeToQueue(" + functionNum + ", \"" + command + 
					"\", " + isUndo + ");";
		document.writeln(temp);
		window.Firebug.CommandLine.evaluate(temp, context);
	} else {
		document.write("Error! Command or uid not valid.");
	}
	
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