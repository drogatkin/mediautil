/* MediaUtil MediaInfo - $RCSfile: MediaInfo.java,v $
 * Copyright (C) 1999-2013 Dmitriy Rogatkin, Suresh Mahalingam.  All rights reserved.
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
 *	$Id: MediaInfo.java,v 1.11 2013/05/20 04:42:56 drogatkin Exp $
 *
 */
package mediautil.gen;

import java.io.Serializable;

public interface MediaInfo extends Serializable {
	// TODO: think about using int instead of string for name
	public static final String ESS_CHARACHTER = "charachter";
	public static final String ESS_TIMESTAMP = "timestamp";
	public static final String ESS_QUALITY = "quality";
	public static final String ESS_MAKE = "make";

	public static final String ALBUM = "Album";
	public static final String ALBUMARTIST = "AlbumArtist";
	public static final String ARTIST = "Artist";
	public static final String ARTISTWEBPAGE = "ArtistWebpage";
	public static final String AUDIOFILEWEBPAGE = "AudioFileWebpage";
	public static final String AUDIOSOURCEWEBPAGE = "AudioSourceWebpage";
	public static final String BAND = "Band";
	public static final String BITRATE = "Bitrate";
	public static final String VBR = "VBR";
	public static final String CDIDENTIFIER = "CDIdentifier";
	public static final String COMMENTS = "Comments";
	public static final String COMMERCIAL = "Commercial";
	public static final String COMMERCIALINFORMATION = "CommercialInformation";
	public static final String COMPOSER = "Composer";
	public static final String CONDUCTOR = "Conductor";
	public static final String CONTENTGROUP = "ContentGroup";
	public static final String COPYRIGHTTEXT = "CopyrightText";

	public static final String COPYRIGHT = "Copyright";
	public static final String COPYRIGHTWEBPAGE = "CopyrightWebpage";
	public static final String DATE = "Date";
	public static final String EMPHASIS = "Emphasis";
	public static final String ENCAPSULATEDOBJECT = "EncapsulatedObject";
	public static final String ENCODEDBY = "EncodedBy";
	public static final String ENCRYPTIONMETHODREGISTRATION = "EncryptionMethodRegistration";
	public static final String EQUALISATION = "Equalisation";
	public static final String EVENTTIMINGCODES = "EventTimingCodes";
	public static final String FILEOWNER = "FileOwner";
	public static final String FILESIZE = "FileSize";
	public static final String FILETYPE = "FileType";
	public static final String GENRE = "Genre";
	public static final String BPM = "BPM"; // (beats per minute)
	public static final String RELATIVEVOLUMENADJUSTMENT = "RelativeVolumenAdjustment";
	public static final String GROUPIDENTIFICATIONREGISTRATION = "GroupIdentificationRegistration";
	public static final String INITIALKEY = "InitialKey"; //Musical key in which sound starts.
	public static final String INTERNETRADIOSTATIONNAME = "InternetRadioStationName";
	public static final String INTERNETRADIOSTATIONOWNER = "InternetRadioStationOwner";
	public static final String INTERNETRADIOSTATIONWEBPAGE = "InternetRadioStationWebpage";
	public static final String ISRC = "ISRC"; // International Standard Recording Code
	public static final String LANGUAGE = "Language";
	public static final String LAYER = "Layer";
	public static final String LENGTH = "Length";
	public static final String LENGTHINTAG = "LengthInTag";
	public static final String LOOKUPTABLE = "LookupTable";
	public static final String LYRICIST = "Lyricist";
	public static final String LYRICS = "Lyrics";
	public static final String MEDIATYPE = "MediaType";
	public static final String MODE = "Mode";
	public static final String MPEGLEVEL = "MPEGLevel";
	public static final String ORIGINAL = "Original";
	public static final String ORIGINALARTIST = "OriginalArtist";
	public static final String ORIGINALFILENAME = "OriginalFilename";
	public static final String ORIGINALLYRICIST = "OriginalLyricist";
	public static final String ORIGINALTITLE = "OriginalTitle";
	public static final String ORIGINALYEAR = "OriginalYear";
	public static final String OWNERSHIP = "Ownership";
	public static final String PAYMENTWEBPAGE = "PaymentWebpage";
	public static final String PICTURE = "Picture";
	public static final String PLAYCOUNTER = "PlayCounter";
	public static final String PARTOFSET = "PartOfSet";
	public static final String PLAYLISTDELAY = "PlaylistDelay";
	public static final String POPULARIMETER = "Popularimeter";
	public static final String PRIVATEDATA = "PrivateData";
	public static final String LASTMODIFIED = "LastModified";

	public static final String SKIPCOUNTER = "SkipCounter";
	public static final String LASTSKIPPED = "LastSkipped";

	public static final String PRIVATE = "Private";
	public static final String PROTECTION = "Protection";
	public static final String PUBLISHER = "Publisher";
	public static final String PUBLISHERSWEBPAGE = "PublishersWebpage";
	public static final String RECORDINGDATES = "RecordingDates";
	public static final String REMIXER = "Remixer";
	public static final String REVERB = "Reverb";
	public static final String SAMPLERATE = "Samplerate";
	public static final String SUBTITLE = "Subtitle";
	public static final String TERMSOFUSE = "TermsOfUse";
	public static final String TIME = "Time";
	public static final String TITLE = "Title";
	public static final String TRACK = "Track";
	public static final String OFTRACKS = "OfTracks";
	public static final String UNIQUEFILEIDENTIFIER = "UniqueFileIdentifier";
	public static final String USERDEFINEDTEXT = "UserDefinedText";
	public static final String YEAR = "Year";
	public static final String RATING = POPULARIMETER;
	public static final String LASTPLAY = "LastPlay";
	public static final String COMPILATION = "Compilation";

	// Video relates
	public static final String SHOW = "Show";
	public static final String SEASON_NUM = "Season #";
	public static final String EPISODE_ID = "Episode ID";
	public static final String EPISODE_NUM = "Episode #";

	public static final String RESOLUTIONX = "ResolutionX";
	public static final String RESOLUTIONY = "ResolutionY";
	public static final String MAKE = "Make";
	public static final String MODEL = "Model";
	public static final String DATETIMEORIGINAL = "DateTimeOriginal";
	public static final String DATETIMEORIGINALSTRING = "DateTimeOriginalString";
	public static final String FNUMBER = "FNumber";
	public static final String APERTURE = FNUMBER;
	public static final String SHUTTER = "Shutter";
	public static final String FLASH = "Flash";
	public static final String ORIENTATION = "Orientation";
	public static final String QUALITY = "Quality";
	public static final String FOCALLENGTH = "FocalLength";
	public static final String METERING = "Metering";
	public static final String EXPOPROGRAM = "ExpoProgram";
	public static final String FORMAT = "Format";

	public static final String THUMBNAIL = "ThumbnailIcon";

	public static final int CLASS_AUDIO = 2;
	public static final int CLASS_VIDEO = 4;
	public static final int CLASS_IMAGE = 1;

	// Methods
	public abstract Object getAttribute(String name);
	
	/*public <T>T getAttribute(String name, T defVal) {
		try {
		Object result = getAttribute(String name);
		if (result != null) {
			if (defVal != null)
				return defVal.getClass().cast(result);
			else
				return (T)result;
		}
		}catch(Exception e) {
			
		}
		return defval;
	}*/
	
	public abstract int getIntAttribute(String name);

	public abstract float getFloatAttribute(String name);

	public abstract long getLongAttribute(String name);

	public abstract boolean getBoolAttribute(String name);

	public abstract double getDoubleAttribute(String name);

	public Object[] getFiveMajorAttributes();

	public abstract void setAttribute(String name, Object value);

	public abstract <C extends MediaComponent> C[] getComponents();
	//public abstract int getMediaClass();

	public static final String MEDIA_ATTRIBUTES[] = { BITRATE, FILESIZE, LAYER, LENGTH, MODE, MPEGLEVEL, SAMPLERATE };

	public static final String PLAY_ATTRIBUTES[] = { ALBUM, ALBUMARTIST, ARTIST, ARTISTWEBPAGE, AUDIOFILEWEBPAGE,
			AUDIOSOURCEWEBPAGE, BAND, CDIDENTIFIER, COMMENTS, COMMERCIAL, COMMERCIALINFORMATION, COMPOSER, CONDUCTOR,
			CONTENTGROUP, COPYRIGHTTEXT, COPYRIGHT, COPYRIGHTWEBPAGE, DATE, EMPHASIS, ENCAPSULATEDOBJECT, ENCODEDBY,
			ENCRYPTIONMETHODREGISTRATION, EQUALISATION, EVENTTIMINGCODES, FILEOWNER, FILETYPE, GENRE,
			GROUPIDENTIFICATIONREGISTRATION, INITIALKEY, INTERNETRADIOSTATIONNAME, INTERNETRADIOSTATIONOWNER,
			INTERNETRADIOSTATIONWEBPAGE, ISRC, LANGUAGE, LENGTHINTAG, LOOKUPTABLE, LYRICIST, LYRICS, RELATIVEVOLUMENADJUSTMENT,
			MEDIATYPE, ORIGINAL, ORIGINALARTIST, ORIGINALFILENAME, ORIGINALLYRICIST, ORIGINALTITLE, ORIGINALYEAR,
			OWNERSHIP, PAYMENTWEBPAGE, BPM, PARTOFSET, COMPILATION, PICTURE, PLAYCOUNTER, PLAYLISTDELAY, POPULARIMETER,
			PRIVATEDATA,

			PRIVATE, PROTECTION, PUBLISHER, PUBLISHERSWEBPAGE, RECORDINGDATES, REMIXER, REVERB, SUBTITLE, TERMSOFUSE,
			TIME, TITLE, TRACK, UNIQUEFILEIDENTIFIER, USERDEFINEDTEXT, YEAR };

	public static final String PICTURE_ATTRIBUTES[] = { RESOLUTIONX, RESOLUTIONY, MAKE, MODEL, DATETIMEORIGINAL,
			DATETIMEORIGINALSTRING, APERTURE, SHUTTER, FLASH, QUALITY, FOCALLENGTH, METERING, EXPOPROGRAM, FORMAT };

}
