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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;



/**
 * @author Subodh.Godbole
 *
 */
public class XPathCacheManager
{
   public static final String PARAM_ENABLED = "Carnot.Client.Caching.ClientXPathMap.Enabled";
   public static final String BEAN_ID = "ippXPathCacheManager";
   public static final Logger trace = LogManager.getLogger(XPathCacheManager.class);

   private boolean cacheEnabled = true;

   // Key = TypeDeclarationCacheKey (QName + Model OID)
   private Map<TypeDeclarationCacheKey, IXPathMap> typeXPathMapCache = new ConcurrentHashMap<TypeDeclarationCacheKey, IXPathMap>();
   
   // Key = model OID + element OID of the Data
   private Map<Long, IXPathMap> dataXPathMapCache = new ConcurrentHashMap<Long, IXPathMap>();

   /**
    * 
    */
   public XPathCacheManager()
   {
      Parameters parameters = Parameters.instance();
      cacheEnabled = parameters.getBoolean(PARAM_ENABLED, true);
   }

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
         dataXPathMapCache.clear();
         typeXPathMapCache.clear();
      }
      catch (Exception e)
      {
         dataXPathMapCache = null;
         typeXPathMapCache = null;
         
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
      Data data = model.getData(dataPath.getData());

      if (!cacheEnabled)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("ClientXPathMap Caching is not enabled. Creating New and returning the same");
         }
         return ClientXPathMap.getXpathMap(model, data);
      }

      // Caching Mechanism
      Long dataCachKey = getDataCacheKey(model, data);
      IXPathMap xPathMap = dataXPathMapCache.get(dataCachKey);
      if (null == xPathMap)
      {
         TypeDeclarationCacheKey typeCachKey = getTypeCacheKey(model, data);
         xPathMap = typeXPathMapCache.get(typeCachKey);
         if (null == xPathMap)
         {
            xPathMap = createIXPathMap(model, data, typeCachKey);
            typeXPathMapCache.put(typeCachKey, xPathMap);
            if (trace.isDebugEnabled())
            {
               trace.debug("Added XPathMap to typeXPathMapCache, Key = " + typeCachKey);
            }
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Returning XPathMap from Type Cache, for typeCachKey = " + typeCachKey);
            }
         }

         // Put the xPathMap in Data Cache
         dataXPathMapCache.put(dataCachKey, xPathMap);
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Returning XPathMap from Data Cache, for dataCachKey = " + dataCachKey);
         }
      }

      //printCacheKeys();

      return xPathMap;
   }

   /**
    * @param model
    * @param data
    * @return
    */
   private Long getDataCacheKey(Model model, Data data)
   {
      long key = model.getModelOID() << 32 + data.getElementOID();
      return key;
   }

   /**
    * @param model
    * @param data
    * @return
    */
   private TypeDeclarationCacheKey getTypeCacheKey(Model model, Data data)
   {
      TypeDeclaration typeDeclaration = null;

      String dataTypeId = data.getTypeId();
      if (dataTypeId.equals(StructuredDataConstants.STRUCTURED_DATA))
      {
         // user-defined structured data
         String typeDeclarationId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
      }
      else
      {
         // build-in schema
         String metadataComplexTypeName = (String)data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
         typeDeclaration = model.getTypeDeclaration(metadataComplexTypeName);
      }

      XSDSchema schema = StructuredTypeRtUtils.getXSDSchema(model, typeDeclaration);
      XSDNamedComponent component = StructuredTypeRtUtils.findElementOrTypeDeclaration(schema, typeDeclaration.getId(),
            true);

      QName qName = null;
      if (null != component.getTargetNamespace())
      {
         qName = new QName(component.getTargetNamespace(), component.getName());
      }
      else
      {
         qName = new QName(component.getName());
      }

      return new TypeDeclarationCacheKey(qName, model.getModelOID());
   }

   /**
    * @param model
    * @param data
    * @param typeCachKey
    * @return
    */
   private IXPathMap createIXPathMap(Model model, Data data, TypeDeclarationCacheKey typeCachKey)
   {
      IXPathMap xPathMap = ClientXPathMap.getXpathMap(model, data);
      if (trace.isDebugEnabled())
      {
         trace.debug("Created new XPathMap for typeCachKey = " + typeCachKey);
      }

      // TODO: To compare the contents of newly created IXPathMap with typeXPathMapCache.
      // Because for internal structured data that have been changed in 2 model versions,
      // they will still have the same QName, but different content.
      // Compare the content of the maps, and only if the content are equal then ignore
      // the newly created map and use the one already existing in typeXPathMapCache.

      return xPathMap;
   }

   /**
    * 
    */
   private void printCacheKeys()
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Data Cache Keys =");
         Iterator<Long> it1 = dataXPathMapCache.keySet().iterator();
         while (it1.hasNext())
         {
            trace.debug("\t[DataCache]:" + it1.next());
         }
         
         trace.debug("Type Cache Keys =");
         Iterator<TypeDeclarationCacheKey> it2 = typeXPathMapCache.keySet().iterator();
         while (it2.hasNext())
         {
            trace.debug("\t[TypeCache]:" + it2.next());
         }
      }
   }

   /**
    * @author florin.herinean
    *
    */
   private static class TypeDeclarationCacheKey
   {
      private QName qname;
      private int modelOID;

      /**
       * @param qname
       * @param modelOID
       */
      public TypeDeclarationCacheKey(QName qname, int modelOID)
      {
         this.qname = qname;
         this.modelOID = modelOID;
      }

      @Override
      public int hashCode()
      {
         return 961 + 31 * modelOID + qname.hashCode();
      }

      @Override
      public boolean equals(Object obj)
      {
         return this == obj || obj instanceof TypeDeclarationCacheKey
               && modelOID == ((TypeDeclarationCacheKey) obj).modelOID
               && qname.equals(((TypeDeclarationCacheKey) obj).qname);
      }
 
      @Override
      public String toString()
      {
         return qname + ":" + modelOID;
      }

      public QName getQname()
      {
         return qname;
      }

      public int getModelOID()
      {
         return modelOID;
      }
   }
}