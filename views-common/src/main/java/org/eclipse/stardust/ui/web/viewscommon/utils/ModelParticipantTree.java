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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Role;


/**
 * Represents the {@link ModelParticipant} tree hierarchy.
 */
public class ModelParticipantTree
{
   /**
    * Returns the path consisting of {@link ModelParticipant} starting with the top level
    * {@link Participant}.
    * 
    * @param qualifiedParticipantId
    * @return
    */
   public static List<ModelParticipant> getModelParticipantPath(String qualifiedParticipantId)
   {
      List<ModelParticipant> participantPath = CollectionUtils.newList();

      String modelId = ModelUtils.extractModelId(qualifiedParticipantId);
      if (null != modelId)
      {
         DefaultTreeModel participantTreeModel = getAllParticipantsTree(modelId);
         DefaultMutableTreeNode participantTreeRoot = (DefaultMutableTreeNode) participantTreeModel.getRoot();
         DefaultMutableTreeNode node = searchNode(participantTreeRoot, qualifiedParticipantId);

         if (null != node)
         {
            Object[] userObjectPath = node.getUserObjectPath();
            for (Object object : userObjectPath)
            {
               if (null != object && object instanceof ModelParticipant) // Ignore "null" root node
               {
                  participantPath.add((ModelParticipant) object);
               }
            }
         }
      }
      else if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(qualifiedParticipantId))
      {
         ModelParticipant adminParticipant = (ModelParticipant) ModelUtils.getAdminParticipant();
         if (null != adminParticipant)
         {
            participantPath.add(adminParticipant);
         }
      }

      return participantPath;
   }

   /**
    * @param modelId
    * @return
    */
   private static DefaultTreeModel getAllParticipantsTree(String modelId)
   {
      Model activeModel = ModelUtils.getActiveModel(modelId);
      return getAllParticipantsTree(activeModel.getModelOID());
   }

   /**
    * Returns the tree of all {@link ModelParticipant}s for the given modelOid.
    * 
    * @param modelOid
    * @return
    */
   private static DefaultTreeModel getAllParticipantsTree(long modelOid)
   {
      DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
      DefaultTreeModel participantTreeModel = new DefaultTreeModel(rootTreeNode);

      Model model = ModelUtils.getModelCache().getModel(modelOid);

      // Top-level Organizations
      @SuppressWarnings("unchecked")
      List<Organization> topLevelOrganizations = model.getAllTopLevelOrganizations();
      for (Organization organization : topLevelOrganizations)
      {
         addOrganization(rootTreeNode, organization);
      }

      // Top-level Roles
      @SuppressWarnings("unchecked")
      List<Role> topLevelRoles = model.getAllTopLevelRoles();
      for (Role role : topLevelRoles)
      {
         addModelParticipantNode(rootTreeNode, role);
      }

      return participantTreeModel;
   }

   /**
    * Adds an {@link Organization} node to the tree. It also adds all containing
    * sub-organizations and sub-roles.
    * 
    * @param node
    * @param organization
    */
   private static void addOrganization(DefaultMutableTreeNode node, Organization organization)
   {
      DefaultMutableTreeNode orgNode = addModelParticipantNode(node, organization);

      @SuppressWarnings("unchecked")
      List<Organization> subOrganizations = organization.getAllSubOrganizations();
      for (Organization subOrg : subOrganizations)
      {
         addOrganization(orgNode, subOrg);
      }

      @SuppressWarnings("unchecked")
      List<Role> subRoles = organization.getAllSubRoles();
      for (Role role : subRoles)
      {
         addModelParticipantNode(orgNode, role);
      }
   }

   /**
    * Adds an {@link ModelParticipant} node to the tree.
    * 
    * @param parentNode
    * @param modelParticipant
    * @return
    */
   private static DefaultMutableTreeNode addModelParticipantNode(DefaultMutableTreeNode parentNode,
         ModelParticipant modelParticipant)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();

      childNode.setUserObject(modelParticipant);
      if (parentNode != null)
      {
         parentNode.add(childNode);
      }

      return childNode;
   }

   /**
    * Returns the node under the root which matches the provided qualified Id. Returns
    * <code>null</code> if no match is found.
    * 
    * @param root
    * @param qualifiedId
    * @return
    */
   private static DefaultMutableTreeNode searchNode(DefaultMutableTreeNode root, String qualifiedParticipantId)
   {
      DefaultMutableTreeNode node = null, searchNode = null;

      if (null != qualifiedParticipantId)
      {
         ModelParticipant participant = null;

         @SuppressWarnings("unchecked")
         Enumeration<DefaultMutableTreeNode> e = root.breadthFirstEnumeration();
         while (e.hasMoreElements())
         {
            node = e.nextElement();
            if (null != node.getUserObject() && node.getUserObject() instanceof ModelParticipant)
            {
               participant = (ModelParticipant) node.getUserObject();
               if (qualifiedParticipantId.equals(participant.getQualifiedId()))
               {
                  searchNode = node;
                  break;
               }
            }
         }
      }

      return searchNode;
   }
}
