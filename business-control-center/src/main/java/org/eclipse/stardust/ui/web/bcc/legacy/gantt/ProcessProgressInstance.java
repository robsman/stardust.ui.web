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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.bcc.legacy.ITimeProvider;



/**
 * An instance of this class represents a process instance which has to be assigned to a
 * corresponding ProcessProgressModel object.
 * 
 * @author mueller1
 * 
 */
public class ProcessProgressInstance
{

   private long oid;

   private Date startTime;

   private Date terminationTime;

   private long duration = -1;

   private Map<String, String> descriptors;

   private String note;

   private String error;

   public final static int PROCESS_STATE_ACTIVE = 1;

   public final static int PROCESS_STATE_COMPLETED = 2;

   public final static int PROCESS_STATE_ALL = 3;

   public final static int PROCESS_STATE_NONE = 4;

   private ProcessInstance processInstance;

   public ProcessProgressInstance(ProcessInstance processInstance)
   {
      this.oid = processInstance.getOID();
      this.startTime = processInstance.getStartTime();
      this.terminationTime = processInstance.getTerminationTime();

      this.descriptors = CollectionUtils.newHashMap();
      this.processInstance = processInstance;
      this.note = readNote();

   }

   private String readNote()
   {
      List<Note> notes = CollectionUtils.newArrayList();
      ProcessInstanceAttributes attributes = processInstance.getAttributes();
      if (attributes != null)
      {
         notes = attributes.getNotes();
      }
      return notes.isEmpty() ? null : (notes.get(notes.size() - 1)).getText();
   }

   public void setDescriptorKeys(List<DataPath> descriptorKeys)
   {
      for (Iterator<DataPath> _iterator = descriptorKeys.iterator(); _iterator
            .hasNext();)
      {
         DataPath dataPath = _iterator.next();
         if (dataPath.isDescriptor())
         {
            String key = dataPath.getId();
            Object value = ((IDescriptorProvider) processInstance)
                  .getDescriptorValue(key);
            if (value != null)
            {
               if (key.equals("Note"))
               {
                  this.note = value.toString();
               }
               else if (key.equals("Error"))
               {
                  this.error = value.toString();
               }
               else
               {
                  this.descriptors.put(key, value.toString());
               }
            }
         }
      }
   }

   public long getDuration()
   {
      ITimeProvider timeProvider = (ITimeProvider) Reflect
            .createInstance((PropertyProvider.getInstance().getTimeProviderClassName()));
      long now = timeProvider.getCurrentTime();
      this.duration = this.terminationTime != null ? this.terminationTime.getTime()
            - startTime.getTime() : now - startTime.getTime();
      return duration;
   }

   public long getOid()
   {
      return oid;
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public Date getTerminationTime()
   {
      return terminationTime;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("OID: ").append(this.oid).append("\n");
      buffer.append("Start Time: ").append(this.startTime).append("\n");
      buffer.append("End Time: ").append(this.terminationTime).append("\n");
      buffer.append("Duration: ").append(this.duration).append("\n");
      return buffer.toString();
   }

   public Map<String, String> getDescriptors()
   {
      return descriptors;
   }

   public String getNote()
   {
      return note;
   }

   public String getError()
   {
      return error;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

}
