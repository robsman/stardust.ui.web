/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.List;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.reporting.core.orm.DataField;

public abstract class ReponseDataBuilder
{
   private List<DataField> dataFields;

   public ReponseDataBuilder(List<DataField> dataFields)
   {
      this.dataFields = dataFields;
   }

   protected List<DataField> getDataFields()
   {
      return dataFields;
   }

   public abstract void addValue(DataField field, Object value);
   public abstract void next();
   public abstract JsonObject getResult();
}

