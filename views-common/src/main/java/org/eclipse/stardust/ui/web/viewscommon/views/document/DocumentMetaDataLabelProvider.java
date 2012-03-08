/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.ui.common.form.jsf.messages.DefaultLabelProvider;
import org.eclipse.stardust.ui.common.form.jsf.utils.MessagePropertiesBean;
import org.eclipse.stardust.ui.common.introspection.Path;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class DocumentMetaDataLabelProvider extends DefaultLabelProvider
{
   private static final long serialVersionUID = 1L;
   
   private Data data;

   /**
    * @param data
    */
   public DocumentMetaDataLabelProvider(Data data)
   {
      this.data = data;
   }

   @Override
   public String getLabel(Path path)
   {
      String label = null;

      if (null != data && null == path.getParentPath())
      {
         label = I18nUtils.getLabel(data, data.getId());
         if(data.getId().equals(label))
         {
            label = null; // Label is same as Id, Means I18N not present
         }
      }

      if(StringUtils.isNotEmpty(label))
      {
         return label;
      }
      else
      {
         return super.getLabel(path);
      }
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
    * @see org.eclipse.stardust.ui.common.form.jsf.messages.DefaultLabelProvider#getDescription(org.eclipse.stardust.ui.common.introspection.Path)
    */
   public String getDescription(Path path)
   {
      String desc = null;

      if (null != data && null == path.getParentPath())
      {
         // Read I18N Description
         desc = I18nUtils.getDescriptionAsHtml(data, "");
         if (StringUtils.isEmpty(desc))
         {
            // I18N Description not available. Get it from Data
            desc = data.getDescription();
            if (StringUtils.isNotEmpty(desc))
            {
               // Format Description as HTML
               desc = StringUtils.replace(desc, "\n", "<br/>");
            }
         }
      }
 
      return desc;
   }
}
