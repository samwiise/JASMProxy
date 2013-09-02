package org.asmmr.Tokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Mar 20, 2006
 * Time: 12:07:49 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileReader;

import org.asmmr.sip.SIPMessage;


public class TestClass {

    public static void main(String arg[]){

        /*Tokenizer obj = new Tokenizer();

        String data = "     This is the \r\n \rcgf test :; 123 parser,323fddf-4332.545.ddf";
        Vector list = obj.parse(data);

        Iterator temp =  list.iterator();
        while(temp.hasNext()){
            /*Token tmp = (Token)temp.next();
            System.out.println("VALUE : " + tmp.Value + "" + "\nTYPE: " + tmp.Type);

        }

        */

        char[]  tmpbuf = new char[1000];
        try{

            FileReader tmp = new FileReader("test.txt");
            int n = tmp.read(tmpbuf,0,1000);
            String ttt = String.valueOf(tmpbuf,0,n);
            System.out.println(ttt);
            SIPMessage tmpmess = SIPMessage.parseMessage(ttt);
            if(tmpmess!=null){
                System.out.println(tmpmess.getMessageType());
                System.out.println(tmpmess.Body);
            }            
        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
