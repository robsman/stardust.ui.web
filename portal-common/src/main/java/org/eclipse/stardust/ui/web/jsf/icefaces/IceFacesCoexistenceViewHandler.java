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
package org.eclipse.stardust.ui.web.jsf.icefaces;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;

import com.icesoft.faces.application.ViewHandlerProxy;
import com.icesoft.faces.facelets.D2DFaceletViewHandler;
import com.sun.facelets.FaceletViewHandler;



/**
 * @author sauer
 * @version $Revision: $
 */
public class IceFacesCoexistenceViewHandler extends ViewHandlerProxy
{

   public IceFacesCoexistenceViewHandler(ViewHandler viewHandler)
   {
      super(initializeViewHandlerChain(viewHandler));
   }

   public UIViewRoot createView(FacesContext context, String viewId)
   {
      UIViewRoot view = super.createView(context, viewId);

      if (IceFacesUtils.isIceFaces(context))
      {
         view.setRenderKitId("ICEfacesRenderKit");
      }

      return view;
   }

   private static ViewHandler initializeViewHandlerChain(ViewHandler delegate)
   {
      FaceletViewHandler faceletViewHandler = new FaceletViewHandler(delegate);
      ViewHandler trinidadViewHandler = (ViewHandler) ReflectionUtils.createInstance(
            "org.apache.myfaces.trinidadinternal.application.ViewHandlerImpl", 
            new Class[] {ViewHandler.class}, new Object[] {faceletViewHandler});
      ViewHandler iceFacesViewHandler = new D2DFaceletViewHandler(trinidadViewHandler);
      
      return iceFacesViewHandler;
   }

}
