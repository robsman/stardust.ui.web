package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils.REPOSITORY_ID_PREFIX;

import org.eclipse.stardust.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.stardust.engine.api.runtime.CredentialProvider;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

public class Util
{
   private final static Logger log = LoggerFactory.getLogger(Util.class);

   /**
    * return portal session factory
    * 
    * @return
    */
   public static ServiceFactory getServiceFactory()
   {
      ServiceFactory sf = ServiceFactoryUtils.getServiceFactory();
      if (sf == null)
      {
         if (log.isDebugEnabled())
            log.debug("Web ServiceFactory is null; looking up Embedded ServiceFacoty");
         sf = ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
      }
      return sf;
   }

   public static DocumentManagementService getDocumentManagementService(ServiceFactory sf)
   {
      return sf.getDocumentManagementService();
   }

   public static String composeRepositoryLocationForTemplates(String location)
   {
      if (isValidDocumentRepositoryId(location))
         return location;
      if (!location.startsWith("/"))
      {
         if (!location.contains("templates/"))
            location = "templates/" + location;

         location = "/artifacts/" + location;
      }
      return location;
   }

   /**
    * Returns true if the request format equals docx
    * 
    * @param request
    * @return
    */
   public static boolean isDocx(TemplatingRequest request)
   {
      return request.getFormat().equalsIgnoreCase("docx");
   }

   public static boolean isValidDocumentRepositoryId(String location)
   {
      String input = null;
      if (StringUtils.isNotEmpty(location))
         input = location.replace("repository://", "");
      return input.startsWith(REPOSITORY_ID_PREFIX);
   }

   public static boolean isClassPathOrRepositoryLocation(TemplatingRequest request)
   {
      return StringUtils.isNotEmpty(request.getTemplateUri())
            && (request.getTemplateUri().startsWith("classpath://")
                  || request.getTemplateUri().startsWith("repository://"));
   }
}
