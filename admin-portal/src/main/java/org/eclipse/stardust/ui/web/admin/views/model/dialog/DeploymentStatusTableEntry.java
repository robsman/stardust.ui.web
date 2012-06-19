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
package org.eclipse.stardust.ui.web.admin.views.model.dialog;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class DeploymentStatusTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   private boolean complete;
   private int errors;
   private int warnings;

   public int getErrors()
   {
      return errors;
   }

   public int getWarnings()
   {
      return warnings;
   }

   public boolean isComplete()
   {
      return complete;
   }

   public void setComplete(boolean complete)
   {
      this.complete = complete;
   }

   public void setErrors(int errors)
   {
      this.errors = errors;
   }

   public void setWarnings(int warnings)
   {
      this.warnings = warnings;
   }

   public boolean hasErrors()
   {
      if (errors > 0)
      {
         return true;
      }
      return false;
   }
}
