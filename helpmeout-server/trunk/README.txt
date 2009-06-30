helpmeout-server documentation
==============================

here's quick rundown on files:

Folder ./
---------

server.py (http://rehearse.stanford.edu/helpmeout/server.py) is a json-rpc web service for querying the helpmeout database. URL: 

echo.py (http://rehearse.stanford.edu/helpmeout/echo.py) is a simple json-rpc web service for echoing strings (for testing)

tokenlineformatter.py is a custom token processing class used by the pygments lexer

helpmeout.py is the original example joel had in the directory and is now obsolete.

index.html (http://rehearse.stanford.edu/helpmeout/index.html)


Folder db/
----------

helpmeout.sqlite is the SQLite database where we're storing everything

Folder test/
------------
	
contains python scripts to test various aspects of the setup (e.g., can i query the database locally, can i reach the json-rpc echo server, can i query the db through json-rpc,...)
