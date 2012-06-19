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
package org.eclipse.stardust.ui.web.common.app.tags;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.spring.scope.TabScopeUtils;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class TabScopedHandler extends ComponentHandler
{

   private final TagAttribute currentView;

   public TabScopedHandler(ComponentConfig config)
   {
      super(config);

      this.currentView = this.getRequiredAttribute(TabScopedComponent.ATTR_CURRENT_TAB);
   }

   
   @Override
   protected void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException,
         FacesException, ELException
   {
      View view = (View) currentView.getObject(ctx, View.class);
      try
      {
         if (null != view)
         {
            TabScopeUtils.bindTabScope(view);
         }

         super.applyNextHandler(ctx, c);
      }
      finally
      {
         if (null != view)
         {
            TabScopeUtils.unbindTabScope(view);
         }
      }
   }

}
