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
package org.eclipse.stardust.ui.web.viewscommon.beans;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpSession;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.Resetable;
import org.eclipse.stardust.ui.web.viewscommon.common.WebUser;


public final class ApplicationContext
{
   protected static final Logger trace = LogManager.getLogger(ApplicationContext.class);

   private final static String BEAN_ID = "carnotApplicationContext";

   private final static String BEAN_ID_EXPRESSION = "#{" + BEAN_ID + "}";
   public final static String UNKNOWN_APPLICATION_ID = "UnknownAppId";
   
   private Map userRegister;

   private Map propertyMap;
   
   private String applicationId;
   
   private String version;

   private final static ApplicationContext _instance = new ApplicationContext();
   
   public ApplicationContext()
   {
      propertyMap = new HashMap();
      userRegister = new HashMap();
      version = CurrentVersion.getBuildVersionName();
   }

   public static ApplicationContext findApplicationContext(FacesContext context)
   {
      try
      {
         if(context != null)
         {
            ValueBinding valueBinding = context.getApplication().createValueBinding(
                  BEAN_ID_EXPRESSION);
            Object val = (null != valueBinding) ? valueBinding.getValue(context) : null;
            if (val == null || !(val instanceof ApplicationContext) || val != _instance)
            {
               valueBinding.setValue(context, _instance);
            }
         }
      }
      catch (Exception e)
      {
         // ignore
      }
      return _instance;
   }

   public static ApplicationContext findApplicationContext()
   {
      return findApplicationContext(FacesContext.getCurrentInstance());
   } 
   
  
   
   public static boolean unregisterSession(HttpSession session)
   {
      ApplicationContext appContext = findApplicationContext();
      boolean sessionRemoved = false;
      if(appContext != null && session != null)
      {
         String sessionId = session.getId();
         Iterator webUserIter = appContext.userRegister.values().iterator();
         while(webUserIter.hasNext() && !sessionRemoved)
         {
            WebUser webUser = (WebUser)webUserIter.next();
            sessionRemoved = webUser.removeSession(sessionId);
         }
      }
      return sessionRemoved;
   }
   
  
   
   public static boolean registerUser(HttpSession session, User user)
   {
      ApplicationContext appContext = findApplicationContext();
      if(appContext != null && user != null)
      {
         Long userOid = new Long(user.getOID());
         WebUser webUser = (WebUser)appContext.userRegister.get(userOid);
         String appId = FacesContext.getCurrentInstance().getExternalContext().
            getInitParameter(Constants.LOGIN_APP_ID);
         if(StringUtils.isEmpty(appId))
         {
            appId = UNKNOWN_APPLICATION_ID;
         }
         if(webUser == null)
         {
            webUser = new WebUser(user, session.getId(), appId);
            appContext.userRegister.put(userOid, webUser);
            return true;
         }
         else
         {
            return webUser.addSession(session.getId(), appId);
         }
      }
      return false;
   }

   public void bind(String property, Object obj)
   {
      propertyMap.put(property, obj);
   }

   public Object lookup(String property)
   {
      return propertyMap.get(property);
   }

  
   
   public String getApplicationId()
   {
      if(StringUtils.isEmpty(applicationId))
      {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         if(facesContext != null)
         {
            applicationId = facesContext.getExternalContext()
               .getInitParameter(Constants.LOGIN_APP_ID);
         }
      }
      return applicationId;
   }

   public void setApplicationId(String applicationId)
   {
      this.applicationId = applicationId;
   }
   
   public static boolean isPrincipalLogin()
   {
      return SecurityProperties.AUTHENTICATION_MODE_PRINCIPAL.compareTo(Parameters.instance()
            .getString(SecurityProperties.AUTHENTICATION_MODE_PROPERTY,
                  SecurityProperties.AUTHENTICATION_MODE_INTERNAL)) == 0 ? true : false;
   }
   
   public String getVersion()
   {
      return version;
   }
}
