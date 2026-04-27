# MediaUtil - A Java API Package for Media Related Utilities

## Purpose
MediaUtil is an offshoot of the
[MediaChest](https://github.com/drogatkin/MediaChest) project to provide a
well packaged Java API for Media Related Utilities.

## API
Below is the Current API Offering from MediaUtil.

### LLJTran

LLJTran is an API for performing Lossless Transformations on JPEG image files
which also provides the Capability of handling Exif information.

Following are the key features:

- Supports lossless rotation, transpose, transverse and crop
- Trimming or relocating Non Transformable edge blocks similiar to [jpegtran](http://www.ijg.org)
   or processing them
  like regular MCU blocks.
- Reading and Modifying Image Header Information (Exif) including Thumbnail
- Built-in transformation of Thumbnail and Orientation marker
- Supports the IterativeReader and IterativeWriter interfaces in
       MediaUtil's mediautil.gen.directio package enabling things like Sharing
       the jpeg input file with say jdk's ImageReader while reading
- Does <b>not</b> Support Multi-Threading for the same Object to be
      used simultaneously by more than one thread. However different threads
      can have their own LLJTran Objects.
- Requires JDK 8 or better


# Documentation

To use,
download [mediautil-1.2.zip](https://github.com/drogatkin/mediautil/releases/tag/v1.2) and unzip is some folder. Then
include the mediautil-1.2.jar file in your CLASSPATH. The download also includes
source under the *src* directory and documentation including javadocs under the
docs directory.

## Getting Started

For Getting Started please see
[LLJTranTutorial.java](https://github.com/drogatkin/mediautil/blob/master/docs/tutorials/LLJTran/LLJTranTutorial.java) which is a
tutorial with different usage examples.

For API reference please see the [Javadocs](javadocs/index.html).

## Projects using the MediaUtil API

Please inform if your project/product is using MediaUtil so that we can add it to
the below list:<p>

- [javadocs/index.html](https://github.com/drogatkin/MediaChest>MediaChest)


## Contact

[Dmitriy Rogatkin](mailto:metricstream@gmail.com)

[Suresh Mahalingam](mailto:msuresh@cheerful.com)

## License

MediaUtil is free to download, use, modify and redistribute for non-commercial and
and commercial purposes without any warranties of course.

## Other Projects of Suresh

* [Jdatestamp](http://jdatestamp.sourceforge.net): A Lossless Date
Stamper for Digital Pictures


