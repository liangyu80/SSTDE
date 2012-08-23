// $ANTLR 3.2 Sep 23, 2009 14:05:07 com\\useekm\\fulltext\\antlr3\\TextSearch.g 2011-11-20 04:25:08
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


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class TextSearchLexer extends Lexer {
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

        public static void suppresCompileWarnings() {
        	Stack<String> stack = new Stack<String>(); stack.empty();
    		List<String> list = new ArrayList<String>(); list.size();
    		ArrayList<String> arrayList = new ArrayList<String>(); arrayList.size();
        }


    // delegates
    // delegators

    public TextSearchLexer() {;} 
    public TextSearchLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public TextSearchLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "com\\useekm\\fulltext\\antlr3\\TextSearch.g"; }

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:29:5: ( '&' )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:29:7: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "ANDX"
    public final void mANDX() throws RecognitionException {
        try {
            int _type = ANDX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:30:6: ( '&&' )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:30:8: '&&'
            {
            match("&&"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ANDX"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:31:4: ( '|' )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:31:6: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "LOOKUP"
    public final void mLOOKUP() throws RecognitionException {
        try {
            int _type = LOOKUP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:32:8: ( '~' )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:32:10: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LOOKUP"

    // $ANTLR start "PREFIXLOOKUP"
    public final void mPREFIXLOOKUP() throws RecognitionException {
        try {
            int _type = PREFIXLOOKUP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:33:14: ( '*' )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:33:16: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PREFIXLOOKUP"

    // $ANTLR start "OPEN"
    public final void mOPEN() throws RecognitionException {
        try {
            int _type = OPEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:34:6: ( '(' )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:34:8: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OPEN"

    // $ANTLR start "CLOSE"
    public final void mCLOSE() throws RecognitionException {
        try {
            int _type = CLOSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:35:7: ( ')' )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:35:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CLOSE"

    // $ANTLR start "PREFIXWORD"
    public final void mPREFIXWORD() throws RecognitionException {
        try {
            int _type = PREFIXWORD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:85:11: ( ( LETTER )+ '*' )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:85:13: ( LETTER )+ '*'
            {
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:85:13: ( LETTER )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='\u0000' && LA1_0<='\b')||(LA1_0>='\u000B' && LA1_0<='\f')||(LA1_0>='\u000E' && LA1_0<='\u001F')||(LA1_0>='\"' && LA1_0<='%')||LA1_0=='\''||(LA1_0>=',' && LA1_0<='{')||(LA1_0>='}' && LA1_0<='\uFFFF')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // com\\useekm\\fulltext\\antlr3\\TextSearch.g:85:13: LETTER
            	    {
            	    mLETTER(); 

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);

            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PREFIXWORD"

    // $ANTLR start "WORD"
    public final void mWORD() throws RecognitionException {
        try {
            int _type = WORD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:86:5: ( ( LETTER )+ )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:86:7: ( LETTER )+
            {
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:86:7: ( LETTER )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='\u0000' && LA2_0<='\b')||(LA2_0>='\u000B' && LA2_0<='\f')||(LA2_0>='\u000E' && LA2_0<='\u001F')||(LA2_0>='\"' && LA2_0<='%')||LA2_0=='\''||(LA2_0>=',' && LA2_0<='{')||(LA2_0>='}' && LA2_0<='\uFFFF')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // com\\useekm\\fulltext\\antlr3\\TextSearch.g:86:7: LETTER
            	    {
            	    mLETTER(); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WORD"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:87:16: (~ ( ' ' | '\\t' | '\\n' | '\\r' | '!' | '&' | '|' | '+' | '*' | '(' | ')' ) )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:87:18: ~ ( ' ' | '\\t' | '\\n' | '\\r' | '!' | '&' | '|' | '+' | '*' | '(' | ')' )
            {
            if ( (input.LA(1)>='\u0000' && input.LA(1)<='\b')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\u001F')||(input.LA(1)>='\"' && input.LA(1)<='%')||input.LA(1)=='\''||(input.LA(1)>=',' && input.LA(1)<='{')||(input.LA(1)>='}' && input.LA(1)<='\uFFFF') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "LETTER"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:88:3: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:88:5: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // com\\useekm\\fulltext\\antlr3\\TextSearch.g:88:5: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt3=1;
                    }
                    break;

                }

                switch (alt3) {
            	case 1 :
            	    // com\\useekm\\fulltext\\antlr3\\TextSearch.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:8: ( AND | ANDX | OR | LOOKUP | PREFIXLOOKUP | OPEN | CLOSE | PREFIXWORD | WORD | WS )
        int alt4=10;
        alt4 = dfa4.predict(input);
        switch (alt4) {
            case 1 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:10: AND
                {
                mAND(); 

                }
                break;
            case 2 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:14: ANDX
                {
                mANDX(); 

                }
                break;
            case 3 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:19: OR
                {
                mOR(); 

                }
                break;
            case 4 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:22: LOOKUP
                {
                mLOOKUP(); 

                }
                break;
            case 5 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:29: PREFIXLOOKUP
                {
                mPREFIXLOOKUP(); 

                }
                break;
            case 6 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:42: OPEN
                {
                mOPEN(); 

                }
                break;
            case 7 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:47: CLOSE
                {
                mCLOSE(); 

                }
                break;
            case 8 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:53: PREFIXWORD
                {
                mPREFIXWORD(); 

                }
                break;
            case 9 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:64: WORD
                {
                mWORD(); 

                }
                break;
            case 10 :
                // com\\useekm\\fulltext\\antlr3\\TextSearch.g:1:69: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA4 dfa4 = new DFA4(this);
    static final String DFA4_eotS =
        "\1\uffff\1\12\1\uffff\1\13\3\uffff\1\15\6\uffff";
    static final String DFA4_eofS =
        "\16\uffff";
    static final String DFA4_minS =
        "\1\0\1\46\1\uffff\1\0\3\uffff\1\0\6\uffff";
    static final String DFA4_maxS =
        "\1\uffff\1\46\1\uffff\1\uffff\3\uffff\1\uffff\6\uffff";
    static final String DFA4_acceptS =
        "\2\uffff\1\3\1\uffff\1\5\1\6\1\7\1\uffff\1\12\1\2\1\1\1\4\1\10"+
        "\1\11";
    static final String DFA4_specialS =
        "\1\1\2\uffff\1\0\3\uffff\1\2\6\uffff}>";
    static final String[] DFA4_transitionS = {
            "\11\7\2\10\2\7\1\10\22\7\1\10\1\uffff\4\7\1\1\1\7\1\5\1\6\1"+
            "\4\1\uffff\120\7\1\2\1\7\1\3\uff81\7",
            "\1\11",
            "",
            "\11\7\2\uffff\2\7\1\uffff\22\7\2\uffff\4\7\1\uffff\1\7\2\uffff"+
            "\1\14\1\uffff\120\7\1\uffff\uff83\7",
            "",
            "",
            "",
            "\11\7\2\uffff\2\7\1\uffff\22\7\2\uffff\4\7\1\uffff\1\7\2\uffff"+
            "\1\14\1\uffff\120\7\1\uffff\uff83\7",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( AND | ANDX | OR | LOOKUP | PREFIXLOOKUP | OPEN | CLOSE | PREFIXWORD | WORD | WS );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_3 = input.LA(1);

                        s = -1;
                        if ( (LA4_3=='*') ) {s = 12;}

                        else if ( ((LA4_3>='\u0000' && LA4_3<='\b')||(LA4_3>='\u000B' && LA4_3<='\f')||(LA4_3>='\u000E' && LA4_3<='\u001F')||(LA4_3>='\"' && LA4_3<='%')||LA4_3=='\''||(LA4_3>=',' && LA4_3<='{')||(LA4_3>='}' && LA4_3<='\uFFFF')) ) {s = 7;}

                        else s = 11;

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_0 = input.LA(1);

                        s = -1;
                        if ( (LA4_0=='&') ) {s = 1;}

                        else if ( (LA4_0=='|') ) {s = 2;}

                        else if ( (LA4_0=='~') ) {s = 3;}

                        else if ( (LA4_0=='*') ) {s = 4;}

                        else if ( (LA4_0=='(') ) {s = 5;}

                        else if ( (LA4_0==')') ) {s = 6;}

                        else if ( ((LA4_0>='\u0000' && LA4_0<='\b')||(LA4_0>='\u000B' && LA4_0<='\f')||(LA4_0>='\u000E' && LA4_0<='\u001F')||(LA4_0>='\"' && LA4_0<='%')||LA4_0=='\''||(LA4_0>=',' && LA4_0<='{')||LA4_0=='}'||(LA4_0>='\u007F' && LA4_0<='\uFFFF')) ) {s = 7;}

                        else if ( ((LA4_0>='\t' && LA4_0<='\n')||LA4_0=='\r'||LA4_0==' ') ) {s = 8;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA4_7 = input.LA(1);

                        s = -1;
                        if ( (LA4_7=='*') ) {s = 12;}

                        else if ( ((LA4_7>='\u0000' && LA4_7<='\b')||(LA4_7>='\u000B' && LA4_7<='\f')||(LA4_7>='\u000E' && LA4_7<='\u001F')||(LA4_7>='\"' && LA4_7<='%')||LA4_7=='\''||(LA4_7>=',' && LA4_7<='{')||(LA4_7>='}' && LA4_7<='\uFFFF')) ) {s = 7;}

                        else s = 13;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 4, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}