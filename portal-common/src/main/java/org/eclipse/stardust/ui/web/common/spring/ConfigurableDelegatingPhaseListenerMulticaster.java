/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.common.spring;

import static java.util.Collections.synchronizedMap;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseListener;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.jsf.DelegatingPhaseListenerMulticaster;

import org.eclipse.stardust.common.config.Parameters;

@SuppressWarnings("serial")
public class ConfigurableDelegatingPhaseListenerMulticaster extends DelegatingPhaseListenerMulticaster
{
   private Map<ListableBeanFactory, Collection<PhaseListener>> delegatesCaches = synchronizedMap(new WeakHashMap<ListableBeanFactory, Collection<PhaseListener>>());

   @Override
   protected Collection<PhaseListener> getDelegates(FacesContext facesContext)
   {
      // by default, broadcasting to singletons is sufficient (and performing considerably
      // better)
      boolean includeNonSingletons = Parameters.instance().getBoolean("Carnot.Client.NonSingletonJsfPhaseListeners",
            false);

      ListableBeanFactory bf = getBeanFactory(facesContext);
      if (!includeNonSingletons)
      {
         // if broadcasting to singletons is sufficient, candidates can be cached after
         // first resolution
         Collection<PhaseListener> cachedDelegates = delegatesCaches.get(bf);
         if (null != cachedDelegates)
         {
            return cachedDelegates;
         }
      }

      Collection<PhaseListener> delegates = BeanFactoryUtils.beansOfTypeIncludingAncestors(bf, PhaseListener.class,
            includeNonSingletons, false).values();
      if (!includeNonSingletons)
      {
         delegatesCaches.put(bf, delegates);
      }

      return delegates;
   }
}
