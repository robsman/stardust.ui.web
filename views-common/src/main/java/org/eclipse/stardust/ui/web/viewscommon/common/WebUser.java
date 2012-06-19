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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.User;


public class WebUser
{
   private final User user;
   private Set context;
   
   public WebUser(User user, String sessionId, String applicationId)
   {
      this.user = user;
      context = Collections.synchronizedSet(new HashSet(3));
      context.add(new Pair(sessionId, applicationId));
   }
   
   private WebUser(WebUser webUser)
   {
      this.user = webUser.user;
      context = new HashSet(webUser.context);
   }
   
   public User getUser()
   {
      return user;
   }
   
   public Set getApplicationIds()
   {
      Set appIds = new HashSet();
      Iterator contextIter = context.iterator();
      while(contextIter.hasNext())
      {
         String appId = (String)((Pair)contextIter.next()).getSecond();
         if(!StringUtils.isEmpty(appId))
         {
            appIds.add(appId);
         }
      }
      return appIds;
   }
   
   public boolean addSession(String sessionId, String applicationId)
   {
      Pair newSession = new Pair(sessionId, applicationId);
      return context.add(newSession);
   }
   
   public boolean removeSession(String sessionId)
   {
      Iterator contextIter = context.iterator();
      boolean removed = false;
      while(contextIter.hasNext())
      {
         Pair session = (Pair)contextIter.next();
         if(CompareHelper.areEqual(session.getFirst(), sessionId))
         {
            contextIter.remove();
            removed = true;
         }
      }
      return removed;
   }
   
   public boolean hasSession()
   {
      return !context.isEmpty();
   }
   
   public static WebUser unmodifiableWebUser(final WebUser webUser)
   {
      return new WebUser(webUser) 
      {   
         public boolean addSession(String sessionId, String applicationId)
         {
            throw new UnsupportedOperationException(
                  Localizer.getString(LocalizerKey.UNMODIFIABLE_WEB_USER));
         }
         
         public boolean removeSession(String sessionId)
         {
            throw new UnsupportedOperationException(
                  Localizer.getString(LocalizerKey.UNMODIFIABLE_WEB_USER));
         }
      };
   }

}