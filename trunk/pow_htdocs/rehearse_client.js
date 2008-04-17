addLoadEvent(setupPOW);

var pow_server_loc = 'http://localhost:6670/rehearse/rehearse_setup.sjs?AJAX=true';

function setupPOW() {
	var url = pow_server_loc;
	window._rehearse_uid = Math.ceil(10000*Math.random());
	
	$.post(url, {rehearse_uid : window._rehearse_uid});
}

function addLoadEvent(func) {
    var oldonload = window.onload;
    if (typeof window.onload != 'function') {
        window.onload = func;
    }
    else {
        window.onload = function(){
            oldonload();
            func();
        }
    }
}

function $I(functionName, parameters) {
	if (defined_functions[functionName]) {
		// todo: call with the right params
	} else {
		var _interactive_now = true;
		var _interactive_now = false;
	}
}



