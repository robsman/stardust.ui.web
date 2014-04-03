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

import java.util.ResourceBundle;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityDetails;
import org.eclipse.stardust.engine.api.dto.DataMappingDetails;
import org.eclipse.stardust.engine.api.dto.DataPathDetails;
import org.eclipse.stardust.engine.api.dto.EventHandlerDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;


public class ModelElementUtils
{
   protected final static Logger trace = LogManager.getLogger(ModelElementUtils.class);
   
   // TODO redundant properties from ag.carnot.bpm.modeling.model.i18n.properties.PropertyModel 
   private final static String DEFAULT_BUNDLE_PREFIX = "ag.carnot.";
   private final static String CARNOT_MODEL_RESOURCE_BUNDLE = "carnot:model:resourceBundle";
   
   public static String getNLSPrefix(ModelElement modelElement)
   {
      if(modelElement instanceof Activity)
      {
         ActivityDetails activity = (ActivityDetails)modelElement;
         StringBuffer prefix = new StringBuffer("Activity.");
         prefix.append(activity.getProcessDefinitionId()).append('.');
         prefix.append(modelElement.getId());
         return prefix.toString();
      }
      if(modelElement instanceof ProcessDefinition)
      {
         return "Process." + modelElement.getId();
      }
      if(modelElement instanceof Model)
      {
         return "Model";
      }
      if(modelElement instanceof Data)
      {
         return "Data." + modelElement.getId();
      }
      if(modelElement instanceof Role)
      {
         return "Role." + modelElement.getId();
      }
      if(modelElement instanceof Organization)
      {
         return "Organization." + modelElement.getId();
      }
      if(modelElement instanceof ConditionalPerformer)
      {
         return "CondPerformer." + modelElement.getId();
      }
      if(modelElement instanceof Application)
      {
         return "Application." + modelElement.getId();
      }
      if(modelElement instanceof Diagram)
      {
         return "Diagram." + modelElement.getId();
      }
      if(modelElement instanceof Trigger)
      {
         return "Trigger." + modelElement.getId();
      }
      if(modelElement instanceof DataPath)
      {
         DataPathDetails dataPath = (DataPathDetails)modelElement;
         StringBuffer prefix = new StringBuffer(dataPath.getDirection().getId());
         prefix.append("DataPath.");
         prefix.append(dataPath.getProcessDefinitionId()).append('.');
         prefix.append(modelElement.getId());
         return prefix.toString();
      }
      if(modelElement instanceof DataMapping)
      {
         DataMappingDetails dataMapping = (DataMappingDetails)modelElement;
         StringBuffer prefix = new StringBuffer(dataMapping.getDirection().getId());
         prefix.append("DataMapping.");
         prefix.append(dataMapping.getProcessDefinitionId()).append('.');
         prefix.append(dataMapping.getActivityId()).append('.');
         prefix.append(dataMapping.getContext()).append('.');
         prefix.append(modelElement.getId());
         return prefix.toString();
      }
      if(modelElement instanceof EventHandler)
      {
         EventHandlerDetails eventHandler = (EventHandlerDetails)modelElement;
         String aId = eventHandler.getActivityId();
         if(StringUtils.isEmpty(aId))
         {
            StringBuffer prefix = new StringBuffer("ProcessEH.");
            prefix.append(eventHandler.getProcessDefinitionId()).append('.');
            prefix.append(modelElement.getId());
            return prefix.toString();
         }
         else
         {
            StringBuffer prefix = new StringBuffer("ActivityEH.");
            prefix.append(eventHandler.getProcessDefinitionId()).append('.');
            prefix.append(eventHandler.getActivityId()).append('.');
            prefix.append(modelElement.getId());
            return prefix.toString();
         }         
      }
      return null;
   }
   
   public static String getBundleName(ModelElement modelElement)
   {
      return getBundleName(modelElement.getModelOID());
   }

   public static String getBundleName(long modelOID)
   {
      try
      {
         ModelCache modelCache = ModelCache.findModelCache();
         Model model = modelCache.getModel(modelOID);
         String baseName = null;
         if(model != null)
         {
            baseName = (String) model.getAttribute(CARNOT_MODEL_RESOURCE_BUNDLE);
            if(StringUtils.isEmpty(baseName))
            {
               baseName = DEFAULT_BUNDLE_PREFIX + model.getId();
               try
               {
                  ResourceBundle.getBundle(baseName, org.eclipse.stardust.ui.web.common.util.FacesUtils.getLocaleFromRequest());
               }
               catch(Exception e)
               {
                  baseName = null;
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("No resource bundle found for model with ID '"
                           + model.getId() + "'.");
                  }
               }
            }
         }
         return baseName;
      }
      catch(Exception e)
      {
         
      }
      return null;
   }
   
   /**
    * REST friendly method - No use of FacesContext or SessionContext
    * @param model
    * @return
    */
   public static String getBundleName(Model model)
   {
      try
      {
         String baseName = null;
         if(model != null)
         {
            baseName = (String) model.getAttribute(CARNOT_MODEL_RESOURCE_BUNDLE);
            if(StringUtils.isEmpty(baseName))
            {
               baseName = DEFAULT_BUNDLE_PREFIX + model.getId();
            }
         }
         return baseName;
      }
      catch(Exception e)
      {
         
      }
      return null;
   }
}