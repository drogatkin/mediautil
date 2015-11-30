/* MediaUtil LLJTran - $RCSfile: JPEG.java,v $
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
 *	$Id: JPEG.java,v 1.7 2008/03/02 04:42:16 drogatkin Exp $
 *
 * Some ideas and algorithms were borrowed from:
 * Thomas G. Lane, and James R. Weeks
 */
package mediautil.image.jpeg;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mediautil.gen.FileFormatException;
import mediautil.gen.Log;
import mediautil.gen.Rational;
import mediautil.image.ImageUtil;

public class JPEG extends AbstractImageInfo<LLJTran> {
    int width, height, precision;

    byte[] header = new byte[2];

    public JPEG(int width, int height, int precision, LLJTran format) throws FileFormatException {
        this.width = width;
        this.height = height;
        this.precision = precision;
        this.format = format;
    }

    public JPEG(InputStream is, byte[] data, int offset, String name, String comments, int width, int height,
            LLJTran format) throws FileFormatException {
        super(is, data, offset, name, comments, format);
//if (format == null)
//new Exception("CALLED WITH NULL").printStackTrace();
        if (this.width <= 0 && width > 0) {
            this.width = width;
            this.height = height;
        } else {
            int len;
            if (data.length == 2) { // too bad, marker can be unread
                len = readMarker(is, true);
                if (len <= 0)
                    return;
                if (data[1] >= M_SOF0 && data[1] <= M_SOF15 && data[1] != M_DHT && data[1] != M_JPG) {
                    precision = (this.data[0] & 255) * (this.data[5] & 255);
                    this.width = bs2i(3, 2);
                    this.height = bs2i(1, 2);
                    return;
                }
            }
            do {
                len = readMarker(is, false);
                if (len <= 0)
                    break;
                if (header[1] >= M_SOF0 && header[1] <= M_SOF15 && header[1] != M_DHT && header[1] != M_JPG) {
                    precision = (this.data[0] & 255) * (this.data[5] & 255);
                    this.width = bs2i(3, 2);
                    this.height = bs2i(1, 2);
                    break;
                }
            } while (true);
        }
    }

    public String toString()
    {
        return getClass().getName() + " (" + width + " x " + height + ")";
    }

    public int getResolutionX() {
        return width;
    }

    public int getResolutionY() {
        return height;
    }

    public int getMetering() {
        return 0;
    }

    public int getExpoProgram() {
        return 0;
    }

    public String getFormat() {
        return NA;
    }

    public void readInfo() {
        data = null; // for gc
    }

    public String getMake() {
        return NA;
    }

    public String getModel() {
        return NA;
    }

    public String getDataTimeOriginalString() {
    	synchronized(dateformat) {
    		return dateformat.format(new Date(/*lastModified()*/));
    	}
    }

    public float getFNumber() {
        return 0;
    }

    public Rational getShutter() {
        return new Rational(0, 1);
    }

    public boolean isFlash() {
        return false;
    }

    public float getFocalLength() {
        return 0;
    }

    public String getQuality() {
        return NA;
    }

    public String getReport() {
        return NA;
    }

    public boolean saveThumbnailImage(OutputStream os) {
        try {
            return saveSizedImage(os, DEFAULT_THUMB_SIZE);
        } catch (Exception e) {
            if(Log.debugLevel >= Log.LEVEL_ERROR)
                e.printStackTrace(System.err);
        }
        return false;
    }

    public boolean saveSizedImage(OutputStream os, Dimension size) throws IOException {
        if (os == null)
            return false;
        if (super.saveThumbnailImage(os/*, size*/) == false) {
            return saveSizedImage(os, createImage(getImageFile()), size);
        }
        return true;
    }    
   
    public static boolean saveSizedImage(OutputStream os, Image image, Dimension size) throws IOException {
    	return ImageUtil.saveSizedImage(os, image, size, LLJTran.JPEG);
    	/*boolean mono = false;
    	if (image instanceof BufferedImage) {
    		mono = ((BufferedImage)image).getType() == BufferedImage.TYPE_BYTE_GRAY;
    		// getColorModel() == 
    	}
        Dimension imageSize = ImageUtil.getScaledSize(image, size, null);/*
        //image = image.getScaledInstance(imageSize.width, imageSize.height, Image.SCALE_SMOOTH);
        BufferedImage biDest = new BufferedImage(imageSize.width, imageSize.height, mono?BufferedImage.TYPE_BYTE_GRAY:BufferedImage.TYPE_INT_RGB);
        // Draw the texture image into the memory buffer.
        Graphics2D big = biDest.createGraphics();
        try {
            final Object monitor = new Object();
            ImageObserver observer = new ImageObserver() {
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    if ((infoflags & ALLBITS) == ALLBITS || (infoflags & ABORT) == ABORT
                            || (infoflags & ERROR) == ERROR) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                        return false;
                    }
                    return true;
                }
            };
            synchronized (monitor) {
                if (big.drawImage(image, 0, 0, imageSize.width, imageSize.height, observer) == false)
                    try {
                        monitor.wait(1000 * 10);
                    } catch (Exception ie) {
                    }
            }

    	
            Iterator writers = ImageIO.getImageWritersByFormatName(LLJTran.JPEG);
            if (writers != null && writers.hasNext()) {
                ImageWriter wr = (ImageWriter) writers.next();
                wr.setOutput(new MemoryCacheImageOutputStream(os));
         //       wr.write(biDest);
                wr.write(getScaledImage(image,imageSize));
                wr.dispose();
                return true;
            }
        } finally {
            big.dispose();
        }
        return false;*/
    }
       

    public Icon getThumbnailIcon(Dimension size) {
        try {
            return new ImageIcon(ImageUtil.getScaled(Toolkit.getDefaultToolkit().getImage(getImageFile().getPath()),
                    size==null?DEFAULT_THUMB_SIZE:size, Image.SCALE_FAST, null));
        } catch (Exception ex) {
            if(Log.debugLevel >= Log.LEVEL_ERROR)
                System.err.println("Exception '" + ex + "' in scaling thumdnail image.");
            if (getAdvancedImage() != null) {
                try {
                    // try advanced image API
                    return getAdvancedImage().createThumbnailIcon(getImageFile().getPath(), size);
                } catch (Throwable t) {
                    if(Log.debugLevel >= Log.LEVEL_ERROR)
                        System.err.println(t);
                }
            }

            int w = 100;
            int h = 100;
            int pix[] = new int[w * h];
            int index = 0;
            for (int y = 0; y < h; y++) {
                int red = (y * 255) / (h - 1);
                for (int x = 0; x < w; x++) {
                    int blue = (x * 255) / (w - 1);
                    pix[index++] = (255 << 24) | (red << 16) | blue;
                }
            }
            return new ImageIcon(Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h, pix, 0, w)));
        }
    }

    public static BufferedImage createImage(File imageFile) throws IOException {
        Iterator readers = ImageIO.getImageReadersByFormatName(LLJTran.JPEG);
        if (readers.hasNext()) {
            ImageReader reader = (ImageReader) readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
            try {
                reader.setInput(iis, true);
                return reader.read(0, reader.getDefaultReadParam());
            } finally {
                iis.close();
            }
        }
        return null;
    }

    int readMarker(InputStream is, boolean bodyOnly) {
        try {
            if (bodyOnly == false)
                if (is.read(header) < header.length)
                    return -1;
            data = new byte[2];
            if (is.read(data) < data.length)
                return -1;
            int len = bs2i(0, 2) - 2;
            data = new byte[len];
            return read(is, data) + header.length + 2;
        } catch (Exception e) {
            if(Log.debugLevel >= Log.LEVEL_ERROR)
                e.printStackTrace(System.err);
            return -1;
        }
    }
}
