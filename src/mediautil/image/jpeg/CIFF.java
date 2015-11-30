/* MediaUtil LLJTran - $RCSfile: CIFF.java,v $
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
 *	$Id: CIFF.java,v 1.4 2007/12/15 01:44:24 drogatkin Exp $
 *
 * Some ideas and algorithms were borrowed from:
 * Thomas G. Lane, and James R. Weeks
 */
package mediautil.image.jpeg;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mediautil.gen.FileFormatException;
import mediautil.gen.Log;
import mediautil.gen.Rational;

public class CIFF extends AbstractImageInfo <LLJTran>{
    final static String FORMAT = "HEAP";

    final static String JPGM = "JPGM"; // subtype

    final static String II = "II";

    final static String MM = "MM";

    // type.dataType and type.IDCode
    public final static int KSTG_FORMATMASK = 0xC000;

    public final static int K_DATATYPEMASK = 0x3800;

    public final static int K_IDCODEMASK = 0x07FF;

    public final static int K_TYPEIDCODEMASK = 0x3FFF;

    public final static int KSTG_INHEAPSPACE = 0xC000;

    public final static int KSTG_INRECORDENTRY = 0x4000;

    public final static int K_DT_BYTE = 0x0000;

    public final static int K_DT_ASCII = 0x0800;

    public final static int K_DT_WORD = 0x1000;

    public final static int K_DT_DWORD = 0x1800;

    public final static int K_DT_BYTE2 = 0x2000;

    public final static int K_DT_HEAPTYPEPROPERTY1 = 0x2800;

    public final static int K_DT_HEAPTYPEPROPERTY2 = 0x3000;

    public final static int K_TC_WILDCARD = 0xFFFF;

    public final static int K_TC_NULL = 0x0000; // null record

    public final static int K_TC_FREE = 0x0001; // free record

    public final static int K_TC_EXUSED = 0x0002;

    // ASCII
    public final static int K_TC_DESCRIPTION = (K_DT_ASCII | 0x0005);

    public final static int K_TC_MODELNAME = (K_DT_ASCII | 0x000A);

    public final static int K_TC_FIRMWAREVERSION = (K_DT_ASCII | 0x000B);

    public final static int K_TC_COMPONENTVESRION = (K_DT_ASCII | 0x000C);

    public final static int K_TC_ROMOPERATIONMODE = (K_DT_ASCII | 0x000D);

    public final static int K_TC_OWNERNAME = (K_DT_ASCII | 0x0010);

    public final static int K_TC_IMAGEFILENAME = (K_DT_ASCII | 0x0016);

    public final static int K_TC_THUMBNAILFILENAME = (K_DT_ASCII | 0x0017);

    // WORD
    public final static int K_TC_TARGETIMAGETYPE = (K_DT_WORD | 0x000A);

    public final static int K_TC_SR_RELEASEMETHOD = (K_DT_WORD | 0x0010);

    public final static int K_TC_SR_RELEASETIMING = (K_DT_WORD | 0x0011);

    public final static int K_TC_RELEASESETTING = (K_DT_WORD | 0x0016);

    public final static int K_TC_BODYSENSITIVITY = (K_DT_WORD | 0x001C);

    // DWORD
    public final static int K_TC_IMAGEFORMAT = (K_DT_DWORD | 0x0003);

    public final static int K_TC_RECORDID = (K_DT_DWORD | 0x0004);

    public final static int K_TC_SELFTIMERTIME = (K_DT_DWORD | 0x0006);

    public final static int K_TC_SR_TARGETDISTANCESETTING = (K_DT_DWORD | 0x0007);

    public final static int K_TC_BODYID = (K_DT_DWORD | 0x000B);

    public final static int K_TC_CAPTURETIME = (K_DT_DWORD | 0x000E);

    public final static int K_TC_IMAGESPEC = (K_DT_DWORD | 0x0010);

    public final static int K_TC_SR_EF = (K_DT_DWORD | 0x0013);

    public final static int K_TC_MI_EV = (K_DT_DWORD | 0x0014);

    public final static int K_TC_SERIALNUMBER = (K_DT_DWORD | 0x0017);

    public final static int K_TC_SR_EXPOSURE = (K_DT_DWORD | 0x0018);

    public final static int K_TC_CAMERAOBJECT = (0x0007 | K_DT_HEAPTYPEPROPERTY1);

    public final static int K_TC_SHOOTINGRECORD = (0x0002 | K_DT_HEAPTYPEPROPERTY2);

    public final static int K_TC_MEASUREDINFO = (0x0003 | K_DT_HEAPTYPEPROPERTY2);

    public final static int K_TC_CAMERASPECIFICATION = (0x0004 | K_DT_HEAPTYPEPROPERTY2);

    public CIFF(InputStream is, byte[] data, int offset, String name,
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

    public void readInfo() {
        readHeapFileHeader();
        data = null; // for gc
    }

    void readHeapFileHeader() {
        // byteorder, headerlength, type, subtype, 
        // version, reserved1, reserved2
        motorola = data[0] == 'M';
        intel = data[0] == 'I';
        if (!isSignature(6 + 4, JPGM))
            return;
        //throw new Exception("Invalid format");
        heapcontent = new Hashtable();
        int heapheaderlength = s2n(2, 4);
        processHeap(heapheaderlength, data.length - heapheaderlength);
    }

    void processHeap(int start, int length) {
        int offsettbloffset = s2n(start + length - 4, 4) + start;
        int numrecords = s2n(offsettbloffset, 2);
        int next = offsettbloffset + 2;
        for (int i = 0; i < numrecords; i++) {
            next = processRecord(next, start);
        }
    }

    int processRecord(int offset, int start) {
        int type = s2n(offset, 2);
        int datatypeidcode = type & K_TYPEIDCODEMASK;
        if ((type & KSTG_INHEAPSPACE) == 0) {
            int length = s2n(offset + 2, 4);
            int recoffext = s2n(offset + 6, 4) + start;
            if (datatypeidcode == K_TC_IMAGESPEC) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, s2n(recoffext, 4),
                        s2n(recoffext + 4, 4), Float.intBitsToFloat(s2n(
                                recoffext + 8, 4)), s2n(recoffext + 12, 4),
                        s2n(recoffext + 16, 4), s2n(recoffext + 20, 4), s2n(
                                recoffext + 24, 4)));
            } else if (datatypeidcode == K_TC_MODELNAME) {
                String manufacturer = s2a(recoffext, length);
                manufacturer.replace((char) 0, ' ');
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, manufacturer));
            } else if (datatypeidcode == K_TC_IMAGEFILENAME
                    || datatypeidcode == K_TC_THUMBNAILFILENAME
                    || datatypeidcode == K_TC_DESCRIPTION
                    || datatypeidcode == K_TC_OWNERNAME
                    || datatypeidcode == K_TC_FIRMWAREVERSION) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, s2a(recoffext, length)));
            } else if (datatypeidcode == K_TC_SR_EXPOSURE) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode,
                        Float.intBitsToFloat(s2n(recoffext, 4)), Float
                                .intBitsToFloat(s2n(recoffext + 4, 4)), //tv
                        Float.intBitsToFloat(s2n(recoffext + 8, 4)))); //av
            } else if (datatypeidcode == K_TC_CAPTURETIME) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, s2n(recoffext, 4),
                        s2n(recoffext + 4, 4), s2n(recoffext + 8, 4)));
            } else if (datatypeidcode == K_TC_CAMERASPECIFICATION) {
                processHeap(recoffext, length);
            } else if (datatypeidcode == K_TC_CAMERAOBJECT) {
                processHeap(recoffext, length);
            } else if (datatypeidcode == K_TC_MEASUREDINFO) {
                processHeap(recoffext, length);
            } else if (datatypeidcode == K_TC_SHOOTINGRECORD) {
                processHeap(recoffext, length);
            } else {
                if ((datatypeidcode & K_DATATYPEMASK) == K_DT_HEAPTYPEPROPERTY1
                        || (datatypeidcode & K_DATATYPEMASK) == K_DT_HEAPTYPEPROPERTY2)
                    processHeap(recoffext, length);
                else
                    heapcontent
                            .put(
                                    new Integer(datatypeidcode),
                                    new Record(
                                            datatypeidcode,
                                            ((datatypeidcode & K_DATATYPEMASK) == K_DT_ASCII) ? s2a(
                                                    recoffext, length)
                                                    : "Unknown "
                                                            + Naming
                                                                    .getCIFFTypeName(datatypeidcode)
                                                            + " in-heap property"));
            }
        } else if ((type & KSTG_INRECORDENTRY) != 0) {
            if (datatypeidcode == K_TC_IMAGEFORMAT) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, s2n(offset + 2, 4), Float
                                .intBitsToFloat(s2n(offset + 6, 4))));
            } else if (datatypeidcode == K_TC_TARGETIMAGETYPE) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, s2n(offset + 2, 2)));
            } else if (datatypeidcode == K_TC_RECORDID
                    || datatypeidcode == K_TC_SERIALNUMBER
                    || datatypeidcode == K_TC_SELFTIMERTIME
                    || datatypeidcode == K_TC_BODYID) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, s2n(offset + 2, 4)));
            } else if (datatypeidcode == K_TC_SR_RELEASEMETHOD
                    || datatypeidcode == K_TC_SR_RELEASETIMING
                    || datatypeidcode == K_TC_BODYSENSITIVITY) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, s2n(offset + 2, 2)));
            } else if (datatypeidcode == K_TC_SR_EF) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, Float
                                .intBitsToFloat(s2n(offset + 2, 4)), Float
                                .intBitsToFloat(s2n(offset + 6, 4))));
            } else if (datatypeidcode == K_TC_SR_TARGETDISTANCESETTING
                    || datatypeidcode == K_TC_MI_EV) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, Float
                                .intBitsToFloat(s2n(offset + 2, 4))));
            } else if (datatypeidcode == K_TC_ROMOPERATIONMODE) {
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, s2a(offset + 2, 8)));
            } else {
                String content;
                if ((datatypeidcode & K_DATATYPEMASK) == K_DT_ASCII)
                    content = s2a(offset + 2, 8);
                else {
                    content = "Unknown "
                            + Naming.getCIFFTypeName(datatypeidcode)
                            + " in-record property";
                    if ((datatypeidcode & K_DATATYPEMASK) == K_DT_WORD)
                        content += "(" + s2n(offset + 2, 2) + ")";
                    else if ((datatypeidcode & K_DATATYPEMASK) == K_DT_DWORD)
                        content += "(" + s2n(offset + 2, 4) + ")";
                    else if ((datatypeidcode & K_DATATYPEMASK) == K_DT_BYTE)
                        content += "(" + s2n(offset + 2, 1) + ")";
                }
                heapcontent.put(new Integer(datatypeidcode), new Record(
                        datatypeidcode, content));
            }
        }
        return offset + 2 + 4 + 4;
    }

    public int getResolutionX() {
        // to optimize, extract sizes in heap parsing process 
        Record r = (Record) heapcontent.get(new Integer(K_TC_IMAGESPEC));
        if (r != null)
            return r.getWidth();
        return -1;
    }

    public int getResolutionY() {
        Record r = (Record) heapcontent.get(new Integer(K_TC_IMAGESPEC));
        if (r != null)
            return r.getHeight();
        return -1;
    }

    public int getMetering() {
        return 0;
    }

    public int getExpoProgram() {
        return 0;
    }

    public String getMake() {
        return heapcontent.get(new Integer(K_TC_MODELNAME)).toString();
    }

    public String getModel() {
        return heapcontent.get(new Integer(K_TC_MODELNAME)).toString();
    }

    public String getDataTimeOriginalString() {
        return heapcontent.get(new Integer(K_TC_CAPTURETIME)).toString();
    }

    public float getFNumber() {
        try {
            return apertureToFnumber(((Record) heapcontent.get(new Integer(
                    K_TC_SR_EXPOSURE))).getFloatValue(2));
        } catch (NullPointerException e) {
        }
        return -1;
    }

    public Rational getShutter() {
        try {
            int si = (int) ((Record) heapcontent.get(new Integer(
                    K_TC_SR_EXPOSURE))).getFloatValue(1);
            return TV_TO_SEC[si];
        } catch (NullPointerException e) {
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return new Rational(0, 1);
    }

    public boolean isFlash() {
        try {
            return ((Record) heapcontent.get(new Integer(K_TC_SR_EF)))
                    .getFloatValue() > 0;
        } catch (NullPointerException e) {
        }
        return false;
    }

    public float getFocalLength() {
        return 0;
    }

    public String getQuality() {
        return "BEST";
    }

    public String getReport() {
        return heapcontent.get(new Integer(K_TC_SR_EXPOSURE)).toString();
    }

    public boolean saveThumbnailImage(OutputStream os)
            throws IOException {
        try {
            String tnfn = heapcontent.get(new Integer(K_TC_THUMBNAILFILENAME))
                    .toString();
            if (tnfn != null) {
                InputStream is = new FileInputStream(new File(getImageFile().getParent(), tnfn));
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0)
                    os.write(buffer, 0, len);
                return true;
            }
        } catch (NullPointerException e) {
        }
        return super.saveThumbnailImage(os);
    }

    public Icon getThumbnailIcon(Dimension size) {
        try {
            File tnf = new File(getImageFile().getParent(),
                    heapcontent.get(new Integer(K_TC_THUMBNAILFILENAME))
                            .toString());
            if (tnf.exists())
                return new ImageIcon(tnf.getAbsolutePath());
        } catch (NullPointerException e) {
        }
        if (getAdvancedImage() != null) {
            try {
                // try advanced image API
                return getAdvancedImage().createThumbnailIcon(
                        getImageFile().getPath(), null);
            } catch (Throwable e) {
                if(Log.debugLevel >= Log.LEVEL_ERROR)
                    e.printStackTrace(System.err);
            }
        }
        return null;
    }

    public Hashtable getProperties() {
        return heapcontent;
    }

    Hashtable heapcontent;
}
