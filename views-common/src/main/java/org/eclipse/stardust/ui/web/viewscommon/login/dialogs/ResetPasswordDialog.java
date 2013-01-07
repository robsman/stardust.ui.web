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

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.PopupDialog;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.TechnicalUserUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

/**
 * @author Subodh.Godbole
 * 
 */
public class ResetPasswordDialog extends PopupDialog
{
   private static final long serialVersionUID = 1L;

   protected static final Logger trace = LogManager.getLogger(ResetPasswordDialog.class);

   private static final String FORM_ID = "resetPwdForm";
   private static final String COMMON_MESSAGE_ID = "resetPwdCommonMsg";

   private String account;
   private String realm;
   private String domain;
   private String partition;

   public ResetPasswordDialog()
   {
      super("");
      fireViewEvents = false;
   }

   @Override
   public void openPopup()
   {
      LoginDialogBean loginDlg = LoginDialogBean.getInstance();
      setDomain(loginDlg.getDomain());
      setPartition(loginDlg.getPartition());
      setRealm(loginDlg.getRealm());
      super.setPopupAutoCenter(false);
      super.openPopup();
   }

   @Override
   public void apply()
   {
      boolean success = false;

      SessionContext sessionCtx;

      Map<String, String> loginProperties = getLoginProperties();

      // Login With Technical User
      try
      {
         sessionCtx = TechnicalUserUtils.login(loginProperties);

         // Reset Password
         if (trace.isDebugEnabled())
         {
            trace.debug("Resetting Pwd for - " + account);
         }

         trace.info("About to call reset pwd for " + account + ", props: " + loginProperties);
         sessionCtx.getServiceFactory().getUserService().resetPassword(account, loginProperties);

         if (trace.isDebugEnabled())
         {
            trace.debug("Reset Pwd Success for - " + account);
         }

         TechnicalUserUtils.logout(sessionCtx);
         success = true;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(FORM_ID + ":" + COMMON_MESSAGE_ID, e);
      }
      reset();
      setVisible(!success); // This is required, otherwise Popup Dialog disappears
   }

   @Override
   public void reset()
   {
      account = null;
      FacesUtils.clearFacesTreeValues();
   }

   @Override
   public void closePopup()
   {
      reset();
      super.closePopup();
   }

   private Map<String, String> getLoginProperties()
   {
      LoginDialogBean loginDlg = LoginDialogBean.getInstance();

      Map<String, String> properties = CollectionUtils.newHashMap();
      if (loginDlg.promptForDomain && !StringUtils.isEmpty(domain))
      {
         properties.put(SecurityProperties.DOMAIN, domain);
      }
      if (loginDlg.promptForPartition || !StringUtils.isEmpty(partition))
      {
         properties.put(SecurityProperties.PARTITION, partition);
      }
      if (loginDlg.promptForRealm && !StringUtils.isEmpty(realm))
      {
         properties.put(SecurityProperties.REALM, realm);
      }
      return Collections.unmodifiableMap(properties);
   }

   public String getAccount()
   {
      return account;
   }

   public void setAccount(String account)
   {
      this.account = account;
   }

   public String getRealm()
   {
      return realm;
   }

   public void setRealm(String realm)
   {
      this.realm = realm;
   }

   public String getDomain()
   {
      return domain;
   }

   public void setDomain(String domain)
   {
      this.domain = domain;
   }

   public String getPartition()
   {
      return partition;
   }

   public void setPartition(String partition)
   {
      this.partition = partition;
   }
}
