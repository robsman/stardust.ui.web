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
import java.lang.reflect.Method;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.PhaseId;

import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.reflect.Reflect;
import org.eclipse.stardust.ui.web.common.spring.scope.TabScopeUtils;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;


/**
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class TabScopedComponent extends UIComponentBase implements MyFacesRestoreStateUtilsBindingAware
{
   private static final Logger trace = LogManager.getLogger(TabScopedComponent.class);
   
   public static final String ATTR_CURRENT_TAB = "currentTab";

   @Override
   public String getFamily()
   {
      return null;
   }

   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      bindTabScope();
      
      super.encodeBegin(context);
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      super.encodeEnd(context);
      
      unbindTabScope();
   }

   @Override
   public void encodeChildren(FacesContext context) throws IOException
   {
      bindTabScope();

      super.encodeChildren(context);
   }

   @Override
   public void processRestoreState(FacesContext context, Object state)
   {
      /* JSF Phase: Restore Tab */
      try
      {
         bindTabScope();
         
         super.processRestoreState(context, state);
      }
      finally
      {
         unbindTabScope();
      }
   }

   @Override
   public void processDecodes(FacesContext context)
   {
      /* JSF Phase: Apply Request Values */
      try
      {
         bindTabScope();
         
         super.processDecodes(context);
      }
      finally
      {
         unbindTabScope();
      }
   }

   @Override
   public void processValidators(FacesContext context)
   {
      /* JSF Phase: Process Validations */
      try
      {
         bindTabScope();
         
         super.processValidators(context);
      }
      finally
      {
         unbindTabScope();
      }
   }

   @Override
   public void processUpdates(FacesContext context)
   {
      /* JSF Phase: Update Model Values */
      try
      {
         bindTabScope();
         
         super.processUpdates(context);
      }
      finally
      {
         unbindTabScope();
      }
   }

   @Override
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      /* JSF Phase: Invoke Application */
      try
      {
         bindTabScope();

         if (event instanceof TabScopedEvent)
         {
            TabScopedEvent vsae = (TabScopedEvent) event;
         
            vsae.wrappedEvent.getComponent().broadcast(vsae.wrappedEvent);
         }
      }
      finally
      {
         unbindTabScope();
      }
   }

   @Override
   public Object processSaveState(FacesContext context)
   {
      /* JSF Phase: Render Response */
      try
      {
         bindTabScope();
         
         return super.processSaveState(context);
      }
      finally
      {
         unbindTabScope();
      }
   }

   @Override
   public void queueEvent(FacesEvent event)
   {
      if ( !(event instanceof TabScopedEvent))
      {
         super.queueEvent(new TabScopedEvent(this, event));
      }
      else
      {
         super.queueEvent(event);
      }
   }

   /**
    * This hooks into a workaround MyFaces provides due to a JSF 1.x spec deficiency.
    * <p>
    * Reflection is being used to prevent a compile time dependency on MyFaces.
    * 
    * @see MyFacesRestoreStateUtilsBindingAware
    * @see org.apache.myfaces.shared_impl.util.RestoreStateUtils
    */
   public void handleBindings()
   {
      try
      {
         bindViewScope();

         Class< ? > clazz = ReflectionUtils
               .getClassFromClassName("org.apache.myfaces.shared_impl.util.RestoreStateUtils");
         if (null != clazz)
         {
            Method delegateMethod = Reflect
                  .decodeMethod(
                        clazz,
                        "recursivelyHandleComponentReferencesAndSetValid(javax.faces.context.FacesContext, javax.faces.component.UIComponent, boolean)");
            if (null != delegateMethod)
            {
               // re-invoke method, but this time forcing it to handle the component
               // itself
               delegateMethod.invoke(null, FacesContext.getCurrentInstance(), this, true);
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Failed invoking MyFaces RestoreStateUtils utility method.", e);
      }
      finally
      {
         unbindViewScope();
      }
   }

   private void bindViewScope()
   {
      Object currentView = getAttributes().get(ATTR_CURRENT_TAB);
      if (currentView instanceof View)
      {
         TabScopeUtils.bindTabScope((View) currentView);
      }
   }

   private void unbindViewScope()
   {
      Object currentView = getAttributes().get(ATTR_CURRENT_TAB);
      if (currentView instanceof View)
      {
         TabScopeUtils.unbindTabScope((View) currentView);
      }
   }

   private void bindTabScope()
   {
      Object currentTab = getAttributes().get(ATTR_CURRENT_TAB);
      if (currentTab instanceof View)
      {
         TabScopeUtils.bindTabScope((View) currentTab);
      }
   }

   private void unbindTabScope()
   {
      Object currentTab = getAttributes().get(ATTR_CURRENT_TAB);
      if (currentTab instanceof View)
      {
         TabScopeUtils.unbindTabScope((View) currentTab);
      }
   }

   private class TabScopedEvent extends FacesEvent
   {
      private static final long serialVersionUID = 1L;

      private final FacesEvent wrappedEvent;
      
      public TabScopedEvent(UIComponent component, FacesEvent wrappedEvent)
      {
         super(component);
         
         this.wrappedEvent = wrappedEvent;
      }
      
      @Override
      public boolean isAppropriateListener(FacesListener listener)
      {
         return wrappedEvent.isAppropriateListener(listener);
      }

      @Override
      public void processListener(FacesListener listener)
      {
         wrappedEvent.processListener(listener);
      }

      @Override
      public PhaseId getPhaseId()
      {
         return wrappedEvent.getPhaseId();
      }
   }
}