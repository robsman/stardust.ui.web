package org.eclipse.stardust.ui.web.documenttriage.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;

public class PdfPageCapture {

   public static byte[] getPageImage(byte[] data, int pageNumber)
   {

      byte[] outputData = null;
      
      // open the file
      Document document = new Document();
      try {
         document.setByteArray(data, 0, data.length, null);
      } catch (PDFException ex) {
         System.out.println("Error parsing PDF document " + ex);
      } catch (PDFSecurityException ex) {
         System.out.println("Error encryption not supported " + ex);
      } catch (FileNotFoundException ex) {
         System.out.println("Error file not found " + ex);
      } catch (IOException ex) {
         System.out.println("Error IOException " + ex);
      }

      // save page captures to file.
      float scale = 1.0f;
      float rotation = 0f;

      // Paint the page content to an image and
      // write the image to a byte array
      BufferedImage image = (BufferedImage) document.getPageImage(
          pageNumber, GraphicsRenderingHints.PRINT, Page.BOUNDARY_CROPBOX, rotation, scale);
      image.flush();

      // clean up resources
      document.dispose();

      outputData = getPNGEnodedImageBytes(image);
      
      return outputData;
   }
   
   public static int getNumPages(byte[] data)
   {
      int numPages = 0;
      
      // open the file
      Document document = new Document();
      try {
         document.setByteArray(data, 0, data.length, null);
      } catch (PDFException ex) {
         System.out.println("Error parsing PDF document " + ex);
      } catch (PDFSecurityException ex) {
         System.out.println("Error encryption not supported " + ex);
      } catch (FileNotFoundException ex) {
         System.out.println("Error file not found " + ex);
      } catch (IOException ex) {
         System.out.println("Error IOException " + ex);
      }

      numPages = document.getNumberOfPages();
      
      document.dispose();
      
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