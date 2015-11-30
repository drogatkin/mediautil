/* MediaUtil LLJTran - $RCSfile: OutStreamToIterativeReader.java,v $
 * Copyright (C) 1999-2005 Dmitriy Rogatkin, Suresh Mahalingam.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  $Id: OutStreamToIterativeReader.java,v 1.3 2005/09/30 21:23:18 drogatkin Exp $
 *
 */
package mediautil.gen.directio;

import java.io.*;

/**
 * This class enables writing directly to an IterativeReader which will read the
 * data written as an InputStream. The write calls to this class which is to be
 * used as an OutputStream translate into nextRead() calls on the underlying
 * IterativeReader after buffering. The only catch is that the IterativeReader
 * in this case should not exceed the read request beyond the readCushion as
 * this will result in an empty buffer which will be flagged as an IOException
 * on the IterativeReader's InputStream.
 * @see IterativeReader
 *
 * @author Suresh Mahalingam (msuresh@cheerful.com)
 */
public class OutStreamToIterativeReader extends OutputStream {
    private static byte UNINITIALISED = 0;
    private static byte OPEN = 1;
    private static byte CLOSED = 2;
    private boolean isDetached;

    /** Internal Queue to hold bytes written */
    protected byte q[];
    /** Internal Queue variables */
    protected int qBegin, qEnd, qSize;
    private int minReadSize, readCushion;
    private IterativeReader reader;
    private final static int DEF_BUF_SIZE = 3072;
    private byte openFlag = UNINITIALISED;
    private byte oneByteArr[] = new byte[1];
    private ReaderInputStream readerStream;

    private class ReaderInputStream extends InputStream implements ByteCounter
    {
        private byte oneByteArr[] = new byte[1];
        public byte writeBuf[];
        public int writeBufPos, writeBufRemain = 0;
        private int counterArr[];
        private boolean upMode;
        private long totalBytes = 0;

        public void setCounter(int counterArr[], boolean upMode)
        {
            int i = counterArr[0]; // Good if an exception is detected
            this.counterArr = counterArr;
            this.upMode = upMode;
        }

        public long getTotalBytes()
        {
            return totalBytes;
        }

        public void setRequestBuf(byte b[], int off, int len)
        {
            writeBuf = b;
            writeBufPos = off;
            writeBufRemain = len;
        }

        private int readOrSkip(byte b[], int off, int len) throws IOException
        {
            if(openFlag == UNINITIALISED || isDetached)
                throw new IOException("IterativeReader not set or has been detached");

            int qReadSize = qSize;
            int remain;

            if(qReadSize > len)
                qReadSize = len;

            // dequeue(q, b, off, qReadSize)
            if(qReadSize > 0)
            {
                remain = q.length - qBegin;

                if(remain > qReadSize)
                    remain = qReadSize;

                if(b != null)
                {
                    System.arraycopy(q, qBegin, b, off, remain);
                    off += remain;
                }

                qBegin += remain;

                if(qBegin == q.length)
                    qBegin = 0;

                remain = qReadSize - remain;

                if(remain > 0)
                {
                    if(b != null)
                    {
                        System.arraycopy(q, qBegin, b, off, remain);
                        off += remain;
                    }
                    qBegin += remain;
                }

                qSize -= qReadSize;

                if(qSize == 0)
                {
                    qBegin = 0;
                    qEnd = 0;
                }
            }
            // End dequeue

            remain = len - qReadSize;

            if(remain > 0)
            {
                if(remain > writeBufRemain)
                {
                    if(openFlag == CLOSED)
                        remain = writeBufRemain>0?writeBufRemain:0;
                    else if(b != null)
                        throw new IOException("Iterative Reader attempting to read beyond buffer end. Need larger read cushion?");
                }

                if(b != null)
                    System.arraycopy(writeBuf, writeBufPos, b, off, remain);

                writeBufPos += remain;
                writeBufRemain -= remain;
            }

            int retVal = qReadSize + remain;

            if(retVal > 0)
            {
                totalBytes += retVal;
                if(counterArr != null)
                {
                    if(upMode)
                        counterArr[0] += retVal;
                    else
                        counterArr[0] -= retVal;
                }
            }

            return retVal;
        }

        public int read(byte b[], int off, int len) throws IOException
        {
            if(len < 0)
                throw new IndexOutOfBoundsException("Negative Length Read attempted, len = " + len);
            byte b1 = b[off], b2 = b[off + len -1];

            int retVal = readOrSkip(b, off, len);

            if(retVal == 0)
                retVal = -1;

            return retVal;
        }

        public long skip(long n) throws IOException
        {
            long retVal = 0;

            if(n < 0)
                n = 0;

            retVal = readOrSkip(null, 0, (int)n);

            return retVal;
        }

        public int read() throws IOException
        {
            int retVal = -1;

            if(read(oneByteArr) == 1)
                retVal = (oneByteArr[0] & 255);

            return retVal;
        }

        /**
         * Does not strictly conform to InputStream spec since it always returns
         * atleast 1 unless the end of file is reached.
         **/
        public int available() throws IOException
        {
            int retVal = qSize;
            if(retVal < 1 && openFlag != CLOSED)
                retVal = 1;

            return retVal;
        }

        public void close() throws IOException
        {
            isDetached = true;
            q = null;
        }
    }

    /**
     * Creates an OutStreamToIterativeReader Object. This Object can be used as
     * an OutputStream. However an IterativeReader must be set before using the
     * stream.
     * @param bufSize Buffer Size
     * @param minReadSize Minimum Read request size for the IterativeRead's
     * nextRead() call
     * @param readCushion Bytes by which the actual bytes read may exceed the
     * requested number of bytes for the ItertativeReader's nextRead() call. If
     * the nextRead() call overshoots this limit also then an empty buffer
     * results which is flagged as an IOException for the reader's InputStream.
     */
    public OutStreamToIterativeReader(int bufSize, int minReadSize,
                                       int readCushion)
    {
        if(minReadSize <= 0)
            minReadSize = 1;
        if(readCushion <= 0)
            readCushion = 1;

        int minBufSize = minReadSize + readCushion + 512;
        if(bufSize < minBufSize)
            bufSize = minBufSize;
        q = new byte[bufSize];
        qBegin = 0;
        qEnd = 0;
        qSize = 0;
        this.minReadSize = minReadSize;
        this.readCushion = readCushion;
        isDetached = false;
    }

    /**
     * Creates an OutStreamToIterativeReader Object. This Object can be used as
     * an OutputStream. However an IterativeReader must be set before using the
     * stream. Uses a a buffer size of 3072, and a minReadSize and readCushion
     * of 1024 bytes.
     * @see #OutStreamToIterativeReader(int,int,int)
     */
    public OutStreamToIterativeReader()
    {
        this(DEF_BUF_SIZE, 1024, 1024);
    }

    /**
     * Gets the InputStream for use by the IterativeReader. This class's write
     * method fills the buffer for use by this InputStream's read. Closing
     * this stream is equivalent to the nextRead of the iterativeReader
     * returning IterativeReader.STOP, in which case IterativeReader is detached
     * and subsequent writes to the OutStreamToIterativeReader Object are
     * ignored.
     * @return Stream for use by IterativeReader. Note that the returned Object
     * implements {@link ByteCounter} and can be cast to a ByteCounter for
     * the IterativeReader to keep track of the number of bytes read.
     * @see #setIterativeReader(IterativeReader)
     */
    public InputStream getReaderInputStream()
    {
        if(readerStream == null)
            readerStream = new ReaderInputStream();
        if(reader != null &&
           openFlag == UNINITIALISED)
            openFlag = OPEN;
        return readerStream;
    }

    /**
     * Sets the IterativeReader for this OutStreamToIterativeReader. This must
     * be called before writing data to this stream.
     * @param iterativeReader IterativeReader to call to read data. The
     * iterativeReader must write to the InputStream got by the
     * getReaderInputStream() call.
     * @see #getReaderInputStream()
     */
    public void setIterativeReader(IterativeReader iterativeReader)
    {
        if(iterativeReader == null)
            throw new NullPointerException("Reader is null");
        if(readerStream != null &&
           openFlag == UNINITIALISED)
            openFlag = OPEN;

        reader = iterativeReader;
    }

    /**
     * Write method of the OutStreamToIterativeReader.
     * See the documentation of OutputStream.write(..) for more information.
     * This writes data to fill the internal buffer and the calls the
     * iterativeReader's nextRead metho to read the buffered data.
     * @exception IOException If the nextRead() call of the iterativeReader
     * throws an IOException
     * @see IterativeReader#nextRead(int)
     */
    public void write(byte b[], int off, int len) throws IOException
    {
        if(openFlag != OPEN)
            throw new IOException("Stream is closed or IterativeReader not set yet");
        int excessSkip = readerStream.writeBufRemain;

        if(excessSkip < 0)
        {
            len += excessSkip;
            off -= excessSkip;
            excessSkip = len>=0?0:len;
            readerStream.writeBufRemain = excessSkip;
        }

detached:
        if(len > 0)
        {
            // Do not do anything if readerStream detached
            if(isDetached)
                break detached;
            int curSize = qSize + len;
            if(curSize > q.length)
            {
                int readSize = curSize - readCushion;
                readerStream.writeBuf = b;
                readerStream.writeBufPos = off;
                readerStream.writeBufRemain = len;
                do
                    if(reader.nextRead(readSize) !=
                       IterativeReader.CONTINUE)
                    {
                        readerStream.close();
                        break detached;
                    }
                while( (readSize = qSize + readerStream.writeBufRemain -
                                    readCushion) >  minReadSize);

                off = readerStream.writeBufPos;
                len = readerStream.writeBufRemain;
                readerStream.writeBufRemain = 0;
            }

            // enqueue(q, b, off, len);
            if(len > 0)
            {
                int remain = q.length - qEnd;

                if(remain > len)
                    remain = len;

                System.arraycopy(b, off, q, qEnd, remain);

                off += remain;
                qEnd += remain;

                if(qEnd >= q.length)
                    qEnd = 0;

                remain = len - remain;

                if(remain > 0)
                {
                    System.arraycopy(b, off, q, qEnd, remain);
                    qEnd += remain;
                }

                qSize += len;
            }
        }
    }

    /**
     * Write method of the OutStreamToIterativeReader.
     * See the documentation of OutputStream.write(..) for more information.
     * This writes data to fill the internal buffer and the calls the
     * iterativeReader's nextRead method to read the buffered data.
     * @exception IOException If the nextRead() call of the iterativeReader
     * throws an IOException
     * @see IterativeReader#nextRead(int)
     */
    public void write(int b) throws IOException
    {
        oneByteArr[0] = (byte)(b & 255);
        write(oneByteArr, 0, 1);
    }

    /**
     * Closes the OutStreamToIterativeReader.
     * See the documentation of OutputStream.write(..) for more information.
     * This flags the end of the internal buffer data as end of file. Then it
     * calls the iterativeReader's nextRead method till it returns
     * IterativeReader.STOP.
     * @exception IOException If the nextRead() call of the iterativeReader
     * throws an IOException
     * @see IterativeReader#nextRead(int)
     */
    public void close() throws IOException
    {
        openFlag = CLOSED;
        int curSize;
        if(!isDetached)
          while( (curSize = qSize + readerStream.writeBufRemain) > 0)
            if(reader.nextRead(curSize) != IterativeReader.CONTINUE)
            {
                readerStream.close();
                break;
            }
    }

    /**
     * Returns if the IterativeReader attached to this
     * OutStreamToIterativeReader has detached.
     * @return True if the reader has detached, false otherwise.
     */
    public boolean isReaderDetached()
    {
        return isDetached;
    }
}
