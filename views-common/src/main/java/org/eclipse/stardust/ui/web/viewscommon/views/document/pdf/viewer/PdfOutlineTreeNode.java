/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ICEsoft Technologies Canada, Corp. - initial API and implementation
 *    SunGard CSA LLC                    - minor adjustments to adapt to change in package names etc.
 *******************************************************************************/

//Note: This file is derived from http://anonsvn.icefaces.org/repo/icepdf/tags/icepdf-3.0.0/icepdf/examples/icefaces/src/org/icepdf/examples/jsf/viewer/view/OutlineItemTreeNode.java (r18941)

package org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.viewer;

import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;

import org.icepdf.core.pobjects.Destination;
import org.icepdf.core.pobjects.OutlineItem;
import org.icepdf.core.pobjects.PageTree;
import org.icepdf.core.pobjects.actions.Action;
import org.icepdf.core.pobjects.actions.GoToAction;
import org.icepdf.core.util.Library;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * PDF document outline which can be used by the ice:tree component.
 */
public class PdfOutlineTreeNode extends DefaultMutableTreeNode
{
   private static final long serialVersionUID = 1L;

   private OutlineItem item;

   private boolean loadedChildren;

   private PageTree pageTree;

   /**
    * Creates a new instance of an OutlineItemTreeNode
    * 
    * @param item
    *           Contains PDF Outline item data
    */
   public PdfOutlineTreeNode(PageTree pageTree, OutlineItem item)
   {
      super();
      this.item = item;
      loadedChildren = false;
      this.pageTree = pageTree;

      // build the tree
      NodeUserObject tmp = new NodeUserObject(this.pageTree, this);
      // set callback
      setUserObject(tmp);
   }

   public OutlineItem getOutlineItem()
   {
      return item;
   }

   /**
    * recursivelyClearOutlineItems
    */
   public void recursivelyClearOutlineItems()
   {
      item = null;
      if (loadedChildren)
      {
         int count = getChildCount();
         PdfOutlineTreeNode node;
         for (int i = 0; i < count; i++)
         {
            node = (PdfOutlineTreeNode) getChildAt(i);
            node.recursivelyClearOutlineItems();
         }
      }
   }

   public int getChildCount()
   {
      ensureChildrenLoaded();
      return super.getChildCount();
   }

   /**
    * Only load children as needed, so don't have to load OutlineItems that the user has
    * not even browsed to
    */
   private void ensureChildrenLoaded()
   {
      if (!loadedChildren)
      {
         loadedChildren = true;

         int count = item.getSubItemCount();
         PdfOutlineTreeNode childTreeNode;
         for (int i = 0; i < count; i++)
         {
            OutlineItem child = item.getSubItem(i);
            childTreeNode = new PdfOutlineTreeNode(pageTree, child);
            add(childTreeNode);
         }
      }
   }

   public class NodeUserObject extends IceUserObject
   {
      private int goToPage;

      /**
       * @param pageTree
       * @param outlineItemTreeNode
       */
      public NodeUserObject(PageTree pageTree, PdfOutlineTreeNode outlineItemTreeNode)
      {
         super(outlineItemTreeNode);
         // append the destination page number
         if (null != outlineItemTreeNode.getOutlineItem().getDest())
         {
            goToPage = pageTree.getPageNumber(outlineItemTreeNode.getOutlineItem().getDest().getPageReference());
         }
         else if (null != outlineItemTreeNode.getOutlineItem().getAction())
         {
            OutlineItem item = outlineItemTreeNode.getOutlineItem();
            Destination dest;
            if (null != item.getAction())
            {
               Action action = item.getAction();
               if (action instanceof GoToAction)
               {
                  dest = ((GoToAction) action).getDestination();
               }
               else
               {
                  Library library = action.getLibrary();
                  Hashtable entries = action.getEntries();
                  dest = new Destination(library, library.getObject(entries, "D"));
               }
               goToPage = pageTree.getPageNumber(dest.getPageReference());
            }
         }

         // set title
         setText(outlineItemTreeNode.getOutlineItem().getTitle());

         // setup not state.
         setLeafIcon("tree_document.gif");
         setBranchContractedIcon("tree_document.gif");
         setBranchExpandedIcon("tree_document.gif");

         // is item a node or a leaf.
         if (outlineItemTreeNode.getOutlineItem().getSubItemCount() > 0)
         {
            setLeaf(false);
         }
         else
         {
            setLeaf(true);
         }

      }

      public int getGoToPage()
      {
         return goToPage;
      }

      public void setGoToPage(int goToPage)
      {
         this.goToPage = goToPage;
      }
   }
}
