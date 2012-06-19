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
package org.eclipse.stardust.ui.web.viewscommon.common;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;


public class PriorityLabelLocalizerKey extends LocalizerKey
{
   private static final String priorityResourceBundleName;
   private static final String priorityLabelProperty;
   
   private static final String PRIO_RESOURCE_BUNDLE = "Carnot.Priority.Label.ResourceBundle";
   private static final String PRIO_LABEL_PROPERTY = "Carnot.Priority.Label.Property";
   
   static
   {
      String property = Parameters.instance().getString(PRIO_RESOURCE_BUNDLE);
      if(StringUtils.isEmpty(property))
      {
         property = "web-ui-common-messages";// "CommonMessages";
      }
      priorityResourceBundleName = property;
      
      property = Parameters.instance().getString(PRIO_LABEL_PROPERTY);
      if(StringUtils.isEmpty(property))
      {
         property = "label.priority.";
      }
      priorityLabelProperty = property;
   }
   
   public PriorityLabelLocalizerKey(int priorityIdent)
   {
      super(priorityResourceBundleName, priorityLabelProperty + priorityIdent);        
   }
}
