package org.eclipse.stardust.engine.extensions.templating.imageprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import fr.opensagres.xdocreport.core.document.ImageFormat;
import fr.opensagres.xdocreport.document.images.AbstractInputStreamImageProvider;

public class LocalImageProvider extends AbstractInputStreamImageProvider
{

   private File file;

   public LocalImageProvider(String path)
   {
      super(true);
      this.file = new File(path);
   }

   @Override
   public ImageFormat getImageFormat()
   {
      return ImageFormat.getFormatByResourceName(file.getName());
   }

   @Override
   protected InputStream getInputStream() throws IOException
   {
      return new FileInputStream(this.file);
   }

}
