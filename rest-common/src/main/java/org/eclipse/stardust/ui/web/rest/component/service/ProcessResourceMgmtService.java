/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.component.service;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.rest.component.util.ProcessResourceMgmtUtils;
import org.eclipse.stardust.ui.web.rest.dto.ProcessResourceMgmtDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProcessResourceMgmtService
{
   @Resource
   private ProcessResourceMgmtUtils processResourceMgmtUtils;

  /**
   * 
   * @return
   */
   public ProcessResourceMgmtDTO getProcessResourceRoles()
   {
      ProcessResourceMgmtDTO processResourceMgmtDTO = new ProcessResourceMgmtDTO();
      processResourceMgmtDTO.processResourceRoleList = processResourceMgmtUtils.getProcessResourceRoles();
      return processResourceMgmtDTO;
   }

   /**
    * 
    * @return
    */
   public ProcessResourceMgmtDTO getProcessResourceUsers()
   {
      ProcessResourceMgmtDTO processResourceMgmtDTO = new ProcessResourceMgmtDTO();
      processResourceMgmtDTO.processResourceUserList = processResourceMgmtUtils.getProcessResourceUsers();
      return processResourceMgmtDTO;
   }

}
