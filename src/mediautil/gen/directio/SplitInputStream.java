/* MediaUtil LLJTran - $RCSfile: SplitInputStream.java,v $
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
 *  $Id: SplitInputStream.java,v 1.3 2005/09/30 21:23:18 drogatkin Exp $
 *
 */
package mediautil.gen.directio;

import java.io.*;

import mediautil.gen.BasicIo;

/**
 * This class enables the Sharing of an InputStream (Say from a file) by One or
 * more IterativeReaders with a main reader. The main reader is any reader like
 * say an ImageReader for reading images.<p>
 *
 * This is accomplished by using a common buffer. The read/skip calls on this
 * class by the main reader calls nextRead() on the SubStreams to consume the
 * buffered data before the buffer is filled again.
 * @see IterativeReader
 *
 * @author Suresh Mahalingam (msuresh@cheerful.com)
 */
public class SplitInputStream extends FilterInputStream {

    private byte q[];
    private final static int DEF_BUF_SIZE = 5120;
    private final static int DEF_INC_SIZE = 1024;
    private byte oneByteArr[] = new byte[1];
    private int bufSize, incSize, qEnd, q2nd;
    private boolean eofFlag;
    private int qPtrs[];
    private int subReaderIds[];
    private IterativeReader codes[];
    private SubStream subStreams[];
    private byte requestBuf[];
    private int requestPos, requestRemain;
    private int curReaderId;
    private int maxReaderId;
    private int maxBufSize, maxBufUsage;

    private class SubStream extends InputStream implements ByteCounter
    {
        public SplitInputStream parentThis;
        private byte oneByteArr[] = new byte[1];
        private int readerNo, readerId;
        public int minReadSize, readCushion;
        public boolean closedFlag;
        private int counterArr[];
        private boolean upMode;
        private long totalBytes;

        public SubStream(SplitInputStream parentThis, int minReadSize,
                         int readCushion)
        {
            this.parentThis = parentThis;
            this.readCushion = readCushion;
            this.minReadSize = minReadSize;
            closedFlag = false;
            readerNo = -1;
            readerId = 0;
            totalBytes = 0;
            counterArr = null;
        }

        public SubStream(SplitInputStream parentThis)
        {
            this(parentThis, 750, 750);
        }

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

        private String validate()
        {
            String retVal = null;
            if(!closedFlag)
            {
                if(readerNo < 0)
                    retVal = "Attempt to read from an invalid or unattached substream";
                else if(readerId != curReaderId)
                    retVal = "Substream Context Error: Iterative code attached as id " + curReaderId + " attempting to read from substream with id " + readerId;
            }

            return retVal;
        }

        public int read(byte b[], int off, int len) throws IOException
        {
            // String ioError = validate(); Inlining validate for performance
            String ioError = null;
            if(!closedFlag)
            {
                if(readerNo < 0)
                    ioError = "Attempt to read from an invalid or unattached substream";
                else if(readerId != curReaderId)
                    ioError = "Substream Context Error: Iterative code attached as id " + curReaderId + " attempting to read from substream with id " + readerId;
                if(ioError != null)
                    throw new IOException(ioError);
            }

            if(len < 0)
                throw new IndexOutOfBoundsException("Negative Length Read attempted, len = " + len);
            byte b1 = b[off], b2 = b[off + len -1];
            if(len == 0)
                return 0;

            if(closedFlag)
                return -1;

            if(q == null)
                q = new byte[bufSize];

            // Begin q.dequeue(in, readerNo, b, off, len)
            int remain = len;
            int qReadSize = qEnd - qPtrs[readerNo];
            if(qReadSize > len)
                qReadSize = len;
            if(qReadSize > 0)
            {
                System.arraycopy(q, qPtrs[readerNo], b, off, qReadSize);
                qPtrs[readerNo] += qReadSize;
                off += qReadSize;
                remain -= qReadSize;
            }

            int retVal = len;

            if(remain > 0)
            {
                if(!eofFlag)
                    remain -= fillAndDequeue(readerNo, b, off, remain);
                retVal -= remain;
                if(retVal <= 0)
                {
                    retVal = -1;
                    detachSubReaderNo(readerNo-1);
                }
            }
            // End q.dequeue(in, readerNo, b, off, len)

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

        public int read() throws IOException
        {
            int retVal = -1;
            if(read(oneByteArr, 0, 1) == 1)
                retVal = oneByteArr[0] & 255;
            return retVal;
        }

        /**
         * Does not strictly conform to InputStream spec since it always returns
         * n unless it is eof.
         **/
        public long skip(long n) throws IOException
        {
            String ioError = validate();
            if(ioError != null)
                throw new IOException(ioError);

            int retVal = (int)n;

            if(closedFlag || retVal < 0)
                retVal = 0;

            qPtrs[readerNo] += retVal;

            totalBytes += retVal;
            if(counterArr != null)
            {
                if(upMode)
                    counterArr[0] += retVal;
                else
                    counterArr[0] -= retVal;
            }

            return retVal;
        }

        /**
         * Does not strictly conform to InputStream spec since it always returns
         * atleast 1 unless the end of file is reached.
         **/
        public int available() throws IOException
        {
            String ioError = validate();
            if(ioError != null)
                throw new IOException(ioError);

            int retVal = 0;

            if(!closedFlag)
            {
                retVal = qEnd - qPtrs[readerNo];
                if(retVal <= 0)
                    retVal = eofFlag?0:1;
            }

            return retVal;
        }

        public void close() throws IOException
        {
            String ioError = validate();
            if(ioError != null)
                throw new IOException(ioError);

            if(readerNo > 0)
                detachSubReaderNo(readerNo-1);
        }
    }

    // This is inlined in read calls to improve performance. Is it going to?
    private int dequeue(int readerNo, byte b[], int off, int n)
    {
        int qReadSize = qEnd - qPtrs[readerNo];
        if(qReadSize > n)
            qReadSize = n;
        if(qReadSize > 0)
        {
            if(b != null)
                System.arraycopy(q, qPtrs[readerNo], b, off, qReadSize);
            qPtrs[readerNo] += qReadSize;
        }
        else
            qReadSize = 0;

        return qReadSize;
    }

    private void reallocBuf(int newLen)
    {
        int offset = findQBegin(null);
        byte newQ[] = new byte[newLen];
        int copyLen = qEnd - offset;

        if(copyLen > 0)
            System.arraycopy(q, offset, newQ, 0, copyLen);
        else
            offset = qEnd;
        q = newQ;
        int i;
        for(i = 0; i < qPtrs.length; ++i)
            qPtrs[i] -= offset;
        qEnd -= offset;
        q2nd -= offset;
        if(newLen > maxBufSize)
            maxBufSize = newLen;
    }

    private int fillAndDequeue(int readerNo, byte b[], int off, int len)
        throws IOException
    {
        // Caller would have done basic dequeue, but still to be sure.
        int qReadSize = dequeue(readerNo, b, off, len);
        int remain = len - qReadSize;
        off += qReadSize;
        int qPtr = qPtrs[readerNo];
        int i;

        // Real Tough fillAndDequeue
        if(remain > 0 && !eofFlag)
        {
            // Since dequeue done skipLen >= 0
            int skipLen = qPtr - qEnd;
            int fillSize = remain + skipLen;
            int reqSize = fillSize + (qEnd - q2nd);
            int readLen, actualRead;
            if(q2nd < qPtr)
                skipLen = q2nd - qEnd;
            if(q2nd > qEnd && fillSize > bufSize
               && (reqSize <= bufSize || (reqSize - bufSize -1)/incSize <
                                          (fillSize - bufSize -1)/incSize))
            {
                int newQStart = qPtr + remain;
                if(newQStart > q2nd)
                    newQStart = q2nd;
                int dFillLen = newQStart - qEnd;
                int dRemain = dFillLen;
                readLen = 0;
                if(requestBuf != null)
                {
                    readLen = requestRemain;
                    if(readLen > dRemain)
                        readLen = dRemain;
                }
                if(readLen > 0)
                {
                    actualRead = BasicIo.read(in, requestBuf, requestPos, readLen,
                                      readLen);
                    if(actualRead < readLen)
                        eofFlag = true;
                    int xferLen = actualRead - skipLen;
                    if(xferLen > 0)
                    {
                        if(b != null)
                            System.arraycopy(requestBuf, requestPos + skipLen,
                                         b, off, xferLen);
                        skipLen = 0;
                        off += xferLen;
                        remain -= xferLen;
                    }
                    else
                        skipLen = -xferLen;
                    dRemain -= actualRead;
                }

                if(skipLen > 0 && !eofFlag)
                {
                    actualRead = (int) BasicIo.skip(in, skipLen);
                    if(actualRead < skipLen)
                        eofFlag = true;
                    skipLen -= actualRead;
                    dRemain -= actualRead;
                }

                if(dRemain > 0 && !eofFlag)
                {
                    if(b!= null)
                        actualRead = BasicIo.read(in, b, off, dRemain, dRemain);
                    else
                        actualRead = (int) BasicIo.skip(in, dRemain);
                    if(actualRead < dRemain)
                        eofFlag = true;
                    off += actualRead;
                    remain -= actualRead;
                    dRemain -= actualRead;
                }

                readLen = dFillLen - dRemain;
                qEnd += readLen;
                if(readLen > requestRemain)
                    readLen = requestRemain;
                requestPos += readLen;
                requestRemain -= readLen;
                qPtr += len - qReadSize - remain;
            }

            if(requestBuf == null && skipLen > 0 && !eofFlag)
            {
                actualRead = (int) BasicIo.skip(in, skipLen);
                if(actualRead < skipLen)
                    eofFlag = true;
                readLen = requestRemain;
                if(readLen > actualRead)
                    readLen = actualRead;
                requestPos += readLen;
                requestRemain -= readLen;
                qEnd += actualRead;
            }

            qPtrs[readerNo] = qPtr;
            if(remain > 0 && !eofFlag)
            {
                int newQBegin = q2nd;
                if(qEnd < q2nd)
                    newQBegin = qEnd;
                fillSize = remain + (qPtr - qEnd);
                reqSize = fillSize + (qEnd - newQBegin);
                if(reqSize > maxBufUsage)
                    maxBufUsage = reqSize;
                if(reqSize > q.length)
                    reallocBuf(reqSize + incSize - 1 -
                               (reqSize - 1 - bufSize)%incSize);
                else
                {
                    if(qEnd > newQBegin && newQBegin > 0)
                        System.arraycopy(q, newQBegin, q, 0, qEnd - newQBegin);
                    i = 0;
                    do
                    {
                        qPtrs[i] -= newQBegin;
                        ++i;
                    } while(i < qPtrs.length);
                    qEnd -= newQBegin;
                    q2nd -= newQBegin;
                }

                int maxFillSize = bufSize - qEnd;
                if(maxFillSize < fillSize)
                    maxFillSize = fillSize;

                actualRead = BasicIo.read(in, q, qEnd, fillSize, maxFillSize);
                if(actualRead < fillSize)
                    eofFlag = true;
                readLen = requestRemain;
                if(readLen > actualRead)
                    readLen = actualRead;
                if(readLen > 0)
                {
                    if(requestBuf != null)
                        System.arraycopy(q, qEnd, requestBuf, requestPos, readLen);
                    requestPos += readLen;
                    requestRemain -= readLen;
                }
                qEnd += actualRead;
                // Finally Dequeue bytes read
                remain -= dequeue(readerNo, b, off, remain);
            }
        }

        return len - remain;
    }

    private int findQBegin(int qBeginIndex[])
    {
        int i, qBegin, index = -1;
        if(qPtrs.length == 0)
            qBegin = 0;
        else
        {
            qBegin = 0x7FFFFFFF;
            i = 0;
            do
                if(qPtrs[i] < qBegin)
                {
                    qBegin = qPtrs[i];
                    index = i;
                }
            while(++i < qPtrs.length);
        }

        if(qBeginIndex != null)
            qBeginIndex[0] = index;

        return qBegin;
    }

    private void init(InputStream mainStream, int bufSize, int incSize)
    {
        if(bufSize < 2048)
            bufSize = 2048;
        if(incSize < 512)
            incSize = 512;

        this.bufSize = bufSize;
        maxBufSize = bufSize;
        this.incSize = incSize;
        qPtrs = new int[1];
        qPtrs[0] = 0;
        qEnd = 0;
        q = null;
        eofFlag = false;
        subReaderIds = new int[0];
        subStreams = new SubStream[0];
        codes = new IterativeReader[0];
        curReaderId = -1;
        maxReaderId = 0;
    }

    /**
     * Creates a new SplitInputStream Object. This can be used as an Input
     * Stream.
     * @param mainStream The main input Stream, say a FileInputStream. Note that
     * this need not be buffered since SplitInputStream itself does buffering.
     * The Main Object (Say ImageReader) will read from the newly constructed
     * SplitInputStream object after attaching zero or more IterativeReaders.
     * The SplitInputStream and Sub Streams see the same data as in mainStream.
     * @param bufSize The Initial BufferSize. This will be increased if one of
     * the Sub Readers read way beyond the requested Size
     * @param incSize The buffer Size is always increase/decreased in multiples
     * of incSize
     */
    public SplitInputStream(InputStream mainStream, int bufSize, int incSize)
    {
        super(mainStream);
        init(mainStream, bufSize, incSize);
    }

    /**
     * Creates a new SplitInputStream Object with default parameters. The
     * bufSize used is 5120 bytes, incSize used is 1024.
     * @param mainStream The main input Stream, say a FileInputStream. Note that
     * this need not be buffered since SplitInputStream itself does buffering.
     * The Main Object (Say ImageReader) will read from the newly constructed
     * SplitInputStream object after attaching zero or more IterativeReaders.
     * The SplitInputStream and Sub Streams see the same data as in mainStream.
     */
    public SplitInputStream(InputStream mainStream)
    {
        this(mainStream, DEF_BUF_SIZE, DEF_INC_SIZE);
    }

    /**
     * Creates a Sub Stream for use by an instance of IterativeReader. The
     * SubStream sees the same data as in the mainStream. The Buffer Size of the
     * SplitInputStream if less than (minReadSize+readCushion+512) is resized to
     * that value.
     * @param minReadSize The minimum Number of Bytes that should be passed to
     * the nextRead method of the IterativeReader. This value is limited to
     * atleast 512.
     * @param readCushion The maximum number of bytes by which a nextRead() call
     * to the subReader may overshoot the requested number of bytes. If a
     * nextRead() call exceeds the limit beyond readCushion a buffer expansion
     * may result which affects performance. This value is limited to atleast
     * 512.
     * @return An InputStream which also implements ByteCounter which can be
     * used by the IterativeReader to keep track of the bytes read/skipped. This
     * Input Stream must be used directly (without creating a
     * BufferedInputStream) by an Object implementing IterativeReader. The
     * attachSubReader() method must be called with the IterativeReader Object
     * and this return value after this call.<p>
     *
     * Please note that the returned Input Stream does not strictly confirm to
     * jdk's InputStream spec in the following:
     * <ul>
     *   <li> The Skip call always skips the required number of bytes unless the
     *   end of file has already been detected. This is because for skip just
     *   the internal q pointers are incremented without actually checking for
     *   the end of file. This is to avoid possible unnecessary buffering of
     *   data.
     *   <li> available always returns atleast 1 unless the end of file has been
     *   detected
     * </ul>
     * @see IterativeReader
     * @see #attachSubReader(IterativeReader, InputStream)
     * @see ByteCounter
     */
    public InputStream createSubStream(int minReadSize, int readCushion)
    {
        if(minReadSize < 512)
            minReadSize = 512;
        if(readCushion < 512)
            readCushion = 512;

        return new SubStream(this, minReadSize, readCushion);
    }

    /**
     * Creates a Sub Stream for use by an instance of IterativeReader. A minimum
     * read Size of 750 and a read Cushion of 750 is used.
     * @return An InputStream which also implements ByteCounter which can be
     * used by the IterativeReader to keep track of the bytes read/skipped. This
     * Input Stream must be used directly (without creating a
     * BufferedInputStream) by an Object implementing IterativeReader. The
     * attachSubReader() method must be called with the IterativeReader Object
     * and this return value after this call.<p>
     *
     * Please note that the returned Input Stream does not strictly confirm to
     * jdk's InputStream spec in the following:
     * <ul>
     *   <li> The Skip call always skips the required number of bytes unless the
     *   end of file has already been detected. This is because for skip just
     *   the internal q pointers are incremented without actually checking for
     *   the end of file. This is to avoid possible unnecessary buffering of
     *   data.
     *   <li> available always returns atleast 1 unless the end of file has been
     *   detected
     * </ul>
     * @see IterativeReader
     * @see #attachSubReader(IterativeReader,InputStream)
     * @see ByteCounter
     */
    public InputStream createSubStream()
    {
        return new SubStream(this);
    }

    /**
     * This call must follow a createSubStream() call to attach an
     * IterativeReader to read from the subStream created. This subReader is
     * detached by a detachSubReader(..) call or when the nextRead() method
     * returns IterativeReader.STOP or when the subStream is closed.
     * @param code The IterativeReader to share the main Input. The nextRead()
     * method is called on code by the read/skip calls on SplitInputStream as
     * and when required.
     * @param subStream The SubStream which was created using createSubStream()
     * call and not yet attached. The nextRead() calls to the IterativeReader
     * specified by the parameter <b>code</b> must read from this subStream.
     * @return An integer representing a SubReader Id. This id should be passed
     * if a stream is required to be detached.
     * @see IterativeReader#nextRead(int)
     * @see #createSubStream(int,int)
     * @see #detachSubReader(int)
     */
    public int attachSubReader(IterativeReader code, InputStream subStream)
    {
        String err = null;
        SubStream sub = null;

        if(code == null)
            throw new NullPointerException("code is NULL");

        if(!(subStream instanceof SubStream))
            err = "subStream passed was not created using createSubStream call";
        else {
            sub = (SubStream) subStream;
            if(sub.parentThis != this)
                err = "subStream was created from another SplitInputStream instance";
            else if(sub.readerNo > 0 || sub.readerId > 0)
                err = "subStream already in use";
        }

        if(err != null)
            throw new RuntimeException(err);

        int minBufSize = sub.minReadSize + sub.readCushion + 512;
        if(minBufSize > bufSize)
        {
            if(q != null)
                reallocBuf(minBufSize);
            bufSize = minBufSize;
        }

        int newSubReaderIds[] = new int[subReaderIds.length+1];
        IterativeReader newCodes[] = new IterativeReader[codes.length+1];
        SubStream newSubStreams[] = new SubStream[subStreams.length+1];
        System.arraycopy(subReaderIds, 0, newSubReaderIds, 0, subReaderIds.length);
        System.arraycopy(codes, 0, newCodes, 0, codes.length);
        System.arraycopy(subStreams, 0, newSubStreams, 0, subStreams.length);
        newCodes[codes.length] = code;
        newSubStreams[subStreams.length] = sub;
        maxReaderId++;
        int retVal = maxReaderId;
        newSubReaderIds[subReaderIds.length] = retVal;
        sub.readerNo = newSubStreams.length;
        sub.readerId = retVal;
        int newQPtrs[] = new int[qPtrs.length + 1];
        System.arraycopy(qPtrs, 0, newQPtrs, 0, qPtrs.length);
        int i;
        newQPtrs[qPtrs.length] = findQBegin(null);
        qPtrs = newQPtrs;

        subReaderIds = newSubReaderIds;
        codes = newCodes;
        subStreams = newSubStreams;

        return retVal;
    }

    /**
     * Detaches a Sub Reader and its Stream. Note that it is not necessary to
     * call this method explicitly as the Sub Reader is automatically detached
     * when the nextRead() returns IterativeReader.STOP or when the subStream is
     * closed.
     * @param subReaderId Id to detach
     * @see #attachSubReader(IterativeReader,InputStream)
     */
    public void detachSubReader(int subReaderId)
    {
        int i;

        for(i=0; i < subReaderIds.length; i++)
            if(subReaderIds[i] == subReaderId)
                break;

        if(i >= subReaderIds.length)
            throw new RuntimeException("SubReaderId " + subReaderId + " Not found for detaching from SplitInputStream");

        detachSubReaderNo(i);
    }

    private void detachSubReaderNo(int subReaderNo)
    {
        int i, j, readerNo = subReaderNo+1;
        codes[subReaderNo] = null;
        SubStream detachedStream = subStreams[subReaderNo];
        detachedStream.readerNo = -1;
        detachedStream.readerId = 0;
        detachedStream.closedFlag = true;
        subStreams[subReaderNo] = null;
        int newSubReaderIds[] = new int[subReaderIds.length-1];
        IterativeReader newCodes[] = new IterativeReader[codes.length-1];
        SubStream newSubStreams[] = new SubStream[subStreams.length-1];
        System.arraycopy(subReaderIds, 0, newSubReaderIds, 0, subReaderNo);
        System.arraycopy(codes, 0, newCodes, 0, subReaderNo);
        System.arraycopy(subStreams, 0, newSubStreams, 0, subReaderNo);
        System.arraycopy(subReaderIds, subReaderNo+1, newSubReaderIds, subReaderNo, newSubReaderIds.length-subReaderNo);
        System.arraycopy(codes, subReaderNo+1, newCodes, subReaderNo, newCodes.length-subReaderNo);
        System.arraycopy(subStreams, subReaderNo+1, newSubStreams, subReaderNo, newSubStreams.length-subReaderNo);
        // q.detach(in, subReaderNo+1);
        int newQPtrs[] = new int[qPtrs.length - 1];
        for(i = 0, j = 0; i < qPtrs.length; ++i)
            if(i != readerNo)
            {
                newQPtrs[j] = qPtrs[i];
                ++j;
            }
        codes = newCodes;
        subStreams = newSubStreams;
        qPtrs = newQPtrs;
        subReaderIds = newSubReaderIds;
        for(i = 0; i < subStreams.length; i++)
            if(subStreams[i].readerNo > readerNo)
                subStreams[i].readerNo--;
    }

    private boolean isSkipCall = false;

    /**
     * Read method of the SplitInputStream.
     * See the documentation of InputStream.read(..) for more information. This
     * reads from the underlying InputStream and buffers the data for use by the
     * subReaders.
     * @exception IOException If the underlying stream throws an IOException or
     * if the nextRead() call of a subReader throws an IOException
     */
    public int read(byte b[], int off, int len) throws IOException
    {
        boolean isSkip = isSkipCall;
        isSkipCall = false;

        byte b1, b2;

        if(!isSkip)
        {
            if(len < 0)
                throw new IndexOutOfBoundsException("Negative Length Read attempted, len = " + len);
            b1 = b[off];
            b2 = b[off + len -1];
        }
        if(len <= 0)
            return 0;

        if(q == null)
            q = new byte[bufSize];

        int remain = len;

        // Begin q.dequeue(in, 0, b, off, remain)
        int qReadSize = qEnd - qPtrs[0];
        if(qReadSize > remain)
            qReadSize = remain;
        if(qReadSize > 0)
        {
            if(b != null)
                System.arraycopy(q, qPtrs[0], b, off, qReadSize);
            remain -= qReadSize;
            off += qReadSize;
        }
        // End q.dequeue(in, 0, b, off, remain)
        qPtrs[0] += len;

        requestBuf = b;
        requestPos = off;
        requestRemain = remain;

        int i, testPtr, indexArr[] = null;

        maxBufUsage = 0;
        while(requestRemain > 0 && (qPtrs.length > 1 || !eofFlag))
        {
            if(indexArr == null)
                indexArr = new int[1];
            int newQBegin = findQBegin(indexArr);
            int newQ2nd = 0x7FFFFFFF;
            int newQBeginIndex = indexArr[0], newQ2ndIndex = 0;

            if(qPtrs.length > 1)
            {
                i = 0;
                do
                {
                    if(i != newQBeginIndex && (testPtr = qPtrs[i])
                                              <= newQ2nd)
                    {
                        newQ2nd = testPtr;
                        newQ2ndIndex = i;
                    }
                    ++i;
                } while(i < qPtrs.length);
            }

            if(newQBegin >= qEnd)
            {
                i = 0;
                if(eofFlag)
                    // Wrapup
                    do {
                        IterativeReader reader = codes[0];
                        SubStream stream = subStreams[0];
                        curReaderId = subReaderIds[0];
                        reader.nextRead(stream.minReadSize);
                        curReaderId = -1;
                        if(!stream.closedFlag)
                            detachSubReaderNo(0);
                    }  while(codes.length > 0);
                else
                {
                    remain = requestRemain;
                    requestRemain = 0;
                    // Should be = qEnd.
                    qPtrs[0] -= remain;
                    qReadSize = bufSize;
                    if(qReadSize > remain)
                        qReadSize = remain;
                    q2nd = newQBegin;
                    qReadSize = fillAndDequeue(0, b, requestPos, qReadSize);
                    requestPos += qReadSize;
                    requestRemain = remain - qReadSize;
                    if(!eofFlag)
                        qPtrs[0] += requestRemain;
                }
            }
            else
            {
                q2nd = newQ2nd;
                int headStreamNo = newQBeginIndex - 1;
                IterativeReader headReader = codes[headStreamNo];
                SubStream headStream = subStreams[headStreamNo];
                int numBytes = qEnd - newQBegin - headStream.readCushion;
                SubStream nextStream = null;
                if(newQ2ndIndex > 0)
                    nextStream = subStreams[newQ2ndIndex-1];
                if(numBytes < headStream.minReadSize || newQ2ndIndex == 0
                   || (qEnd - newQ2nd - nextStream.readCushion)
                      < nextStream.minReadSize)
                    numBytes += bufSize - (qEnd - newQ2nd);
                curReaderId = subReaderIds[headStreamNo];
                int readerStatus = headReader.nextRead(numBytes);
                curReaderId = -1;
                if(!headStream.closedFlag
                   && (readerStatus != IterativeReader.CONTINUE))
                   detachSubReaderNo(headStreamNo);
            }
        }

        if(q.length > bufSize && maxBufUsage > 0 && q.length - maxBufUsage >= incSize)
        {
            int resizeLen = bufSize;
            if(maxBufUsage > bufSize)
                resizeLen = maxBufUsage + (incSize - 1 -
                            (maxBufUsage - 1 - bufSize)%incSize);
            reallocBuf(resizeLen);
        }

        int retVal = len - requestRemain;

        requestBuf = null;
        requestRemain = 0;

        if(retVal == 0)
            retVal = -1;

        return retVal;
    }

    /**
     * Read method of the SplitInputStream.
     * See the documentation of InputStream.read(..) for more information. This
     * reads from the underlying InputStream and buffers the data for use by the
     * subReaders.
     * @exception IOException If the underlying stream throws an IOException or
     * if the nextRead() call of a subReader throws an IOException
     */
    public int read() throws IOException
    {
        int retVal = -1;
        if(read(oneByteArr, 0, 1) == 1)
            retVal = oneByteArr[0] & 255;
        return retVal;
    }

    /**
     * Skip method of the SplitInputStream.
     * See the documentation of InputStream.skip(..) for more information. This
     * skips from the underlying InputStream and buffers the data for use by the
     * subReaders.
     * @exception IOException If the underlying stream throws an IOException or
     * if the nextRead() call of a subReader throws an IOException
     */
    public long skip(long n) throws IOException
    {
        long retVal = n;

        if(retVal < 0)
            retVal = 0;

        if(retVal > 0)
        {
            isSkipCall = true;
            retVal = read(null, 0, (int)retVal);
            if(retVal < 0)
                retVal = 0;
        }

        return retVal;
    }

    /**
     * Available method of the SplitInputStream.
     * See the documentation of InputStream.available(..) for more information.
     * This method does not strictly conform to InputStream spec since it always
     * returns atleast 1 unless the end of file is reached.
     */
    public int available()
    {
        int retVal = qEnd - qPtrs[0];
        if(retVal <= 0)
            retVal = eofFlag?0:1;

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

    /**
     * This should be called when the Main Object (Say ImageReader) has finished
     * reading.  This is to ensure that all the SubReaders finish reading any
     * data left in the buffer/mainStream and detach.
     */
    public void wrapup() throws IOException
    {
        int readerNo;
        while(codes.length > 0)
            skip(100000);

        q = null;
    }
}
