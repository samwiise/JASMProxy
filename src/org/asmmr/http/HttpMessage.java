package org.asmmr.http;

/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jun 24, 2006
 * Time: 9:06:05 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Stack;

import org.asmmr.Tokenizer.Tokenizer;
import org.asmmr.Tokenizer.TokensEnumerator;
import org.asmmr.Tokenizer.Tokenizer.TokenType;
import org.asmmr.common.MessageParts.HeaderField;
import org.asmmr.common.MessageParts.HeaderFieldValue;
import org.asmmr.common.MessageParts.HeadersCollection;
import org.asmmr.common.MessageParts.Parameter;


public abstract class  HttpMessage
{

    public static final int REQUESTMESSAGE=1;
    public static final int RESPONSEMESSAGE=2;

    //states
    private static final int GETSTARTLINE=1;
    private static final int GETHEADER=2;
    private static final int GETHEADERVALUE=3;
    //private static final int ERROR=4;
    private static final int GETPARAMETER=4;
    private static final int PASSCARRIAGE=5;
    private static final int GETBODY=6;
    private static final int GETPARAMETERVALUE=7;
    
    private static final int PASSPARENTHESES=8;
    private static final int PASSQUOTES=9;


    public HeadersCollection Headers;
    public String Body;
    
    public HttpMessage(){
        Headers = new HeadersCollection();
    }

    public void addHeader(HeaderField header){
        Headers.addHeader(header);
    }
    public HeaderField getHeader(String name){
        return Headers.getHeader(name);
    }
    public Parameter findParameter(String name){
        return Headers.findParameter(name);
    }

    public String toString(){
        String temp="";
        temp = getMessageStartLine().toString() + "\r\n";
        temp += Headers.toString() + "\r\n";
        temp += Body;
        return temp;
    }
    public String toStringHeaders() {
    	  String temp="";
          temp = getMessageStartLine().toString() + "\r\n";
          temp += Headers.toString() + "\r\n";
          
          return temp;
    }
    protected abstract HttpStartLine getMessageStartLine();
    public abstract int getMessageType();

    public static HttpMessage parse(String data)
    {

        HttpMessage message=null;

        Tokenizer tokenizer = new Tokenizer();
        TokensEnumerator tokens = new
                TokensEnumerator(tokenizer.parse(data));

        HeaderField tempheader = null;
        HeaderFieldValue tempheadervalue = null;
        Parameter tempparameter = null;

        int currentstate = GETSTARTLINE;
        Stack<Integer> prestates = new Stack<Integer>();
        int pcount=0;
        
        String tempstring="";

        int tempnum=0;

        while(tokens.hasNext()){

            Tokenizer.Token token = tokens.getNext();

            boolean savetoken=true;

            switch(currentstate){

                case GETSTARTLINE:
                    if(token.Type == Tokenizer.TokenType.WORD){
                        if(token.Value.compareToIgnoreCase("HTTP")==0 &&
                                tokens.peekNext().Type == Tokenizer.TokenType.OPERATOR && tokens.peekNext().Value.compareToIgnoreCase("/")==0){
                            String version = token.Value + tokens.skipToType(Tokenizer.TokenType.WHITESPACE);
                            tokens.skipToType(Tokenizer.TokenType.NUMBER);
                            int statuscode=Integer.parseInt(tokens.skipToTypeInLine(Tokenizer.TokenType.WHITESPACE));
                            tokens.skipToType(Tokenizer.TokenType.WORD);
                            message = new HttpResponseMessage(statuscode,version,tokens.skipToType(Tokenizer.TokenType.CARRIAGERETURN));
                            currentstate = PASSCARRIAGE;
                        }else{
                            String method = token.Value + tokens.skipToType(Tokenizer.TokenType.WHITESPACE);
                            tokens.skipToType(Tokenizer.TokenType.WORD);
                            String requesturi=tokens.skipToType(Tokenizer.TokenType.WHITESPACE);
                            tokens.skipToType(Tokenizer.TokenType.WORD);
                            message = new HttpRequestMessage(method,requesturi,tokens.skipToType(Tokenizer.TokenType.CARRIAGERETURN));
                            currentstate = PASSCARRIAGE;
                        }
                    }
                    savetoken=false;
                    break;

                case GETHEADER:
                    if(token.Type == Tokenizer.TokenType.COLON){
                        tempheader = new HeaderField(tempstring.trim());
                        currentstate = GETHEADERVALUE;
                        tempstring="";
                        savetoken=false;
                    }
                    break;

                case GETHEADERVALUE:
                    if(token.Type== Tokenizer.TokenType.SEMICOLON){
                        tempheadervalue = new HeaderFieldValue(tempstring.trim());
                        currentstate = GETPARAMETER;
                        tempstring="";
                        savetoken=false;
                    }else if(token.Type== Tokenizer.TokenType.COMMA && 
                    		!tempheader.Name.equalsIgnoreCase("expires") && !tempheader.Name.equalsIgnoreCase("date") ){
                        tempheader.addValue(new HeaderFieldValue(tempstring.trim()));
                        tempstring="";
                        savetoken=false;
                    }else if(token.Type== Tokenizer.TokenType.CARRIAGERETURN){
                        if(tokens.peekNext().Type== Tokenizer.TokenType.WHITESPACE){
                            tempstring+=" ";
                            tokens.skipType(Tokenizer.TokenType.WHITESPACE);
                            savetoken=false;
                        }else{
                            tempheader.addValue(new HeaderFieldValue(tempstring.trim()));
                            message.addHeader(tempheader);
                            tokens.unGet();
                            tempstring="";
                            currentstate = PASSCARRIAGE;
                            savetoken=false;
                        }
                    }else if(token.Type==TokenType.OPENBRACKET && token.Value.compareTo("(")==0){
                    	prestates.push(currentstate);
                    	currentstate=PASSPARENTHESES;
                    	pcount++;
                    }else if(token.Type==TokenType.DOUBLEQUOTE){
                    	prestates.push(currentstate);
                    	currentstate=PASSQUOTES;
                    }
                    break;

                case GETPARAMETER:
                    if(token.Type== Tokenizer.TokenType.OPERATOR && token.Value.compareTo("=")==0){
                        tempparameter = new Parameter(tempstring.trim());
                        tempstring="";
                        savetoken=false;
                        currentstate = GETPARAMETERVALUE;
                    }else if(token.Type== Tokenizer.TokenType.CARRIAGERETURN){
                        if(tokens.peekNext().Type== Tokenizer.TokenType.WHITESPACE){
                            tempstring+=" ";
                            tokens.skipType(Tokenizer.TokenType.WHITESPACE);
                        }else{
                        	tempparameter = new Parameter(tempstring.trim());
                        	tempheadervalue.addParameter(tempparameter);
                            tempstring="";
                            tempheader.addValue(tempheadervalue);
                            message.addHeader(tempheader);
                            tokens.unGet();
                            currentstate = PASSCARRIAGE;
                        }
                        savetoken=false;
                    }
                    break;

                case GETPARAMETERVALUE:
                    if(token.Type== Tokenizer.TokenType.SEMICOLON){
                        tempparameter.Value = tempstring.trim();
                        tempheadervalue.addParameter(tempparameter);
                        tempstring="";
                        currentstate=GETPARAMETER;
                        savetoken=false;
                    }else if(token.Type== Tokenizer.TokenType.COMMA && 
                    		!tempparameter.Name.equalsIgnoreCase("expires") && !tempparameter.Name.equalsIgnoreCase("date")){
                        tempparameter.Value = tempstring.trim();
                        tempheadervalue.addParameter(tempparameter);
                        tempheader.addValue(tempheadervalue);
                        tempstring="";
                        currentstate=GETHEADERVALUE;
                        savetoken=false;
                    }else if(token.Type== Tokenizer.TokenType.CARRIAGERETURN){
                        if(tokens.peekNext().Type== Tokenizer.TokenType.WHITESPACE){
                            tempstring+=" ";
                            tokens.skipType(Tokenizer.TokenType.WHITESPACE);
                        }else{
                            tempparameter.Value = tempstring.trim();
                            tempheadervalue.addParameter(tempparameter);
                            tempstring="";
                            tempheader.addValue(tempheadervalue);
                            message.addHeader(tempheader);
                            tokens.unGet();
                            currentstate = PASSCARRIAGE;
                        }
                        savetoken=false;
                    }else if(token.Type==TokenType.OPENBRACKET && token.Value.compareTo("(")==0){
                    	prestates.push(currentstate);
                    	currentstate=PASSPARENTHESES;
                    	pcount++;
                    }else if(token.Type==TokenType.DOUBLEQUOTE){
                    	prestates.push(currentstate);
                    	currentstate=PASSQUOTES;
                    }
                    break;

                case PASSCARRIAGE:
                    if(tempnum<2){
                        if(token.Type== Tokenizer.TokenType.CARRIAGERETURN)
                            tempnum++;
                        else{
                            tempnum=0;
                            if(prestates.size()==0)
                            	currentstate=GETHEADER;
                            else
                            	currentstate=prestates.pop();
                            tokens.unGet();
                        }
                    }
                    savetoken=false;
                    if(tempnum>=2)
                        currentstate=GETBODY;
                    break;
                
                case PASSPARENTHESES:
                	if(token.Type== TokenType.OPENBRACKET && token.Value.compareTo("(")==0)
                		pcount++;
                	else if(token.Type==TokenType.CLOSEBRACKET && token.Value.compareTo(")")==0) 
                	{
                		pcount--;
                		if(pcount<=0) 
                		{
                			currentstate = prestates.pop();
                		}
                	}else if(token.Type==TokenType.DOUBLEQUOTE) {
                		prestates.push(currentstate);
                		currentstate=PASSQUOTES;
                	}else if(token.Type==TokenType.CARRIAGERETURN) {
                		prestates.push(currentstate);
                		currentstate=PASSCARRIAGE;
                		tokens.unGet();
                		savetoken=false;
                	}
                   	break;
   
                case PASSQUOTES:
                	if(token.Type==TokenType.DOUBLEQUOTE) 
                		currentstate=prestates.pop();
                	break;
                	
                case GETBODY:
                    if(token.Type != Tokenizer.TokenType.END){
                        if(message.Body==null)message.Body="";
                        message.Body += token.Value;
                    }
                    savetoken=false;
                    break;
            }

            if(savetoken)tempstring+=token.Value;

        }

        if(currentstate!=GETBODY)return null;

        return message;
    }


    /*public static HttpMessage parse(String data)throws java.net.MalformedURLException
        {

            HttpMessage message=null;

            Tokenizer tokenizer = new Tokenizer();
            TokensEnumerator tokens = new
                    TokensEnumerator(tokenizer.parse(data));

            HeaderField tempheader = null;
            HeaderFieldValue tempheadervalue = null;
            Parameter tempparameter = null;

            int currentstate = GETSTARTLINE;
            String tempstring="";

            int tempnum=0;

            while(tokens.hasNext()){

                Tokenizer.Token token = tokens.getNext();

                boolean savetoken=true;

                switch(currentstate){

                    case GETSTARTLINE:
                        if(token.Type == Tokenizer.TokenType.WORD){
                            if(token.Value.compareToIgnoreCase("HTTP")==0 &&
                                    tokens.peekNext().Type == Tokenizer.TokenType.OPERATOR && tokens.peekNext().Value.compareToIgnoreCase("/")==0){
                                String version = token.Value + tokens.skipToType(Tokenizer.TokenType.WHITESPACE);
                                tokens.skipToType(Tokenizer.TokenType.NUMBER);
                                int statuscode=Integer.parseInt(tokens.skipToType(Tokenizer.TokenType.WHITESPACE));
                                tokens.skipToType(Tokenizer.TokenType.WORD);
                                message = new HttpResponseMessage(statuscode,version,tokens.skipToType(Tokenizer.TokenType.CARRIAGERETURN));
                                currentstate = PASSCARRIAGE;
                            }else{
                                String method = token.Value + tokens.skipToType(Tokenizer.TokenType.WHITESPACE);
                                tokens.skipToType(Tokenizer.TokenType.WORD);
                                String requesturi=tokens.skipToType(Tokenizer.TokenType.WHITESPACE);
                                tokens.skipToType(Tokenizer.TokenType.WORD);
                                message = new HttpRequestMessage(method,requesturi,tokens.skipToType(Tokenizer.TokenType.CARRIAGERETURN));
                                currentstate = PASSCARRIAGE;
                            }
                        }
                        savetoken=false;
                        break;

                    case GETHEADER:
                        if(token.Type == Tokenizer.TokenType.COLON){
                            tempheader = new HeaderField(tempstring.trim());
                            currentstate = GETHEADERVALUE;
                            tempstring="";
                            savetoken=false;
                        }
                        break;

                    case GETHEADERVALUE:
                        if(token.Type== Tokenizer.TokenType.SEMICOLON){
                            tempheadervalue = new HeaderFieldValue(tempstring.trim());
                            currentstate = GETPARAMETER;
                            tempstring="";
                            savetoken=false;
                        }else if(token.Type== Tokenizer.TokenType.COMMA){
                            tempheader.addValue(new HeaderFieldValue(tempstring.trim()));
                            tempstring="";
                            savetoken=false;
                        }else if(token.Type== Tokenizer.TokenType.CARRIAGERETURN){
                            if(tokens.peekNext().Type== Tokenizer.TokenType.WHITESPACE){
                                tempstring+=" ";
                                tokens.skipType(Tokenizer.TokenType.WHITESPACE);
                                savetoken=false;
                            }else{
                                tempheader.addValue(new HeaderFieldValue(tempstring.trim()));
                                message.addHeader(tempheader);
                                tokens.unGet();
                                tempstring="";
                                currentstate = PASSCARRIAGE;
                                savetoken=false;
                            }
                        }
                        break;

                    case GETPARAMETER:
                        if(token.Type== Tokenizer.TokenType.OPERATOR && token.Value.compareTo("=")==0){
                            tempparameter = new Parameter(tempstring.trim());
                            tempstring="";
                            savetoken=false;
                            currentstate = GETPARAMETERVALUE;
                        }
                        break;

                    case GETPARAMETERVALUE:
                        if(token.Type== Tokenizer.TokenType.SEMICOLON){
                            tempparameter.Value = tempstring.trim();
                            tempheadervalue.addParameter(tempparameter);
                            tempstring="";
                            currentstate=GETPARAMETER;
                            savetoken=false;
                        }else if(token.Type== Tokenizer.TokenType.COMMA){
                            tempparameter.Value = tempstring.trim();
                            tempheadervalue.addParameter(tempparameter);
                            tempheader.addValue(tempheadervalue);
                            tempstring="";
                            currentstate=GETHEADERVALUE;
                            savetoken=false;
                        }else if(token.Type== Tokenizer.TokenType.CARRIAGERETURN){
                            if(tokens.peekNext().Type== Tokenizer.TokenType.WHITESPACE){
                                tempstring+=" ";
                                tokens.skipType(Tokenizer.TokenType.WHITESPACE);
                            }else{
                                tempparameter.Value = tempstring.trim();
                                tempheadervalue.addParameter(tempparameter);
                                tempstring="";
                                tempheader.addValue(tempheadervalue);
                                message.addHeader(tempheader);
                                tokens.unGet();
                                currentstate = PASSCARRIAGE;
                            }
                            savetoken=false;
                        }
                        break;

                    case PASSCARRIAGE:
                        if(tempnum<2){
                            if(token.Type== Tokenizer.TokenType.CARRIAGERETURN)
                                tempnum++;
                            else{
                                tempnum=0;
                                currentstate=GETHEADER;
                                tokens.unGet();
                            }
                        }
                        savetoken=false;
                        if(tempnum>=2)
                            currentstate=GETBODY;
                        break;

                    case GETBODY:
                        if(token.Type != Tokenizer.TokenType.END){
                            if(message.Body==null)message.Body="";
                            message.Body += token.Value;
                        }
                        savetoken=false;
                        break;
                }

                if(savetoken)tempstring+=token.Value;

            }

            if(currentstate!=GETBODY)return null;

            return message;
        }

      */

    public static interface HttpStartLine{

        public static final int HTTPREQUESTLINE=1;
        public static final int HTTPSTATUSLINE=2;

        public int getType();
        public boolean checkPrimaryValue(String value);

    }



}

