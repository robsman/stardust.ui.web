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
package org.eclipse.stardust.ui.web.rest.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;

/**
 * 
 * @author Johnson.Quadras
 *
 */
public class PortalConfigurationDTO extends AbstractDTO
{

   public Integer maxTabsDisplay;

   public Integer pageSize;

   public Integer paginatorMaxPages;

   public Integer paginatorFastStep;

   public List<SelectItemDTO> availableSkins;

   public String selectedSkin;

   public UserProvider userProvider;

   public ThemeProvider themeProvider;

   public ConfirmationDialog portalConfirmationDialog;

   public List<SelectItemDTO> availablePerspectives;

   public String selectedPerspective;

}
