/**
 * This is a generic package which helps in reducing Disk IO. The features
 * provided by this package are similiar to a pipe within the same thread.<p>
 *
 * LLJTran supports the features provided by this package.<p>
 *
 * The class supporting directio features has to support the
 * {@link mediautil.gen.directio.IterativeReader IterativeReader} and/or
 * {@link mediautil.gen.directio.IterativeWriter IterativeWriter} interfaces.
 * So it has to be able to read and/or write in small chunks as specified by
 * the nextRead(..)/nextWrite(..) methods in these Interfaces. This involves
 * some code reorganization See the
 * {@link mediautil.image.jpeg.LLJTran LLJTran} documentation and source for
 * example.  How this was done in LLJTran is as follows:
 *
 * <ul>
 *   <li> The read/write method is split between an init method and a
 *        nextRead/nextWrite method. The actual read/write method calls the init
 *        method and then repeatedly calls the nextRead/nextWrite method with
 *        large number of bytes till it returns IterativeReader.STOP.
 *   <li> All local variables in the original read/write method are transferred
 *        to a class to make them persistent across calls. For the
 *        nextRead/nextWrite method different stages of Reading or Writing are
 *        converted similar to a State Machine.  Loops are converted so that
 *        you can re-enter the loop and also break from a loop on completion of
 *        number of bytes. The init method has common initializations.
 *   <li> Any read or write call which possibly reads/writes a big array (Like
 *        Appx in LLJTran) is split into iterative calls.
 * </ul>
 *
 * The following can be used with Classes implementing IterativeReader:
 *
 * <ul>
 *   <li> {@link mediautil.gen.directio.SplitInputStream SplitInputStream} for
 *        sharing an Input source from a file with another Object reading from
 *        the file.
 *   <li> {@link mediautil.gen.directio.OutStreamToIterativeReader OutStreamToIterativeReader}
 *        enabling an Object to write directly to an IterativeReader so that
 *        the output need not be written to a temporary file and then be read.
 * </ul>
 *
 * The following can be used with Classes implementing IterativeWriter:
 *
 * <ul>
 *   <li> {@link mediautil.gen.directio.InStreamFromIterativeWriter InStreamFromIterativeWriter}
 *        enabling an Object to read directly from an IterativeWriter so that
 *        the output need not be written to a temporary file and then be read.
 * </ul>
 */
package mediautil.gen.directio;

import mediautil.image.jpeg.LLJTran;
