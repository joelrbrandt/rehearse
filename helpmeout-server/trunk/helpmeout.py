from mod_python import apache, util
import sys,os

def handler(req):
    req.content_type = "text/plain"

    (modulePath, fileName) = os.path.split(req.filename)
    (moduleName, ext) = os.path.splitext(fileName)
    req.write(modulePath+ "\n\n")
    req.write(moduleName+ "\n\n")
    req.write("Looking up:"+os.path.join(modulePath, moduleName + ".py\n\n"))
    
    req.write("1 + 1 = " + str(1+1) + "\n\n")

    req.write("The parameter values in this request were:\n")
    request_data = util.FieldStorage(req, keep_blank_values=True)
    for k, v in request_data.items():
        req.write("  " + str(k) + ": " + str(v) + "\n")

    return apache.OK
