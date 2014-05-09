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
package org.eclipse.stardust.ui.web.processportal.interaction.iframe;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;

import com.icesoft.faces.context.BridgeFacesContext;


/**
 * @author sauer
 * @version $Revision: $
 */
public class IframePanelUtils
{
   private static final Logger trace = LogManager.getLogger(IframePanelUtils.class);

   public static boolean isIceFaces(FacesContext facesContext)
   {
      return (facesContext instanceof BridgeFacesContext);
   }
   
   public static String getContentFrameId(ActivityInstance activityInstance)
   {
      return "ipp-activity-panel-" + activityInstance.getOID();
   }
   
   /**
    * @param context
    * @param script
    */
   public static void addJavaScriptCallForTrinidadContext(FacesContext context, String script)
   {
      try
      {
         // Use Reflection as we do not have any compile time dependency on trinidad components 

         Class<?> extRenderKitServiceClass = Class.forName("org.apache.myfaces.trinidad.render.ExtendedRenderKitService");
   
         /*org.apache.myfaces.trinidad.render.ExtendedRenderKitService*/
         Object service = ReflectionUtils
               .invokeStaticMethod(
                     "org.apache.myfaces.trinidad.util.Service",
                     "getRenderKitService(javax.faces.context.FacesContext, java.lang.Class)",
                     context, extRenderKitServiceClass);
   
         ReflectionUtils.invokeMethod("org.apache.myfaces.trinidad.render.ExtendedRenderKitService",
               "addScript(javax.faces.context.FacesContext, java.lang.String)", service, context, script);
      }
      catch (Exception e)
      {
         trace.warn("Failed to add JavaScript in Trinidad Context - " + script, e);
      }
   }
}
