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
package org.eclipse.stardust.ui.web.viewscommon.login.dialogs;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.beans.ApplicationContext;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.login.InfinityStartup;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;



public class LoginDialogBean implements Serializable
{
   private static final long serialVersionUID = -2703702864230398366L;

   protected static final Logger trace = LogManager.getLogger(LoginDialogBean.class);

   public static final String DEFAULT_LOGIN_SKIN_CSS_NAME = "login.css";
   public static final String LOGIN_SKIN_CSS_PARAM = "Carnot.Login.Skin.StyleSheet";

   private String account;

   private String password;

   private String realm;

   private String domain;

   private String partition;
   
   boolean promptForPartition;

   boolean promptForRealm;

   boolean promptForDomain;
   
   ChangePasswordDialog changePwdDialog;
   ResetPasswordDialog resetPwdDialog;

   private static final String FORM_ID = "loginForm";
   
   private static final String MESSAGE_ID = "commonMessage";

   public static final String BEAN_ID = "ippLoginDialog";
   
   private boolean principalLogin;
   
   private String loginStyleSheetName;
   
   public LoginDialogBean()
   {
	  changePwdDialog = new ChangePasswordDialog();
	  resetPwdDialog = new ResetPasswordDialog();
	  
	  principalLogin = ApplicationContext.isPrincipalLogin();
	  
      this.promptForPartition = Parameters.instance().getBoolean(
            SecurityProperties.PROMPT_FOR_PARTITION, false);
      this.promptForRealm = Parameters.instance().getBoolean(
            SecurityProperties.PROMPT_FOR_REALM, false);
      this.promptForDomain = Parameters.instance().getBoolean(
            SecurityProperties.PROMPT_FOR_DOMAIN, false);

      if (promptForPartition)
      {
         partition = Parameters.instance().getString(
               SecurityProperties.DEFAULT_PARTITION, "");
      }
      if (promptForRealm)
      {
         realm = Parameters.instance().getString(SecurityProperties.DEFAULT_REALM, "");
      }
      if (promptForDomain)
      {
         domain = Parameters.instance().getString(SecurityProperties.DEFAULT_DOMAIN, "");
      }

      String tenant = FacesUtils.getRequestParameter("tenant");
      if(StringUtils.isNotEmpty(tenant))
      {
         partition = tenant;
      }
      
      loginStyleSheetName = Parameters.instance().getString(LoginDialogBean.LOGIN_SKIN_CSS_PARAM,
            LoginDialogBean.DEFAULT_LOGIN_SKIN_CSS_NAME);
   }

   /**
    * @return
    */
   public static LoginDialogBean getInstance()
   {
	   return (LoginDialogBean)FacesUtils.getBeanFromContext("ippLoginDialog");
   }
   
   public String getLoginHeader()
   {
      String result = null;

      String headingNlsKey = FacesContext.getCurrentInstance()
            .getExternalContext()
            .getInitParameter(Constants.LOGIN_HEADING);
      if ( !StringUtils.isEmpty(headingNlsKey) && ( -1 != headingNlsKey.indexOf("#")))
      {
         String bundleName = null;
         String nlsKey = null;
         Iterator<String> i = StringUtils.split(headingNlsKey, "#");
         if (i.hasNext())
         {
            bundleName = i.next();
         }
         if (i.hasNext())
         {
            nlsKey = i.next();
         }

         if ( !StringUtils.isEmpty(bundleName) && !StringUtils.isEmpty(nlsKey))
         {
            result = Localizer.getString(new LocalizerKey(bundleName, nlsKey));
         }
      }

      if (null == result)
      {
         result = MessagePropertiesBean.getInstance().getString("portalFramework.title");
      }

      return result;
   }

   public String getAccount()
   {
      trace.debug("getAccount");
      return account;
   }

   public void setAccount(String account)
   {
      this.account = account;
      trace.debug("setAccount: " + account);
   }

   public String getPassword()
   {
      trace.debug("getPassword");
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
      trace.debug("setPassword");
   }

   public Map<String, String> getLoginProperties()
   {
      Map<String, String> properties = CollectionUtils.newHashMap();
      if (promptForDomain && !StringUtils.isEmpty(domain))
      {
         properties.put(SecurityProperties.DOMAIN, domain);
      }
      if (promptForPartition || !StringUtils.isEmpty(partition))
      {
         properties.put(SecurityProperties.PARTITION, partition);
      }
      if (promptForRealm && !StringUtils.isEmpty(realm))
      {
         properties.put(SecurityProperties.REALM, realm);
      }
      return Collections.unmodifiableMap(properties);
   }
   
   /**
    * @return
    */
   public boolean isPrincipalLogin()
   {
      return principalLogin;
   }

   public String login()
   {
      try
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("About to log in ...");
         }

         if (org.eclipse.stardust.common.StringUtils.isEmpty(account))
         {
            ExceptionHandler.handleException(FORM_ID + ":" + MESSAGE_ID,
                  MessagePropertiesBean.getInstance().getString("loginDialog.error.principalAuthFailed"));
            return null;
         }

         if (ApplicationContext.isPrincipalLogin())
         {
            ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
            Map<String, Object> sessionMap = ectx.getSessionMap();
            sessionMap.put(Constants.HTTP_LOGIN_PROP_ATTR, getLoginProperties());
            return Constants.WORKFLOW_PRINCIPAL_LOGIN;
         }
         else
         {
            SessionContext sessionCtx = SessionContext.findSessionContext();
            sessionCtx.login(account, password, getLoginProperties());
            
            if (trace.isDebugEnabled())
            {
               trace.debug("User " + getAccount() + " successfully logged in.");
            }

            // Change Pwd Dialog
            if(sessionCtx.getUser().isPasswordExpired()) 
            {
            	changePwdDialog.setAccount(getAccount());
            	changePwdDialog.openPopup();
            	return null;
            }
            else
            {
               sessionCtx.initInternalSession();
            }
         }

         return proceedToMainPage();
      }
      catch (Exception e)
      {
         trace.error("Error occurred durin login", e);
         ExceptionHandler.handleException(MESSAGE_ID, e);
         return null; // Constants.WORKFLOW_FAILURE;
      }
      finally
      {
         // No need to keep sensitive/private information
         password = null;
      }
   }
   
   /**
    * @throws IOException
    */
   public String proceedToMainPage() throws IOException
   {
      String returnUrl = FacesUtils.getQueryParameterValue(InfinityStartup.RETURN_URL_PARAM);

      if (!StringUtils.isEmpty(returnUrl))
      {
         FacesUtils.sendRedirect(returnUrl);
         return null;
      }

      return getNavigationOutcome();
   }
   
   public String getNavigationOutcome()
   {
      String applicationId = ApplicationContext.findApplicationContext().getApplicationId();
      return !StringUtils.isEmpty(applicationId)
         ? Constants.LOGGED_INTO_PREFIX + applicationId
         : Constants.WORKFLOW_SUCCESS;
   }

   public String getDomain()
   {
      return domain;
   }

   public void setDomain(String domain)
   {
      this.domain = domain;
   }

   public boolean isPromptForDomain()
   {
      return promptForDomain;
   }

   public String getPartition()
   {
      return partition;
   }

   public void setPartition(String partition)
   {
      this.partition = partition;
   }

   public boolean isPromptForPartition()
   {
      return promptForPartition;
   }

   public String getRealm()
   {
      return realm;
   }

   public void setRealm(String realm)
   {
      this.realm = realm;
   }

   public boolean isPromptForRealm()
   {
      return promptForRealm;
   }

   public ChangePasswordDialog getChangePwdDialog()
   {
	   return changePwdDialog;
   }

   public ResetPasswordDialog getResetPwdDialog()
   {
      return resetPwdDialog;
   }

   public String getLoginStyleSheetName()
   {
      return loginStyleSheetName;
   }
}
