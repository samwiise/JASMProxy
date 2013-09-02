package org.asmmr.nio;

import java.nio.channels.spi.AbstractSelectableChannel;

/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jul 14, 2006
 * Time: 10:28:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class SCRegistrationRequest{

    private AbstractSelectableChannel channel;
    private int operationset;
    private ISocketEventHandler handler;

    public SCRegistrationRequest(AbstractSelectableChannel channel,int
                                 operationset,ISocketEventHandler handler){
        this.channel = channel;
        this.operationset = operationset;
        this.handler = handler;
    }

    public AbstractSelectableChannel getChannel() {
        return channel;
    }

    public int getOperationset() {
        return operationset;
    }

    public ISocketEventHandler getHandler() {
        return handler;
    }
}