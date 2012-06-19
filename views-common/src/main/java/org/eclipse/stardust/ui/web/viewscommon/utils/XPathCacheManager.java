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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class XPathCacheManager
{
   public static final String BEAN_ID = "ippXPathCacheManager";
   public static final Logger trace = LogManager.getLogger(XPathCacheManager.class);

   private Map<String, IXPathMap> xPathMapCache = new HashMap<String, IXPathMap>();

   /**
    * @return
    */
   public static XPathCacheManager getInstance()
   {
      return (XPathCacheManager) FacesUtils.getBeanFromContext(BEAN_ID);
   }

   /**
    * 
    */
   public void reset()
   {
      try
      {
         xPathMapCache.clear();
      }
      catch (Exception e)
      {
         xPathMapCache = null;
         trace.error("Error while resetting XPathCacheManager", e);
      }
   }
   
   /**
    * @param model
    * @param dataPath
    * @return
    */
   public IXPathMap getXpathMap(Model model, DataPath dataPath)
   {
      IXPathMap xPathMap = null;

      String cachKey = getCacheKey(model, dataPath);
      if (xPathMapCache.containsKey(cachKey))
      {
         if (trace.isInfoEnabled())
         {
            trace.info("Reading XPathMap from cache for-> DataPath: " + dataPath.getId() + ", CacheKey: " + cachKey);
         }

         xPathMap = xPathMapCache.get(cachKey);
      }
      else
      {          
         if (trace.isInfoEnabled())
         {
            trace.info("Computing XPathMap for-> DataPath: " + dataPath.getId() + ", CacheKey: " + cachKey);
         }

         Model refModel = getReferenceModel(model, dataPath);
         xPathMap = ClientXPathMap.getXpathMap(refModel, model.getData(dataPath.getData()));
         xPathMapCache.put(cachKey, xPathMap);
      }
      
      return xPathMap;
   }
   
   /**
    * @param model
    * @param dataPath
    * @return
    */
   private String getCacheKey(Model model, DataPath dataPath)
   {
      return model.getModelOID() + ":" + dataPath.getElementOID();
   }

   /**
    * @param model
    * @param dp
    * @return
    */
   private Model getReferenceModel(Model model, DataPath dp)
   {
      Data data = model.getData(dp.getData());
      Reference ref = data.getReference();
      Model refModel = model;

      if (ref != null)
      {
        refModel = ModelCache.findModelCache().getModel(ref.getModelOid());
      }
      
      return refModel;
   }
}