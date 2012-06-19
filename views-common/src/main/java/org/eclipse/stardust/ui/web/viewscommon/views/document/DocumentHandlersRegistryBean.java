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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentHandlersRegistryBean implements InitializingBean
{
   private static final String BEAN_NAME = "documentHandlersRegistryBean";
   private List<String> registeredContentHandlers = new ArrayList<String>();
   private Map<MIMEType, String> documentViewersMap = new HashMap<MIMEType, String>();
   private Map<MIMEType, String> documentEditorsMap = new HashMap<MIMEType, String>();
   private MimeTypesHelper ippMimeTypesHelper;

   public static DocumentHandlersRegistryBean getInstance()
   {
      return (DocumentHandlersRegistryBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * Initialize Viewers and Editors Map
    */
   public void afterPropertiesSet() throws Exception
   {
      IDocumentViewer contentHandler;
      MIMEType[] registeredMimeTypes;
      Map<MIMEType, String> tempHandlersMap;

      for (String contentHandlerName : registeredContentHandlers)
      {
         contentHandler = (IDocumentViewer) Class.forName(contentHandlerName.trim()).newInstance();
         registeredMimeTypes = contentHandler.getMimeTypes();
         if (contentHandler instanceof IDocumentEditor)
         {
            tempHandlersMap = documentEditorsMap;
         }
         else
         {
            tempHandlersMap = documentViewersMap;
         }
         ippMimeTypesHelper.registerMimeTypes(registeredMimeTypes);
         for (MIMEType mimeType : registeredMimeTypes)
         {
            if (!tempHandlersMap.containsKey(mimeType))
            {
               tempHandlersMap.put(mimeType, contentHandlerName);
            }
         }
      }
   }
   
   /**
    * @param documentContentInfo
    * @param view
    * @return
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws ClassNotFoundException
    */
   public IDocumentViewer getContentHandler(IDocumentContentInfo documentContentInfo, View view)
         throws InstantiationException, IllegalAccessException, ClassNotFoundException
   {
      IDocumentViewer handler = null;
      if (documentContentInfo.isContentEditable())
      {
         handler = getEditor(documentContentInfo, view);
      }
      if (null == handler)
      {
         handler = getViewer(documentContentInfo, view);
      }
      return handler;
   }

   /**
    * @param documentContentInfo
    * @param view
    * @return
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws ClassNotFoundException
    */
   public IDocumentViewer getViewer(IDocumentContentInfo documentContentInfo, View view) throws InstantiationException,
         IllegalAccessException, ClassNotFoundException
   {
      IDocumentViewer viewer = null;
      MIMEType mimeType = documentContentInfo.getMimeType();
      if (documentViewersMap.containsKey(mimeType))
      {
         viewer = (IDocumentViewer) Class.forName(documentViewersMap.get(mimeType)).newInstance();
         viewer.initialize(documentContentInfo, view);
      }
      return viewer;
   }

   /**
    * @param documentContentInfo
    * @param view
    * @return
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws ClassNotFoundException
    */
   public IDocumentEditor getEditor(IDocumentContentInfo documentContentInfo, View view) throws InstantiationException,
         IllegalAccessException, ClassNotFoundException
   {
      IDocumentEditor editor = null;
      MIMEType mimeType = documentContentInfo.getMimeType();
      if (documentEditorsMap.containsKey(mimeType))
      {
         editor = (IDocumentEditor) Class.forName(documentEditorsMap.get(mimeType)).newInstance();
         editor.initialize(documentContentInfo, view);
      }
      return editor;
   }

   /**
    * Spring configured list
    * 
    * @param registeredContentHandlers
    */
   public void setRegisteredContentHandlers(List<String> registeredContentHandlers)
   {
      this.registeredContentHandlers = registeredContentHandlers;
   }

   /**
    * return the mimetypes for which editors are registered
    * 
    * @return
    */
   public Set<MIMEType> getRegisteredMimeTypes()
   {
      return documentEditorsMap.keySet();
   }

   /**
    * return the mimetypes for which editors are registered
    * 
    * @return
    */
   public Set<MIMEType> getAllRegisteredMimeTypes()
   {
      return documentViewersMap.keySet();
   }

   public void setIppMimeTypesHelper(MimeTypesHelper ippMimeTypeUtils)
   {
      this.ippMimeTypesHelper = ippMimeTypeUtils;
   }
}
