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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import javax.swing.tree.DefaultMutableTreeNode;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * @author roland.stamm
 * 
 */
public class WorklistsTreeRoot extends DefaultMutableTreeNode
{
   private static final long serialVersionUID = -1719334047605758745L;

   /**
    * 
    */
   public WorklistsTreeRoot()
   {
      super();
      IceUserObject rootUserObject = new IceUserObject(this);
      rootUserObject.setExpanded(true);
      rootUserObject.setText("root");
      this.setUserObject(rootUserObject);
   }

}
