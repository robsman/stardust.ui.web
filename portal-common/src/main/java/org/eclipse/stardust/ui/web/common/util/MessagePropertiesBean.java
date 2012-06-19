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
package org.eclipse.stardust.ui.web.common.util;

import javax.faces.context.FacesContext;

/**
 * @author subodh.godbole
 * 
 */
public class MessagePropertiesBean extends AbstractMessageBean
{
   private static final String BUNDLE_NAME = "portal-common-messages";
   private static final String BEAN_NAME = "common_msgPropsBean";
   
   /**
    * 
    */
   public MessagePropertiesBean()
   {
      super(BUNDLE_NAME);
   }

   /**
    * @return
    */
   public static MessagePropertiesBean getInstance()
   {
      return (MessagePropertiesBean) FacesContext.getCurrentInstance().getApplication()
            .getVariableResolver().resolveVariable(FacesContext.getCurrentInstance(),
                  BEAN_NAME);
   }
}
