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
package org.eclipse.stardust.ui.web.viewscommon.common.structureddata;

import java.util.List;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;


/**
 * Wraps structured data value (Collection API) for displaying in JSF
 */
public class ComplexTypeWrapper 
{
   private ModelCreator modelCreator;
   private Object complexType;
   private String dataMappingId;
   private String startXPath;
   private IXPathMap xPathMap;
   
   /**
    * @param dataMappingId
    * @param complexType
    */
   public ComplexTypeWrapper(String dataMappingId, Object complexType)
   {
      // find data mapping with this id for current activity instance
      this(findDataMapping(dataMappingId), complexType);
   }

   /**
    * @param dataMapping
    * @param complexType
    */
   public ComplexTypeWrapper(DataMapping dataMapping, Object complexType)
   {
      if (null != dataMapping)
      {
         Model model = ModelCache.findModelCache().getModel(dataMapping.getModelOID());
         Data data = model.getData(dataMapping.getDataId());
         // TODO (ab) optimize: do not parse schema every time, extend ModelCache 
         // to keep arbitrary runtime client attributes
         // xpathmap must also be cached
         this.init(dataMapping.getId(), dataMapping.getDataPath(), complexType, ClientXPathMap.getXpathMap(model, data));
      }
   }  
   
   /**
    * @param dataMappingId
    * @param dataPath
    * @param complexType
    * @param xPathMap
    */
   public ComplexTypeWrapper(String dataMappingId, String dataPath, Object complexType, IXPathMap xPathMap)
   {
      this.init(dataMappingId, dataPath, complexType, xPathMap);
   }
   
   /**
    * @param dataMappingId
    * @param dataPath
    * @param complexType
    * @param xPathMap
    */
   private void init(String dataMappingId, String dataPath, Object complexType, IXPathMap xPathMap)
   {
      this.dataMappingId = dataMappingId;
      this.startXPath = dataPath;
      this.xPathMap = xPathMap;

      // TODO (ab) not only in case of null, but every time, should fill out missing parts  
      if (complexType == null)
      {
         // in case of a/b[1] the value is expected to be there, otherwise createInitialValue won't work
         this.complexType = StructuredDataXPathUtils.createInitialValue(xPathMap, this.startXPath);
      }
      else
      {
         this.complexType = complexType;
      }
      this.modelCreator = new ModelCreator(this.complexType);
   }

   /**
    * @param dataMappingId
    * @return
    */
   private static DataMapping findDataMapping(String dataMappingId)
   {
      try 
      {
         ActivityInstance ai = null;
         View view = PortalApplication.getInstance().getFocusView();
         if (null != view)
         {
            ai = (ActivityInstance) view.getViewParams().get(
                  ActivityInstance.class.getName());
         }

         if (null != ai)
         {
            Activity activity = ai.getActivity();
   
            for (ApplicationContext context : (List<ApplicationContext>) activity.getAllApplicationContexts())
            {
               for (DataMapping mapping : (List<DataMapping>) context.getAllDataMappings()) 
               {
                  if (mapping.getId().equals(dataMappingId))
                  {
                     return mapping;
                  }
               }
            }
            throw new PublicException("Data Mapping with ID '" + dataMappingId
                  + "' was not found for activity '" + activity.getId()
                  + "' please check if JSF sources are in sync with the model definition");
         }
         else
         {
            // Not the right context. Ignore.
            return null;
         }
      } 
      catch (Exception e) 
      {
         throw new PublicException(e);
      }
   }

   public Object getComplexType()
   {
      return this.complexType;
   }

   public String getStartXPath()
   {
      return this.startXPath;
   }

   public String getDataMappingId()
   {
      return dataMappingId;
   }
   
   public IXPathMap getXPathMap()
   {
      return this.xPathMap;
   }
   
   public ModelCreator getTableModels()
   {
      return this.modelCreator;
   }
}
