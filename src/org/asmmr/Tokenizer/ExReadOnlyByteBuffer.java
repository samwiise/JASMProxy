package org.asmmr.Tokenizer;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jul 14, 2006
 * Time: 2:24:38 AM
 * To change this template use File | Settings | File Templates.
 */



public class ExReadOnlyByteBuffer
{
    ByteBuffer buffer;
    int lastposition;

    public ExReadOnlyByteBuffer(int capacity){
        buffer = ByteBuffer.allocate(capacity);
        lastposition = -1;
    }

    public ExReadOnlyByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        lastposition = -1;
    }
    public ByteBuffer getBuffer() {
        return buffer;
    }

    public char getChar(){
        lastposition = buffer.position();
        return buffer.getChar();
    }

    public byte get() {
        lastposition = buffer.position();
        return buffer.get();
    }
    public void undo(){
        if(lastposition>-1){
                        
        }
    }
}
