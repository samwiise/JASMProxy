package org.asmmr.Tokenizer;

import java.util.Vector;

import org.asmmr.Tokenizer.Tokenizer.Token;
import org.asmmr.Tokenizer.Tokenizer.TokenType;

        
public class TokensEnumerator {
    
    private Vector _tokens;
    private int _index;
    
    public TokensEnumerator(Vector tokens){
        _tokens = tokens;
        _index=0;
    }
    
    public Token getNext(){
        if(_index<_tokens.size()){
            Token temp = (Token)_tokens.get(_index++);
            return temp;
        }
        return null;
    }
    public void unGet(){
        if(_index>0)_index--;
    }
    public Token peekNext(){
        if(_index<_tokens.size())return (Token)_tokens.get(_index);
        return (new Token());
    }

    public boolean hasNext(){
        if(_index<_tokens.size())return true;
        return false;
    }

    public String skipTokens(int num){
        String temp="";
        while (num>0 && hasNext()){
            temp+=getNext().Value;
            num--;
        }
        return temp;
    }
    public String skipToWord(){
        return skipToType(TokenType.WORD);
    }
    public String skipToType(int type){
        String temp="";
        while (hasNext()){
            if(peekNext().Type != type)
                temp+=getNext().Value;
            else
                break;
        }
        return temp;
    }
    public String skipToTypeInLine(int type){
        String temp="";
        while (hasNext()){
        	int tType = peekNext().Type;
            if(tType != type && tType!=TokenType.CARRIAGERETURN)
                temp+=getNext().Value;
            else
                break;
        }
        return temp;
    }
    public String skipType(int type){
        String temp="";
        while (hasNext()){
            if(peekNext().Type == type)
                temp+=getNext().Value;
            else
                break;
        }
        return temp;
    }
    public void moveBack(int num){
        if(_index-num>=0)
            _index-=num;
        else
            _index=0;
    }
    public String skipToTypeValue(int type,String value){
        String temp="";
        while (hasNext()){
            if(peekNext().Type != type || peekNext().Value.compareTo(value)!=0)
                temp+=getNext().Value;
            else
                break;
        }
        return temp;
    }
    public String merge(int num){
        String temp="";
        int bindex =_index;
        while (num>0 && hasNext()){
            temp+=getNext().Value;
            num--;
        }
        _index=bindex;
        return temp;
    }


}
