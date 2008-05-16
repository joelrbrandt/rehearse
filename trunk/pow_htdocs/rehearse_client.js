addLoadEvent(setupPOW);

var pow_server_loc = 'http://localhost:6670/rehearse/rehearse_setup.sjs?AJAX=true';
var defined_functions = {};
var _interactive_now;

var rehearse_helpers = new Array(10);

for(var i = 0; i < rehearse_helpers.length; i++) {
	rehearse_helpers[i] = new Object();
	rehearse_helpers[i].functionNum = i;
	rehearse_helpers[i].inUse = false;
	rehearse_helpers[i].finishedEditing = false;
	rehearse_helpers[i].commandQueue = new Array();
	rehearse_helpers[i].func = function(functionNum, functionName, parameters) {
		var last_var;
		while(true) {
			if(rehearse_helpers[functionNum].commandQueue.length > 0) {
				eval(rehearse_helpers[functionNum].commandQueue.shift());
			} else if(rehearse_helpers[functionNum].finishedEditing) {
				return last_var;
			} else {
				1+1; //breakpoint here
			}
		}
	}
}

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

/*
 * Saves the defined function to be accessed later.
 * Parameters are given as comma-separated list of parameter names.
 */
function addDefinedFunction(name, parameters) {
	var newf = new Object();
	newf["name"] = name;
	temp = parameters.split(",");
	newf.params = new Array(temp.length);
	for(var i = 0; i < temp.length; i++) {
		newf.params[i] = temp[i];
	}
	defined_functions[name] = newf;
}

/*
 * Calls a defined function of the given name.
 * Parameters are given as name-value pairs. 
 * The bulk of work here is figuring out the right ordering of variables.
 */
function callDefinedFunction(name, parameters) {
	var f = defined_functions[name];
	if(f==null)	return;
	var temp = new Array(f.params.length);
	for(var pname in parameters) {
		for(var i = 0; i < f.params.length; i++) {
			if(pname == f.params[i]) {
				temp[i] = parameters[pname];
				break;
			}
		}
	}
	var callStr = name + "(";
	for(var i = 0; i < temp.length; i++) {
		callStr += "temp[" + i + "]";
		if(i != temp.length-1)
			callStr += ",";
	}
	callStr += ");";
	eval(callStr);
}

function getRehearseHelper() {
	for(var i = 0; i < rehearse_helpers.length; i++) {
		if(!rehearse_helpers[i].inUse) {
			return i;
		}
	}
	alert("no more helpers!");
}

function $I(functionName, parameters) {
	console.log("$I Called!");
	if (defined_functions[functionName]) {
		callDefinedFunction(functionName, parameters);
	} else {
		var num = getRehearseHelper();
		var f = rehearse_helpers[num].func;
		return f(num, functionName, parameters);
	}
	/*
	else if (_interactive_now) {
		var closure = function() { // needed to keep the params around
     		$I(functionName, parameters);
   		}
   		setTimeout(closure, 500);
   		return;
	} else {
		for(param in parameters) {
			this[param] = parameters[param];
		}
		_interactive_now = true;
		_interactive_now = false;
	}*/
}