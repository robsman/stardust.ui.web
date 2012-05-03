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
package org.eclipse.stardust.ui.web.processportal.views.qualityassurance;

import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



/**
 * @author Yogesh.Manware
 * 
 */
public class QualityAssuranceCodeEntry extends DefaultRowModel implements Comparable<QualityAssuranceCodeEntry>
{
   private static final long serialVersionUID = 1397845474594716415L;
   private QualityAssuranceCode qualityAssuranceCode;
   private boolean selectedRow;
   private QualityACAutocompleteMultiSelector codesAutocompleteMultiSelector;
   private String description;
   private String code;

   public QualityAssuranceCodeEntry(QualityAssuranceCode qaCode, long modelOID)
   {
      super();
      this.qualityAssuranceCode = qaCode;
      description = I18nUtils.getQualityAssuranceDesc(qualityAssuranceCode, modelOID);
      code = I18nUtils.getQualityAssuranceCode(qualityAssuranceCode, modelOID);
   }

   public void remove()
   {
      codesAutocompleteMultiSelector.removeSelectedQualityAssuranceCodes(this);
   }

   public String getCode()
   {
      return code;
   }

   public String getDescription()
   {
      return description;
   }

   public boolean isSelectedRow()
   {
      return selectedRow;
   }

   public void setSelectedRow(boolean selectedRow)
   {
      this.selectedRow = selectedRow;
   }

   public QualityAssuranceCode getQualityAssuranceCode()
   {
      return qualityAssuranceCode;
   }

   public QualityACAutocompleteMultiSelector getCodesAutocompleteMultiSelector()
   {
      return codesAutocompleteMultiSelector;
   }

   public void setCodesAutocompleteMultiSelector(QualityACAutocompleteMultiSelector codesAutocompleteMultiSelector)
   {
      this.codesAutocompleteMultiSelector = codesAutocompleteMultiSelector;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((qualityAssuranceCode == null) ? 0 : qualityAssuranceCode.hashCode());
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      QualityAssuranceCodeEntry other = (QualityAssuranceCodeEntry) obj;
      if (qualityAssuranceCode == null)
      {
         if (other.qualityAssuranceCode != null)
            return false;
      }
      else if (!(qualityAssuranceCode.getCode().equalsIgnoreCase(other.qualityAssuranceCode.getCode())))
         return false;
      return true;
   }

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(QualityAssuranceCodeEntry other)
   {
      return qualityAssuranceCode.getCode().compareTo(other.getCode());
   }
}