/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;
import java.util.List;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class ProcessInstanceDTO
{
   private long oid;

   private Date start;

   private Date end;

   private ProcessDefinitionDTO processDefinition;

   private List<DescriptorDTO> descriptors;

   private List<SpecificDocumentDTO> specificDocuments;

   private List<DocumentDTO> processAttachments;

   /**
    * 
    */
   public ProcessInstanceDTO()
   {

   }

   /**
    * @return the oid
    */
   public long getOid()
   {
      return oid;
   }

   /**
    * @param oid
    *           the oid to set
    */
   public void setOid(long oid)
   {
      this.oid = oid;
   }

   /**
    * @return the start
    */
   public Date getStart()
   {
      return start;
   }

   /**
    * @param start
    *           the start to set
    */
   public void setStart(Date start)
   {
      this.start = start;
   }

   /**
    * @return the end
    */
   public Date getEnd()
   {
      return end;
   }

   /**
    * @param end
    *           the end to set
    */
   public void setEnd(Date end)
   {
      this.end = end;
   }

   /**
    * @return the processDefinition
    */
   public ProcessDefinitionDTO getProcessDefinition()
   {
      return processDefinition;
   }

   /**
    * @param processDefinition
    *           the processDefinition to set
    */
   public void setProcessDefinition(ProcessDefinitionDTO processDefinition)
   {
      this.processDefinition = processDefinition;
   }

   /**
    * @return the descriptors
    */
   public List<DescriptorDTO> getDescriptors()
   {
      return descriptors;
   }

   /**
    * @param descriptors
    *           the descriptors to set
    */
   public void setDescriptors(List<DescriptorDTO> descriptors)
   {
      this.descriptors = descriptors;
   }

   /**
    * @return the specificDocuments
    */
   public List<SpecificDocumentDTO> getSpecificDocuments()
   {
      return specificDocuments;
   }

   /**
    * @param specificDocuments
    *           the specificDocuments to set
    */
   public void setSpecificDocuments(List<SpecificDocumentDTO> specificDocuments)
   {
      this.specificDocuments = specificDocuments;
   }

   /**
    * @return the processAttachments
    */
   public List<DocumentDTO> getProcessAttachments()
   {
      return processAttachments;
   }

   /**
    * @param processAttachments
    *           the processAttachments to set
    */
   public void setProcessAttachments(List<DocumentDTO> processAttachments)
   {
      this.processAttachments = processAttachments;
   }

}
