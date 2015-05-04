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

package org.eclipse.stardust.ui.web.rest.service;

import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.LocalizationInfoDTO;
import org.springframework.stereotype.Component;

/**
 * @author Johnson.Quadras
 *
 */
@Component
public class LocalizationService
{
   /**
    * Gets the localization info like date and time formats.
    * @return LocalizationInfoDTO dto
    */
   public LocalizationInfoDTO getLocalizationInfo()
   {
      LocalizationInfoDTO dto = new LocalizationInfoDTO();
      dto.dateFormat = DateUtils.getDateFormat();
      dto.dateTimeFormat = DateUtils.getDateTimeFormat();
      dto.timeFormat = DateUtils.getTimeFormat();
      return dto;
   }
}
