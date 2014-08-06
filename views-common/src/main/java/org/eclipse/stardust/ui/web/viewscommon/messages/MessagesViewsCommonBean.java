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
package org.eclipse.stardust.ui.web.viewscommon.messages;

import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;


public class MessagesViewsCommonBean extends AbstractMessageBean
{
   private static final String BUNDLE_NAME = "views-common-messages";

   private static final String BEAN_NAME = "views_common_msgPropsBean";

   public MessagesViewsCommonBean()
   {
      super(BUNDLE_NAME);
   }

   /**
    * @return
    */
   public static MessagesViewsCommonBean getInstance()
   {
      return (MessagesViewsCommonBean) ManagedBeanUtils.getManagedBean(BEAN_NAME);
   }
}
