package org.asmmr.http;

/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jun 24, 2006
 * Time: 9:10:02 PM
 * To change this template use File | Settings | File Templates.
 */


public class HttpResponseMessage extends HttpMessage
{
    public HttpStatusLine StartLine;

    public HttpResponseMessage() {
        super();
        StartLine = new HttpStatusLine();
    }

    public HttpResponseMessage(int statuscode, String version, String reasonphrase) {
        super();
        StartLine = new HttpStatusLine(version, statuscode, reasonphrase);
    }

    protected HttpStartLine getMessageStartLine() {
        return StartLine;
    }

    public int getMessageType() {
        return RESPONSEMESSAGE;
    }

    public static class HttpStatusLine implements HttpStartLine{

            public String Version;
            public int StatusCode;
            public String ReasonPhrase;

            public HttpStatusLine(){
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
            public HttpStatusLine(String version,int statuscode,String reasonphrase){
                StatusCode=statuscode;
                Version=version;
                ReasonPhrase =reasonphrase;
            }
            public int getType(){
                return HTTPSTATUSLINE;
            }

        }

}
