package org.eclipse.stardust.ui.web.documenttriage.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

public class TiffReader
{
   public static byte[] getPageImage(byte[] data, int pageNumber) {

      byte[] output = null;
      
      ImageReader reader = getTIFFReader();
      try
      {
         reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(data)));
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      BufferedImage bufferedImage = null;
      try
      {
         synchronized (reader)
         {
            bufferedImage = reader.read(pageNumber, null);
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      
      output = getPNGEnodedImageBytes(bufferedImage);
      bufferedImage.flush();
      reader.dispose();
      
      return output;
   }

   public static int getNumPages(byte[] data)
   {
      int numPages = 0;
      
      ImageReader reader = getTIFFReader();
      try
      {
         reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(data)));
         numPages = reader.getNumImages(true);
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      reader.dispose();
      
      return numPages; 
   }
   
   private static ImageReader getTIFFReader()
   {
      final int TIFF_READER_SCAN_RETRY_COUNT_MAX = 1;
      final String TIFF_FORMAT_NAME = "tiff";
      int retryCount = 0;
      Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(TIFF_FORMAT_NAME);
      
      while (!readers.hasNext())
      {
         if (retryCount < TIFF_READER_SCAN_RETRY_COUNT_MAX)
         {
            ImageIO.scanForPlugins();            
            readers = ImageIO.getImageReadersByFormatName(TIFF_FORMAT_NAME);
            retryCount++;
         }
         else
         {
            System.out.println("TIFF Error");
         }
      }

      ImageReader reader = (ImageReader) readers.next();
      return reader;
   }
   
   /**
    * @param image
    * @return
    */
   public static byte[] getPNGEnodedImageBytes(BufferedImage image)
   {
      final String IMAGE_ENCODING = "png";
      
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(IMAGE_ENCODING);
      if (!writers.hasNext())
      {
         throw new RuntimeException("no image writer");
      }
      ImageWriter writer = writers.next();
      try
      {
         writer.setOutput(ImageIO.createImageOutputStream(bs));
         writer.write(image);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error in converting the image to byte array");
      }

      return bs.toByteArray();
   }
   
}
