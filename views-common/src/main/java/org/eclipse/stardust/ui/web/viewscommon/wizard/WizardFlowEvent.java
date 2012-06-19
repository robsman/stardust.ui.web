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
package org.eclipse.stardust.ui.web.viewscommon.wizard;

/**
 * An WizardFlowEvent represents the flow event of a wizard page.
 * 
 * @author vikas.mishra
 * @version $Revision: $
 */
public class WizardFlowEvent
{
   private final WizardFlowEventType type;
   private final WizardPage newPage;
   private final WizardPage oldPage;
   private boolean vetoed;
   private boolean jumped;
   private int jumpToIndex;

   public WizardFlowEvent(WizardFlowEventType type, WizardPage newPage, WizardPage oldPage)
   {
      this.type = type;
      this.newPage = newPage;
      this.oldPage = oldPage;
   }

   public WizardPage getNewPage()
   {
      return newPage;
   }

   public WizardPage getOldPage()
   {
      return oldPage;
   }

   public WizardFlowEventType getType()
   {
      return type;
   }

   public boolean isVetoed()
   {
      return vetoed;
   }

   public void setVetoed(boolean vetoed)
   {
      this.vetoed = vetoed;
   }

   public static enum WizardFlowEventType {
      PREVIOUS, FINISH, NEXT, UNKNOWN
   }

   public void setJumpToIndex(int jumpToIndex)
   {
      this.jumpToIndex = jumpToIndex;
      jumped = true;
   }

   public boolean isJumped()
   {
      return jumped;
   }

   public int getJumpToIndex()
   {
      return jumpToIndex;
   }

}
