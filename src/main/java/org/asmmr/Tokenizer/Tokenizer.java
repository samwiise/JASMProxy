package org.asmmr.Tokenizer;

import java.util.Vector;

/**
 * @author asim.ali
 *
 */
public class Tokenizer {

    //states
    private static final int START=1;
    private static final int DONE=2;
    private static final int WORD=3;
    private static final int NUMBER=4;
    private static final int WHITESPACE=5;
    private static final int CARRIAGE=6;
    //private static final int UNKNOWN =7;

    private static final String SPECIAL=",.;:'\"<>\\";
    private static final String OTHERSPECIAL="`~!@#$%^&_|?";

    private int index;
    private String data;

    public Tokenizer(){
        data="";index=0;
    }

    private char getNextCharacter(){
        if(index<data.length()){
            char tempc = data.charAt(index++);
            return tempc;
        }else{
            index++;
            return '\0';
        }
    }
    private void unGetNextCharacter(){
        if(index>0)
            index--;
    }

    public Vector parse(String data){
        this.data = data;
        index=0;

        Vector tokens=new Vector();
        while(index<data.length()){
            tokens.add(getNextToken());
        }

        return tokens;
    }

    public Vector parse(){
        return parse(data);
    }

    private Token getNextToken(){



        int currentstate = START;
        Token temptoken=new Token();
        boolean flag1=false;

        while(currentstate!=DONE){
            char currentchar= getNextCharacter();
            boolean savechar=true;

            switch(currentstate){

                case START:
                    if(isAlphabet(currentchar))
                        currentstate = WORD;
                    else if(isDigit(currentchar))
                        currentstate = NUMBER;
                    else if(isWhiteSpace(currentchar))
                        currentstate = WHITESPACE;
                    else if(currentchar == '\r')
                        currentstate = CARRIAGE;
                    else{

                        if(SPECIAL.indexOf(currentchar)>-1 )
                            temptoken.Type = SPECIAL.indexOf(currentchar)+3;
                        else if(OTHERSPECIAL.indexOf(currentchar)>-1 )
                            temptoken.Type = TokenType.OTHERSPECIAL;
                        else if(currentchar == '\0')
                            temptoken.Type = TokenType.END;
                        else if(currentchar == '\n')
                            temptoken.Type = TokenType.CARRIAGERETURN;
                        else
                            switch(currentchar){

                                case '+':
                                case '-':
                                case '/':
                                case '*':
                                case '=':
                                    temptoken.Type = TokenType.OPERATOR;
                                    break;
                                case '[':
                                case '(':
                                case '{':
                                    temptoken.Type = TokenType.OPENBRACKET;
                                    break;
                                case ']':
                                case ')':
                                case '}':
                                    temptoken.Type = TokenType.CLOSEBRACKET;
                                    break;

                                default:
                                    temptoken.Type = TokenType.OTHERUNKNOWN;
                                    break;
                            }

                        currentstate = DONE;
                    }
                    break;


                case WORD:
                    if(!isAlphabet(currentchar) && !isDigit(currentchar)){
                        unGetNextCharacter();
                        currentstate=DONE;
                        temptoken.Type = TokenType.WORD;
                        savechar=false;
                    }
                    break;

                case NUMBER:
                    if(!isDigit(currentchar)){
                        if(currentchar=='.' && !flag1)
                            flag1=true;
                        else if(isAlphabet(currentchar))
                            currentstate=WORD;
                        else{
                            unGetNextCharacter();
                            currentstate=DONE;
                            temptoken.Type = TokenType.NUMBER;
                            savechar=false;
                            flag1=false;
                        }
                    }
                    break;

                case WHITESPACE:
                    if(!isWhiteSpace(currentchar)){
                        unGetNextCharacter();
                        currentstate=DONE;
                        temptoken.Type = TokenType.WHITESPACE;
                        savechar=false;
                    }
                    break;

                case CARRIAGE:
                    if(currentchar=='\n')
                        temptoken.Type = TokenType.CARRIAGERETURN;
                    else{
                        temptoken.Type = TokenType.OTHERUNKNOWN;
                        savechar=false;
                        unGetNextCharacter();
                    }
                    currentstate=DONE;
                    break;

            }

            if(savechar)temptoken.Value += currentchar;
        }



        return temptoken;
    }

    private boolean isWhiteSpace(char c){
        if((int)c ==9 || (int)c==32 )
            return true;
        return false;
    }

    private boolean isAlphabet(char c){
        if(((int)c >=65 && (int)c<=90) || ((int)c >=97 && (int)c<=122)  )
            return true;
        return false;
    }

    private boolean isDigit(char c){
        if((int)c >=48 && (int)c<=57 )
            return true;
        return false;
    }

    public static  class Token{

        public int Type;
        public String Value;

        public Token(){
            Type=0;
            Value="";
        }

        public Token(int type,String value){
            Type=type;
            Value=value;
        }

    }

    public class TokenType{



        public static final int WORD=1;
        public static final int NUMBER=2;
        public static final int COMMA=3;
        public static final int DOT=4;
        public static final int SEMICOLON=5;
        public static final int COLON=6;
        public static final int SINGLEQUOTE=7;
        public static final int DOUBLEQUOTE=8;
        public static final int LESSTHAN=9;
        public static final int GREATERTHAN=10;
        public static final int BACKSLASH=11;
        public static final int OPENBRACKET=12;
        public static final int CLOSEBRACKET=13;
        public static final int OPERATOR=14;
        public static final int WHITESPACE=15;
        public static final int CARRIAGERETURN=16;
        public static final int OTHERSPECIAL=17;
        public static final int OTHERUNKNOWN=18;
        public static final int END=19;

    }
}





