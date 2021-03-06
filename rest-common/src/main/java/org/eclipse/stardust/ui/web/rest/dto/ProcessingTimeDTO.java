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
package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
@DTOClass
public class ProcessingTimeDTO extends AbstractDTO
{
   public String averageTime;

   public String averageWaitingTime;

   public int state;
   
   public ProcessingTimeDTO(String averageTime, String averageWaitingTime, int state)
   {
      super();
      this.averageTime = averageTime;
      this.averageWaitingTime = averageWaitingTime;
      this.state = state;
   }

public ProcessingTimeDTO() {
	// TODO Auto-generated constructor stub
}

}
