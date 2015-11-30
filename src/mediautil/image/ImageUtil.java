/* MediaUtil ImageUtil - $RCSfile: ImageUtil.java,v $
 * Copyright (C) 1999-2008 Dmitriy Rogatkin, Suresh Mahalingam.  All rights reserved.
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
 *	$Id: ImageUtil.java,v 1.3 2013/02/26 08:21:51 drogatkin Exp $
 **/

package mediautil.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageUtil {

	public static BufferedImage getScaledBufferedImage(Image image,
			Dimension size, double[] stretchFactor) {
		boolean mono = false;
		if (image instanceof BufferedImage) {
			mono = ((BufferedImage) image).getType() == BufferedImage.TYPE_BYTE_GRAY;
			// getColorModel() ==
		}
		if (stretchFactor == null || stretchFactor.length == 0)
			stretchFactor = new double[1];
		Dimension imageSize = getScaledSize(image, size, stretchFactor);

		BufferedImage biDest = new BufferedImage(imageSize.width,
				imageSize.height, mono ? BufferedImage.TYPE_BYTE_GRAY
						: BufferedImage.TYPE_INT_RGB);
		// Draw the texture image into the memory buffer.
		Graphics2D big = biDest.createGraphics();
		try {
			final Object monitor = new Object();
			ImageObserver observer = new ImageObserver() {
				public boolean imageUpdate(Image img, int infoflags, int x,
						int y, int width, int height) {
					if ((infoflags & ALLBITS) == ALLBITS
							|| (infoflags & ABORT) == ABORT
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
				if (big.drawImage(image, 0, 0, imageSize.width,
						imageSize.height, observer) == false)
					try {
						monitor.wait(1000 * 2);
					} catch (Exception ie) {
					}
			}
			return biDest;
		} finally {
			big.dispose();
		}
	}

	/**
	 * creates a scaled image
	 * 
	 * @param image
	 *            source image to scale
	 * @param newSize
	 *            new size of result image
	 * @param method
	 *            one of SCALE_DEFAULT, SCALE_FAST, SCALE_SMOOTH,
	 *            SCALE_REPLICATE, SCALE_AREA_AVERAGING
	 * @param stretchFactor
	 *            returned back as side effect
	 * @return a new image most fit target size
	 */
	public static Image getScaled(Image image, Dimension newSize, int method,
			double[] stretchFactor) {
		if (stretchFactor == null || stretchFactor.length == 0)
			stretchFactor = new double[1];
		Dimension scaledSize = getScaledSize(image, newSize, stretchFactor);
		if (stretchFactor[0] == 0.0)
			return image;
		return image.getScaledInstance(scaledSize.width, scaledSize.height,
				method);
	}

	public static Dimension getScaledSize(Image image, Dimension newSize,
			double[] stretchFactor) {
		Dimension imageSize = getImageSize(image, true);
		if (imageSize.width <= 0 || imageSize.height <= 0)
			return newSize; // can not scale
		if (imageSize.width <= newSize.width
				&& imageSize.height <= newSize.height)
			return imageSize;

		double wScale = (double) newSize.width / imageSize.width;
		double hScale = (double) newSize.height / imageSize.height;
		if (hScale < wScale)
			wScale = hScale;
		if (stretchFactor != null && stretchFactor.length > 0)
			stretchFactor[0] = wScale;

		return new Dimension((int) (imageSize.width * wScale),
				(int) (imageSize.height * wScale));
	}

	public static Dimension getImageSize(Image image, final boolean sizeOnly) {
		final Dimension imageSize = new Dimension();
		synchronized (imageSize) {
			imageSize.width = image.getWidth(new ImageObserver() {
				public boolean imageUpdate(Image img, int infoflags, int x,
						int y, int width, int height) {
					// System.err.println("Sizing image, flags "+infoflags);
					if ((sizeOnly && (infoflags & (WIDTH + HEIGHT)) == (WIDTH + HEIGHT))
							|| (infoflags & FRAMEBITS) == ALLBITS
							|| (infoflags & ABORT) == ABORT
							|| (infoflags & ERROR) == ERROR)
						synchronized (imageSize) {
							imageSize.width = width;
							imageSize.height = height;
							imageSize.notify();
							// System.err.println("Returned size at flags
							// "+infoflags);
							return false;
						}
					// return width <= 0 || height <= 0;
					return true;
				}
			});
			if (imageSize.width < 0) {
				// System.err.println(" Size wait "+imageSize.width+'x');
				try {
					imageSize.wait(1 * 1000);
				} catch (Exception ie) {
				}
			} else {
				imageSize.height = image.getHeight(null);
				// System.err.println(" Size instant
				// "+imageSize.width+'x'+imageSize.height);
			}
		}
		return imageSize;
	}

	public static boolean saveSizedImage(OutputStream os, Image image,
			Dimension size, String format) throws IOException {
		Iterator writers = ImageIO.getImageWritersByFormatName(format);
		if (writers != null && writers.hasNext()) {
			ImageWriter wr = (ImageWriter) writers.next();
			wr.setOutput(new MemoryCacheImageOutputStream(os));
			wr.write(getScaledBufferedImage(image, size, null));
			wr.dispose();
			return true;
		}
		return false;
	}
	
}