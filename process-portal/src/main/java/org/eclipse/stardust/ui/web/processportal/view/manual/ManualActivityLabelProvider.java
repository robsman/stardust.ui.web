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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.util.Map;

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.ui.common.form.jsf.messages.DefaultLabelProvider;
import org.eclipse.stardust.ui.common.form.jsf.utils.MessagePropertiesBean;
import org.eclipse.stardust.ui.common.introspection.Path;
import org.eclipse.stardust.ui.common.introspection.xsd.XsdPath;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class ManualActivityLabelProvider extends DefaultLabelProvider
{
   private static final long serialVersionUID = 1L;
   
   private Map<Path, DataMapping> path2DataMappingMap;
   private Model model;

   /**
    * @param model
    * @param path2DataMappingMap
    */
   public ManualActivityLabelProvider(Model model, Map<Path, DataMapping> path2DataMappingMap)
   {
      this.model = model;
      this.path2DataMappingMap = path2DataMappingMap;
      if (CollectionUtils.isEmpty(this.path2DataMappingMap))
      {
         this.path2DataMappingMap = CollectionUtils.newHashMap();
      }
   }

   @Override
   public String getLabel(Path path)
   {
      DataMapping mapping = path2DataMappingMap.get(path);
      String label = null;

      if (null != mapping)
      {
         label = I18nUtils.getLabel(mapping, mapping.getId());
         if(mapping.getId().equals(label))
         {
            label = null; // Label is same as Id, Means I18N not present
         }
      }
      else if (path instanceof XsdPath)
      {
         label = I18nUtils.getLabel(((XsdPath)path).getTypedXPath(), model, null);
      }

      // If I18N Label not found, then return from super Default Provider 
      if(StringUtils.isNotEmpty(label))
      {
         return label;
      }
      else
      {
         return super.getLabel(path);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.common.form.jsf.messages.DefaultLabelProvider#getDescription(org.eclipse.stardust.ui.common.introspection.Path)
    */
   public String getDescription(Path path)
   {
      DataMapping mapping = path2DataMappingMap.get(path);
      String desc = null;

      if (null != mapping)
      {
         Data data = model.getData(mapping.getDataId());
         
         // Read I18N Description
         desc = I18nUtils.getDescriptionAsHtml(data, "");
         if (StringUtils.isEmpty(desc))
         {
            // I18N Description not available. Get it from Data
            desc = data.getDescription();
            if (StringUtils.isNotEmpty(desc))
            {
               // Make Description HTML complaint
               desc = StringUtils.replace(desc, "\n", "<br/>");
            }
         }
      }
      else if (path instanceof XsdPath)
      {
         desc = I18nUtils.getDescription(((XsdPath)path).getTypedXPath(), model, null);
      }

      // If I18N not found, No description defined 
      return desc;
   }

   @Override
   public String getLabel(String key)
   {
      if ("ui.form.validationMessage.globalMessage.label".equals(key)
            || "formPanel.listController.addAction".equals(key) || "formPanel.listController.removeAction".equals(key))
      {
         return MessagePropertiesBean.getInstance().getString(key);
      }

      return super.getLabel(key);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.common.form.jsf.messages.DefaultLabelProvider#getEnumerationLabel(org.eclipse.stardust.ui.common.introspection.Path, java.lang.String)
    */
   protected String getEnumerationLabel(Path path, String enumValue)
   {
      DataMapping mapping = path2DataMappingMap.get(path);
      if (null != mapping)
      {
         if ("PROCESS_PRIORITY".equals(mapping.getDataId()))
         {
            return PriorityConverter.getPriorityLabel(Integer.parseInt(enumValue));
         }
      }
      return getLabel(enumValue);
   }
}
