package org.asmmr.util;



/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Mar 24, 2006
 * Time: 4:13:47 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
/**
 * @author asim.ali
 *
 */
public class MLogger {

    //FileHandler _handler;
    //Date _date;
    //java.util.logging.Logger _logger;

    String _fname;
    FileWriter _filewriter;


    private static MLogger _mlogger;

    protected MLogger(){
    }
    public static MLogger getLogger(){
        if (_mlogger == null)
              _mlogger = new MLogger();
        return _mlogger;
    }
    public String logMessage(String msg){
        try{
            if(_filewriter==null){
                _fname =  new Date().toString();
                _filewriter=new FileWriter("c:\\" + _fname + ".log");
            }
            //_logger.log(Level.ALL,msg);
            _filewriter.write(msg);
            _filewriter.flush();
            return _fname ;
        }catch(IOException e){
            e.printStackTrace();
            return "Could not write log file. ";// + _date.toString();
        }
    }
}
