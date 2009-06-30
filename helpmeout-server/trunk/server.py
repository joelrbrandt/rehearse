#!/user/bin/python

# Expose a json-rpc service that stores into a sqlite db and queries from it
#
# bjoern 6/27/09 foo boo
# @see http://json-rpc.org/wiki/python-json-rpc

from jsonrpc import handleCGI, ServiceMethod
from pysqlite2 import dbapi2 as sqlite
import difflib
from pygments import highlight
from pygments.lexers import JavaLexer
from tokenlineformatter import TokenLineFormatter

class HelpMeOutService(object):
    def connect(self):
        con = sqlite.connect("/project/helpmeout-server/db/helpmeout.sqlite")
        con.execute('create table if not exists errs(errmsg,diff)')
        return con
    
    
    # Find the best example fixes in our database for the error 
    # assume error is a single line string, diffs is an array of multiline strings
    def find_best_source_match(self,error_line,diffs,num_results=3):
        ranked_diffs = []
        for diff in diffs:
        
            max_sim = 0.0 #maximum similarity metric found so far
            all_lines = diff.splitlines(1)
        
            # split the diff report into lines unique to broken, fixed source
            # [2:] omits the "- " and "+ " of the diff format.
            old_lines = [l[2:] for l in all_lines if l.startswith('-')]
            new_lines = [l[2:] for l in all_lines if l.startswith('+')]
        
            # within broken old lines, find line with max similarity to error_line argument
            # version 1: operate directly on source text
            #for old_line in old_lines:
            #    s = difflib.SequenceMatcher(None,error_line,old_line)
            #    ratio = s.ratio() # calculate edit-distance based metric, [0.0-1.0]
            #    if ratio > max_sim:
            #        max_sim = ratio # new maximum-similarity line
        
            #version2: tokenized distance metric
            old_string = ''.join([l for l in old_lines]) #zip lines back up
            #tokenize both 
            token_error_line = highlight(error_line, JavaLexer(), TokenLineFormatter())
            token_old_string = highlight(old_string, JavaLexer(), TokenLineFormatter())
            for line in token_old_string.splitlines(1):
                s = difflib.SequenceMatcher(None,token_error_line,line)
                ratio = s.ratio()
                if ratio > max_sim:
                    max_sim = ratio
            #
            ranked_diffs.append((max_sim,{'old':old_lines,'new':new_lines}))
            
        #sort by decreasing ranking 
        ranked_diffs.sort(reverse=True)
            
        #now return up to top num_results as [{'new':[new_lines1],'old':[old_lines1]),...]
        return [r[1] for r in ranked_diffs[0:num_results]] # how many?
    
    @ServiceMethod
    def echo(self,msg):
        return msg
    
    # store a record into the db
    @ServiceMethod
    def store(self,error,diff):
        con = self.connect()
        cur = con.cursor()
        cur.execute("insert into errs values ('%s','%s')" % (error,diff))
        con.commit()
        return "Stored error %s in db" % error
    
    # transmit string and all of both files
    # easier to call from java so we can generate right diff format in python
    @ServiceMethod
    def store2(self,error,file1,file2):
        diff_obj = difflib.ndiff(file1.splitlines(1),file2.splitlines(1))
        diff_str = ''.join(diff_obj)
        return store(error,diff_str)
    
    #query the database
    @ServiceMethod
    def query(self,error,code):
        con = self.connect()
        cur = con.cursor()
        res = con.execute("select diff from errs where errmsg = '%s'" % error)
        if res==None:
            return ['error']
        arr = [d[0] for d in res]
        if len(arr)==0:
            return ['nothing found']
        # we have at least one useful result:
        # TODO: go over returned results one by one and check distance
        best = self.find_best_source_match(code,arr)
        return best

    #dump the entire database out
    @ServiceMethod
    def dumpdb(self):
        con = self.connect()
        res = con.execute("select * from errs")
        if res==None:
            return ['error']
        return [d for d in res]
    
    
service = HelpMeOutService()