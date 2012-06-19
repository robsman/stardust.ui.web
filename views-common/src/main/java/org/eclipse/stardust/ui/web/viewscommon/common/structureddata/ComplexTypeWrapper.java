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

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;


/**
 * Wraps structured data value (Collection API) for displaying in JSF
 */
public class ComplexTypeWrapper 
{

   private Object complexType; 
   private String dataMappingId;
   private String startXPath;
   private IXPathMap xPathMap;
   
  

   public ComplexTypeWrapper(DataMapping dataMapping, Object complexType)
   {
      Model model = ModelCache.findModelCache().getModel(dataMapping.getModelOID());
      Data data = model.getData(dataMapping.getDataId());
      // TODO (ab) optimize: do not parse schema every time, extend ModelCache 
      // to keep arbitrary runtime client attributes
      // xpathmap must also be cached
      this.init(dataMapping.getId(), dataMapping.getDataPath(), complexType, ClientXPathMap.getXpathMap(model, data));
   }  
   
   
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
      } else {
         this.complexType = complexType;
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

}
