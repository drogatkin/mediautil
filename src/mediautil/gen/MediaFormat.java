/* MediaUtil MediaFormat - $RCSfile: MediaFormat.java,v $
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
 *	$Id: MediaFormat.java,v 1.7 2013/02/26 08:21:51 drogatkin Exp $
 *
 */
package mediautil.gen;

import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.net.URL;
import java.awt.Dimension;
import javax.swing.Icon;

public interface MediaFormat /*<MI>*/ extends Serializable {
	
	public static final int STILL = 1;
	
	public static final int ANIMATED = 2;
	
	public static final int AUDIO = 4;
	
	public static final int VIDEO = 8;
	
	public static final int SMELL = 16;
	
	public static final int TASTE = 32;
	
	public static final int MOVEMENT = 64;
	
	public static final int CLIMATE = 128;
	

    public MediaInfo getMediaInfo();

    public abstract boolean isValid();

    public abstract int getType();
    
    public abstract String getFormat(int type);
    
    public abstract String getDescription();

    public abstract String getName();

    public abstract File getFile();

    public abstract boolean renameTo(File dest);

    public abstract URL getUrl();

    // visualization attributes
    public abstract Icon getThumbnail(Dimension size);

    /* TODO: Some problem with javadoc since getLength  is not defined
     * @deprecated
     * Use getLength()
     * @see #getLength()
     */
    public abstract long getFileSize();

    //public abstract long getLength();
    public abstract InputStream getAsStream() throws IOException;

    public abstract byte[] getThumbnailData(Dimension size);

    public abstract String getThumbnailType();

}
