<?sjs
	var code = pow_server.POST['code'];
	var uid = pow_server.POST['rehearse_uid'];
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
		var functionName = window.Firebug.CommandLine.evaluate("functionName", context);
		var parameters = window.Firebug.CommandLine.evaluate("_ry='';for(_rx in parameters) _ry = _ry + _rx + ',';", context);
		parameters = parameters.substring(0, parameters.length - 1);
		var functionStr = functionName + "= function(" + parameters + ") {" + code + "}";
		var result = window.Firebug.CommandLine.evaluate(functionStr, context);
		document.writeln(result);
		result = window.Firebug.CommandLine.evaluate("addDefinedFunction(\"" + functionName + "\",\"" + parameters + "\");", context);
		document.writeln(result);
	} else {
		document.write("The specified uid (" + uid + ") wasn't found!");
	}
?>
