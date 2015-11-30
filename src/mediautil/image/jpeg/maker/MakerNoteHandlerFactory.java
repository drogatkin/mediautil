/* MediaUtil LLJTran - $RCSfile: MakerNoteHandlerFactory.java,v $
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
 *  $Id: MakerNoteHandlerFactory.java,v 1.4 2007/12/15 01:44:24 drogatkin Exp $
 *
 * Some ideas and algorithms were borrowed from:
 * Thomas G. Lane, and James R. Weeks
 * Contribution for Maker Notes handling by Vincent Deconinck
 *
 */
package mediautil.image.jpeg.maker;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.xpath.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.lang.reflect.Constructor;

import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.gen.Log;

/**
 * This factory instaciates a class suited to handle the MakerNote block used for a given make and model
 */
public class MakerNoteHandlerFactory {
    private static final String MAKERNOTE_CONFIG_FILE = "MakerNote.xml";

    /**
     * Returns a handler according to XML config file.
     * @param make
     * @param model
     * @return
     */
    public static MakerNoteHandler getHandler(String make, String model) {
        MakerNoteHandler handler = null;

        if (!AbstractImageInfo.NA.equals(make)) {
            if (!AbstractImageInfo.NA.equals(model)) {
                // Both make and models are specified. Try to find an exact match
                handler = instanciate(make, model);
                if (handler != null) {
                    return handler;
                }
            }
            // Either model was not specified, or no specific handler is defined for this model
            // Try to find a match for the make
            handler = instanciate(make, null);
            if (handler != null) {
                return handler;
            }
        }
        // Either the make was not specified, or no generic handler is defined for the make
        // Get a universal handler
        handler = instanciate(null, null);

        return handler;
    }

    private static MakerNoteHandler instanciate(String make, String model) {
        String className = getHandlerClassName(make, model);
        if (className != null && className.trim().length() >0) {
            try {
                if(true || Log.debugLevel >= Log.LEVEL_DEBUG)
                    System.err.print("Trying " + className + "... ");
                Class handlerClass = Class.forName(className);

                // Get its default constructor
                Constructor ct = handlerClass.getConstructor(null);

                // Call the constructor to get a plugin instance
                MakerNoteHandler handler =  (MakerNoteHandler) ct.newInstance(null);

                if(Log.debugLevel >= Log.LEVEL_DEBUG)
                    System.err.println("OK.");
                return handler;
            }
            catch (Exception e) {
                if(Log.debugLevel >= Log.LEVEL_DEBUG)
                    System.err.println("failed.");
            }
        }
        return null;
    }

    private static String getHandlerClassName(String make, String model) {
        String path;
        if (make != null) {
            if (model != null) {
                path = "/MakerNote/Make[@name='" + make + "']/Model[@name='" + model + "']/Handler";
            }
            else {
                path = "/MakerNote/Make[@name='" + make + "']/Handler";
            }
        }
        else {
            path = "/MakerNote/Handler";
        }
        if(Log.debugLevel >= Log.LEVEL_DEBUG)
            System.err.println("Searching " + path + "... ");
        path += "/@className";
        try {
            XPathFactory factory = XPathFactory.newInstance( XPathFactory.DEFAULT_OBJECT_MODEL_URI );
            XPath xpath = factory.newXPath();
            Document doc = loadXML(MAKERNOTE_CONFIG_FILE);
            if (doc != null) {
                return (xpath.evaluate(path, doc));
            }
        }
        catch (XPathFactoryConfigurationException e) {
            e.printStackTrace();
        }
        catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Document loadXML (String xmlSource) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//            return documentBuilder.parse(new InputSource(ClassLoader.getSystemResourceAsStream(xmlSource)));
//Fix for Error parsing XML: java.net.MalformedURLException
            return documentBuilder.parse(new InputSource(MakerNoteHandlerFactory.class
            		.getClassLoader().getResourceAsStream(xmlSource)));
        }
        catch (Exception e) {
            if(Log.debugLevel >= Log.LEVEL_ERROR)
                System.err.println("Error parsing XML: " + e);
            return null;
        }
    }
}
