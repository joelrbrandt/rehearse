var empty_window_props = ['__scope__', 'window', 'navigator', 'document', '__firebug__',
 'location', 'Packages', 'sun', 'java', 'netscape', 'Components', 'parent', 'top',
  'scrollbars', 'frames', 'self', 'screen', 'history', 'content', 'menubar', 'toolbar',
   'locationbar', 'personalbar', 'statusbar', 'directories', 'crypto', 'pkcs11',
    'controllers', 'opener', 'frameElement', 'sessionStorage', 'globalStorage', '$', 'jQuery',
	'snapshots', 'empty_window_props'];

var snapshots = new Array();

/**
* Takes a 'snapshot' of the current window state.
* Returns a unique id associated with this state, which 
* can be used to load this state later.
* 
* @return sid snapshot id
*/
function snapshot() {
	var s = {};
	s.dom_body = $('body').clone(true);
	for (var prop in window) {
		if(empty_window_props.indexOf(prop) == -1) {
			if(window[prop] instanceof Node) {
				//do nothing? s[prop] = window[prop].clone(true);
			}
			else if(typeof window[prop] == 'object') {
				s[prop] = clone_obj(window[prop]);
			} else {
				s[prop] = window[prop];
			}
		}
	}
	snapshots[snapshots.length] = s;
	return snapshots.length - 1;
}

/**
* Updates the current state with the given snapshot id.
*
* @param id snapshot id to load
*/
function load(id) {
	if(id < 0 || id >= snapshots.length) 
		return false;
	var s = snapshots[id];
	$('body').replaceWith(s.dom_body);
	for(var prop in s) {
		if(prop != 'dom_body') {
			if(typeof s[prop] == 'object') {
				window[prop] = clone_obj(s[prop]);
			} else {
				window[prop] = s[prop];
			}
		}
	}
	return true;
}

/**
* function clone_obj deep-clones an js-object
* Handles circular references with caching
*
* @param obj javascript-object
* @return c cloned javascript-object
*/
function clone_obj(obj) {
	var map = new Array();
	return clone_obj_helper(obj, map);
}

function clone_obj_helper(obj, clone_map) {
    if (typeof obj !== 'object' || obj == null) {
        return obj;
    }

    var c = obj instanceof Array ? [] : {};
	if(map_get(clone_map, obj) == "")
		map_put (clone_map, obj, c);
    
    for (var i in obj) {
        var prop = obj[i];
        if (typeof prop == 'object') {
           if (prop instanceof Array) {
               c[i] = [];
               for (var j = 0; j < prop.length; j++) {
                   if (typeof prop[j] != 'object') {
                       c[i].push(prop[j]);
                   } else {
				   		var cloned_obj = map_get(clone_map, prop[j]);
		   				if(cloned_obj == "") {
							c[i].push(clone_obj_helper(prop[j], clone_map));
						} else {
							c[i].push(cloned_obj);
						}
                   }
               }
           } else {
		   		var cloned_obj = map_get(clone_map, prop);
		   		if(cloned_obj == "") {
               		c[i] = clone_obj_helper(prop, clone_map);
				} else {
					c[i] = cloned_obj;
				}
           }
        } else {
           c[i] = prop;
        }
    }
    return c;
}



/* Object-to-Object map */
function map_put(map, key, value)
{
	var obj = {};
	obj.key = key;
	obj.value = value;
	map[map.length] = obj;
}

function map_get(map, key)
{
	for( var i = 0 ; i < map.length ; i++ )
    {
        if(map[i].key == key ) {
            return map[i].value;
        }
    }
    return "";
}