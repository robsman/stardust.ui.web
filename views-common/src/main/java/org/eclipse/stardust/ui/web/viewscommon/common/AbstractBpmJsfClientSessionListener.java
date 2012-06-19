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

import java.util.List;

import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.listener.IBpmClientSessionListener;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractBpmJsfClientSessionListener
      implements IBpmClientSessionListener
{
   private List initializedBeans;

   public List getInitializedBeans()
   {
      return initializedBeans;
   }

   public void setInitializedBeans(List initializedBeans)
   {
      this.initializedBeans = initializedBeans;
   }
   
   protected void initializeBeans()
   {
      if ((null != getInitializedBeans()) && !getInitializedBeans().isEmpty())
      {
         SessionContext.setBindContextValueByName(getInitializedBeans(), null);
      }
   }
}
