<?sjs
var command = pow_server.POST['command'];
if (command != null) {
    var context = window.TabWatcher.getContextByWindow(getBrowser().browsers[0].contentWindow);
    var result = window.Firebug.CommandLine.evaluate(command, context);
    document.write(result);
} else {
    document.write("<p>Enter a command:<br/>");
    document.write("<form method='post'><input name='command' type='text' size='50'/><form></p>");
}


?>
