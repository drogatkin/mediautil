/* MediaUtil LLJTran - $RCSfile: Naming.java,v $
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
 *	$Id: Naming.java,v 1.4 2007/07/23 18:53:53 drogatkin Exp $
 *
 * Some ideas and algorithms were borrowed from:
 * Thomas G. Lane, and James R. Weeks
 */
package mediautil.image.jpeg;

public final class Naming { 
    
    // TODO: extend with expected result type and conversion func
    // for better vieweing
    static final Object[][] ExifTagNames = {
        { new Integer(Exif.NEWSUBFILETYPE),     "NewSubFileType"},
        { new Integer(Exif.IMAGEWIDTH),	"ImageWidth"},
        { new Integer(Exif.IMAGELENGTH),	"ImageLength"},
        { new Integer(Exif.BITSPERSAMPLE),	"BitsPerSample"},
        { new Integer(Exif.COMPRESSION),	"Compression"},
        { new Integer(Exif.PHOTOMETRICINTERPRETATION),	"PhotometricInterpretation"},
        { new Integer(Exif.FILLORDER),	"FillOrder"},
        { new Integer(Exif.DOCUMENTNAME),	"DocumentName"},
        { new Integer(Exif.IMAGEDESCRIPTION),	"ImageDescription"},
        { new Integer(Exif.MAKE),	"Make"},
        { new Integer(Exif.MODEL),	"Model"},
        { new Integer(Exif.STRIPOFFSETS),	"StripOffsets"},
        { new Integer(Exif.ORIENTATION),	"Orientation"},
        { new Integer(Exif.SAMPLESPERPIXEL),	"SamplesPerPixel"},
        { new Integer(Exif.ROWSPERSTRIP),	"RowsPerStrip"},
        { new Integer(Exif.STRIPBYTECOUNTS),	"StripByteCounts"},
        { new Integer(Exif.XRESOLUTION),	"XResolution"},
        { new Integer(Exif.YRESOLUTION),	"YResolution"},
        { new Integer(Exif.PLANARCONFIGURATION),	"PlanarConfiguration"},
        { new Integer(Exif.RESOLUTIONUNIT),	"ResolutionUnit"},
        { new Integer(Exif.TRANSFERFUNCTION),	"TransferFunction"},
        { new Integer(Exif.SOFTWARE),	"Software"},
        { new Integer(Exif.DATETIME),	"DateTime"},
        { new Integer(Exif.ARTIST),	"Artist"},
        { new Integer(Exif.WHITEPOINT),	"WhitePoint"},
        { new Integer(Exif.PRIMARYCHROMATICITIES),	"PrimaryChromaticities"},
        { new Integer(Exif.SUBIFDS),	"SubIFDs"},
        { new Integer(Exif.JPEGTABLES),	"JPEGTables"},
        { new Integer(Exif.TRANSFERRANGE),	"TransferRange"},
        { new Integer(Exif.JPEGPROC),	"JPEGProc"},
        { new Integer(Exif.JPEGINTERCHANGEFORMAT),	"JPEGInterchangeFormat"},
        { new Integer(Exif.JPEGINTERCHANGEFORMATLENGTH),	"JPEGInterchangeFormatLength"},
        { new Integer(Exif.YCBCRCOEFFICIENTS),	"YCbCrCoefficients"},
        { new Integer(Exif.YCBCRSUBSAMPLING),	"YCbCrSubSampling"},
        { new Integer(Exif.YCBCRPOSITIONING),	"YCbCrPositioning"},
        { new Integer(Exif.REFERENCEBLACKWHITE),	"ReferenceBlackWhite"},
        { new Integer(Exif.CFAREPEATPATTERNDIM),	"CFARepeatPatternDim"},
        { new Integer(Exif.CFAPATTERN),	"CFAPattern"},
        { new Integer(Exif.SUBJECTDDISTANCERANGE), "SubjectDistanceRange"},
        { new Integer(Exif.BATTERYLEVEL),	"BatteryLevel"},
        { new Integer(Exif.COPYRIGHT),	"Copyright"},
        { new Integer(Exif.EXPOSURETIME),	"ExposureTime"},
        { new Integer(Exif.FNUMBER),	"FNumber"},
        { new Integer(Exif.IPTC_NAA),	"IPTC/NAA"},
        { new Integer(Exif.EXIFOFFSET),	"ExifOffset"},
        { new Integer(Exif.INTERCOLORPROFILE),	"InterColorProfile"},
        { new Integer(Exif.EXPOSUREPROGRAM),	"ExposureProgram"},
        { new Integer(Exif.SPECTRALSENSITIVITY),	"SpectralSensitivity"},
        { new Integer(Exif.GPSINFO),	"GPSInfo"},
        { new Integer(Exif.ISOSPEEDRATINGS),	"ISOSpeedRatings"},
        { new Integer(Exif.OECF),	"OECF"},
        { new Integer(Exif.EXIFVERSION),	"ExifVersion"},
        { new Integer(Exif.DATETIMEORIGINAL),	"DateTimeOriginal"},
        { new Integer(Exif.DATETIMEDIGITIZED),	"DateTimeDigitized"},
        { new Integer(Exif.COMPONENTSCONFIGURATION),	"ComponentsConfiguration"},
        { new Integer(Exif.COMPRESSEDBITSPERPIXEL),	"CompressedBitsPerPixel"},
        { new Integer(Exif.SHUTTERSPEEDVALUE),	"ShutterSpeedValue"},
        { new Integer(Exif.APERTUREVALUE),	"ApertureValue"},
        { new Integer(Exif.BRIGHTNESSVALUE),	"BrightnessValue"},
        { new Integer(Exif.EXPOSUREBIASVALUE),	"ExposureBiasValue"},
        { new Integer(Exif.MAXAPERTUREVALUE),	"MaxApertureValue"},
        { new Integer(Exif.SUBJECTDISTANCE),	"SubjectDistance"},
        { new Integer(Exif.METERINGMODE),	"MeteringMode"},
        { new Integer(Exif.LIGHTSOURCE),	"LightSource"},
        { new Integer(Exif.FLASH),	"Flash"},
        { new Integer(Exif.FOCALLENGTH),	"FocalLength"},
        { new Integer(Exif.MAKERNOTE),	"MakerNote"},
        { new Integer(Exif.USERCOMMENT),	"UserComment"},
        { new Integer(Exif.SUBSECTIME),	"SubSecTime"},
        { new Integer(Exif.SUBSECTIMEORIGINAL),	"SubSecTimeOriginal"},
        { new Integer(Exif.SUBSECTIMEDIGITIZED),	"SubSecTimeDigitized"},
        { new Integer(Exif.FLASHPIXVERSION),	"FlashPixVersion"},
        { new Integer(Exif.COLORSPACE),	"ColorSpace"},
        { new Integer(Exif.EXIFIMAGEWIDTH),	"ExifImageWidth"},
        { new Integer(Exif.EXIFIMAGELENGTH),	"ExifImageLength"},
        { new Integer(Exif.INTEROPERABILITYOFFSET),	"InteroperabilityOffset"},
        { new Integer(Exif.FLASHENERGY),	"FlashEnergy"},
        { new Integer(Exif.SPATIALFREQUENCYRESPONSE),	"SpatialFrequencyResponse"},
        { new Integer(Exif.FOCALPLANEXRESOLUTION),	"FocalPlaneXResolution"},
        { new Integer(Exif.FOCALPLANEYRESOLUTION),	"FocalPlaneYResolution"},
        { new Integer(Exif.FOCALPLANERESOLUTIONUNIT),	"FocalPlaneResolutionUnit"},
        { new Integer(Exif.SUBJECTLOCATION),	"SubjectLocation"},
        { new Integer(Exif.EXPOSUREINDEX),	"ExposureIndex"},
        { new Integer(Exif.SENSINGMETHOD),	"SensingMethod"},
        { new Integer(Exif.FILESOURCE),	"FileSource"},
        { new Integer(Exif.SCENETYPE),	"SceneType"},
        { new Integer(Exif.FOCALLENGTHIN35MMFILM),  "FocalLengthIn35mmFilm"},
        { new Integer(Exif.SHARPNESS),  "Sharpness"},
        { new Integer(Exif.CUSTOMRENDERED),  "CustomRendered"},
        { new Integer(Exif.SATURATION),  "Saturation"},
        { new Integer(Exif.WHITEBALANCE),  "WhiteBalance"},
        { new Integer(Exif.DIGITALZOOMRATIO),  "DigitalZoomRatio"},
        { new Integer(Exif.CONTRAST),  "Contrast"},
        { new Integer(Exif.GAINCONTROL),  "GainControl"},
        { new Integer(Exif.EXPOSUREMODE),  "ExposureMode"},
        { new Integer(Exif.DIGITALZOOMRATIO),  "DigitalZoomRatio"},
        { new Integer(Exif.PRINTMODE),  "PrintMode"},
        { new Integer(Exif.SCENECAPTURETYPE),  "SceneCaptureType"}
    };

    static final Object[][] CIFFPropsNames = {
        { new Integer(CIFF.K_TC_DESCRIPTION), "Description"},
        { new Integer(CIFF.K_TC_MODELNAME), "ModelName"},
        { new Integer(CIFF.K_TC_FIRMWAREVERSION), "FirmwareVersion"},
        { new Integer(CIFF.K_TC_COMPONENTVESRION), "ComponentVesrion"},
        { new Integer(CIFF.K_TC_ROMOPERATIONMODE), "ROMOperationMode"},
        { new Integer(CIFF.K_TC_OWNERNAME), "OwnerName"},
        { new Integer(CIFF.K_TC_IMAGEFILENAME), "ImageFilename"},
        { new Integer(CIFF.K_TC_THUMBNAILFILENAME), "ThumbnailFilename"},
        
        { new Integer(CIFF.K_TC_TARGETIMAGETYPE), "TargetImageType"},
        { new Integer(CIFF.K_TC_SR_RELEASEMETHOD), "ReleaseMethod"},
        { new Integer(CIFF.K_TC_SR_RELEASETIMING), "ReleaseTiming"},
        { new Integer(CIFF.K_TC_RELEASESETTING), "ReleaseSetting"},
        { new Integer(CIFF.K_TC_BODYSENSITIVITY), "BodySensitivity"},
        
        { new Integer(CIFF.K_TC_IMAGEFORMAT), "ImageFormat"},
        { new Integer(CIFF.K_TC_RECORDID), "RecordId"},
        { new Integer(CIFF.K_TC_SELFTIMERTIME), "SelfTimerTime"},
        { new Integer(CIFF.K_TC_SR_TARGETDISTANCESETTING), "TargetDistanceSetting"},
        { new Integer(CIFF.K_TC_BODYID), "BodyId"},
        { new Integer(CIFF.K_TC_CAPTURETIME), "CaptureTime"},
        { new Integer(CIFF.K_TC_IMAGESPEC), "ImageSpec"},
        { new Integer(CIFF.K_TC_SR_EF), "EF"},
        { new Integer(CIFF.K_TC_MI_EV), "EV"},
        { new Integer(CIFF.K_TC_SERIALNUMBER), "SerialNumber"},
        { new Integer(CIFF.K_TC_SR_EXPOSURE), "Exposure"},
        
        { new Integer(CIFF.K_TC_CAMERAOBJECT), "CameraObject"},
        { new Integer(CIFF.K_TC_SHOOTINGRECORD), "ShootingRecord"},
        { new Integer(CIFF.K_TC_MEASUREDINFO), "MeasuredInfo"},
        { new Integer(CIFF.K_TC_CAMERASPECIFICATION), "CameraSpecification"}
    };

    public static String [] ExifTagTypes = {"B",	// BYTE
            "A",	// ASCII
            "S",	// SHORT
            "L",	// LONG
            "R",	// RATIONAL
            "SB",	// SBYTE
            "U",	// UNDEFINED
            "SS",	// SSHORT
            "SL",	// SLONG
            "SR",	// SRATIONAL
        };
    
    public static String [] OrientationNames = {
        "TopLeft",
        "TopRight",
        "BotRight",
        "BotLeft",
        "LeftTop",
        "RightTop",
        "RightBot",
        "LeftBot" 
    };

    public static String getCIFFTypeName(int type) {
        switch (type & CIFF.K_DATATYPEMASK) {
        case CIFF.K_DT_BYTE:
            return "Byte";
        case CIFF.K_DT_ASCII:
            return "ASCII";
        case CIFF.K_DT_WORD:
            return "Word";
        case CIFF.K_DT_DWORD:
            return "Double word";
        case CIFF.K_DT_BYTE2:
            return "Byte2";
        case CIFF.K_DT_HEAPTYPEPROPERTY1:
            return "Heap1";
        case CIFF.K_DT_HEAPTYPEPROPERTY2:
            return "Heap2";
        }
        return "Unknown";
    }

    public static String getTagName(Integer tag) {
	String result = (String)tagnames.get(tag);
	return (result != null)?result:("0x"+tag.toHexString(tag.intValue()));
    }

    public static String getPropName(Integer tag) {
	String result = (String)propnames.get(tag);
	return (result != null)?result:("0x"+tag.toHexString(tag.intValue()));
    }

    public static String getTypeName(int type) {
        return ExifTagTypes[type-1];
    }

    static java.util.Hashtable tagnames;
    static java.util.Hashtable propnames;

    static {
        tagnames = new java.util.Hashtable(ExifTagNames.length);
        for (int i=0; i< ExifTagNames.length; i++)
            tagnames.put(ExifTagNames[i][0], ExifTagNames[i][1]);
        propnames = new java.util.Hashtable(CIFFPropsNames.length);
        for (int i=0; i< CIFFPropsNames.length; i++)
            propnames.put(CIFFPropsNames[i][0], CIFFPropsNames[i][1]);
    }

}
