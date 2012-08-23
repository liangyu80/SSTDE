tree grammar QueryGenerator;

options {
    tokenVocab = TextSearch;
    ASTLabelType = CommonTree;
}

@header {// Copyright 2011 by TalkingTrends (Amsterdam, The Netherlands)
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
}

@members {
    public static void suppresCompileWarnings() {
        Stack<String> stack = new Stack<String>(); stack.empty();
        List<String> list = new ArrayList<String>(); list.size();
        ArrayList<String> arrayList = new ArrayList<String>(); arrayList.size();
    }
}

query returns [FulltextSearch value]
    : f=andQuery { $value = $f.value; }
    | f=orQuery { $value = $f.value; }
    | f=prefix { $value = $f.value; }
    | f=word { $value = $f.value; }
    ;
    
word returns [FulltextSearch value]
    : ^(LOOKUP wrd=WORD) { $value = new Word($wrd.text); };

prefix returns [FulltextSearch value]
    : ^(PREFIXLOOKUP wrd=PREFIXWORD) { $value = new Prefix(new Word($wrd.text, true)); };

andQuery returns [FulltextSearch value]
    : ^(ANDX f1=query f2=query) { $value = new And($f1.value, $f2.value); };

orQuery returns [FulltextSearch value]
    : ^(OR f1=query f2=query) { $value = new Or($f1.value, $f2.value); };
