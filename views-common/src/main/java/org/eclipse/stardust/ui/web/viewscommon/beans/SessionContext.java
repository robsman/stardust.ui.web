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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.web.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferencesManager;
import org.eclipse.stardust.engine.core.repository.IRepositoryManager;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.ISessionListener;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.Resetable;
import org.eclipse.stardust.ui.web.viewscommon.common.listener.IBpmClientSessionListener;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;



public final class SessionContext implements Serializable
{  
   protected static final Logger trace = LogManager.getLogger(SessionContext.class);
   
   private final static long serialVersionUID = 1l;
   
   public final static String BEAN_ID = "sessionContext";
   private final static String BEAN_ID_EXPRESSION = "#{" + BEAN_ID + "}";
   
   // TODO We have to prepare these Map for serialization in order to ensure the session state
   private transient Map propertyMap;
   private transient Map firstClassPropertyMap;
   
   private boolean adminRoleRequired;
   private boolean modelRequired;
   private LoginData loginData;
   private transient ServiceFactory serviceFactory;
   private User loggedInUser;

   private transient List sessionListener;
   
   private transient IPreferencesManager preferencesManager;
   private transient IRepositoryManager repositoryManager;
   
   public SessionContext()
   {
      propertyMap = getPropertyMap(false);
      firstClassPropertyMap = getPropertyMap(true);
      sessionListener = CollectionUtils.newList();
      adminRoleRequired = false;
      ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
      this.adminRoleRequired = "true".equalsIgnoreCase(externalContext
            .getInitParameter(Constants.LOGIN_ADMIN_ROLE_REQUIRED))
            ? true
            : false;
      this.modelRequired = "true".equalsIgnoreCase(externalContext
            .getInitParameter(Constants.LOGIN_MODEL_REQUIRED))
            ? true
            : false;
      preferencesManager = getPreferencesManager();
   }
   
   private Map getPropertyMap(boolean firstClass)
   {
      propertyMap = propertyMap != null 
         ? propertyMap : new HashMap();
      firstClassPropertyMap = firstClassPropertyMap != null 
         ? firstClassPropertyMap : new HashMap();
      return firstClass ? firstClassPropertyMap : propertyMap;
   }
   
   private static ValueBinding createValueBinding(FacesContext context, String binding)
   {
      if ( !(binding.startsWith("#{") && binding.endsWith("}")))
      {
         binding = "#{" + binding + "}";
      }
      return context.getApplication().createValueBinding(binding);
   }
   
   public static SessionContext findSessionContext(FacesContext context)
   {
      Object val = null;
      try
      {
         ValueBinding valueBinding = createValueBinding(context, BEAN_ID_EXPRESSION);
         val = (null != valueBinding) ? valueBinding.getValue(context) : null;
         if (val == null || !(val instanceof SessionContext))
         {
            val = new SessionContext();
            valueBinding.setValue(context, val);
         }
      }
      catch(Exception e)
      {
         // ignore
      }
      return (SessionContext)val;
   }
   
   public static SessionContext findSessionContext()
   {
      return findSessionContext(FacesContext.getCurrentInstance()); 
   }
   
   public static Object findBindContextValue(FacesContext context, String binding)
   {
      try
      {
         ValueBinding valueBinding = createValueBinding(context, binding);
         return valueBinding.getValue(context);
      }
      catch(Exception e)
      {
         return null;
      }
   }
   
   public static Object findBindContextValue(String binding)
   {
      return findBindContextValue(FacesContext.getCurrentInstance(), binding);
   }
   
   public static void setBindContextValueByClass(Class bean, Object obj)
   {
      String binding = bean.getName();
      binding = binding.substring(binding.lastIndexOf(".") + 1);
      int pos = binding.lastIndexOf("Bean");
      binding = binding.substring(0, 1).toLowerCase() + binding.substring(1, pos);
      trace.info("set Value for " + binding );
      setBindContextValueByName(binding, obj);
   }
   
   public static void setBindContextValueByName(String bean, Object obj)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      try
      {
         context.getApplication().createValueBinding("#{" + bean + "}").setValue(context, obj);
      }
      catch(NullPointerException e)
      {
         trace.error("unable to set the binding for '" + bean + "'");
      }
   }
   
   public static void setBindContextValueByName(List beans, Object obj)
   {
      Iterator beanIter = beans != null ? beans.iterator() : null;
      while(beanIter != null && beanIter.hasNext())
      {
         setBindContextValueByName((String)beanIter.next(), obj);
      }
   }
   
   public void bind(String property, Object obj, boolean firstClassProperty)
   {
      propertyMap = getPropertyMap(false);
      synchronized(propertyMap)
      {
         if(firstClassProperty)
         {
            firstClassPropertyMap.put(property, obj);
         }
         else
         {
            propertyMap.put(property, obj);
         }
      }
   }
   
   public void bind(String property, Object obj)
   {
      bind(property, obj, false);
   }
   
   public Object lookup(String property, boolean firstClassProperty)
   {
      return firstClassProperty ? 
            getPropertyMap(true).get(property) :
               getPropertyMap(false).get(property);
   }
   
   public Object lookup(String property)
   {
      return lookup(property, false);
   }
   
   private void resetPropertyMap(Map properties)
   {
      Set entrySet = properties.entrySet();
      
      for(Iterator iter = entrySet.iterator(); iter.hasNext();)
      {
         Map.Entry mapEntry = (Entry) iter.next();
         String property = (String)mapEntry.getKey();
         Object entry = mapEntry.getValue();
         if(entry instanceof Resetable)
         {
            Resetable resetObject = (Resetable)entry;
            resetObject.reset();
            if(resetObject.isValueBindingNullable())
            {
               properties.put(property, null);
            }
         }
      }
   }
   
   public void resetSession()
   {
      resetUser();
      propertyMap = getPropertyMap(false);
      synchronized(propertyMap)
      {
         resetPropertyMap(firstClassPropertyMap);
         resetPropertyMap(propertyMap);
      }
   }
   
   public String logout()
   {
      if(propertyMap != null)
      {
         propertyMap.clear(); 
         propertyMap = null;
      }
      if(firstClassPropertyMap != null)
      {
         firstClassPropertyMap.clear();
         firstClassPropertyMap = null;
      }
      FacesContext context = FacesContext.getCurrentInstance();
      if(context != null)
      {
         ValueBinding binding = context.getApplication().
            createValueBinding("#{sessionContext}");
         if(binding != null)
         {
            binding.setValue(context, null);
         }
         if(loggedInUser != null)
         {
            context.getExternalContext().getSessionMap().put("infinity.tenant", 
                  loggedInUser.getPartitionId());
         }
      }
      if(serviceFactory != null)
      {
         serviceFactory.close();
      }
      serviceFactory = null;
      loginData = null;
      return Constants.ACTION_LOGOUT;
   }
   
   /**
    * Tries to login, validates session, and sets logged in user and service factory.
    * In order to completely initialize the session {@link #initInternalSession()} has to be called.
    */
   public void login(String account, String password, Map properties) throws PortalException
   {
      loginData = new LoginData(account, password != null ? password.toCharArray() : new char[0], properties);
      ServiceFactory serviceFactory = loginData.getServiceFactory();
      
      login(serviceFactory);
   }

   /**
    * @param serviceFactory
    * @throws PortalException
    */
   public void login(ServiceFactory serviceFactory) throws PortalException
   {
      validateSession(serviceFactory);
      this.serviceFactory = serviceFactory;
   }
   
   /**
    * Completes initialization of session started with {@link #login(String, String, Map)}.
    * 
    *  @throws PortalException if previous login call was not successful or other exceptions occur. 
    */
   public void initInternalSession() throws PortalException
   {
      initSession();
   }
   
   public void initInternalSession(String account, String password, Map properties) throws PortalException
   {
      loginData = new LoginData(account, password != null ? password.toCharArray() : new char[0], properties);
      ServiceFactory serviceFactory = loginData.getServiceFactory();
      initSession(serviceFactory);
   }
   
   public void initPrincipalSession(HttpServletRequest request) throws PortalException
   {
      HttpSession httpSession = request.getSession(); 
      Map properties = null;
      if(httpSession != null)
      {
         properties = (Map) httpSession.getAttribute(Constants.HTTP_LOGIN_PROP_ATTR);
         httpSession.removeAttribute(Constants.HTTP_LOGIN_PROP_ATTR);
      }
      properties = properties != null ? properties : Collections.EMPTY_MAP;
      loginData = new LoginData(null, null, properties);
      ServiceFactory serviceFactory = loginData.getServiceFactory(request);
      initSession(serviceFactory);
   }
   
   private void initSession(ServiceFactory serviceFactory) throws PortalException
   {
      validateSession(serviceFactory);
      this.serviceFactory = serviceFactory;

      initSession();
   }

   private void validateSession(ServiceFactory serviceFactory)
   {
      try
      {
         // execute a dummy call to validate session
         if(isAdminRoleRequired())
         {
            AdministrationService service = serviceFactory.getAdministrationService();
            loggedInUser = service != null ? service.getUser() : null;
         }
         else
         {
            UserService service = serviceFactory.getUserService();
            loggedInUser = service != null ? service.getUser() : null;
         }
      }
      finally
      {
         if(loggedInUser == null)
         {
            loginData = null;
         }
         else
         {
            FacesContext.getCurrentInstance()
               .getExternalContext().getSessionMap().put("infinity.tenant", 
                     loggedInUser.getPartitionId());
         }
      }
   }
   
   private void initSession() throws PortalException
   {
      if (this.serviceFactory == null)
      {
         // Service factory needs to be set already
         throw new PortalException(PortalErrorClass.UNABLE_TO_INITIALIZE_SESSION);
      }
      try
      {
         /* Reset model cache to ensure that the latest models are fetched */
         ModelCache modelCache = ModelCache.findModelCache();
         modelCache.reset();

         if (isModelRequired())
         {
            Collection models = modelCache.getAllModels();
            if (models.isEmpty())
            {
               logout();
               throw new PortalException(PortalErrorClass.NO_DEPLOYED_MODEL);
            }
         }

         ApplicationContext.registerUser((HttpSession) FacesContext.getCurrentInstance().getExternalContext()
               .getSession(false), loggedInUser);

         this.propertyMap = getPropertyMap(false);
         synchronized (propertyMap)
         {
            if (!propertyMap.isEmpty())
            {
               Iterator mapIter = propertyMap.entrySet().iterator();
               Map sessionListenerMap = CollectionUtils.newMap();
               while (mapIter.hasNext())
               {
                  Map.Entry mapEntry = (Map.Entry) mapIter.next();
                  Object value = mapEntry.getValue();
                  if (value instanceof ISessionListener)
                  {
                     sessionListenerMap.put(mapEntry.getKey(), value);
                  }
               }
               propertyMap.clear();
               propertyMap.putAll(sessionListenerMap);
               resetPropertyMap(propertyMap);
            }
         }

         propagateNewSession();
      }
      catch (LoginFailedException e)
      {
         logout();
         throw e;
      }
      catch (AccessForbiddenException ex)
      {
         // ModelCache throws AFE when user with no-role tries login
         logout();
         throw ex;
      }
   }
   
   public void registerSessionListener(ISessionListener listener)
   {
      boolean alreadyInList = false;
      sessionListener = sessionListener != null ? sessionListener : CollectionUtils.newList();
      Iterator iter = sessionListener.iterator();
      while(iter.hasNext() && !alreadyInList)
      {
         String className = iter.next().getClass().getName();
         if(listener != null && listener.getClass().getName().compareTo(className) == 0)
         {
            alreadyInList = true;
         }
      }
      if(!alreadyInList)
      {
         sessionListener.add(listener);
      }
   }
   
   public boolean isAdminRoleRequired()
   {
      return adminRoleRequired;
   }
   
   public void setAdminRoleRequired(boolean required)
   {
      adminRoleRequired = required;
   }
   
   public boolean isModelRequired()
   {
      return modelRequired;
   }

   public void setModelRequired(boolean modelRequired)
   {
      this.modelRequired = modelRequired;
   }

   public boolean isSessionInitialized()
   {
      if(serviceFactory == null && loginData != null)
      {
         serviceFactory = loginData.getServiceFactory();
      }
      return serviceFactory != null;
   }
   
   public ServiceFactory getServiceFactory()
   {
      if(serviceFactory == null && loginData != null)
      {
         serviceFactory = loginData.getServiceFactory();
      }
      return serviceFactory;
   }
   
   public User getUser()
   {
      if(loggedInUser == null)
      { 
         // fallback - get currently logged in user from UserService
         resetUser();
      }
      return loggedInUser;
   }
   
   public void resetUser()
   {
      loggedInUser = null;
      if(getServiceFactory() != null)
      {
         UserService service = getServiceFactory().getUserService();
         if(service != null)
         {
            loggedInUser = service.getUser();
         }
      }
   }
   
   public IPreferencesManager getPreferencesManager()
   {
      if(preferencesManager == null)
      {
         List factories = ExtensionProviderUtils.getExtensionProviders(
               IPreferencesManager.Factory.class);
         for (int i = 0; i < factories.size() && preferencesManager == null; ++i)
         {
            final IPreferencesManager.Factory factory = (IPreferencesManager.Factory)factories.get(i);
            preferencesManager = factory.getPreferencesManager();
         }
      }
      return preferencesManager;
   }
   
   public IRepositoryManager getRepositoryManager()
   {
      if(repositoryManager == null)
      {
         List factories = ExtensionProviderUtils.getExtensionProviders(
               IRepositoryManager.Factory.class);
         for (int i = 0; i < factories.size() && repositoryManager == null; ++i)
         {
            final IRepositoryManager.Factory factory = (IRepositoryManager.Factory)factories.get(i);
            repositoryManager = factory.getRepositoryManager();
         }
      }
      return repositoryManager;
   }
   
   private void propagateNewSession()
   {
      String listeners = FacesContext.getCurrentInstance()
         .getExternalContext()
         .getInitParameter(Constants.SESSION_LISTENER_BEANS);
      if ( !StringUtils.isEmpty(listeners))
      {
         for (Iterator i = StringUtils.split(listeners, ","); i.hasNext();)
         {
            String listenerBeanId = (String) i.next();
            Object listenerBean = ManagedBeanUtils.getManagedBean(listenerBeanId);
            if (listenerBean instanceof IBpmClientSessionListener)
            {
               ((IBpmClientSessionListener) listenerBean).intializeSession(serviceFactory);
            }
         }
      }
      if(sessionListener != null)
      {
         Iterator iter = sessionListener.iterator();
         while(iter.hasNext())
         {
            ISessionListener listener = (ISessionListener)iter.next();
            if(listener != null)
            {
               listener.initializeSession();
            }
         }
      }
   }
   
   private static class LoginData implements Serializable
   {
      private final String userName;
      private final char[] password;
      private final Map properties;
      
      LoginData(String userName, char[] password, Map properties)
      {
         this.userName = userName;
         this.password = password;
         this.properties = properties != null ? new HashMap(properties) : Collections.EMPTY_MAP;
        
         HttpSession httpSession = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         Map mergedProps = new HashMap(properties);
         mergedProps.put(SecurityProperties.CRED_USER, userName);
         mergedProps.put(SecurityProperties.CRED_PASSWORD, password);
         LoginUtils.mergeDefaultCredentials(mergedProps);
         httpSession.setAttribute("properties",  mergedProps);
      }
            
      protected ServiceFactory getServiceFactory()
      {
         if(!StringUtils.isEmpty(userName))
         {
            return ServiceFactoryLocator.get(userName, new String(password), properties);
         }
         FacesContext facesCtx = FacesContext.getCurrentInstance();
         Object request = facesCtx != null ? facesCtx.getExternalContext() : null;
         if(request instanceof HttpServletRequest)
         {
            return getServiceFactory((HttpServletRequest)request);
         }
         return null;
      }
      
      protected ServiceFactory getServiceFactory(HttpServletRequest request)
      {
         return ServiceFactoryLocator.get(request, properties);
      }
   }
}