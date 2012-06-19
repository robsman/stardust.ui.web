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
package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

import java.io.Serializable;

import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;


/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class SelectItemModel implements Serializable
{

   private static final long serialVersionUID = 1L;
   private Object value;
   private String label;
   private boolean disable;
   private String allLabel;

   public SelectItemModel(String label, Object value, boolean disable)
   {
      this.value = value;
      this.label = label;
      this.disable = disable;
      if(disable)
      {
         allLabel = MessagesViewsCommonBean.getInstance().getString("views.authorizationManagerView.participant.all");
      }
   }

   public Object getValue()
   {
      return value;
   }

   public void setValue(Object value)
   {
      this.value = value;
   }

   public String getLabel()
   {
      return (disable && allLabel!=null) ? label + " ("+allLabel+")" : label;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public boolean isDisable()
   {
      return disable;
   }

   public void setDisable(boolean disable)
   {
      this.disable = disable;
   }

}
