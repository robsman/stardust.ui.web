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

import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.PopupDialog;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.login.util.PasswordUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;



/**
 * @author Subodh.Godbole
 *
 */
public class ChangePasswordDialog extends PopupDialog
{
	private static final long serialVersionUID = 1L;
	
	protected static final Logger trace = LogManager.getLogger(ChangePasswordDialog.class);

	private static final String FORM_ID = "changePwdForm";
	private static final String COMMON_MESSAGE_ID = "changePwdCommonMsg";

	private String account;
	private String oldPassword;
	private String newPassword;
	private String newPasswordConf;
	
	/**
	 * 
	 */
	public ChangePasswordDialog()
	{
		super("");
		fireViewEvents = false;
	}

	@Override
	public void apply()
	{
	}

	@Override
	public void reset()
	{
	}

	/**
	 * Can not use the PopupDialog.apply as it's returning void
	 * @return
	 */
	public String submit()
	{
		try
		{
			if(!newPassword.equals(newPasswordConf))
			{
			   raiseUIMessage(MessagePropertiesBean.getInstance().getString(
                  "common.passwordRules.message.newPasswordsMismatch"));
			   return null;
			}

			// Change password
			SessionContext sessionCtx = SessionContext.findSessionContext();
			User user = sessionCtx.getUser();
			sessionCtx.getServiceFactory().getUserService().modifyLoginUser(oldPassword,
               user.getFirstName(), user.getLastName(), newPassword, user.getEMail());
			trace.debug("Change Password Successful...");
			
			sessionCtx.resetUser();
			LoginDialogBean loginDlg = LoginDialogBean.getInstance();
			sessionCtx.initInternalSession(account, newPassword, loginDlg.getLoginProperties());
			trace.debug("New session initialized...");
			
			String str = LoginDialogBean.getInstance().proceedToMainPage();
			super.closePopup();
			
			trace.debug("Forwarding to Main Page...");
			return str;
		}
		catch (InvalidPasswordException ipe)
		{
		   trace.error("Error in Changing Password", ipe);
		   raiseErrorWithMessageCode(ipe);
		}
		catch (Exception e)
		{
		   trace.error("Error in Changing Password", e);
		   ExceptionHandler.handleException(FORM_ID + ":" + COMMON_MESSAGE_ID, e);
		}

        trace.debug("Modify Pwd Dialog staying Visible...");
		super.setVisible(true); // Stay Visible

		return null;
	}

	/**
	 * @param ipe
	 */
	private void raiseErrorWithMessageCode(InvalidPasswordException ipe)
	{
	   String errMessages = PasswordUtils.decodeInvalidPasswordMessage(ipe, null); 

	   if(StringUtils.isNotEmpty(errMessages))
	   {
      	 raiseUIMessage(errMessages);
	   }
	   else
	   {
	      ExceptionHandler.handleException(FORM_ID + ":" + COMMON_MESSAGE_ID, ipe);
	   }
	}

	/**
	 * @param msg
	 */
	private void raiseUIMessage(String msg)
	{
	   ExceptionHandler.handleException(FORM_ID + ":" + COMMON_MESSAGE_ID, msg);
	}
	
	/**
	 * @return
	 */
	public String cancel()
	{
	   SessionContext.findSessionContext().logout();
	   closePopup();
	   return null;
	}

	public String getAccount()
	{
		return account;
	}

	public void setAccount(String account)
	{
		this.account = account;
	}

	public String getOldPassword()
	{
		return oldPassword;
	}

	public void setOldPassword(String oldPassword)
	{
		this.oldPassword = oldPassword;
	}

	public String getNewPassword()
	{
		return newPassword;
	}

	public void setNewPassword(String newPassword)
	{
		this.newPassword = newPassword;
	}

	public String getNewPasswordConf()
	{
		return newPasswordConf;
	}

	public void setNewPasswordConf(String newPasswordConf)
	{
		this.newPasswordConf = newPasswordConf;
	}
}
