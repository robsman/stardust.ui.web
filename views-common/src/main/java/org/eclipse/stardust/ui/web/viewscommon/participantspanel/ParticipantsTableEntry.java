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
package org.eclipse.stardust.ui.web.viewscommon.participantspanel;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;


/**
 * @author subodh.godbole
 *
 */
public class ParticipantsTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = -2157476347403836668L;

    private String firstName;
	private String lastName;
	private String account;
	private boolean online;
	private Date lastLoginTime;
	private String email;
	private Date validFrom;
	private Date validTo;
	
	private List<ParticipantsTableEntryDyna> dynaAttributes;

	/**
	 * @param firstName
	 * @param lastName
	 * @param account
	 * @param online
	 * @param lastLoginTime
	 * @param email
	 * @param validFrom
	 * @param validTo
	 */
	public ParticipantsTableEntry(String firstName, String lastName, String account,
         boolean online, Date lastLoginTime, String email, Date validFrom, Date validTo)
   {
      super();
      this.firstName = firstName;
      this.lastName = lastName;
      this.account = account;
      this.online = online;
      this.lastLoginTime = lastLoginTime;
      this.email = email;
      this.validFrom = validFrom;
      this.validTo = validTo;
   }

	/**
	 * @param firstName
	 * @param lastName
	 * @param account
	 * @param online
	 * @param lastLoginTime
	 */
	public ParticipantsTableEntry(String firstName, String lastName, String account,
         boolean online, Date lastLoginTime)
   {
      super();
      this.firstName = firstName;
      this.lastName = lastName;
      this.account = account;
      this.online = online;
      this.lastLoginTime = lastLoginTime;
   }

	public String getFirstName()
	{
		return firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getAccount()
	{
		return account;
	}

	public boolean isOnline()
	{
		return online;
	}

	public Date getLastLoginTime()
	{
		return lastLoginTime;
	}

   public String getEmail()
   {
      return email;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public Date getValidTo()
   {
      return validTo;
   }

   public List<ParticipantsTableEntryDyna> getDynaAttributes()
   {
      return dynaAttributes;
   }

   public void setDynaAttributes(List<ParticipantsTableEntryDyna> dynaAttributes)
   {
      this.dynaAttributes = dynaAttributes;
   }

   @Override
   public String toString()
   {
      return firstName + ":" + lastName;
   }
}
