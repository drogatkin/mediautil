/* MediaUtil LLJTran - $RCSfile: JFXX.java,v $
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
 *	$Id: JFXX.java,v 1.4 2007/12/15 01:44:24 drogatkin Exp $
 *
 * Some ideas and algorithms were borrowed from:
 * Thomas G. Lane, and James R. Weeks
 */
package mediautil.image.jpeg;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mediautil.gen.FileFormatException;
import mediautil.gen.Log;
import mediautil.gen.Rational;
import mediautil.image.ImageResources;

public class JFXX extends AbstractImageInfo <LLJTran>{
    final static String FORMAT = "JFXX";

    static final String PICTURE_INFO = "picture info";

    static final String CAMERA_INFO = "camera info";

    static final String DIAG_INFO = "diag info";

    static final String USER = "user";

    static final String END = "end";

    static final String FILE_INFO = "file info";

    // [picture info]
    static final String TIMEDATE = "TimeDate";

    static final String SHUTTER = "Shutter";

    static final String FNUMBER = "Fnumber";

    static final String CFNUMBER = "FNumber";

    static final String ZOOM = "Zoom";

    static final String RESOLUTION = "Resolution";

    static final String IMAGESIZE = "ImageSize";

    static final String FLASH = "Flash";

    // [camera info]
    static final String ID = "ID";

    static final String TYPE = "Type";

    final DecimalFormat fnumberformat = new DecimalFormat("F##.##");

    final DecimalFormat zoomformat = new DecimalFormat("x##.##");

    public JFXX(InputStream is, byte[] data, int offset, String name,
            String comments, LLJTran format) throws FileFormatException {
        super(is, data, offset, name, comments, format);
        // an unusual problem is here
        // no own variables are initialized here
        // but super's constructor calls our method read, which is using
        // uninitialized local variables, so they are moved to parent
    }

    public String getFormat() {
        return FORMAT;
    }

    public static byte[] getMarkerData() {
        return new byte[] { (byte) 0xff, (byte) 0xe0, (byte) 0x00, (byte) 0x10,
                (byte) 0x4a, (byte) 0x46, (byte) 0x49, (byte) 0x46,
                (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00 };
    }

    public String getMake() {
        if (make != null)
            return make;
        return FORMAT;
    }

    public String getModel() {
        return "" + (String) camerainfo.get(ID) + " "
                + (String) camerainfo.get(TYPE);
    }

    public String getReport() {
        StringBuffer report = new StringBuffer();
        report.append("Shutter: ");
        report.append((String) pictureinfo.get(SHUTTER));
        report.append(", Aperture: ");
        String a = (String) pictureinfo.get(FNUMBER);
        if (a == null)
            a = (String) pictureinfo.get(CFNUMBER);
        report.append(a).append(", Flash: ");
        report
                .append("0".equals((String) pictureinfo.get(FLASH)) ? ImageResources.YES
                        : ImageResources.NO);
        return report.toString();

    }

    public String getDataTimeOriginalString() {
        String date = (String) pictureinfo.get(TIMEDATE);
        if (date != null) synchronized (dateformat) {
            return dateformat.format(new Date(Integer.parseInt(date) * 1000l));
        }
        return null;
    }

    public int getResolutionX() {
        String s = (String) pictureinfo.get(IMAGESIZE);
        if (s != null) {
            int dp = s.indexOf('-');
            if (dp > 0) {
                try {
                    return Integer.parseInt(s.substring(0, dp));
                } catch (NumberFormatException e) {
                }
            }
        }
        return -1;
    }

    public int getResolutionY() {
        String s = (String) pictureinfo.get(IMAGESIZE);
        if (s != null) {
            int dp = s.indexOf('-');
            if (dp > 0) {
                try {
                    return Integer.parseInt(s.substring(dp + 1));
                } catch (NumberFormatException e) {
                }
            }
        }
        return -1;
    }

    public int getMetering() {
        return 0;
    }

    public int getExpoProgram() {
        return 0;
    }

    public float getFNumber() {
        try {
            String a = (String) pictureinfo.get(FNUMBER);
            if (a == null)
                a = (String) pictureinfo.get(CFNUMBER);
            return fnumberformat.parse(a).floatValue();
        } catch (NumberFormatException e) {
        } catch (Exception e) {
        }
        return -1;
    }

    public Rational getShutter() {
        try {
            return new Rational(Integer.parseInt((String) pictureinfo
                    .get(SHUTTER)), 1);
        } catch (NumberFormatException e) {
        } catch (Exception e) {
        }
        return new Rational(0, 1);
    }

    public boolean isFlash() {
        if (pictureinfo == null)
            return false;
        String flash = (String) pictureinfo.get(FLASH);
        if (flash != null)
            return !"0".equals(flash);
        return false;
    }

    public float getFocalLength() {
        try {
            return zoomformat.parse((String) pictureinfo.get(ZOOM))
                    .floatValue();
        } catch (NumberFormatException e) {
        } catch (Exception e) {
        }
        return 0;
    }

    public String getQuality() {
        return (String) pictureinfo.get(RESOLUTION);
    }

    public boolean saveThumbnailImage(OutputStream os) throws IOException {
        if (image != null && os != null) {
            os.write(image);
            return true;
        }
        return super.saveThumbnailImage(os);
    }

    public Icon getThumbnailIcon(Dimension size) {
        if (image != null)
            return new ImageIcon(image);
        if (getAdvancedImage() != null) {
            try {
                // try advanced image API
                return getAdvancedImage().createThumbnailIcon(
                        getImageFile().getPath(), null);
            } catch (Throwable e) {
                if(Log.debugLevel >= Log.LEVEL_ERROR)
                    System.err.println(e);
            }
        }
        return null;
    }

    public void readInfo() {
        try {
            readAPP0X();
            readAPP12();
        } catch (NullPointerException e) {
            if(Log.debugLevel >= Log.LEVEL_ERROR)
                e.printStackTrace(System.err);
        } catch (IOException e) {
            if(Log.debugLevel >= Log.LEVEL_ERROR)
                e.printStackTrace(System.err);
        }
        data = null; // for gc
    }

    void readAPP0X() {
        // x'FF', APP0, length, extension_code, extension_data
        switch (data[5]) {
        case 0x10:
            image = new byte[data.length - 6];
            System.arraycopy(data, 6, image, 0, image.length);
        case 0x0F:
        case 0x11:
        case 0x13:
        }
    }

    void readAPP12() throws IOException {
        data = new byte[4];
        is.read(data);
        if (data[0] == M_PRX && data[1] == M_APP12) {
            int len = bs2i(2, 2) - 2;
            data = new byte[len];
            BasicJpegIo.read(is, data);
            ParserAPP12 parser = new ParserAPP12(0);
            make = parser.next();
            parser.next(); // skip size
            pictureinfo = new Hashtable();
            camerainfo = new Hashtable();
            diaginfo = new Hashtable();
            fileinfo = new Hashtable();
            Hashtable currentinfo = null;
            String el;
            while (parser.hasMore()) {
                el = parser.next();
                if (el.startsWith("[")) {
                    if (el.indexOf(PICTURE_INFO) == 1)
                        currentinfo = pictureinfo;
                    else if (el.indexOf(CAMERA_INFO) == 1)
                        currentinfo = camerainfo;
                    else if (el.indexOf(DIAG_INFO) == 1)
                        currentinfo = diaginfo;
                    else if (el.indexOf(FILE_INFO) == 1)
                        currentinfo = fileinfo;
                    else
                        currentinfo = null;
                } else {
                    if (currentinfo == null)
                        continue;
                    StringTokenizer st = new StringTokenizer(el, "=");
                    if (st.hasMoreTokens()) {
                        String key = st.nextToken();
                        if (st.hasMoreTokens()) {
                            currentinfo.put(key, st.nextToken());
                        }
                    }
                }
            }
        }
    }

    class ParserAPP12 {
        int curpos;

        ParserAPP12(int offset) {
            curpos = offset;
        }

        boolean hasMore() {
            return curpos < data.length - 1;
        }

        String next() {
            int startpos = curpos;
            while (curpos < data.length && data[curpos] != 0
                    && data[curpos] != 0x0A && data[curpos] != 0x0D)
                curpos++;
            String result = null;
            try {
                result = new String(data, startpos, curpos - startpos,
                        "Default");
            } catch (UnsupportedEncodingException e) {

            }
            // skip unused
            while (curpos < data.length
                    && (data[curpos] == 0 || data[curpos] == 0x0A || data[curpos] == 0x0D))
                curpos++;
            return result;
        }
    }

    // JFIF specific
    public Hashtable getPictureInfo() {
        return pictureinfo;
    }

    public Hashtable getCameraInfo() {
        return camerainfo;
    }

    public Hashtable getDiagInfo() {
        return diaginfo;
    }

    public Hashtable getFileInfo() {
        return fileinfo;
    }

    private byte[] image;

    private String make;

    private Hashtable pictureinfo, camerainfo, diaginfo, fileinfo;
}
