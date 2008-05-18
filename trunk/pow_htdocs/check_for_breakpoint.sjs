<?sjs
	var found = false;
	for(var i = 0; i < getBrowser().browsers.length; i++) {
		var context =
			window.TabWatcher.getContextByWindow(getBrowser().browsers[i].contentWindow);
		if(context != null && context.stopped) {
			var result = window.Firebug.CommandLine.evaluate("_rehearse_uid", context);
			var functionNum = window.Firebug.CommandLine.evaluate("functionNum", context);
			var functionName = window.Firebug.CommandLine.evaluate("functionName", context);
			var parameters = window.Firebug.CommandLine.evaluate("ry='';for(rx in parameters) ry = ry + rx + '=' + parameters[rx] + ',';", context);
			if(parameters != null && parameters != "")
				parameters = parameters.substring(0, parameters.length - 1);
			var response = window.Firebug.CommandLine.evaluate("getResponseFromQueue(" + functionNum + ");", context);
			document.writeln(result);
			document.writeln(functionNum + "");
			document.writeln(functionName);
			document.writeln(parameters);
			if(response != null)
				document.write(response.sid + "," + response.type + "," + response.text);
			found = true;
			break;
		}
	}
	
	if(!found)
		document.writeln(-1);
?>