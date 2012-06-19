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
package org.eclipse.stardust.ui.web.common.treetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 *
 */
public class TreeTableNode extends DefaultMutableTreeNode implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Override
	public TreeTableUserObject getUserObject()
	{
		return (TreeTableUserObject) super.getUserObject();
	}

	@Override
	public TreeTableNode getParent()
	{
		return (TreeTableNode) parent;
	}

	public List<TreeTableNode> getChildren()
	{
		List<TreeTableNode> list = new ArrayList<TreeTableNode>();
		Enumeration enumer = super.children();
		
		while (enumer.hasMoreElements())
		{
			list.add((TreeTableNode) enumer.nextElement());
		}
		
		return list;
	}
}
