/* MediaUtil LLJTran - $RCSfile: BasicJpeg.java,v $
 * Copyright (C) 1999-2005 Dmitriy Rogatkin.  All rights reserved.
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
 *	$Id: BasicJpeg.java,v 1.16 2013/08/09 00:46:27 drogatkin Exp $
 *
 */
package mediautil.image.jpeg;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mediautil.gen.Log;
import mediautil.gen.MediaFormat;
import mediautil.gen.MediaInfo;

public class BasicJpeg extends LLJTran implements MediaFormat {

    public BasicJpeg(File file, String enc) {
        super(file);
        if (enc != null)
        	setEncoding(enc);
        read();
    }

    public BasicJpeg(InputStream inStream) {
        super(inStream);
    }

    // keep_appxs is ignored and taken as info_only. Does nothing if image has
    // already been read, i.e readUpto == READ_ALL or if info_only is true and
    // readUpto >= READ_INFO. Closes Internal Input Stream at end.
    protected void read(boolean info_only, boolean keep_appxs) {
        int currentRead = getReadUpto();
        if(currentRead < READ_ALL && (!info_only || currentRead < READ_INFO))
        {
            int readUpto = READ_ALL;
            if(info_only)
                readUpto = READ_INFO;
            if(currentRead > READ_NONE && getFile() != null)
                resetInput((InputStream)null);
            try {
                read(readUpto, info_only);
            }
            catch(LLJTranException e)
            {
                if(Log.debugLevel >= Log.LEVEL_ERROR)
                    System.err.println("Error Reading Jpeg: " + e.getMessage());
                if(inStream != null)
                    try {
                        inStream.close();
                        inStream = null;
                    }
                    catch (IOException ioe) {
                    }
            }
            finally {
                closeInternalInputStream();
            }
        }
    }

    protected void read() {
        read(true, false);
    }

    public boolean isValid() {
        return valid;
    }

    public InputStream getAsStream() {
        return createInputStream();
    }

    public String getParentPath() {
        return file == null ? null : file.getParent();
    }

    public long getFileSize() {
        return length();
    }

    public long length() {
        return file == null ? -1 : file.length();
    }

    public java.net.URL getUrl() {
        if (file == null)
            return null;
        try {
            return file.toURL();
        } catch (java.net.MalformedURLException me) {
            return null;
        }
    }

    public Date getDateTimeOriginal() {
        return file == null ? new Date() : new Date(file.lastModified());
    }

    public boolean renameTo(File dest) {
        if (file == null)
            return false;
        if (file.renameTo(dest)) {
            file = dest;
            try {
                imageinfo.setName(file.getName());
            } catch (NullPointerException e) {
            }
            return true;
        }
        return false;
    }

    public MediaInfo getMediaInfo() {
        return getImageInfo();
    }

    public int getType() {
    	return MediaFormat.STILL;
    }
    
    public String getDescription() {
        return JPEG; //getImageInfo().getFormat();
    }

    public String getFormat(int type) {
		if ((MediaFormat.STILL & type) > 0)
			return JPEG;
		return null;
	}

    public String getThumbnailType() {
        return ((AbstractImageInfo) getMediaInfo()).getThumbnailExtension();
    }

    public byte[] getThumbnailData(Dimension size) {
        throw new RuntimeException("Method getThumbnailData is not implemented yet");
    }

    public Image getImage() {
        //try {
        //	return getBufferedImage();
        //}catch(Exception e){}

        if (valid)
            return Toolkit.getDefaultToolkit().getImage(getLocationName());
        return null;
    }

    public static BufferedImage getBufferedImage(File imageFile) throws IOException {
    	return getBufferedImage((Object)imageFile);
    }
    
    public static BufferedImage getBufferedImage(Object imageObj) throws IOException {
    	Iterator readers = ImageIO.getImageReadersByFormatName(JPEG);
        if (readers.hasNext()) {
            ImageReader reader = (ImageReader) readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(imageObj);
            
            try {
                reader.setInput(iis, true);
                return reader.read(0, reader.getDefaultReadParam());
            } finally {
                iis.close();
                reader.dispose();
            }
        }
        return null;
    }
    
    public BufferedImage getBufferedImage() throws IOException {
        if (valid) {
            return getBufferedImage(getFile());
        }
        return null;
    }

    /** returns thumbnail of image, if size is null, no thumbnail generation will happen
     * and original thumbnail if any will be returned
     */
    public Icon getThumbnail(Dimension size) {
        return ((AbstractImageInfo) getMediaInfo()).getThumbnailIcon(size);
    }

    protected AdvancedImage advancedImage;

    public void setAdvancedImage(AdvancedImage advancedImage) {
        this.advancedImage = advancedImage;
    }

    public AdvancedImage getAdvancedImage() {
        return advancedImage;
    }

    public Icon getIcon() {
        try {
            if (valid)
                if (getAdvancedImage() != null) {
                    // try advanced image API
                    return getAdvancedImage().createIcon(getLocationName());
                } else {
                    return new ImageIcon(getLocationName());
                }
        } catch (Throwable e) {
            if(Log.debugLevel >= Log.LEVEL_ERROR)
                e.printStackTrace(System.err);
        }
        return null;
    }

    public void setComment(String comment) {
        out_comment = comment;
        if (imageinfo != null)
            imageinfo.setAttribute(imageinfo.COMMENTS, out_comment);
    }

    protected void transformAppHeader(int op, boolean transformThumbnail) throws IOException {
        if ((imageinfo instanceof Exif) == false)
            return;
        Exif exif = ((Exif) imageinfo);
        if (artist != null) {
            Entry e = exif.getTagValue(Exif.ARTIST, true);
            // TODO: localization, put in resources
            if (e == null) {
                e = new Entry("Camera owner, " + artist);
                exif.setTagValue(Exif.ARTIST, Exif.EXIFOFFSET, e, true);
            } else
                e.setValue(0, "Camera owner, " + artist);
        }
        int options = OPT_DEFAULTS;
        if (transformThumbnail)
            options |= OPT_XFORM_THUMBNAIL;
        super.transformAppHeader(op, options, true);
    }

    protected void addMarker(int len, byte markercode) {
        //System.err.println("Adding non processed marker "+Integer.toHexString(markercode)+" len "+len);
        if (tables == null)
            tables = new Hashtable();
        if (markers == null)
            markers = new byte[1];
        else {
            byte[] ta = new byte[markers.length + 1];
            System.arraycopy(markers, 0, ta, 0, markers.length);
            markers = ta;
        }
        markers[markers.length - 1] = markercode;
        byte[] marker = new byte[len + 4];
        marker[0] = M_PRX;
        marker[1] = markercode;
        // not relaible code, because we suppose to markerid contains a packed len
        System.arraycopy(markerid, 0, marker, 2, 2);
        System.arraycopy(data, 0, marker, 4, len);
        tables.put("" + markercode, marker);
    }

    void writeUnprocessedMarkers(OutputStream os) throws IOException {
        if (tables != null && markers != null) {
            for (int i = 0; i < markers.length; i++) {
                byte[] marker = (byte[]) tables.get("" + markers[i]);
                //System.err.println("Writing "+Integer.toHexString(markers[i])+" of "+marker);
                if (marker != null)
                    os.write(marker);
            }
        }
    }

    void writeRawDCT(OutputStream os) throws IOException {
        if (rawDct != null)
            os.write(rawDct);
    }

    void readRawDCT(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024 * 4);
        byte[] buffer = new byte[1024 * 4];
        int len;
        while ((len = is.read(buffer)) > 0) {
            readcounter += len;
            os.write(buffer, 0, len);
        }
        rawDct = os.toByteArray();
    }

    // TODO: reconsider raising an exception instead of return code
    public boolean transform(String destname, int op) {
        return transform(destname, op, false);
    }

    public boolean transform(String destname, int op, boolean preserve_appxs) {
        return transform(destname, op, preserve_appxs, null);
    }

    public boolean transform(String destname, int op, boolean preserve_appxs, Class custom_appx) {
        if (new File(destname).exists()) {
            if(Log.debugLevel >= Log.LEVEL_ERROR)
                System.err.println("File " + destname + " already exists. The operation abandoned.");
        } else {
            OutputStream os = null;
            try {
                return transform(os = new BufferedOutputStream(new FileOutputStream(destname), 4096), op,
                        preserve_appxs, custom_appx);
            } catch (FileNotFoundException fne) {
                if(Log.debugLevel >= Log.LEVEL_ERROR)
                    System.err.println(fne + " in saving of " + getName());
            } finally {
                if (os != null)
                    try {
                        os.flush();
                        os.close();
                    } catch (IOException ioe) {
                    }
            }
        }
        return false;
    }

    public boolean transform(OutputStream outStream, int op, boolean preserve_appxs, Class custom_appx) {
        // added return exec condition instead passing up exceptions
        int additionalInfo = OPT_WRITE_COMMENTS;
        if (preserve_appxs) {
            additionalInfo |= OPT_WRITE_ALL | OPT_XFORM_THUMBNAIL | OPT_XFORM_APPX | OPT_XFORM_ORIENTATION;
        }
        read(false, preserve_appxs);
        try {        	
            transform(outStream, op, additionalInfo, getCropRect(), restart_interval, custom_appx);
        } catch (IOException e) {
            if(Log.debugLevel >= Log.LEVEL_ERROR) {
                System.err.println(e + " in saving of " + getName());
                e.printStackTrace(System.err);
            }
            return false;
        } catch(IllegalArgumentException iae) {
        	if(Log.debugLevel >= Log.LEVEL_ERROR) {
        		System.err.println("Invalid parameters at calling transform:"+iae+" of "+getName());
        	}
        	return false;
        } catch(RuntimeException rte) {
        	if(Log.debugLevel >= Log.LEVEL_ERROR) {
        		System.err.println("Runtime exception at calling transform:"+rte+" of "+getName());
        		rte.printStackTrace();
        	}
        	return false;
        } finally {
            freeMemory();
        }
        return true;
    }

    void save(OutputStream os, int op, Class custom_appx) throws IOException {
        String comment;

        if (op == COMMENT)
            comment = in_comment;
        else
            comment = "Mediautil (c) 2005 Dmitriy Rogatkin, Suresh Mahalingam " + (out_comment.length() == 0 ? "" : "\n") + out_comment;

        if ("".equals(comment))
            comment = null;

        writeJpeg(os, op, comment, appxs_read, custom_appx);
    }

    // If comment is null no comment will be written. If "" then existing
    // comment data if any will be written, else comment will be written.
    protected void writeJpeg(OutputStream os, int op, String comment, boolean writeAppxs, Class custom_appx)
            throws IOException {
        if(op == CROP)
            op = NONE;
        int options = OPT_DEFAULTS;
        if (!writeAppxs)
            options &= ~OPT_WRITE_APPXS;
        super.writeJpeg(os, op, comment, options, null, custom_appx, restart_interval, false);
        if (!canBeProcessed) {
            writeUnprocessedMarkers(os);
            writeRawDCT(os);
        }
        os.flush();
        os.close();
        os = null;
    }

    public void saveMarkers(OutputStream os) throws IOException {
        try {
            read(true, true);
            if (os != null) {
                writeMarkerAppXs(os);
                os.close();
            }
        } finally {
            appxs = null;
        }
    }

    public void freeMemory() {
        super.freeMemory();
        tables = null;
        markers = null;
        rawDct = null;
    }

    public void setArtist(String val) {
        artist = val;
    }

    public static final String PROGRAMNAME = "LLJTran";

    public static void main(String[] args) {
        try {
            System.setErr(new PrintStream(new FileOutputStream(PROGRAMNAME + ".log"), true));
        } catch (IOException e) {
            System.err.println(PROGRAMNAME + ": Can't redirect error stream.");
        }
        new BasicJpeg(new File(args[0]), null).transform(args[1], Integer.parseInt(args[2]), true);
    }

    public void setCropRect(Rectangle cr) {
        getImageInfo().setAttribute(AbstractImageInfo.CROP_REGION, cr);
    }
    
    public Rectangle getCropRect() {
    	return (Rectangle)getImageInfo().getAttribute(AbstractImageInfo.CROP_REGION);
    }

    private Map tables;

    private byte[] markers;

    private byte[] rawDct;

    private String in_comment;
}
