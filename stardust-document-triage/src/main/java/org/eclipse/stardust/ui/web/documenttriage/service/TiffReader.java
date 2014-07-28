package org.eclipse.stardust.ui.web.documenttriage.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.ImageUtils;

public class TiffReader
{
   public static byte[] getPageImage(byte[] data, int pageNumber)
   {
      BufferedImage[] extractedTiffImages = null;
      byte[] extractedImage = null;
      
      try
      {
         HashSet<Integer> pageSet = new HashSet<Integer>();
         pageSet.add(pageNumber + 1);
         
         extractedTiffImages = ImageUtils.extractTIFFImage(data, pageSet);
         extractedImage = getPNGEnodedImageBytes(extractedTiffImages[0]);
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      return extractedImage;
   }

   public static byte[] getSplitTiff(byte[] data, Set<Integer> pageNumbers)
   {
      byte[] tiffDocument = null;
      
      try
      {
         tiffDocument = ImageUtils.createTiffImage(ImageUtils.extractTIFFImage(data, pageNumbers));
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      return tiffDocument;
   }
   
   public static int getNumPages(byte[] data)
   {
      int numPages = 0;
      
      ImageReader reader = ImageUtils.getTiffImageReader();
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
