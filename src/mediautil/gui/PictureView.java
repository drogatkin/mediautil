/* MediaUtil LLJTran - $RCSfile: PictureView.java,v $
 * Copyright (C) 1999-2006 Dmitriy Rogatkin, Suresh Mahalingam.  All rights reserved.
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
 *  $Id: PictureView.java,v 1.1 2006/05/29 14:11:11 msuresh Exp $
 *
 */
package mediautil.gui;

import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.BoundedRangeModel;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

import java.io.*;
import java.util.Date;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

public class PictureView extends JScrollPane {

    private static class ImageWaiter implements  ImageObserver
    {
        int side = -1, reqdFlag = 0;
        public boolean imageUpdate(Image img, int infoflags, int x, int y,
                                   int width, int height)
        {
            System.out.println("ImageUpdate Called");
            boolean retVal = (reqdFlag != 0);
            if(retVal)
            {
              synchronized(this)
              {
                if((infoflags & reqdFlag) != 0)
                {
                    side = reqdFlag==WIDTH?width:height;
                    retVal = false;
                    notifyAll();
                }
              }
            }
            return retVal;
        }

        public synchronized int getSide()
        {
            while(side == -1)
            {
              try
              {
                wait();
              }
              catch(InterruptedException e) {}
            }

            return side;
        }
    }

    public static class PictureComponent extends JComponent {
        private Image refImage;
        private Insets extraInsets;
        private Insets totalInsets;
        private double scale;
        private ImageWaiter dummyObserver;

        public PictureComponent()
        {
            extraInsets = new Insets(0, 0, 0, 0);
            totalInsets = new Insets(0, 0, 0, 0);
            dummyObserver = new ImageWaiter();
        }

        private int blockingGetImageWidth(Image image)
        {
          int retVal = image.getWidth(dummyObserver);
            if(retVal == -1)
            {
              ImageWaiter waiter = new ImageWaiter();
                waiter.reqdFlag = ImageObserver.WIDTH;
                retVal = image.getWidth(waiter);
                if(retVal == -1)
                    retVal = waiter.getSide();
            }

            return retVal;
        }

        private int blockingGetImageHeight(Image image)
        {
          int retVal = image.getHeight(dummyObserver);
            if(retVal == -1)
            {
              ImageWaiter waiter = new ImageWaiter();
                waiter.reqdFlag = ImageObserver.HEIGHT;
                retVal = image.getHeight(waiter);
                if(retVal == -1)
                    retVal = waiter.getSide();
            }

            return retVal;
        }

        public Insets getTotalInsets(Insets totalInsets)
        {
            if(totalInsets == null)
                totalInsets = new Insets(0, 0, 0, 0);
            Insets insets = getInsets();
            totalInsets.top = insets.top + extraInsets.top;
            totalInsets.left = insets.left + extraInsets.left;
            totalInsets.bottom = insets.bottom + extraInsets.bottom;
            totalInsets.right = insets.right + extraInsets.right;
            return totalInsets;
        }

        public void setExtraInsets(Insets insets)
        {
            if(insets == null)
            {
                extraInsets.top = 0;
                extraInsets.left = 0;
                extraInsets.bottom = 0;
                extraInsets.right = 0;
            }
            else
            {
                extraInsets.top = insets.top;
                extraInsets.left = insets.left;
                extraInsets.bottom = insets.bottom;
                extraInsets.right = insets.right;
            }
        }

        public void paintComponent(Graphics g)
        {
            if(refImage == null)
                super.paintComponent(g);
            else
            {
                int width = getWidth();
                int height = getHeight();
                Graphics g1 = g.create();
                if (isOpaque()) { //paint background
                    g1.setColor(getBackground());
                    g1.fillRect(0, 0, width, height);
                }
                getTotalInsets(totalInsets);
                int x = totalInsets.left;
                int y = totalInsets.top;
                width -= (totalInsets.left + totalInsets.right);
                height -= (totalInsets.top + totalInsets.bottom);
                /* TODO: delete
                double s1, s2;
                s1 = width/(double)refImage.getWidth();
                s2 = height/(double)refImage.getHeight();
                if(s1 < s2)
                    height = (int)Math.floor(s1*refImage.getHeight());
                else
                    width = (int)Math.floor(s2*refImage.getWidth());
                */
                g1.drawImage(refImage, x, y, width, height, this);
                g1.dispose();
            }
        }

        private Dimension getDispSize()
        {
            Dimension retVal = new Dimension();
            int imageWidth = blockingGetImageWidth(refImage);
            int imageHeight = blockingGetImageHeight(refImage);
            getTotalInsets(totalInsets);
            int xInsets = totalInsets.left + totalInsets.right;
            int yInsets = totalInsets.top + totalInsets.bottom;
            if(scale > 0)
            {
              retVal.width = (int)Math.round(scale*imageWidth) + xInsets;
              retVal.height = (int)Math.round(scale*imageHeight) + yInsets;
            }
            else
            {
              Dimension viewArea = getParent().getParent().getSize();
                retVal.width = viewArea.width - xInsets;
                retVal.height = viewArea.height - yInsets;
                if(retVal.width < imageWidth || retVal.height < imageHeight
                   || scale == 0)
                {
                  double s1, s2;
                    s1 = retVal.width/(double)imageWidth;
                    s2 = retVal.height/(double)imageHeight;
                    if(s1 < s2)
                        retVal.height = (int)Math.round(s1*imageHeight);
                    else
                        retVal.width = (int)Math.round(s2*imageWidth);
                }
                else
                {
                    retVal.width = imageWidth;
                    retVal.height = imageHeight;
                }
                retVal.width += xInsets;
                retVal.height += yInsets;
            }
            return retVal;
        }

        public Dimension getPreferredSize()
        {
    System.out.println("Preferred Size Called");
          Dimension retVal;
            if(refImage != null)
                retVal = getDispSize();
            else
                retVal = super.getPreferredSize();
            return retVal;
        }

        public Dimension getMinimumSize()
        {
    System.out.println("Minimum Size Called");
          Dimension retVal;
            if(refImage != null)
                retVal = getDispSize();
            else
                retVal = super.getMinimumSize();
            return retVal;
        }

        public Dimension getMaximumSize()
        {
    System.out.println("Maximum Size Called");
          Dimension retVal;
            if(refImage != null)
                retVal = getDispSize();
            else
                retVal = super.getMaximumSize();
            return retVal;
        }
    }

    protected PictureComponent picture;

    private void init(PictureComponent p)
    {
        if(p == null)
            p = new PictureComponent();
        p.scale = -1;
        picture = p;
        JPanel pane;
        pane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        pane.add(picture, c);
        setViewportView(pane);
    }

    public PictureView(PictureComponent p)
    {
        super();
        init(p);
    }

    public PictureView()
    {
        this(null);
    }

    public PictureComponent getPictureComponent()
    {
        return picture;
    }

    public Image getImage()
    {
        return picture.refImage;
    }

    public double getScale()
    {
        return picture.scale;
    }

    private void adjustScrollBar(JScrollBar sb, int startInset, int oldScrollPos,
                                 double anchorPos, int oldImageSide,
                                 int newImageSide)
    {
      int anchorOffset = 0;
        if(anchorPos <= 1)
            anchorOffset = (int)Math.round(anchorPos*sb.getVisibleAmount()) +
                           oldScrollPos;
        else
            anchorOffset = (int)Math.round(anchorPos) - 2;
      int oldImageOff = anchorOffset - startInset;
      int newVal = 0;
        if(oldImageOff > 0)
            newVal = (int)(oldImageOff*(long)newImageSide/oldImageSide) +
                     startInset - (anchorOffset - oldScrollPos);
        sb.setValue(newVal);
      if(sb == verticalScrollBar) System.out.println("Vertical ImageOff = " + oldImageOff + " oldImage Side = " + oldImageSide + " new ImageSide = " + newImageSide + " startInset = " + startInset + " newVal = " + newVal + " Actual Value = " + sb.getValue() + " listeners = " + sb.getAdjustmentListeners());
      if(sb == horizontalScrollBar) System.out.println("Horizontal Side = " + oldImageSide + " new ImageSide = " + newImageSide + " startInset = " + startInset + " newVal = " + newVal + " Actual Value = " + sb.getValue() + " listeners = " + sb.getAdjustmentListeners());
    }

    public void setImage(Image image, boolean resetScrollBars,
                         double newScale)
    {
        double oldScale = picture.scale;
        Image orgImage = picture.refImage;
        int oldHPos = horizontalScrollBar.getValue();
        int oldVPos = verticalScrollBar.getValue();
        picture.refImage = image;
        picture.scale = newScale;
        if(image != null)
        {
          picture.invalidate();
          validateTree();
          if(orgImage != null && oldScale > 0 && newScale > 0)
            if(resetScrollBars)
            {
                horizontalScrollBar.setValue(0);
                verticalScrollBar.setValue(0);
            }
            else
            {
              Insets insets = new Insets(0, 0, 0, 0);
                picture.getTotalInsets(insets);
                adjustScrollBar(horizontalScrollBar, insets.left, oldHPos,
                                oldHPos + 2, (int)Math.round(oldScale*
                                           picture.blockingGetImageWidth(orgImage)),
                                (int)Math.round(newScale*picture.blockingGetImageWidth(image)));
                adjustScrollBar(verticalScrollBar, insets.top, oldVPos,
                                oldVPos + 2, (int)Math.round(oldScale*
                                           picture.blockingGetImageHeight(orgImage)),
                                (int)Math.round(newScale*picture.blockingGetImageHeight(image)));
            }
        }
        // TODO: Delete if not required
        // picture.revalidate();
        picture.repaint();
    }

    public void setImage(Image image, boolean resetScrollBars)
    {
        setImage(image, resetScrollBars, picture.scale);
    }

    public void setImage(Image image)
    {
        setImage(image, false, picture.scale);
    }

    public void setScale(double aspectRatio, double xAnchor, double yAnchor)
    {
JScrollBar h = horizontalScrollBar, v = verticalScrollBar; BoundedRangeModel vm = v.getModel(), hm = h.getModel(); System.out.println("increment = " + h.getUnitIncrement(-1) + '/' + h.getUnitIncrement(1) + ',' +  v.getUnitIncrement(-1) + '/' + v.getUnitIncrement(1) + " Block = " + h.getBlockIncrement(-1) + '/' + h.getBlockIncrement(1) + ',' +  v.getBlockIncrement(-1) + '/' + v.getBlockIncrement(1) + " Vals = " + h.getValue() + ',' + v.getValue() + " Model min = " + hm.getMinimum() + ',' + vm.getMinimum() + " Model max = " + hm.getMaximum() + ',' + vm.getMaximum() + " Model val = " + hm.getValue() + ',' + vm.getValue() + " Model extent = " + hm.getExtent() + ',' + vm.getExtent());
        boolean adjustScrollBars = picture.refImage != null && (picture.scale > 0 || xAnchor > 0 || yAnchor > 0) && aspectRatio > 0;
        int imageWidth = 0;
        int imageHeight = 0;
        int oldHPos = 0;
        int oldVPos = 0;
        Insets insets = null;
        int oldImageWidth = 0, oldImageHeight = 0;
        if(adjustScrollBars)
        {
            insets = new Insets(0, 0, 0, 0);
            oldHPos = horizontalScrollBar.getValue();
            oldVPos = verticalScrollBar.getValue();
            imageWidth = picture.blockingGetImageWidth(picture.refImage);
            imageHeight = picture.blockingGetImageHeight(picture.refImage);
            picture.getTotalInsets(insets);
            if(picture.scale > 0)
            {
                oldImageWidth = (int)Math.round(imageWidth*picture.scale);
                oldImageHeight = (int)Math.round(imageHeight*picture.scale);
            }
            else
            {
                Dimension d = picture.getPreferredSize();
                oldImageWidth = d.width - (insets.left + insets.right);
                oldImageHeight = d.height - (insets.top + insets.bottom);
            }
        }
        picture.scale = aspectRatio;
        if(picture.refImage != null)
        {
            picture.invalidate();
            validateTree();
            if(adjustScrollBars)
            {
                if(xAnchor < 0)
                    horizontalScrollBar.setValue(0);
                else
                    adjustScrollBar(horizontalScrollBar, insets.left, oldHPos, xAnchor,
                                oldImageWidth,
                                (int)Math.round(imageWidth*aspectRatio));
                if(yAnchor < 0)
                    verticalScrollBar.setValue(0);
                else
                    adjustScrollBar(verticalScrollBar, insets.top, oldVPos, yAnchor,
                                oldImageHeight,
                                (int)Math.round(imageHeight*aspectRatio));
            }
        }
        // TODO: Delete if its ok
        // picture.revalidate();
    }

    public void setScale(double aspectRatio)
    {
        setScale(aspectRatio, 0, 0);
    }

    public static BufferedImage getImage(String fileName) throws Exception
    {
        InputStream ip = new BufferedInputStream(new FileInputStream(fileName));
        ImageReader reader;
        ImageInputStream iis = ImageIO.createImageInputStream(ip);
        reader = (ImageReader) ImageIO.getImageReaders(iis).next();
        reader.setInput(iis);
        BufferedImage retVal = reader.read(0);
        iis.close();
        ip.close();
        return retVal;
    }

    public static void main(String args[]) throws Exception
    {
        BufferedImage refImage = getImage(args[0]);
        JFrame frame = new JFrame();
        PictureView pictureView = new PictureView();
        pictureView.setImage(refImage);
        pictureView.getPictureComponent().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        pictureView.getPictureComponent().extraInsets = new Insets(16, 16, 16, 16);
        frame.add(pictureView);
        frame.setSize(400, 300);
        frame.show();
        Toolkit toolkit = frame.getToolkit();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String line;
        double scaleTo;
        do
        {
            line = stdin.readLine();
            if(line.indexOf('p') >= 0 || line.indexOf('P') >= 0)
            {
                Image image = toolkit.createImage(line);
                System.out.println("Setting Image: " + image);
                pictureView.setImage(image);
            }
            else
            {
                System.out.println("Setting Scale");
                scaleTo = Double.parseDouble(line);
                pictureView.setScale(scaleTo);
            }
        } while(true);
    }
}
