<?sjs
	var command = pow_server.POST['command'];
	var uid = pow_server.POST['rehearse_uid'];
	var found = false;
	var context;
	
	if (command != null) {
		for(var i = 0; i < getBrowser().browsers.length; i++) {
			context =
				window.TabWatcher.getContextByWindow(getBrowser().browsers[i].contentWindow);
			if (context != null) {
				try {
					var result = window.Firebug.CommandLine.evaluate("_rehearse_uid", context);
					if (result == uid) {
						found = true;
						break;
					}
				} catch (exc) {}	
			}
		}
		if (found) {
			var errorCode = 1;
			var result;
			var snapshot_id = window.Firebug.CommandLine.evaluate("snapshot();", context);
			document.writeln(snapshot_id);
			try {
				result = window.Firebug.CommandLine.evaluate(command, context);
			} catch (exc) { errorCode = 2; result = exc;}
			document.writeln(errorCode);
			document.writeln(result);
			
		} else {
			document.write("The specified uid wasn't found!");
		}
	} else {
	    document.write("<p>Enter a command:<br/>");
	    document.write("<form method='post'><input name='command' type='text' size='50'/><form></p>");
	}

?>
