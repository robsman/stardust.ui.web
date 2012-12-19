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
package org.eclipse.stardust.ui.client.dms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Function;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet.ExecutionServiceProvider;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.ui.client.util.PluginResourceUtils;
import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IFolder;
import org.eclipse.stardust.vfs.RepositoryOperationFailedException;
import org.eclipse.stardust.vfs.impl.jcr.JcrDocumentRepositoryService;
import org.eclipse.stardust.vfs.impl.jcr.web.AbstractVfsContentServlet;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;
import org.eclipse.stardust.vfs.jcr.spring.JcrSpringSessionFactory;
import org.springframework.core.io.Resource;


/**
 * Serves the 'default-skin' artifacts, which are configured for a partition.
 * 
 * This Servlet is invoked with URIs like /<partitionId>/default-skin/<skin-artifacts>
 * It replaces the 'default-skin' work with the actually skin folder configured,
 * and returns the contents of the resource from Repository under SKIN folder
 * 
 * E.g. 
 * /default/default-skin/custom/login.css
 * /default/default-skin/custom/css-images/login-logo.png
 * 
 * @author Subodh.Godbole
 *
 */
public class SkinContentServlet extends AbstractVfsContentServlet
{
   static final long serialVersionUID = 1L;

   public static final String CLIENT_CONTEXT_PARAM = "clientContext";

   public static final String DEFAULT_SKIN_URI = "default-skin";

   public static final String SKIN_PREF_MODULE_ID = "ipp-portal-common";
   public static final String SKIN_PREF_ID = "preference";
   public static final String SKIN_PREF_KEY = "ipp-portal-common.configuration.prefs.skin";
   public static final String EJB = "ejb"; 
   public static final String PLUGIN_FOLDER_PATH = "/public/skins";
   public static final String PLUGIN_ROOT_FOLDER_PATH = "/plugins/";
   private boolean ejbEnvironment = false;

   private String context;

   /**
    * fileUri   will be /<partitionId>/default-skin/<skin-artifacts>
    */
   @Override
   protected int doDownloadFileContent(final String fileUri,
         final ContentDownloadController downloadManager) throws IOException
   {
      Integer status = (Integer) getForkingService().isolate(new Function<Integer>()
      {
         protected Integer invoke()
         {
            int result;

            if (fileUri.contains(DEFAULT_SKIN_URI))
            {
               DecodedRequest request = decodeRequest(fileUri);
               if (StringUtils.isNotEmpty(request.partitionId) && StringUtils.isNotEmpty(request.resource))
               {
                  try
                  {
                     BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
                     rtEnv.initDetailsFactory();

                     // This is required to set the Preference Store
                     PreferenceStorageFactory.getCurrent();

                     // This is required, otherwise
                     // IPreferenceStorageManager#getPreferences() throws NPE!
                     Parameters parameters = Parameters.instance();
                     parameters.set(SecurityProperties.CURRENT_PARTITION,
                           AuditTrailPartitionBean.findById(request.partitionId));

                     String skinFolder = getDefaultSkinId(rtEnv);
                     
                     if (StringUtils.isNotEmpty(skinFolder))
                     {
                        try
                        {
                           if (skinFolder.contains(PLUGIN_FOLDER_PATH))
                           {
                              return loadPluginSkin(skinFolder, request.resourceName, downloadManager);
                           }
                           else
                           {
                              Repository repository = null;
                              if (ejbEnvironment)
                              {
                                 Context context = new InitialContext();
                                 String path = parameters.getString("ejb.contentRepository.path",
                                       "java:comp/env/jcr/ContentRepository");
                                 repository = (javax.jcr.Repository) context.lookup(path);
                              }
                              else
                              {
                                 IDocumentRepositoryService drs = rtEnv.getDocumentRepositoryService();
                                 if (null != drs && drs instanceof JcrDocumentRepositoryService)
                                 {
                                    JcrDocumentRepositoryService jcrDocumentRepositoryService = (JcrDocumentRepositoryService) drs;
                                    ISessionFactory sessionFactory = jcrDocumentRepositoryService.getSessionFactory();
                                    if (sessionFactory instanceof JcrSpringSessionFactory)
                                    {
                                       repository = ((JcrSpringSessionFactory) sessionFactory).getRepository();
                                    }
                                 }
                              }

                              if (null == repository)
                              {
                                 throw new PublicException(
                                       "No JCR document repository is set. Check the configuration.");
                              }

                              JcrDocumentRepositoryService jcrDocumentRepService = new JcrDocumentRepositoryService();
                              jcrDocumentRepService.setSessionFactory(new IppSkinJcrSessionFactory(repository));

                              IFolder folder = jcrDocumentRepService.getFolder(skinFolder);
                              String skinId = folder.getName();

                              request.resourceFullPath = request.resourceFullPath
                                    .replaceFirst(DEFAULT_SKIN_URI, skinId);

                              IFile file = jcrDocumentRepService.getFile(request.resourceFullPath);
                              if (null != file)
                              {
                                 downloadManager.setContentLength((int) file.getSize());
                                 downloadManager.setContentType(file.getContentType());

                                 if (!StringUtils.isEmpty(file.getEncoding()))
                                 {
                                    downloadManager.setContentEncoding(file.getEncoding());
                                 }
                                 downloadManager.setFilename(file.getName());

                                 jcrDocumentRepService.retrieveFileContent(request.resourceFullPath,
                                       downloadManager.getContentOutputStream());

                                 result = HttpServletResponse.SC_OK;
                              }
                              else
                              {
                                 // file not found
                                 result = HttpServletResponse.SC_NOT_FOUND;
                              }
                           }
                        }
                        catch (RepositoryOperationFailedException rofe)
                        {
                           throw new PublicException(MessageFormat.format(
                                 "Failed retrieving content for file ''{0}''.", new Object[] {request.resource}), rofe);
                        }
                        catch (IOException ioe)
                        {
                           throw new PublicException(MessageFormat.format(
                                 "Failed retrieving content for file ''{0}''.", new Object[] {request.resource}), ioe);
                        }
                     }
                     else
                     {
                        // No skin configured. So no content
                        result = HttpServletResponse.SC_NO_CONTENT;
                     }
                  }
                  catch (Exception e)
                  {
                     throw new PublicException(MessageFormat.format("Failed retrieving content for file ''{0}''.",
                           new Object[] {request.resource}), e);
                  }
               }
               else
               {
                  // request can not be decoded
                  result = HttpServletResponse.SC_BAD_REQUEST;
               }
            }
            else
            {
               // This serves only "default-skin" artifacts
               result = HttpServletResponse.SC_FORBIDDEN;
            }

            return result;
         }
      });

      // report outcome
      return status.intValue();
   }

   /**
    * @param rtEnv
    * @return
    */
   private String getDefaultSkinId(BpmRuntimeEnvironment rtEnv)
   {
      IPreferenceStorageManager pm = rtEnv.getPreferenceStore();
      if (null != pm)
      {
         Preferences prefs = pm.getPreferences(PreferenceScope.PARTITION, SKIN_PREF_MODULE_ID, SKIN_PREF_ID);
         return (String)prefs.getPreferences().get(SKIN_PREF_KEY);
      }
      else
      {
         return "";
      }
   }
   
   /**
    * 
    * @param skinFolder
    * @param fileName
    * @param downloadManager
    * @return
    * @throws IOException
    */
   private int loadPluginSkin(String skinFolder, String fileName, ContentDownloadController downloadManager) throws IOException
   {
      Map<String, List<Resource>> pluginAvailableSkins = PluginResourceUtils
            .findPluginSkins(PLUGIN_FOLDER_PATH, fileName);

      if (!CollectionUtils.isEmpty(pluginAvailableSkins))
      {
         for (Map.Entry<String, List<Resource>> entry : pluginAvailableSkins.entrySet())
         {
            if (entry.getKey().equals(skinFolder))
            {
               for (Resource resource : entry.getValue())
               {
                  InputStream is = resource.getInputStream();
                     
                     byte[] bbuf = new byte[4096];
                     int length = 0;
                     OutputStream op = downloadManager.getContentOutputStream();
                     while ((is != null) && ((length = is.read(bbuf)) != -1))
                     {
                        op.write(bbuf, 0, length);
                     }

                     is.close();
                     op.flush();
                     op.close();
                  }
            }
            else
            {
               return HttpServletResponse.SC_NO_CONTENT;
            }
            return HttpServletResponse.SC_OK;
         }
      }
      return HttpServletResponse.SC_NO_CONTENT;
   }

   @Override
   protected int doUploadFileContent(final String fileUri, final InputStream contentStream, final int contentLength,
         final String contentType, final String contentEncoding) throws IOException
   {
      throw new RuntimeException("Not supported");
   }

   /**
    * @param uri
    * @return
    */
   private static DecodedRequest decodeRequest(String uri)
   {
      DecodedRequest result = new DecodedRequest();

      result.partitionId = uri.substring(0, uri.indexOf("/"));
      result.resource = uri.substring(uri.indexOf("/"));
      result.resourceName = uri.substring(uri.lastIndexOf("/") + 1);
      result.resourceFullPath = getSkinsFolderBasePath(result.partitionId) + result.resource;

      return result;
   }

   /**
    * @return
    */
   private ForkingService getForkingService()
   {
      ForkingServiceFactory factory = null;
      ForkingService forkingService = null;
      factory = (ForkingServiceFactory) Parameters.instance().get(
            EngineProperties.FORKING_SERVICE_HOME);
      if (factory == null)
      {
         List<ExecutionServiceProvider> exProviderList = ExtensionProviderUtils
               .getExtensionProviders(ExecutionServiceProvider.class);
         for (ExecutionServiceProvider executionServiceProvider : exProviderList)
         {
            forkingService = executionServiceProvider.getExecutionService(context);
            if (forkingService != null)
            {
               break;
            }
         }
      }
      else
      {
         forkingService = factory.get();
      }
      return forkingService;
   }

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);

      context = config.getInitParameter(CLIENT_CONTEXT_PARAM);
      context = context != null ? context.toLowerCase() : null;
      
      if (EJB.equalsIgnoreCase(context))
      {
         ejbEnvironment = true;
      }
   }

   /**
    * @param partitionId
    * @return
    */
   private static String getSkinsFolderBasePath(String partitionId)
   {
      StringBuffer folderPath = new StringBuffer(1024);
      folderPath.append(DocumentRepositoryFolderNames.getRepositoryRootFolder());
      folderPath.append(DocumentRepositoryFolderNames.PARTITIONS_FOLDER).append(partitionId).append("/")
            .append(DocumentRepositoryFolderNames.ARTIFACTS_FOLDER);

      folderPath.append(DocumentRepositoryFolderNames.SKINS_FOLDER);

      String folderPathString = folderPath.toString();
      if (folderPathString.endsWith("/"))
      {
         folderPath.deleteCharAt(folderPath.length() - 1);
         folderPathString = folderPath.toString();
      }

      return folderPathString;
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   private static class DecodedRequest
   {
      public String partitionId;
      public String resource;
      public String resourceFullPath;
      public String resourceName;
   }

   private String getJcrUserProperty()
   {
      final Parameters params = Parameters.instance();
      return params.getString("ContentRepository.User", "jcr-user");
   }

   private String getJcrPasswordProperty()
   {
      final Parameters params = Parameters.instance();
      return params.getString("ContentRepository.Password", "jcrPassword");
   }
  
   /**
    * @author Yogesh.Manware
    * 
    */
   private class IppSkinJcrSessionFactory implements ISessionFactory
   {
      Repository repository;

      private IppSkinJcrSessionFactory(final Repository repository)
      {
         this.repository = repository;
      }

      public Session getSession() throws RepositoryException
      {
         SimpleCredentials credentials = new SimpleCredentials(getJcrUserProperty(), getJcrPasswordProperty()
               .toCharArray());
         return this.repository.login(credentials);
      }

      public void releaseSession(Session session)
      {}
   }
}
