# test whether we can create/write to read from a pysqlite database
# from a HTTP request
# 
from mod_python import apache, util
from pysqlite2 import dbapi2 as sqlite

def handler(req):
	req.content_type = "text/plain"
	
	try:
		req.write("connecting to db\n\n")
		con = sqlite.connect("/project/helpmeout-server/db/helpmeout.sqlite")
    	con.execute('create table if not exists errs(errmsg,diff)')
		req.write("writing to db\n\n")
		cur = con.cursor()
		cur.execute("insert into errs values ('test','test')")
		con.commit()
		req.write("reading from db\n\n")
		res = con.execute("select * from errs")
		req.write([r for r in res])
	except:
		req.write("oops, something went wrong.")

	return apache.OK