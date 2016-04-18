package org.eclipse.stardust.engine.extensions.velocity.tool;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.*;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

@DefaultKey("dms")
@InvalidScope({Scope.APPLICATION, Scope.SESSION})
public class DocumentManagementServiceTool extends SafeConfig
{
   private ServiceFactory sf;

   private DocumentManagementService dms;

   public void configure(ValueParser parser)
   {

   }

   private void initialize()
   {
      sf = getServiceFactory();
      dms = getDocumentManagementService(sf);
   }

   public byte[] retrieveContent(String path)
   {
      initialize();
      return dms.retrieveDocumentContent(path);
   }
   
   public byte[] retrieveContent(String path,ServiceFactory sf )
   {
      if(sf!=null)
         return sf.getDocumentManagementService().retrieveDocumentContent(path);
      return null;
   }
}
