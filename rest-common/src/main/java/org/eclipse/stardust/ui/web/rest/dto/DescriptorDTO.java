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

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class DescriptorDTO extends AbstractDTO{
	public String key;

	public String value;

	public boolean isDocument;
	
	public boolean isLink;
	
	public String linkText;
	
	public List<DocumentDTO> documents;

	public DescriptorDTO(String key, String value, boolean isDocument,
			List<DocumentDTO> documents) {
		super();
		this.key = key;
		this.value = value;
		this.isDocument = isDocument;
		this.documents = documents;
	}
	
	public DescriptorDTO(String key, String value, boolean isDocument,
          List<DocumentDTO> documents, boolean isLink, String linkText) 
	{
      super();
      this.key = key;
      this.value = value;
      this.isDocument = isDocument;
      this.documents = documents;
      this.isLink = isLink;
      this.linkText = linkText;
  }

   public DescriptorDTO()
   {
      super();
   }


}
