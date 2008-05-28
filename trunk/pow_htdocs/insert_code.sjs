<?sjs
	var code = pow_server.POST['code'];
	var uid = pow_server.POST['rehearse_uid'];
	
	var trycount = 0;
	var context = getContextById(uid);
	while(context == null && trycount < 1500) {
		context = getContextById(uid);
		trycount++;
	}
	if (context != null) {
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
