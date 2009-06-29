from mod_python import apache, util

def handler(req):
    req.content_type = "text/plain"
    
    req.write("1 + 1 = " + str(1+1) + "\n\n")

    req.write("The parameter values in this request were:\n")
    request_data = util.FieldStorage(req, keep_blank_values=True)
    for k, v in request_data.items():
        req.write("  " + str(k) + ": " + str(v) + "\n")

    return apache.OK
