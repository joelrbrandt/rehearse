#!/user/bin/env python

# THIS IS THE ARDUINO SERVER - MINIMAL CHANGES: DIFFERENT SQLITE FILE, DIFFERENT PYGMENTS LEXER

# Expose a json-rpc service that stores into a sqlite db and queries from it
#
# bjoern 6/27/09 foo boo
# @see http://json-rpc.org/wiki/python-json-rpc

from jsonrpc import handleCGI, ServiceMethod
from pysqlite2 import dbapi2 as sqlite
import difflib
from pygments import highlight
from pygments.lexers import CppLexer
from tokenlineformatter import TokenLineFormatter
import re

class HelpMeOutService(object):
    def connect(self):
        #todo: added fields here
        con = sqlite.connect("/project/helpmeout-server/db/helpmeout-arduino.sqlite") #ARUDINO - use different db
        con.execute('create table if not exists compilererrors(id INTEGER PRIMARY KEY,timestamp,errmsg,diff,votes INTEGER DEFAULT 0)')
        con.execute('CREATE TABLE if not exists exceptions(id INTEGER PRIMARY KEY,timestamp VARCHAR,errmsg VARCHAR,line VARCHAR,stacktrace VARCHAR,diff VARCHAR, votes INTEGER DEFAULT 0)')
        con.execute('CREATE TABLE if not exists querylog(id INTEGER PRIMARY KEY, timestamp VARCHAR, type INTEGER, status INTEGER, error VARCHAR, code VARCHAR, stacktrace VARCHAR)')
        con.execute('CREATE TABLE if not exists comments(id INTEGER PRIMARY KEY, timestamp VARCHAR, type INTEGER, fix_id INTEGER, comment VARCHAR)')
        con.execute('CREATE TABLE if not exists fixidlog(id INTEGER PRIMARY KEY, log_id INTEGER, fix_id INTEGER)')
        con.execute('CREATE TABLE if not exists idelog(id INTEGER PRIMARY KEY, timestamp VARCHAR, log VARCHAR)')
        return con
    
    def find_best_exception_match(self,code,querytrace,arr,num_results=3) :
        htmlDiff = difflib.HtmlDiff()
        ranked_results = []
        #clean up querytrace
        lines = querytrace.splitlines(1)
        good_lines = [l for l in lines if (l.strip().startswith("at") and not l.strip().startswith("at bsh."))]
        cleaned_querytrace = ''.join(good_lines)
        
        for einfo in arr:
            fixid = einfo[0]
            trace = einfo[1]
            diff = einfo[2]
            all_lines = diff.splitlines(1)
            old_lines = [l[2:] for l in all_lines if l.startswith('-')]
            new_lines = [l[2:] for l in all_lines if l.startswith('+')]
            votes = einfo[3]
            comment = einfo[4]
            #clean fix trace
            lines = trace.splitlines(1)
            good_lines = [l for l in lines if (l.strip().startswith("at") and not l.strip().startswith("at bsh."))]
            cleaned_trace = ''.join(good_lines)
            # calculate similarity between cleaned stacktraces
            s = difflib.SequenceMatcher(None,cleaned_querytrace,cleaned_trace)
            ratio = s.ratio()
            
             # generate a html diff report
            file1 = difflib.restore(all_lines,1)
            file2 = difflib.restore(all_lines,2)
            # use more context for exceptions
            table = htmlDiff.make_table(file1,file2,"Before&nbsp;(Broken)","After&nbsp;(Fixed)",context=True,numlines=1)

            # hack: for now make sure everything we return is a list of strings
            ranked_results.append((ratio,votes,{'id':[str(fixid),''],'old':old_lines,'new':new_lines,'table':[table,''],'comment':[comment,'']}))
            
        #sort by decreasing similarity ranking 
        ranked_results.sort(reverse=True)
        
        # take voting into account
        vote_ranked_results = [r[1:] for r in ranked_results[0:(2*num_results)]]
        vote_ranked_results.sort(reverse=True)

        #now return up to top num_results as [{'new':[new_lines1],'old':[old_lines1]),...]
        return [r[1] for r in vote_ranked_results[0:num_results]] # how many?
    
    # Find the best example fixes in our database for the error 
    # assume error is a single line string, 
    # assume diffs is an array of (id,multiline string,votes) tuples
    def find_best_source_match(self,error_line,diffs,num_results=3):
        htmlDiff = difflib.HtmlDiff()
        
        ranked_diffs = []
        for diff in diffs:
            fixid = diff[0]
            votes = diff[2]
            comment = diff[3]
            max_sim = 0.0 #maximum similarity metric found so far
            all_lines = diff[1].splitlines(1)
        
            # split the diff report into lines unique to broken, fixed source
            # [2:] omits the "- " and "+ " of the diff format.
            old_lines = [l[2:] for l in all_lines if l.startswith('-')]
            new_lines = [l[2:] for l in all_lines if l.startswith('+')]
        
            # within broken old lines, find line with max similarity to error_line argument
            # version 1: ofperate directly on source text
            #for old_line in old_lines:
            #    s = difflib.SequenceMatcher(None,error_line,old_line)
            #    ratio = s.ratio() # calculate edit-distance based metric, [0.0-1.0]
            #    if ratio > max_sim:
            #        max_sim = ratio # new maximum-similarity line
        
            #version2: tokenized distance metric
            old_string = ''.join([l for l in old_lines]) #zip lines back up
            #tokenize both 
            token_error_line = highlight(error_line, CppLexer(), TokenLineFormatter("arduino")) #ARDUNIO
            token_old_string = highlight(old_string, CppLexer(), TokenLineFormatter("arduino")) #ARDUINO
            for line in token_old_string.splitlines(1):
                s = difflib.SequenceMatcher(None,token_error_line,line)
                ratio = s.ratio()
                if ratio > max_sim:
                    max_sim = ratio
            
            # generate a html diff report
            file1 = difflib.restore(all_lines,1)
            file2 = difflib.restore(all_lines,2)
            table = htmlDiff.make_table(file1,file2,"Before&nbsp;(Broken)","After&nbsp;(Fixed)",context=True,numlines=0)
            
            # hack: for now make sure everything we return is a list of strings
            ranked_diffs.append((max_sim,votes,{'id':[str(fixid),''],'old':old_lines,'new':new_lines,'table':[table,''],'comment':[comment,'']}))
            
        #sort by decreasing similarity ranking 
        ranked_diffs.sort(reverse=True)
        
        #how should we take voting into account?
        #take top 2N results (if we're returning n) and sort by votes
        vote_ranked_diffs = [r[1:] for r in ranked_diffs[0:(2*num_results)]]
        vote_ranked_diffs.sort(reverse=True)
        
        #now return up to top num_results as [{'new':[new_lines1],'old':[old_lines1]),...]
        return [r[1] for r in vote_ranked_diffs[0:num_results]] # how many?
    
    @ServiceMethod
    def echo(self,msg):
        return msg
    
    # store an IDE log into the db
    @ServiceMethod
    def storeidelog(self,log):
        con = self.connect()
        cur = con.cursor()
        cur.execute("insert into idelog values (null,datetime('now','localtime'),?)",(log,))
        con.commit()
        return "Stored log in db"
    
    # store a record into the db
    @ServiceMethod
    def store(self,error,diff):
        con = self.connect()
        cur = con.cursor()
        cur.execute("insert into compilererrors values (null,datetime('now','localtime'),?,?,0)",(error,diff))
        con.commit()
        return "Stored error in db"
    
    @ServiceMethod
    def storeexception(self,error,line,stacktrace,file1,file2):
        con = self.connect()
        cur = con.cursor()
        diff_obj = difflib.ndiff(file1.splitlines(1),file2.splitlines(1))
        diff_str = ''.join(diff_obj)
        cur.execute("insert into exceptions values (null,datetime('now','localtime'),?,?,?,?,0)",(error,line,stacktrace,diff_str))
        con.commit()
        return "Stored error into table exceptions"
    
    # transmit string and all of both files
    # easier to call from java so we can generate right diff format in python
    @ServiceMethod
    def store2(self,error,file1,file2):
        diff_obj = difflib.ndiff(file1.splitlines(1),file2.splitlines(1))
        diff_str = ''.join(diff_obj)
        return self.store(error,diff_str)
    
    # vote for a fix - either up or down
    @ServiceMethod
    def errorvote(self,id,vote):
        con = self.connect()
        cur = con.cursor()
        res = cur.execute("update compilererrors set votes = (select votes from compilererrors where id = :id)+(:vote) where id=:id",locals())
        con.commit()
        return "Updated vote"

    # vote for a fix - either up or down
    @ServiceMethod
    def errorvoteexception(self,id,vote):
        con = self.connect()
        cur = con.cursor()
        res = cur.execute("update exceptions set votes = (select votes from exceptions where id = :id)+(:vote) where id=:id",locals())
        con.commit()
        return "Updated vote"
    
    #query the database
    #error comes in pre-cleaned in java - this did not work: cleaned_error = re.sub(u'[\u201c].*?[\u201d]','%',error)
    @ServiceMethod
    def query(self,error,code):
        con = self.connect()
        cur = con.cursor()
        # note: assumption is that there is at most one comment for each error.
        # otherwise we should group_concat(comment) but our sqlite3 version doesn't support it yet
        res = con.execute("SELECT compilererrors.id,diff,votes,comment FROM compilererrors LEFT JOIN comments ON compilererrors.id=comments.fix_id WHERE (comments.type IS NULL OR comments.type=0) AND errmsg LIKE ?",(error,))
        if res==None:
            cur.execute("insert into querylog values (null, datetime('now','localtime'), 0, 2, ?, ?, '')",(error,code))
            con.commit()
            return 'ERROR'
        arr = [d for d in res] # d[0] has id, d[1] has diff, d[2] the votes, d[3] the comment or None
        if len(arr)==0:
            cur.execute("insert into querylog values (null, datetime('now','localtime'), 0, 1, ?, ?, '')",(error,code))
            con.commit()
            return 'NO_RESULT'
        # we have at least one useful result:
        # go over returned results one by one and check distance
        best = self.find_best_source_match(code,arr)
        best_ids = [int(r['id'][0]) for r in best]
        cur.execute("insert into querylog values (null, datetime('now','localtime'), 0, 0, ?, ?, '')",(error,code))
        res = con.execute("select last_insert_rowid() from querylog")
        last_query_log_id = [r for r in res][0][0]
        for id in best_ids:
            cur.execute("insert into fixidlog values(null, ?, ?)",(last_query_log_id,id))
        con.commit()
        return best
    
    # query the database for matching exceptions
    @ServiceMethod
    def queryexception(self,error,code,stacktrace):
        con = self.connect()
        cur = con.cursor()
        res = con.execute("SELECT exceptions.id,stacktrace,diff,votes,comment FROM exceptions LEFT JOIN comments on exceptions.id=comments.fix_id WHERE (comments.type IS NULL OR comments.type=1) AND errmsg LIKE ?",(error,))
        if res==None:
            cur.execute("insert into querylog values(null, datetime('now','localtime'), 1, 2, ?, ?, ?)",(error,code,stacktrace))
            con.commit()
            return 'ERROR'
        arr = [d for d in res] # d[0] has id, d[1] has stacktrace, d[2] the diff, d[3] the votes, d[4] the comment
        if len(arr)==0:
            cur.execute("insert into querylog values(null, datetime('now','localtime'), 1, 1, ?, ?, ?)",(error,code,stacktrace))
            con.commit()
            return 'NO_RESULT'
        best = self.find_best_exception_match(code,stacktrace,arr)
        best_ids = [int(r['id'][0]) for r in best]
        cur.execute("insert into querylog values (null, datetime('now','localtime'), 1, 0, ?, ?, ?)",(error,code,stacktrace))
        res = con.execute("select last_insert_rowid() from querylog")
        last_query_log_id = [r for r in res][0][0]
        for id in best_ids:
            cur.execute("insert into fixidlog values (null, ?, ?)",(last_query_log_id,id))
        con.commit()
        return best

    #dump the entire database out
    @ServiceMethod
    def dumpdb(self):
        con = self.connect()
        res = con.execute("select * from compilererrors")
        if res==None:
            return 'ERROR'
        return [d for d in res]
    
    
service = HelpMeOutService()
