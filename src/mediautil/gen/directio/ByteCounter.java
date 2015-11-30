/* MediaUtil LLJTran - $RCSfile: ByteCounter.java,v $
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
 *  $Id: ByteCounter.java,v 1.3 2005/09/30 21:23:18 drogatkin Exp $
 *
 */
package mediautil.gen.directio;

import java.io.InputStream;

/**
 * This Interface provides for tracking the number of bytes read/written.
 * The Stream given by directio Utility classes for use by a class implementing
 * IterativeReader/IterativeWriter implements this Interface so that the number
 * of bytes read/written can easily be tracked without tracking each
 * read/write/skip call.<p>
 *
 * The method to initialize read/write of the class implementing
 * IterativeReader or IterativeWriter can check if the Stream is an instance of
 * this Interface and setup the counter if it is.
 *
 * @author Suresh Mahalingam (msuresh@cheerful.com)
 */
public interface ByteCounter {
     /**
      * This increments/decrements the supplied integer counter as and when
      * bytes are read/written. This method is recommended as it needs to be
      * called only once to specify the counter during initialization of
      * Read/Write. The initialization could check if the Supplied Stream is an
      * instance of this Interface and the setup the counters. If not the
      * nextRead/nextWrite call will read/write till the end since the counter
      * will not change which is okay.
      *
      * @param counterArr Array countaining the counter. counterArr[0] will be
      * incremented/decremented by the number of bytes as and when bytes are
      * read/written/skipped. counterArr[0] is not changed otherwise. The
      * application using the counter is free to reset it. For example an
      * IterativeReader can initialize the counterArr[0] to number of bytes to
      * be read at the beginning of the nextRead call and can check if
      * counterArr[0] &gt; 0 to determine if the required bytes have been read.
      * @param upMode Pass as true to indicate the counter is to be incremented.
      * False if it is to be decremented.
      */
     public void setCounter(int counterArr[], boolean upMode);

    /**
     * This method returns the Total Number of Bytes read/written since the
     * Creation of the Stream. This is mainly for information
     */
    public long getTotalBytes();
}
