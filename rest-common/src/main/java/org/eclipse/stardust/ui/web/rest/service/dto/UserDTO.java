/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
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
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

public class UserDTO extends AbstractDTO {
	@DTOAttribute("id")
	private String id;

	@DTOAttribute("name")
	private String name;

	@DTOAttribute("validFrom")
	private Date validFrom;

	@DTOAttribute("validTo")
	private Date validTo;

	@DTOAttribute("description")
	private String description;

	@DTOAttribute("eMail")
	private String eMail;

	@DTOAttribute("realm.name")
	private String realm;
	
	private String userImageURI;

	public String getUserImageURI() {
		return userImageURI;
	}

	public void setUserImageURI(String userImageURI) {
		this.userImageURI = userImageURI;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEMail() {
		return eMail;
	}

	public void setEMail(String eMail) {
		this.eMail = eMail;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
