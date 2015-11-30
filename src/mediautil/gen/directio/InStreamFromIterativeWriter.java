/* MediaUtil LLJTran - $RCSfile: InStreamFromIterativeWriter.java,v $
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
 *  $Id: InStreamFromIterativeWriter.java,v 1.3 2005/09/30 21:23:18 drogatkin Exp $
 *
 */
package mediautil.gen.directio;

import java.io.*;

/**
 * This class enables reading off directly from an IterativeWriter. The
 * read/skip calls to this class which is to be used as an InputStream translate
 * into nextWrite() calls on the underlying IterativeWriter. Excess bytes
 * written are buffered for the next read call.<p>
 *
 * Another possible use of this class is in converting an IterativeWriter
 * processing data from an input into a FilterInputStream.
 * @see IterativeWriter
 *
 * @author Suresh Mahalingam (msuresh@cheerful.com)
 */
public class InStreamFromIterativeWriter extends InputStream {
    private static byte UNINITIALISED = 0;
    private static byte OPEN = 1;
    private static byte CLOSED = 2;

    /** Internal Byte Queue */
    protected byte q[];
    /** Internal Byte Queue variables */
    protected int qBegin, qEnd, qSize, bufSize, incSize;
    /** Internal Byte Queue variables */
    protected int minWriteSize, writeCushion;
    private InputStream in;
    private IterativeWriter writer;
    private final static int DEF_BUF_SIZE = 4096;
    private byte oneByteArr[] = new byte[1];
    private WriterOutputStream writerStream;
    private int maxBufSize;

    private class WriterOutputStream extends OutputStream implements ByteCounter
    {
        private byte oneByteArr[] = new byte[1];
        public byte requestBuf[];
        public int requestPos, requestRemaining;
        public byte openFlag = UNINITIALISED;
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
            requestBuf = b;
            requestPos = off;
            requestRemaining = len;
        }

        public void write(byte b[], int off, int len) throws IOException
        {
            if(openFlag != OPEN)
                throw new IOException("Stream is not closed or IterativeWriter not set yet");
            if(len < 0)
                throw new IndexOutOfBoundsException("Negative Length Read attempted, len = " + len);
            byte b1 = b[off], b2 = b[off + len -1];
            int lenRequired = len;

            if(requestRemaining > 0)
            {
                int copyLen = len;
                if(requestRemaining < len)
                    copyLen = requestRemaining;
                if(requestBuf != null)
                    System.arraycopy(b, off, requestBuf, requestPos, copyLen);
                requestPos += copyLen;
                requestRemaining -= copyLen;
                off += copyLen;
                len -= copyLen;
            }

            if(len > 0)
            {
                // q.enqueue(b, off, len);
                int newQSize = qSize + len;
                if(newQSize > q.length)
                    reallocBuf(newQSize + incSize - 1 -
                                        (newQSize - 1 - bufSize)%incSize);

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

                qSize = newQSize;
            }

            if(lenRequired > 0)
            {
                totalBytes += lenRequired;
                if(counterArr != null)
                {
                    if(upMode)
                        counterArr[0] += lenRequired;
                    else
                        counterArr[0] -= lenRequired;
                }
            }
        }

        public void write(int b) throws IOException
        {
            oneByteArr[0] = (byte)(b & 255);
            write(oneByteArr, 0, 1);
        }

        public void close() throws IOException
        {
            openFlag = CLOSED;
        }
    }

    /**
     * Creates an InStreamFromIterativeWriter Object. This Object can be used as
     * an InputStream. However an IterativeWriter must be set before using the
     * stream.
     * @param bufSize Buffer Size
     * @param incSize Bytes by which to increment Buffer Size. Expanding the
     * buffer will be required when the IterativeWriter's nextWrite method
     * writes too many bytes beyond the requested size.
     * @param minWriteSize Minimum Write request size for the IterativeWrite's
     * nextWrite() call
     * @param writeCushion Bytes by which the actual bytes written may exceed
     * the requested number of bytes for the ItertativeWriter's nextWrite()
     * call. If the nextWrite() call overshoots this limit also then a buffer
     * expansion may be required which affects performance.
     */
    public InStreamFromIterativeWriter(int bufSize, int incSize,
        int minWriteSize, int writeCushion)
    {
        if(bufSize < 1024)
            bufSize = 1024;
        if(incSize < 128)
            incSize = 128;
        if(minWriteSize < 256)
            minWriteSize = 256;
        if(writeCushion < 256)
            writeCushion = 256;
        int minBufSize = minWriteSize + writeCushion + 512;
        if(bufSize < minBufSize)
            bufSize = minBufSize;

        this.bufSize = bufSize;
        this.incSize = incSize;
        this.writeCushion = writeCushion;
        q = new byte[bufSize];
        qBegin = 0;
        qEnd = 0;
        qSize = 0;
        maxBufSize = bufSize;
    }

    /**
     * Creates an InStreamFromIterativeWriter Object. This Object can be used as
     * an InputStream. However an IterativeWriter must be set before using the
     * stream. Uses a buffer increment size of 512 and minWriteSize and
     * writeCushion of 750 bytes.
     * @param bufSize Buffer Size
     */
    public InStreamFromIterativeWriter(int bufSize)
    {
        this(bufSize<2048?2048:bufSize, 512, 750, 750);
    }

    /**
     * Creates an InStreamFromIterativeWriter Object. This Object can be used as
     * an InputStream. However an IterativeWriter must be set before using the
     * stream. Uses a a buffer size of 4096, buffer increment size of 512 and
     * minWriteSize and writeCushion of 750 bytes.
     */
    public InStreamFromIterativeWriter()
    {
        this(DEF_BUF_SIZE, 1024, 750, 750);
    }

    private void reallocBuf(int newLen)
    {
        byte newQ[] = new byte[newLen];
        int copyLen = q.length - qBegin;
        if(copyLen > qSize)
            copyLen = qSize;
        System.arraycopy(q, qBegin, newQ, 0, copyLen);
        int pos = copyLen;
        copyLen = qSize - copyLen;
        if(copyLen > 0)
            System.arraycopy(q, 0, newQ, pos, copyLen);
        q = newQ;
        qBegin = 0;
        qEnd = qSize;
        if(newLen > maxBufSize)
            maxBufSize = newLen;
    }

    /**
     * Gets the OutputStream for use by the IterativeWriter.
     * The data written to this stream is buffered for use by this class's
     * read/skip method. Closing this stream is equivalent to the nextRead of
     * the iterativeWriter returning IterativeReader.STOP, in which case the
     * InStreamFromIterativeWriter Object shows end of file after its buffer is
     * read.
     * @return Stream for use by IterativeWriter. Note that the returned Object
     * implements {@link ByteCounter} and can be cast to a ByteCounter for
     * the IterativeWriter to keep track of the number of bytes written.
     * @see #setIterativeWriter(IterativeWriter)
     */
    public OutputStream getWriterOutputStream()
    {
        if(writerStream == null)
            writerStream = new WriterOutputStream();
        if(writer != null &&
           writerStream.openFlag == UNINITIALISED)
            writerStream.openFlag = OPEN;
        return writerStream;
    }

    /**
     * Sets the IterativeWriter for this InStreamFromIterativeWriter. This must
     * be called before reading data from this stream.
     * @param iterativeWriter IterativeWriter to call to write data. The
     * iterativeWriter must write to the OutputStream got by the
     * getWriterOutputStream() call.
     * @see #getWriterOutputStream()
     */
    public void setIterativeWriter(IterativeWriter iterativeWriter)
    {
        if(iterativeWriter == null)
            throw new NullPointerException("Writer is null");
        if(writerStream != null &&
           writerStream.openFlag == UNINITIALISED)
            writerStream.openFlag = OPEN;

        writer = iterativeWriter;
    }

    // Skips if b is null, reads otherwise. No validation
    private int readOrSkip(byte b[], int off, int len) throws IOException
    {
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
            // writerStream.setRequestBuf(b, newOff, remain);
            writerStream.requestBuf = b;
            writerStream.requestPos = off;
            writerStream.requestRemaining = remain;
            int curSize;
            while(writerStream.openFlag == OPEN
                  && (curSize = bufSize + writerStream.requestRemaining
                                - qSize - writeCushion) > minWriteSize)
                if(writer.nextWrite(curSize) != IterativeReader.CONTINUE)
                    writerStream.close();

            if(q.length > bufSize && q.length - qSize >= incSize)
            {
                int resizeLen = bufSize;
                if(qSize > bufSize)
                    resizeLen = qSize + (incSize - 1 -
                                         (qSize - 1 - bufSize)%incSize);
                reallocBuf(resizeLen);
            }

            remain -= writerStream.requestRemaining;
        }

        return qReadSize + remain;
    }

    /**
     * Read method of the InStreamFromIterativeWriter.
     * See the documentation of InputStream.read(..) for more information. This
     * reads data from an internal buffer calling the iterativeWriter's
     * nextWrite method to fill the buffer.
     * @exception IOException If the nextWrite() call of the iterativeWriter
     * throws an IOException
     * @see IterativeWriter#nextWrite(int)
     */
    public int read(byte b[], int off, int len) throws IOException
    {
        if(len < 0)
            throw new IndexOutOfBoundsException("Negative Length Read attempted, len = " + len);
        byte b1 = b[off], b2 = b[off + len -1];
        if(len == 0)
            return 0;

        int retVal = readOrSkip(b, off, len);

        if(retVal == 0)
            retVal = -1;

        return retVal;
    }

    /**
     * Read method of the InStreamFromIterativeWriter.
     * See the documentation of InputStream.read(..) for more information. This
     * reads data from an internal buffer calling the iterativeWriter's
     * nextWrite method to fill the buffer.
     * @exception IOException If the nextWrite() call of the iterativeWriter
     * throws an IOException
     * @see IterativeWriter#nextWrite(int)
     */
    public int read() throws IOException
    {
        int retVal = -1;
        if(read(oneByteArr, 0, 1) == 1)
            retVal = oneByteArr[0] & 255;
        return retVal;
    }

    /**
     * Skip method of the InStreamFromIterativeWriter.
     * See the documentation of InputStream.skip(..) for more information. This
     * skips data from an internal buffer calling the iterativeWriter's
     * nextWrite method to fill the buffer.
     * @exception IOException If the nextWrite() call of the iterativeWriter
     * throws an IOException
     * @see IterativeWriter#nextWrite(int)
     */
    public long skip(long n) throws IOException
    {
        long retVal = 0;

        if(n < 0)
            n = 0;

        retVal = readOrSkip(null, 0, (int)n);

        return retVal;
    }

    /**
     * Available method of the SplitInputStream.
     * See the documentation of InputStream.available(..) for more information.
     * This method does not strictly conform to the InputStreams spec since it
     * always returns atleast 1 unless the IterativeWriter's OutputStream is
     * closed.
     */
    public int available() throws IOException
    {
        // int retVal = q.getQSize(0);
        int retVal = qSize;
        if(retVal < 0)
            retVal = q.length + retVal;
        if(retVal <= 0)
            retVal = writerStream.openFlag == OPEN?1:0;

        return retVal;
    }

    /**
     * Returns the max bufSize. For debugging and informational purposes.
     * @return Max Buf Size
     */
    public int getMaxBufSize()
    {
        return maxBufSize;
    }
}
