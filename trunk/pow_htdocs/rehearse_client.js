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
	rehearse_helpers[i].responseQueue = new Array();	
	rehearse_helpers[i].func = function(functionNum, functionName, parameters) {
		for(param in parameters) {
			this[param] = parameters[param];
		}
		var last_var;
		var initial_snapshot = snapshot();
		console.log("Rehearse helper #" + functionNum + " called, with name=" + functionName);
		while(true) {
			if(rehearse_helpers[functionNum].commandQueue.length > 0) {
				var response = new Object();
				var o = rehearse_helpers[functionNum].commandQueue.shift();
				try {
					response.text = eval(o.code);
					response.type = 1;
				} catch(e) {
					response.text = e;
					response.type = 2;
				} finally {
					response.sid = snapshot();
					if (!o.isUndo) {
						queue = rehearse_helpers[functionNum].responseQueue;
						queue[queue.length] = response;
					}
				}
			} else if(rehearse_helpers[functionNum].finishedEditing) {
				return last_var;
			} else {
				1+1; //breakpoint here
			}
		}
	} 
}


function addCodeToQueue(functionNum, code, isUndo) {
	console.log("code: " + code);
	var queue = rehearse_helpers[functionNum].commandQueue;
	o = new Object();
	o.code = code;
	o.isUndo = isUndo;
	queue[queue.length] = o;
}

function getResponseFromQueue(functionNum) {
	if(rehearse_helpers[functionNum].responseQueue.length == 0)
		return null;
	var r = rehearse_helpers[functionNum].responseQueue.shift();
	console.log("response sent: " + r.text);
	return r;
}

function setupPOW() {
	window._rehearse_uid = Math.ceil(10000*Math.random());
	var url = pow_server_loc + "&rehearse_uid=" + window._rehearse_uid;
	
	$.get(url);
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
			rehearse_helpers[i].inUse = true;
			rehearse_helpers[i].finishedEditing = false;
			return i;
		}
	}
	alert("no more helpers!");
}

function markDone(functionNum) {
	rehearse_helpers[functionNum].finishedEditing = true;
}

function $I(functionName, parameters) {
	console.log("$I Called!");
	if (defined_functions[functionName]) {
		callDefinedFunction(functionName, parameters);
	} else {
		var num = getRehearseHelper();
		var f = rehearse_helpers[num].func;
		var ret = f(num, functionName, parameters);
		rehearse_helpers[num].inUse = false;
		return ret;
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


function rehearse_ajax_get(url, data, successfn_name) {
	successfn = function(msg) {
		$I(successfn_name, {response: msg});
	};
	$.ajax({type: "GET", url: url, data: data, success: successfn, async: false});
}

function rehearse_ajax_post(url, data, successfn_name) {
	successfn = function(msg) {
		$I(successfn_name, {response: msg});
	};
	$.ajax({type: "POST", url: url, data: data, success: successfn, async: false});
}
