package org.eclipse.stardust.engine.extensions.templating.imageprovider;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.getDocumentManagementService;
import static org.eclipse.stardust.engine.extensions.templating.core.Util.getServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;

import fr.opensagres.xdocreport.core.document.ImageFormat;
import fr.opensagres.xdocreport.document.images.AbstractInputStreamImageProvider;

public class JCRImageProvider extends AbstractInputStreamImageProvider
{
   private DocumentManagementService dms;

   private Document document;

   private byte[] content;

   public JCRImageProvider(String documentId)
   {
      super(true);
      dms = getDocumentManagementService(getServiceFactory());
      document = dms.getDocument(documentId);
      if (document != null)
         content = dms.retrieveDocumentContent(documentId);
   }

   @Override
   public ImageFormat getImageFormat()
   {
      if (document != null)
         return ImageFormat.getFormatByResourceName(document.getName());
      return null;
   }

   @Override
   protected InputStream getInputStream() throws IOException
   {
      if (content != null)
         return new ByteArrayInputStream(content);
      return null;
   }

}
