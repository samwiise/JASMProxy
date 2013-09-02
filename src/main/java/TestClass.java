/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jun 25, 2006
 * Time: 6:22:30 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

//import java.uti


public class TestClass
{

    public static void main(String[] arg){


        try{
            ServerSocketChannel server = ServerSocketChannel.open();

            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(70));
            server.accept();

            
        }catch(IOException e){

            System.out.println(e.getMessage());

        }
        Thread tg = new Thread();



    }



}
