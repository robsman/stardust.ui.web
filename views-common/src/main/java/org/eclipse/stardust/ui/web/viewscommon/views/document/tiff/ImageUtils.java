/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.views.document.tiff;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.stream.ImageOutputStream;

import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;
import com.sun.media.imageio.plugins.tiff.TIFFTag;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageMetadata;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter;

/**
 * 
 * @author vikas.mishra
 * @since 7.0
 * 
 *        Utility class for image
 */
public final class ImageUtils
{
   public static final String TIFF_FORMAT_NAME = "tiff";
   private static final float TIFF_IMAGE_QUALITY = 0.21f;
   private static final int DEFLATE_COMP_TYPE = 32946;

   private ImageUtils()
   {}

   /**
    * <p>
    * This method returns the TIFF Image Writer
    * 
    * @return
    */
   public static ImageWriter getTiffImageWriter()
   {
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(TIFF_FORMAT_NAME);
      if (writers.hasNext())
      {
         return (ImageWriter) writers.next();
      }
      else
      {
         throw new RuntimeException("Writer not found");
      }
   }

   /**
    * <p>
    * This method returns the TIFF Image Reader
    * 
    * @return
    */
   public static ImageReader getTiffImageReader()
   {
      Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(TIFF_FORMAT_NAME);
      if (readers.hasNext())
      {
         return (ImageReader) readers.next();
      }
      else
      {
         throw new RuntimeException(MessagesViewsCommonBean.getInstance().getString(
               "views.tiffViewer.error.noReaderFound"));
      }
   }

   /**
    * 
    * @param fileName
    */
   public static String removeExtention(String fileName)
   {
      int index = fileName.lastIndexOf('.');
      if (index > 0 && index <= fileName.length() - 2)
      {
         return fileName.substring(0, index);
      }
      return fileName;

   }

   /**
    * 
    * Gets the extension of a filename.
    * <p>
    * This method returns the textual part of the filename after the last dot.
    * 
    * @param fileName
    * @return
    */

   public static String getExtension(String fileName)
   {
      if (fileName == null)
      {
         return null;
      }
      int index = fileName.lastIndexOf('.');
      if (index == -1)
      {
         return "";
      }
      else
      {
         return fileName.substring(index + 1);
      }
   }

   /**
    * <p>
    * This method returns the array of BufferedImages contained within fromIndex to
    * toIndex in original tiff image
    * 
    * @param tiffFile
    * @param from
    * @param to
    * @return
    * @throws Exception
    */
   public static BufferedImage[] extractTIFFImage(byte[] tiffFile, int fromIndex, int toIndex) throws Exception
   {
      ImageReader reader = getTiffImageReader();
      
      reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(tiffFile)));
      BufferedImage[] images = new BufferedImage[toIndex - fromIndex + 1];
      for (int i = 0, j = fromIndex; i < images.length; i++, j++)
      {
         images[i] = reader.read(j);
      }
      return images;

   }

   /**
    * <p>
    * This method returns the array of BufferedImages from original image which fall in
    * updateList pageIndex array
    * 
    * @param tiffFile
    * @param updateList
    * @return
    * @throws Exception
    */
   public static BufferedImage[] extractTIFFImage(byte[] tiffFile, Set<Integer> updateList) throws Exception
   {
      ImageReader reader = getTiffImageReader();
      reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(tiffFile)));
      int noOfPages = reader.getNumImages(true);
      BufferedImage[] images = new BufferedImage[updateList.size()];
      for (int i = 1, j = 0; i <= noOfPages; i++)
      {
         if (updateList.contains(i))
         {
            images[j] = reader.read(i - 1);
            j++;
         }
      }
      return images;
   }

   /**
    * <p>
    * This method returns the byteArray from bufferedImages after compressing and
    * attaching metadata
    * 
    * @param tiffFile
    * @param pages
    * @throws IOException
    */
   public static byte[] createTiffImage(BufferedImage[] pages) throws IOException
   {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageWriter writer = getTiffImageWriter();
      ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
      writer.setOutput(ios);
      writer.prepareWriteSequence(null);
      TIFFImageWriteParam iwp = new TIFFImageWriteParam(writer.getLocale());
      iwp.setCompressionMode(TIFFImageWriteParam.MODE_EXPLICIT);
      iwp.setCompressionType(getTiffCompressionTypes(DEFLATE_COMP_TYPE));
      iwp.setCompressionQuality(TIFF_IMAGE_QUALITY);
      for (int i = 0; i < pages.length; i++)
      {
         ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromRenderedImage(pages[i]);
         TIFFImageMetadata imageMetadata = (TIFFImageMetadata) writer.getDefaultImageMetadata(imageType, iwp);
         imageMetadata = createImageMetadata(imageMetadata, pages[i].getHeight(), pages[i].getWidth(), 1, 1,
               pages[i].getType());
         writer.writeToSequence(new IIOImage(pages[i], null, imageMetadata), iwp);
      }
      ios.flush();
      writer.dispose();
      ios.close();
      return outputStream.toByteArray();
   }

   /**
    * Get compression string
    * 
    * @param compression
    * @return
    */
   private static String getTiffCompressionTypes(int compression)
   {
      String c = null;
      for (int i = 0; i < TIFFImageWriter.compressionTypes.length; i++)
      {
         if (compression == TIFFImageWriter.compressionNumbers[i])
         {
            c = TIFFImageWriter.compressionTypes[i];
            break;
         }
      }
      return c;
   }

   /**
    * Return the image meta data for the new TIF image
    * 
    * @param imageMetadata
    * @return
    * @throws IIOInvalidTreeException
    * 
    *            Based on TIFF V6.0 specifications.</br></br>
    * @see <a href="http://partners.adobe.com/public/developer/en/tiff/TIFF6.pdf">TIFF 6.0
    *      Specification</a>
    */
   private static TIFFImageMetadata createImageMetadata(TIFFImageMetadata imageMetadata, int height, int width,
         int dpi, int compression, int type) throws IIOInvalidTreeException
   {

      /*
       * Fields are arrays Each TIFF field has an associated Count. This means that all
       * fields are actually one-dimensional arrays, even though most fields contain only
       * a single value.
       */
      char[] cImageWidth = new char[] {(char) width};
      char[] cImageLength = new char[] {(char) height};
      char[] cResolutionUnit = new char[] {BaselineTIFFTagSet.RESOLUTION_UNIT_INCH};
      long[][] cDpiResolutionX = new long[][] { {(long) dpi, (long) 1}, {(long) 0, (long) 0}};
      long[][] cDpiResolutionY = new long[][] { {(long) dpi, (long) 1}, {(long) 0, (long) 0}};
      char[] cRowsPerStrip = new char[] {(char) (height)};
      char[] cStripOffsets = new char[] {(char) 1};
      char[] cStripByteCounts = new char[] {(char) 1};

      /*
       * Get the IFD (Image File Directory) which is the root of all the tags for this
       * image. From here we can get all the tags in the image.
       */
      TIFFDirectory ifd = imageMetadata.getRootIFD();

      /*
       * Create the necessary TIFF tags that we want to add to the image meta data
       */
      BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();

      /*
       * Rows and Columns
       */
      TIFFTag tagImageLength = base.getTag(BaselineTIFFTagSet.TAG_IMAGE_LENGTH);
      TIFFTag tagImageWidth = base.getTag(BaselineTIFFTagSet.TAG_IMAGE_WIDTH);
      TIFFField fieldImageLength = new TIFFField(tagImageLength, TIFFTag.TIFF_SHORT, 1, cImageLength);
      TIFFField fieldImageWidth = new TIFFField(tagImageWidth, TIFFTag.TIFF_SHORT, 1, cImageWidth);
      ifd.addTIFFField(fieldImageLength);
      ifd.addTIFFField(fieldImageWidth);

      /*
       * Physical Dimensions
       */
      TIFFTag tagResUnit = base.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT);
      TIFFTag tagXRes = base.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION);
      TIFFTag tagYRes = base.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION);
      TIFFField fieldResUnit = new TIFFField(tagResUnit, TIFFTag.TIFF_SHORT, 1, cResolutionUnit);
      TIFFField fieldXRes = new TIFFField(tagXRes, TIFFTag.TIFF_RATIONAL, 1, cDpiResolutionX);
      TIFFField fieldYRes = new TIFFField(tagYRes, TIFFTag.TIFF_RATIONAL, 1, cDpiResolutionY);
      ifd.addTIFFField(fieldResUnit);
      ifd.addTIFFField(fieldXRes);
      ifd.addTIFFField(fieldYRes);

      /*
       * Location of the Data
       */
      TIFFTag tagRowsPerStrip = base.getTag(BaselineTIFFTagSet.TAG_ROWS_PER_STRIP);
      TIFFTag tagStripOffSets = base.getTag(BaselineTIFFTagSet.TAG_STRIP_OFFSETS);
      TIFFTag tagStripByteCounts = base.getTag(BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS);
      TIFFField fieldRowsPerStrip = new TIFFField(tagRowsPerStrip, TIFFTag.TIFF_SHORT, 1, cRowsPerStrip);
      TIFFField fieldStripOffsets = new TIFFField(tagStripOffSets, TIFFTag.TIFF_SHORT, 1, cStripOffsets);
      TIFFField fieldStripByteCounts = new TIFFField(tagStripByteCounts, TIFFTag.TIFF_SHORT, 1, cStripByteCounts);
      ifd.addTIFFField(fieldRowsPerStrip);
      ifd.addTIFFField(fieldStripOffsets);
      ifd.addTIFFField(fieldStripByteCounts);

      return imageMetadata;

   }

}
