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
package org.eclipse.stardust.ui.web.admin.views.model;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelElementLocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;



public class ModelConfigurationTreeItem
{
   private final Object parent;
   private final Object source;
   private String defaultValue;
   private String description;
   private String modelName;
   private String name;
   private String value;
   private boolean visible = false;

   public ModelConfigurationTreeItem(Object source, Object parent)
   {
      this.source = source;
      this.parent = parent;

      if ((source != null) && source instanceof ConfigurationVariables)
      {
         ConfigurationVariables conVariables = (ConfigurationVariables) source;
         this.modelName = getModelName(conVariables.getModelId());
         this.name = "";
         this.description = getModelDescription(conVariables.getModelId());
         this.value = "";
         this.defaultValue = "";
      }
      else if ((source != null) && source instanceof ConfigurationVariable)
      {
         ConfigurationVariable configurationVariable = (ConfigurationVariable) source;
         this.name = configurationVariable.getName();
         this.modelName = "";
         this.description = configurationVariable.getDescription();
         this.value = configurationVariable.getValue();
         this.defaultValue = configurationVariable.getDefaultValue();
      }
   }

   /**
    * @param variables
    * @return
    */
   private String getModelName(String modelId)
   {
      String modelName = (modelId != null) ? modelId : "";
      Model model = null;
      
      if (modelId != null)
      {
         model = ModelUtils.getModel(modelId);
         if (model != null)
         {
            modelName = I18nUtils.getLabel(model, model.getName());
         }
      }
      
      return modelName;
   }
   
   /**
    * 
    * @param modelId
    * @return
    */
   private String getModelDescription(String modelId)
   {
      Model model = null;

      if (modelId != null)
      {
         model = ModelUtils.getModel(modelId);
         return I18nUtils.getDescriptionAsHtml(model, model.getDescription());
      }
      return null;
   }
   
   public String getDefaultValue()
   {
      return defaultValue;
   }

   public String getDescription()
   {
      return description;
   }

   public String getModelName()
   {
      return modelName;
   }

   public String getName()
   {
      return name;
   }

   public Object getParent()
   {
      return parent;
   }

   public Object getSource()
   {
      return source;
   }

   public String getValue()
   {
      return value;
   }

   public boolean isVisible()
   {
      return visible;
   }

   public void setDefaultValue(String defaultValue)
   {
      this.defaultValue = defaultValue;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public void setModelName(String modelName)
   {
      this.modelName = modelName;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   public void setVisible(boolean visible)
   {
      this.visible = visible;
   }
}
