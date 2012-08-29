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
package org.eclipse.stardust.ui.web.common.app.messaging;

import java.util.Map;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;

/**
 * @author Subodh.Godbole
 *
 */
public class MessageProcessor implements MessageTypeConstants
{
   public static void processs(Message message)
   {
      if (T_OPEN_VIEW.equalsIgnoreCase(message.getType()))
      {
         String viewId = GsonUtils.extractString(message.getData(), D_VIEW_ID);
         String viewKey = GsonUtils.extractString(message.getData(), D_VIEW_KEY);
         Map<String, Object> params = GsonUtils.extractMap(message.getData(), D_VIEW_PARAMS);
         Boolean nested = GsonUtils.extractBoolean(message.getData(), D_NESTED, false);

         PortalApplication.getInstance().openViewById(viewId, viewKey, params, null, nested);
      }
      else if (T_CHANGE_PERSPECTIVE.equalsIgnoreCase(message.getType()))
      {
         String perspectiveId = GsonUtils.extractString(message.getData(), D_PERSPECTIVE_ID);
         PortalApplication.getInstance().getPortalUiController().loadPerspective(perspectiveId);
      }
      else
      {
         // TODO: It's possible to keep a list of listeners and propagate the message further 
         // to listeners on custom Perspectives or Views
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString(
               "portalFramework.error.messageNotSupported"));
      }
   }
}
