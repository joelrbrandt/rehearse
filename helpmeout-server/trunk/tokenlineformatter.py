# -*- coding: utf-8 -*-
"""
    Token Line Formatter - outputs token stream lines
    literals,names,and keywords are abstracted.
    
    Based on other.py NullFormatter, RawTokenFormatter which was
    :copyright: 2006-2007 by Georg Brandl, Armin Ronacher.
    :license: BSD, see LICENSE for more details.
"""

from pygments.formatter import Formatter
from pygments.token import Token
from pygments.token import STANDARD_TYPES
from pygments.token import is_token_subtype

__all__ = ['TokenLineFormatter']

class TokenLineFormatter(Formatter):
    """
    Output the text as a token stream, but preserving newlines
    """
    def format(self,tokensource,outfile):
        for ttype, value in tokensource:
            #if it's a literal,or a name, keyword or comment abstract it
            if is_token_subtype(ttype,Token.Name) \
            or is_token_subtype(ttype,Token.Literal) \
            or is_token_subtype(ttype,Token.Comment):
                outfile.write(STANDARD_TYPES[ttype])
            #else, write it
            else:
                outfile.write(value.strip("\t "))
            #if(value==u'\n'):
            #    outfile.write("\n")
            #else:
            #    #look at token.py
            #    # translate token type
            #    outfile.write("%s "%STANDARD_TYPES[ttype])
        

