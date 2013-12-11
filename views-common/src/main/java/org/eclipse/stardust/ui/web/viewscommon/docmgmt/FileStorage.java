package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlerBean.InputParameters;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;

public class FileStorage implements Serializable
{
   private static final long serialVersionUID = -7062477503597547388L;

   private static final String BEAN_NAME = "fileStorage";

   private final static String FIRE_STORAGE_LOCATION = "Carnot.Portal.FileUploadPath";

   private Map<String, String> uuidPathMap = new HashMap<String, String>();

   private Map<String, InputParameters> uuidDocumentHandlerInputParamtMap = new HashMap<String, InputParameters>();

   public static FileStorage getInstance()
   {
      return (FileStorage) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   // Physical Path
   public String pullPath(String uuid)
   {
      String path = uuidPathMap.get(uuid);
      // uuidPathMap.remove(uuid);
      return path;
   }

   public String pushPath(String path)
   {
      String uuid = getUUID();
      uuidPathMap.put(uuid, path);
      return uuid;
   }

   // File System Document
   public InputParameters pullFile(String uuid)
   {
      InputParameters path = uuidDocumentHandlerInputParamtMap.get(uuid);
      // uuidDocumentHandlerInputParamtMap.remove(uuid);
      return path;
   }

   public void pushFile(String uuid, InputParameters file)
   {
      uuidDocumentHandlerInputParamtMap.put(uuid, file);
   }

   private String getUUID()
   {
      return UUID.randomUUID().toString();
   }

   public String getStoragePath(ServletContext servletContext)
   {
      return Parameters.instance().getString(FIRE_STORAGE_LOCATION,
            servletContext.getRealPath("/"));
   }
}
