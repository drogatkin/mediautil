# MediaUtil - A Java API Package for Media Related Utilities


MediaUtil is an offshoot of the
<a href=https://github.com/drogatkin/MediaChest>MediaChest</a> Project to provide a
well packaged Java API for Media Related Utilities.<p>

Below is the Current API Offering from MediaUtil.<p>

<H2>LLJTran</H2>

LLJTran is an API for performing Lossless Transformations on JPEG image files
which also provides the Capability of handling Exif information.

Following are the key features:<p>

<ul>
  <li> Supports lossless rotation, transpose, transverse and crop
  <li> Trimming or relocating Non Transformable edge blocks similiar to
  <a target=_blank href="http://www.ijg.org">jpegtran</a> or processing them
  like regular MCU blocks.
  <li> Reading and Modifying Image Header Information (Exif) including Thumbnail
  <li> Built-in transformation of Thumbnail and Orientation marker
  <li> Supports the IterativeReader and IterativeWriter interfaces in
       MediaUtil's mediautil.gen.directio package enabling things like Sharing
       the jpeg input file with say jkd's ImageReader while reading
  <li>Does <b>not</b> Support Multi-Threading for the same Object to be
      used simultaneously by more than one thread. However different threads
      can have their own LLJTran Objects.
  <li>Requires JDK 1.5 or better
</ul>

<H2>Documentation</H2>

To use
<a href="http://sourceforge.net/project/showfiles.php?group_id=35208&package_id=165212&release_id=360314">
Download mediautil-1.0.zip</a> and extract it to a suitable folder. Then
include the mediautil-1.0.jar file in your CLASSPATH. The download also includes
source under the src directory and documentation including javadocs under the
docs directory.<p>

For Getting Started please see
<a href=tutorials/LLJTran/LLJTranTutorial.java>LLJTranTutorial.java</a> which is a
tutorial with different usage examples.<p>

For API reference please see the <a href=javadocs/index.html>Javadocs</a>.

<H2>Projects using the MediaUtil API</H2>

Please inform if your project/product is using MediaUtil so that we can add it to
the below list:<p>

<ul>
  <li><a href=https://github.com/drogatkin/MediaChest>MediaChest</a>
</ul>

<H2>Contact</H2>

[Dmitriy Rogatkin](mailto:metricstream@gmail.com)

[Suresh Mahalingam](mailto:msuresh@cheerful.com)

## License

MediaUtil is free to download, use, modify and redistribute for non-commercial and
and commercial purposes without any warranties of course.

## Other Projects of Suresh


<a href=http://jdatestamp.sourceforge.net>Jdatestamp</a>: A Lossless Date
Stamper for Digital Pictures


