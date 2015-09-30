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
package org.eclipse.stardust.ui.web.viewscommon.processContextExplorer;

import java.util.Date;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class DescriptorHistoryTableEntry extends DefaultRowModel
{
   /**
    * 
    */
   private static final long serialVersionUID = 4612785067825232167L;

   private Date timestamp;

   private String pathId;

   private String eventType;

   private String user;

   private String descDetails;

   public DescriptorHistoryTableEntry(Date timestamp, String pathId, String eventType, String user, String descDetails)
   {
      super();
      this.timestamp = timestamp;
      this.pathId = pathId;
      this.eventType = eventType;
      this.user = user;
      this.descDetails = descDetails;
   }

   public Date getTimestamp()
   {
      return timestamp;
   }

   public String getPathId()
   {
      return pathId;
   }

   public String getEventType()
   {
      return eventType;
   }

   public String getUser()
   {
      return user;
   }

   public String getDescDetails()
   {
      return descDetails;
   }
}