/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.component.util.ProcessActivityUtils;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
public class FilterAttributesDTO implements Serializable
{
   private static final long serialVersionUID = 1L;

   private int state;

   private Long oid;

   private Long rootOid;

   private Date startedFrom;
   private Date startedTo;
   private Date endTimeFrom;
   private Date endTimeTo;

   private boolean caseOnlySearch;
   private boolean includeRootProcess;
   private User user;
   private int priority;
   private boolean includeCaseSearch;

   /**
    * 
    */
   public FilterAttributesDTO()
   {
      state = ProcessActivityUtils.PROCESS_INSTANCE_STATE_ALIVE;
      priority = ProcessActivityUtils.ALL_PRIORITIES;
   }

  
   public Long getOid()
   {
      return oid;
   }

   public void setOid(Long oid)
   {
      this.oid = oid;
   }

   public Long getRootOid()
   {
      return rootOid;
   }

   public void setRootOid(Long rootOid)
   {
      this.rootOid = rootOid;
   }

   public int getState()
   {
      return state;
   }

   public void setState(int state)
   {
      this.state = state;
   }

   protected boolean validParameters()
   {
      return true;
   }

   public TimeZone getTimeZone()
   {
      return java.util.TimeZone.getDefault();
   }

   public Date getStartedFrom()
   {
      return startedFrom;
   }

   public void setStartedFrom(Date startedFrom)
   {
      this.startedFrom = startedFrom;
   }

   public Date getStartedTo()
   {
      return startedTo;
   }

   public void setStartedTo(Date startedTo)
   {
      this.startedTo = startedTo;
   }

   public Date getEndTimeFrom()
   {
      return endTimeFrom;
   }

   public void setEndTimeFrom(Date endTimeFrom)
   {
      this.endTimeFrom = endTimeFrom;
   }

   public Date getEndTimeTo()
   {
      return endTimeTo;
   }

   public void setEndTimeTo(Date endTimeTo)
   {
      this.endTimeTo = endTimeTo;
   }

   public boolean isCaseOnlySearch()
   {
      return caseOnlySearch;
   }

   public void setCaseOnlySearch(boolean caseOnlySearch)
   {
      this.caseOnlySearch = caseOnlySearch;
   }

   public boolean isIncludeRootProcess()
   {
      return includeRootProcess;
   }

   public void setIncludeRootProcess(boolean includeRootProcess)
   {
      this.includeRootProcess = includeRootProcess;
   }

   public User getUser()
   {
      return user;
   }

   public void setUser(User user)
   {
      this.user = user;
   }

   public int getPriority()
   {
      return priority;
   }

   public void setPriority(int priority)
   {
      this.priority = priority;
   }

   public void setIncludeCaseSearch(boolean includeCaseSearch)
   {
      this.includeCaseSearch = includeCaseSearch;
   }

   public boolean isIncludeCaseSearch()
   {
      return includeCaseSearch;
   }

}
