package org.asmmr.http;

/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jun 24, 2006
 * Time: 9:08:55 PM
 * To change this template use File | Settings | File Templates.
 */

public class HttpRequestMessage extends HttpMessage
{

    public HttpRequestLine StartLine;

    public HttpRequestMessage() {
        super();
        StartLine = new HttpRequestLine();
    }

    public HttpRequestMessage(String method, String requesturi, String version) 
    {
        super();
        StartLine = new HttpRequestLine(method, requesturi, version);
    }

    protected HttpStartLine getMessageStartLine() {
        return StartLine;
    }

    public int getMessageType() {
        return REQUESTMESSAGE;
    }

    public static class HttpRequestLine implements HttpStartLine{

        public String Method;
        public String RequestURI;
        public String Version;

        public HttpRequestLine(){
            Method="";
            RequestURI = null;
            Version ="";
        }
        public HttpRequestLine(String method,String requesturi,String version) 
        {
            Method=method;
            RequestURI= requesturi;
            Version =version;
        }
        public boolean checkPrimaryValue(String value){
            if(Method.compareToIgnoreCase(value)!=0)
                return false;
            return true;
        }
        public String toString(){
            return Method + " " + RequestURI + " " + Version;
        }

        public int getType(){
            return HTTPREQUESTLINE;
        }
    }
}

