package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceStoreUtils;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

import com.icesoft.faces.context.Resource;

/**
 * @author Shrikant.Gangal
 *
 */
public class PreferencesResource implements Resource, Serializable
{
   private static final long serialVersionUID = 1L;
   private final Date lastModified;
   private List<Preferences> preferencesList;
   private ServiceFactory serviceFactory;
   
   /**
    * @param preferencesList
    */
   public PreferencesResource(List<Preferences> preferencesList)
   {
      this.preferencesList = preferencesList;
      this.lastModified = new Date();
      serviceFactory = SessionContext.findSessionContext().getServiceFactory();
   }

   /* (non-Javadoc)
    * @see com.icesoft.faces.context.Resource#calculateDigest()
    */
   public String calculateDigest()
   {
      return "" + new Date().getTime(); //Added randomization to avoid exporting cached data.
   }

   public Date lastModified()
   {
      return lastModified;
   }

   /* (non-Javadoc)
    * @see com.icesoft.faces.context.Resource#open()
    */
   public InputStream open() throws IOException
   {
      ByteArrayOutputStream outputStream = null;
      InputStream inputStream = null;

      try
      {
         outputStream = new ByteArrayOutputStream();

         if (preferencesList != null)
         {
            PreferenceStoreUtils.backupToZipFile(outputStream, preferencesList, serviceFactory);
         }

         byte[] bytes = outputStream.toByteArray();
         inputStream = new ByteArrayInputStream(bytes);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      finally
      {
         FileUtils.close(outputStream);
      }

      return inputStream;
   }

   /* (non-Javadoc)
    * @see com.icesoft.faces.context.Resource#withOptions(com.icesoft.faces.context.Resource.Options)
    */
   public void withOptions(Options arg0) throws IOException
   {}
}