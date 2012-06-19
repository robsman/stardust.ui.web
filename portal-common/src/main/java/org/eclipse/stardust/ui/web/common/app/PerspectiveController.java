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
package org.eclipse.stardust.ui.web.common.app;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.PerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class PerspectiveController implements Serializable
{
   private static final long serialVersionUID = 1L;

   private PortalUiController uiController;

   public PerspectiveController(PortalUiController delegate)
   {
      this.uiController = delegate;
   }

   public IPerspectiveDefinition getPerspective()
   {
      return this.uiController.getPerspective();
   }
   
   public View getFocusView()
   {
      return uiController.getFocusView();
   }

   public boolean setFocusView(View focusView)
   {
      return uiController.setFocusView(focusView);
   }

   public void setActiveView(View activeView)
   {
      uiController.setActiveView(activeView);
   }

   public List<View> getOpenViews()
   {
      return uiController.getOpenViews();
   }

   public View findView(String url)
   {
      return uiController.findView(url);
   }

   /**
    * Used to open View from Java Bean
    * 
    * @param viewId
    *           like specified in <ippui:view> (name attribute)
    * @param viewKey
    *           must be unique to open distinctive Views of the same viewId type. e.g.
    *           "oid=10".
    * @param viewParams
    *           additional parameters for initialization of the Views beans
    * @param msgBean
    *           Properties bean to pick View's Label and Description
    * @param nestedView
    * @return the created <code>View</code>
    */
   public View openViewById(String viewId, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean msgBean, boolean nested)
   {
      return uiController.openViewById(viewId, viewKey, viewParams, msgBean, nested);
   }
   
   public View getViewById(String viewId, String viewKey)
   {
      return uiController.getViewById(viewId, viewKey);
   }

   public View openView(ViewDefinition viewDef, String viewKey,
         Map<String, Object> params, AbstractMessageBean msgBean, boolean nestedView)
   {

      return uiController.openView(viewDef, viewKey, params, msgBean, nestedView);
   }

   public View openView()
   {
      return uiController.openView();
   }

   public View openView(String viewUrl, AbstractMessageBean msgBean)
   {
      return uiController.openView(viewUrl, msgBean);
   }

   public View openView(String viewUrl, AbstractMessageBean msgBean, boolean nestedView)
   {
      return uiController.openView(viewUrl, msgBean, nestedView);
   }

   public View openView(ViewDefinition viewDef, String viewUrl,
         AbstractMessageBean msgBean)
   {
      return uiController.openView(viewDef, viewUrl, msgBean);
   }

   public View openView(ViewDefinition viewDef, String viewUrl,
         AbstractMessageBean msgBean, boolean nestedView)
   {
      return uiController.openView(viewDef, viewUrl, msgBean, nestedView);
   }

   /**
    * @param viewDef
    * @param url
    * @param messageBean
    * @return
    */
   public View createView(ViewDefinition viewDef, String url,
         AbstractMessageBean messageBean)
   {
      return uiController.createView(viewDef, url, messageBean);
   }
   
   /**
    * @param viewDef
    * @param url
    * @param messageBean
    * @param nestedView
    * @return
    */
   public View createView(ViewDefinition viewDef, String url,
         AbstractMessageBean messageBean, boolean nestedView)
   {
      return uiController.createView(viewDef, url, messageBean, nestedView);
   }

   /**
    * @param viewDef
    * @param url
    * @param messageBean
    * @param nestedView
    * @return
    */
   public View createView(ViewDefinition viewDef, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean messageBean,
         boolean nestedView)
   {
      return uiController.createView(viewDef, viewKey, viewParams, messageBean, nestedView);
   }

   /**
    * @param viewId
    * @param viewKey
    * @param viewParams
    * @param msgBean
    * @param nested
    * @return
    */
   public View createView(String viewId, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean messageBean, boolean nestedView)
   {
      return uiController.createView(viewId, viewKey, viewParams, messageBean, nestedView);
   }

   public void selectView()
   {
      uiController.selectView();
   }

   public void closeFocusView()
   {
     uiController.closeFocusView();
   }

   public View closeView()
   {
      return uiController.closeView();
   }

   public View closeView(View view)
   {
      return closeView(view, false);
   }

   public View closeView(View view, boolean force)
   {
      if(uiController.closeView(view, force))
      {
         return view;
      }
      return null;
   }
   
   // Package Scope
   boolean broadcastVetoableViewEvent(View view, ViewEventType eventType)
   {
      return uiController.broadcastVetoableViewEvent(view, eventType);
   }
   
   // Package Scope
   void broadcastNonVetoableViewEvent(View view, ViewEventType eventType)
   {
      uiController.broadcastNonVetoableViewEvent(view, eventType);
   }
}
