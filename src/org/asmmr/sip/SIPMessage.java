
/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Mar 17, 2006
 * Time: 3:35:29 PM
 * To change this template use File | Settings | File Templates.
 */

package org.asmmr.sip;


import java.util.Iterator;
import java.util.Vector;

import org.asmmr.Tokenizer.Tokenizer;
import org.asmmr.Tokenizer.TokensEnumerator;
import org.asmmr.Tokenizer.Tokenizer.Token;
import org.asmmr.Tokenizer.Tokenizer.TokenType;

/**
 * @author asim.ali
 *
 */

public abstract class SIPMessage{


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


    /*INVITE sip:bob@biloxi.com SIP/2.0
          Via: SIP/2.0/UDP pc33.atlanta.com;branch=z9hG4bK776asdhds
          Max-Forwards: 70
          To: Bob <sip:bob@biloxi.com>
          From: Alice <sip:alice@atlanta.com>;tag=1928301774
          Call-ID: a84b4c76e66710@pc33.atlanta.com
          CSeq: 314159 INVITE
          Contact: <sip:alice@pc33.atlanta.com>
          Content-Type: application/sdp
          Content-Length: 142
      */

    /*public HeaderField Via;
    public HeaderField MaxForwards;
    public HeaderField To;
    public HeaderField From;
    public HeaderField CallId;
    public HeaderField CSeq;
    public HeaderField Contact;
    public HeaderField ContentType;
    public HeaderField ContentLength;*/


    public HeadersCollection Headers;
    public String Body;

    protected SIPMessage(){
        Headers = new HeadersCollection();
    }

    public void addHeader(HeaderField header){
        Headers.addHeader(header);
    }
    /*public void removeHeader(HeaderField header){
        OtherHeaders.remove(header);
    }*/
    /*public HeaderField getHeader(int index){
        return (HeaderField)OtherHeaders.get(index);
    }*/

    public HeaderField getHeader(String name){
        return Headers.getHeader(name);
    }
    public Parameter findParameter(String name){
        return Headers.findParameter(name);
    }

    public String toString(){
        String temp="";
        temp = getStartLine().toString() + "\n";
        temp += Headers.toString() + "\n";
        temp += Body;
        return temp;
    }

    public static SIPMessage parseMessage(String data){


        Tokenizer tokenizer = new Tokenizer();

        TokensEnumerator tokens = new TokensEnumerator(tokenizer.parse(data));


        SIPMessage tempmessage=null;
        HeaderField tempheader = null;
        HeaderFieldValue tempheadervalue = null;
        Parameter tempparameter = null;

        int currentstate = GETSTARTLINE;
        String tempstring="";

        int tempnum=0;

        while(tokens.hasNext()){

            Token token = tokens.getNext();

            boolean savetoken=true;

            switch(currentstate){

                case GETSTARTLINE:
                    if(token.Type == TokenType.WORD){
                        if(token.Value.compareToIgnoreCase("SIP")==0 &&
                                tokens.peekNext().Type == TokenType.OPERATOR && tokens.peekNext().Value.compareToIgnoreCase("/")==0){
                            String version = token.Value + tokens.skipToType(TokenType.WHITESPACE);
                            tokens.skipToType(TokenType.NUMBER);
                            int statuscode=Integer.parseInt(tokens.skipToType(TokenType.WHITESPACE));
                            tokens.skipToType(TokenType.WORD);
                            tempmessage = new SIPResponseMessage(statuscode,version,tokens.skipToType(TokenType.CARRIAGERETURN));
                            currentstate = PASSCARRIAGE;
                        }else{
                            String method = token.Value + tokens.skipToType(TokenType.WHITESPACE);
                            tokens.skipToType(TokenType.WORD);
                            String requesturi=tokens.skipToType(TokenType.WHITESPACE);
                            tokens.skipToType(TokenType.WORD);
                            tempmessage = new SIPRequestMessage(method,requesturi,tokens.skipToType(TokenType.CARRIAGERETURN));
                            currentstate = PASSCARRIAGE;
                        }
                    }
                    savetoken=false;
                    break;

                case GETHEADER:
                    if(token.Type == TokenType.COLON){
                        tempheader = new HeaderField(tempstring.trim());
                        currentstate = GETHEADERVALUE;
                        tempstring="";
                        savetoken=false;
                    }
                    break;

                case GETHEADERVALUE:
                    if(token.Type==TokenType.SEMICOLON){
                        tempheadervalue = new HeaderFieldValue(tempstring.trim());
                        currentstate = GETPARAMETER;
                        tempstring="";
                        savetoken=false;
                    }else if(token.Type==TokenType.COMMA){
                        tempheader.addValue(new HeaderFieldValue(tempstring.trim()));
                        tempstring="";
                        savetoken=false;
                    }else if(token.Type==TokenType.CARRIAGERETURN){
                        if(tokens.peekNext().Type==TokenType.WHITESPACE){
                            tempstring+=" ";
                            tokens.skipType(TokenType.WHITESPACE);
                            savetoken=false;
                        }else{
                            tempheader.addValue(new HeaderFieldValue(tempstring.trim()));
                            tempmessage.addHeader(tempheader);
                            tokens.unGet();
                            tempstring="";
                            currentstate = PASSCARRIAGE;
                            savetoken=false;
                        }
                    }
                    break;

                case GETPARAMETER:
                    if(token.Type==TokenType.OPERATOR && token.Value.compareTo("=")==0){
                        tempparameter = new Parameter(tempstring.trim());
                        tempstring="";
                        savetoken=false;
                        currentstate = GETPARAMETERVALUE;
                    }
                    break;

                case GETPARAMETERVALUE:
                    if(token.Type==TokenType.SEMICOLON){
                        tempparameter.Value = tempstring.trim();
                        tempheadervalue.addParameter(tempparameter);
                        tempstring="";
                        currentstate=GETPARAMETER;
                        savetoken=false;
                    }else if(token.Type==TokenType.COMMA){
                        tempparameter.Value = tempstring.trim();
                        tempheadervalue.addParameter(tempparameter);
                        tempheader.addValue(tempheadervalue);
                        tempstring="";
                        currentstate=GETHEADERVALUE;
                        savetoken=false;
                    }else if(token.Type==TokenType.CARRIAGERETURN){
                        if(tokens.peekNext().Type==TokenType.WHITESPACE){
                            tempstring+=" ";
                            tokens.skipType(TokenType.WHITESPACE);
                        }else{
                            tempparameter.Value = tempstring.trim();
                            tempheadervalue.addParameter(tempparameter);
                            tempstring="";
                            tempheader.addValue(tempheadervalue);
                            tempmessage.addHeader(tempheader);
                            tokens.unGet();
                            currentstate = PASSCARRIAGE;
                        }
                        savetoken=false;
                    }
                    break;

                case PASSCARRIAGE:
                    if(tempnum<2){
                        if(token.Type==TokenType.CARRIAGERETURN)
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
                    if(token.Type != TokenType.END){
                        if(tempmessage.Body==null)tempmessage.Body="";
                        tempmessage.Body += token.Value;
                    }
                    savetoken=false;
                    break;
            }

            if(savetoken)tempstring+=token.Value;

        }

        if(currentstate!=GETBODY)return null;
        return tempmessage;
   }

    /*private void placeHeader(HeaderField header){


        if(header.Name.compareToIgnoreCase("VIA")==0){
            if(Via == null)
                Via = header;
            else
                Via.appendHeaderValues(header);
        }if(header.Name.compareToIgnoreCase("MaxForwards")==0){
            if(MaxForwards == null)
                MaxForwards = header;
            else
                MaxForwards.appendHeaderValues(header);
        }


    } */


    //public HeaderField getHeader(String name){

        //Headers.
    //}

    public SIPStartLine getStartLine(){
        return getMessageStartLine();
    }
    protected abstract SIPStartLine getMessageStartLine();
    public abstract int getMessageType();

    public static class SIPRequestMessage extends SIPMessage{

        public SIPRequestLine StartLine;

        public SIPRequestMessage(){
            super();
            StartLine = new SIPRequestLine();
        }

        public SIPRequestMessage(String method,String requesturi,String version){
            super();
            StartLine = new SIPRequestLine(method, requesturi,version);
        }

        protected  SIPStartLine getMessageStartLine(){
            return StartLine;
        }

        public int getMessageType(){
            return REQUESTMESSAGE;
        }
    }

    public static class SIPResponseMessage extends SIPMessage{

        public SIPStatusLine StartLine;

        public SIPResponseMessage(){
            super();
            StartLine = new SIPStatusLine();
        }

        public SIPResponseMessage(int statuscode ,String version,String reasonphrase){
            super();
            StartLine = new SIPStatusLine(version,statuscode,reasonphrase);
        }

        protected  SIPStartLine getMessageStartLine(){
            return StartLine;
        }

        public int getMessageType(){
            return RESPONSEMESSAGE;
        }
    }

    public static class HeadersCollection{

        public Vector _headers;

        public HeadersCollection(){
            _headers=new Vector();
        }

        public void addHeader(HeaderField header){
            Iterator iterator = _headers.iterator();
            while(iterator.hasNext()){
                HeaderField tmp = (HeaderField)iterator.next();
                if(tmp.Name.compareToIgnoreCase(header.Name)==0){
                    tmp.appendHeaderValues(header);
                    return;
                }
            }
            _headers.add(header);
        }
        public HeaderField getHeader(String name){
            Iterator iterator = _headers.iterator();
            while(iterator.hasNext()){
                HeaderField tmp = (HeaderField)iterator.next();
                if(tmp.Name.compareToIgnoreCase(name)==0)
                    return tmp;
            }

            return new HeaderField(name,new HeaderFieldValue());
        }
        public Parameter findParameter(String name){

            Iterator iterator = _headers.iterator();
            while(iterator.hasNext()){
                HeaderField tmp = (HeaderField)iterator.next();
                Parameter tmp2;
                if((tmp2 = tmp.findParameter(name))!=null)
                    return tmp2;
            }
            return null;
        }
        public String toString(){
            Iterator iterator = _headers.iterator();
            String temp="";
            while(iterator.hasNext()){
                HeaderField tmp = (HeaderField)iterator.next();
                temp+=tmp.toString() + "\n";
            }
            return temp;
        }
        public void clear(){
            _headers.clear();
        }
    }

    public static class HeaderField{

        public String Name;
        public Vector Values;

        public HeaderField(){
            Values = new Vector();
            Name="Unknown";
        }
        public HeaderField(String name){
            this.Name = name;
            Values = new Vector();
        }
        public HeaderField(String name,HeaderFieldValue value){
            this.Name = name;
            Values = new Vector();
            addValue(value);
        }
        public void addValue(HeaderFieldValue value){
            Values.add(value);
        }
        public void appendHeaderValues(HeaderField header){
            Values.addAll(header.Values);
        }
        public void removeValue(HeaderFieldValue value){
            Values.remove(value);
        }
        public HeaderFieldValue getValue(int index){
            return (HeaderFieldValue)Values.get(index);
        }

        public String toString(){
            String result = Name + ": ";

            Iterator iterator = Values.iterator();
            while(iterator.hasNext()){
                HeaderFieldValue tmp = (HeaderFieldValue)iterator.next();
                result += tmp.toString() + ",";
            }
            if(result.charAt(result.length()-1)==',')
                result = result.substring(0,result.length()-1);
            
            return result;
        }
        public Parameter findParameter(String name){

            Iterator iterator = Values.iterator();
            while(iterator.hasNext()){
                HeaderFieldValue tmp = (HeaderFieldValue)iterator.next();
                Parameter tmp2;
                if((tmp2 = tmp.findParameter(name))!=null)
                    return tmp2;
            }
            return null;
        }

    }
//Method SP Request-URI SP SIP-Version CRLF
//Status-Line  =  SIP-Version SP Status-Code SP Reason-Phrase CRLF

    public static interface SIPStartLine{

        public static final int SIPREQUESTLINE=1;
        public static final int SIPSTATUSLINE=2;

        public int getType();
        public boolean checkPrimaryValue(String value);
    }

    public static class SIPStatusLine implements SIPStartLine{

        String Version;
        int StatusCode;
        String ReasonPhrase;

        public SIPStatusLine(){
            StatusCode=0;
            ReasonPhrase="";
            Version ="";
        }
        public String toString(){
            return Version + " " + String.valueOf(StatusCode) + " " + ReasonPhrase;
        }
        public boolean checkPrimaryValue(String value){
            if(Integer.parseInt(value)!=StatusCode)
                return false;
            return true;
        }
        public SIPStatusLine(String version,int statuscode,String reasonphrase){
            StatusCode=statuscode;
            Version=version;
            ReasonPhrase =reasonphrase;
        }
        public int getType(){
            return SIPSTATUSLINE;
        }

    }

    public static class SIPRequestLine implements SIPStartLine{

        String Method;
        String RequestURI;
        String Version;

        public SIPRequestLine(){
            Method="";
            RequestURI="";
            Version ="";
        }
        public boolean checkPrimaryValue(String value){
            if(Method.compareToIgnoreCase(value)!=0)
                return false;
            return true;
        }
        public String toString(){
            return Method + " " + RequestURI + " " + Version;
        }
        public SIPRequestLine(String method,String requesturi,String version){
            Method=method;
            RequestURI=requesturi;
            Version =version;
        }
        public int getType(){
            return SIPREQUESTLINE;
        }

    }

    public static class HeaderFieldValue {
        public String Value;
        public Vector Parameters;

        public HeaderFieldValue(){
            Parameters = new Vector();
            Value="";
        }
        public HeaderFieldValue(String Value){
            this.Value = Value;
            Parameters = new Vector();
        }

        public void addParameter(Parameter parameter){
            Parameters.add(parameter);
        }
        public void removeParameter(Parameter parameter){
            Parameters.remove(parameter);
        }
        public Parameter getParameter(int index){
            return (Parameter)Parameters.get(index);
        }
        public String toString(){
            String result = Value;

            Iterator iterator = Parameters.iterator();
            while(iterator.hasNext()){
                Parameter tmp = (Parameter)iterator.next();
                result += ";" + tmp.toString();
            }
            return result;
        }
        public Parameter findParameter(String name){

            Iterator iterator = Parameters.iterator();
            while(iterator.hasNext()){
                Parameter tmp = (Parameter)iterator.next();
                if(tmp.Name.compareToIgnoreCase(name)==0)
                    return tmp;
            }
            return null;
        }
    }
    public static class Parameter {
        public String Name;
        public String Value;

        public Parameter(){
            this.Name="Unknown";
            this.Value = "Unknown";
        }
        public Parameter(String Name){
            this.Name=Name;
            if(this.Name.compareToIgnoreCase("")==0)this.Name="Unknown";
            this.Value = "Unknown";
        }
        public Parameter(String Name,String Value){
            this.Name=Name;
            this.Value = Value;
        }
        public String toString()
        {
            return Name + "=" + Value;
        }
    }
}

