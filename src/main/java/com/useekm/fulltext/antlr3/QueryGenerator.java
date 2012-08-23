// $ANTLR 3.2 Sep 23, 2009 14:05:07 com\\useekm\\fulltext\\antlr3\\QueryGenerator.g 2011-11-20 04:25:09
// Copyright 2011 by TalkingTrends (Amsterdam, The Netherlands)
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// http://opensahara.com/licenses/apache-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and limitations under
// the License.
//CHECKSTYLE:ANTLR
    package com.useekm.fulltext.antlr3;
    import com.useekm.fulltext.FulltextSearch;
    import com.useekm.fulltext.Prefix;
    import com.useekm.fulltext.And;
    import com.useekm.fulltext.Or;
    import com.useekm.fulltext.Word;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QueryGenerator extends TreeParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "ANDX", "OR", "LOOKUP", "PREFIXLOOKUP", "OPEN", "CLOSE", "WORD", "PREFIXWORD", "LETTER", "WS"
    };
    public static final int WORD=11;
    public static final int OPEN=9;
    public static final int LOOKUP=7;
    public static final int WS=14;
    public static final int CLOSE=10;
    public static final int LETTER=13;
    public static final int OR=6;
    public static final int PREFIXWORD=12;
    public static final int AND=4;
    public static final int EOF=-1;
    public static final int ANDX=5;
    public static final int PREFIXLOOKUP=8;

    // delegates
    // delegators


        public QueryGenerator(TreeNodeStream input) {
            this(input, new RecognizerSharedState());
        }
        public QueryGenerator(TreeNodeStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return QueryGenerator.tokenNames; }
    public String getGrammarFileName() { return "com\\useekm\\fulltext\\antlr3\\QueryGenerator.g"; }


        public static void suppresCompileWarnings() {
            Stack<String> stack = new Stack<String>(); stack.empty();
            List<String> list = new ArrayList<String>(); list.size();
            ArrayList<String> arrayList = new ArrayList<String>(); arrayList.size();
        }



    // $ANTLR start "query"
    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:36:1: query returns [FulltextSearch value] : (f= andQuery | f= orQuery | f= prefix | f= word );
    public final FulltextSearch query() throws RecognitionException {
        FulltextSearch value = null;

        FulltextSearch f = null;


        try {
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:37:5: (f= andQuery | f= orQuery | f= prefix | f= word )
            int alt1=4;
            switch ( input.LA(1) ) {
            case ANDX:
                {
                alt1=1;
                }
                break;
            case OR:
                {
                alt1=2;
                }
                break;
            case PREFIXLOOKUP:
                {
                alt1=3;
                }
                break;
            case LOOKUP:
                {
                alt1=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:37:7: f= andQuery
                    {
                    pushFollow(FOLLOW_andQuery_in_query61);
                    f=andQuery();

                    state._fsp--;

                     value = f; 

                    }
                    break;
                case 2 :
                    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:38:7: f= orQuery
                    {
                    pushFollow(FOLLOW_orQuery_in_query73);
                    f=orQuery();

                    state._fsp--;

                     value = f; 

                    }
                    break;
                case 3 :
                    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:39:7: f= prefix
                    {
                    pushFollow(FOLLOW_prefix_in_query85);
                    f=prefix();

                    state._fsp--;

                     value = f; 

                    }
                    break;
                case 4 :
                    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:40:7: f= word
                    {
                    pushFollow(FOLLOW_word_in_query97);
                    f=word();

                    state._fsp--;

                     value = f; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "query"


    // $ANTLR start "word"
    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:43:1: word returns [FulltextSearch value] : ^( LOOKUP wrd= WORD ) ;
    public final FulltextSearch word() throws RecognitionException {
        FulltextSearch value = null;

        CommonTree wrd=null;

        try {
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:44:5: ( ^( LOOKUP wrd= WORD ) )
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:44:7: ^( LOOKUP wrd= WORD )
            {
            match(input,LOOKUP,FOLLOW_LOOKUP_in_word125); 

            match(input, Token.DOWN, null); 
            wrd=(CommonTree)match(input,WORD,FOLLOW_WORD_in_word129); 

            match(input, Token.UP, null); 
             value = new Word((wrd!=null?wrd.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "word"


    // $ANTLR start "prefix"
    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:46:1: prefix returns [FulltextSearch value] : ^( PREFIXLOOKUP wrd= PREFIXWORD ) ;
    public final FulltextSearch prefix() throws RecognitionException {
        FulltextSearch value = null;

        CommonTree wrd=null;

        try {
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:47:5: ( ^( PREFIXLOOKUP wrd= PREFIXWORD ) )
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:47:7: ^( PREFIXLOOKUP wrd= PREFIXWORD )
            {
            match(input,PREFIXLOOKUP,FOLLOW_PREFIXLOOKUP_in_prefix149); 

            match(input, Token.DOWN, null); 
            wrd=(CommonTree)match(input,PREFIXWORD,FOLLOW_PREFIXWORD_in_prefix153); 

            match(input, Token.UP, null); 
             value = new Prefix(new Word((wrd!=null?wrd.getText():null), true)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "prefix"


    // $ANTLR start "andQuery"
    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:49:1: andQuery returns [FulltextSearch value] : ^( ANDX f1= query f2= query ) ;
    public final FulltextSearch andQuery() throws RecognitionException {
        FulltextSearch value = null;

        FulltextSearch f1 = null;

        FulltextSearch f2 = null;


        try {
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:50:5: ( ^( ANDX f1= query f2= query ) )
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:50:7: ^( ANDX f1= query f2= query )
            {
            match(input,ANDX,FOLLOW_ANDX_in_andQuery173); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_query_in_andQuery177);
            f1=query();

            state._fsp--;

            pushFollow(FOLLOW_query_in_andQuery181);
            f2=query();

            state._fsp--;


            match(input, Token.UP, null); 
             value = new And(f1, f2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "andQuery"


    // $ANTLR start "orQuery"
    // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:52:1: orQuery returns [FulltextSearch value] : ^( OR f1= query f2= query ) ;
    public final FulltextSearch orQuery() throws RecognitionException {
        FulltextSearch value = null;

        FulltextSearch f1 = null;

        FulltextSearch f2 = null;


        try {
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:53:5: ( ^( OR f1= query f2= query ) )
            // com\\useekm\\fulltext\\antlr3\\QueryGenerator.g:53:7: ^( OR f1= query f2= query )
            {
            match(input,OR,FOLLOW_OR_in_orQuery201); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_query_in_orQuery205);
            f1=query();

            state._fsp--;

            pushFollow(FOLLOW_query_in_orQuery209);
            f2=query();

            state._fsp--;


            match(input, Token.UP, null); 
             value = new Or(f1, f2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "orQuery"

    // Delegated rules


 

    public static final BitSet FOLLOW_andQuery_in_query61 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_orQuery_in_query73 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_prefix_in_query85 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_word_in_query97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOOKUP_in_word125 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_WORD_in_word129 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PREFIXLOOKUP_in_prefix149 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_PREFIXWORD_in_prefix153 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANDX_in_andQuery173 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_query_in_andQuery177 = new BitSet(new long[]{0x00000000000001E0L});
    public static final BitSet FOLLOW_query_in_andQuery181 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_OR_in_orQuery201 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_query_in_orQuery205 = new BitSet(new long[]{0x00000000000001E0L});
    public static final BitSet FOLLOW_query_in_orQuery209 = new BitSet(new long[]{0x0000000000000008L});

}