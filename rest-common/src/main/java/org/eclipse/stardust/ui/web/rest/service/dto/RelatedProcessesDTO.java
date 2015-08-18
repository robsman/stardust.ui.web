package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

public class RelatedProcessesDTO
{

   @DTOAttribute("oid")
   public long oid;

   @DTOAttribute("priority")
   public String priority;

   @DTOAttribute("startTime")
   public Date startTime;

   @DTOAttribute("processName")
   public String processName;

   @DTOAttribute("descriptorValues")
   public Map<String, Object> descriptorValues;

   @DTOAttribute("caseInstance")
   public boolean caseInstance;

   @DTOAttribute("caseOwner")
   public String caseOwner;
}
