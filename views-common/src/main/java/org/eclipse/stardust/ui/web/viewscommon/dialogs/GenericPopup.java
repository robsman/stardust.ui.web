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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class GenericPopup extends PopupUIComponentBean {

	private static final long serialVersionUID = 1L;
	private String includePath;

	public GenericPopup() {
		this("");
	}

	/**
	 * @param title
	 */
	public GenericPopup(String title) {
		super(title);
	}

	/**
	 * @return
	 */
	public static GenericPopup getCurrent() {
		return (GenericPopup) FacesUtils.getBeanFromContext("genericPopup");
	}

	public String getIncludePath() {
		return includePath;
	}

	public void setIncludePath(String includePath) {
		this.includePath = includePath;
	}

	@Override
	public void initialize() {

	}

}
