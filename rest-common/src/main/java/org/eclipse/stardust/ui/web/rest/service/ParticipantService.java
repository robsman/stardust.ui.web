/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
public interface ParticipantService
{
   /**
    * @param participantQidIn
    * @return
    */
   public List<ParticipantDTO> getParticipant(String participantQidIn);
}