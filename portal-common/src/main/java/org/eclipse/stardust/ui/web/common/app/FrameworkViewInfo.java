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

import org.eclipse.stardust.ui.web.common.util.StringUtils;

import com.icesoft.util.encoding.Base64;


/**
 * @author Subodh.Godbole
 *
 */
public class FrameworkViewInfo
{
   private String html5FwViewId;
   private String viewId;
   private String typeId;
   private String id;

   /**
    * @param html5FwViewId
    * @param viewId
    * @param params
    */
   public FrameworkViewInfo(String html5FwViewId, String viewId, String typeId, String id)
   {
      super();
      this.html5FwViewId = html5FwViewId;
      this.viewId = viewId;
      this.typeId = typeId;
      this.id = id;
   }
   
   /**
    * 
    */
   public static FrameworkViewInfo generateHTML5FrameworkViewInfo(View view)
   {
      String html5FwViewId = "";
      String viewId = "";
      String typeId = "";
      String id = "";

      if (view.getDefinition() != null)
      {
         boolean ext = !view.getDefinition().getInclude().toLowerCase().endsWith(".html");
         if (ext)
         {
            viewId = "Ext/:type/:id";
         }
         else
         {
            viewId = "Int/" + view.getDefinition().getName() + "/:id";
         }

         typeId = view.getDefinition().getName();
         id = StringUtils.isNotEmpty(view.getViewKey()) ? view.getViewKey() : "all";
         
         if(!StringUtils.isEmpty(typeId) && !typeId.equals("configurationTreeView"))
         {
            viewId = "/bpm/portal/" + viewId;
            
            html5FwViewId = viewId;
            html5FwViewId = StringUtils.replace(html5FwViewId, ":type", typeId);
            html5FwViewId = StringUtils.replace(html5FwViewId, ":id", id);
         }
         else
         {
            html5FwViewId = "/bpm/portal/configurationTreeView";
         }
      }
      return new FrameworkViewInfo(html5FwViewId, viewId, typeId, id);
   }

   public String getHtml5FwViewId()
   {
      return html5FwViewId;
   }

   public String getViewId()
   {
      return viewId;
   }

   public String getTypeId()
   {
      return typeId;
   }

   public String getId()
   {
      return id;
   }
}
