<?sjs
	var uid = pow_server.POST['rehearse_uid'];
	var sid = pow_server.POST['snapshot_id'];

	var found = false;
	var context;

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
		var command = "load(" + sid + ");";
		window.Firebug.CommandLine.evaluate(command, context);
	} else {
		document.write("The specified uid wasn't found!");
	}

?>
