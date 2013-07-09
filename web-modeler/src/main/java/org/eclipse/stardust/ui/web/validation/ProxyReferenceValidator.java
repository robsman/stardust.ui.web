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
package org.eclipse.stardust.ui.web.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.IConnectionManager;
import org.eclipse.stardust.model.xpdl.xpdl2.Extensible;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.modeling.repository.common.Connection;
import org.eclipse.stardust.modeling.validation.IModelElementValidator;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.stardust.modeling.validation.ValidationException;

public class ProxyReferenceValidator implements IModelElementValidator
{
   public Issue[] validate(IModelElement element) throws ValidationException
   {
      List<Issue> issues = new ArrayList<Issue>();
      ModelType model = ModelUtils.findContainingModel(element);
      if (this.isExternalReference(element))
      {
         ModelType referencedModel = null;
         URI proxyUri = ((InternalEObject) element).eProxyURI();
         referencedModel = getModelByProxyURI(model, proxyUri);
         // Todo: IMO for doing this in a clean and consistent way, we need to refactor
         // connection handling etc.
         String elementID = proxyUri.lastSegment();
         if (element instanceof DataType)
         {
            DataType refData = findData(referencedModel, elementID);
            if (refData == null)
            {
               issues.add(new Issue(Issue.WARNING, element, "Referenced data "
                     + proxyUri.lastSegment() + " not found.",
                     StructuredDataConstants.TYPE_DECLARATION_ATT));
            }
         }

      }
      return issues.toArray(new Issue[issues.size()]);
   }

   private DataType findData(ModelType referencedModel, String elementID)
   {
      for (DataType data : referencedModel.getData())
      {
         if (data.getId().equals(elementID))
         {
            return data;
         }
      }
      return null;
   }

   private ModelType getModelByProxyURI(ModelType model, URI proxyUri)
   {
      ModelType referencedModel = null;
      if (model != null && model.getConnectionManager() != null)
      {
         EObject connectionObject = model.getConnectionManager().find(
               proxyUri.scheme() + "://" + proxyUri.authority() + "/");
         if (connectionObject != null)
         {
            referencedModel = (ModelType) Reflect.getFieldValue(connectionObject,
                  "eObject");
         }
      }
      return referencedModel;
   }

   public boolean isExternalReference(EObject modelElement)
   {
      if (modelElement != null)
      {
         if (modelElement.eIsProxy())
         {
            return true;
         }
         if (modelElement instanceof DataType)
         {
            DataType dataType = (DataType) modelElement;
            if (dataType.eIsProxy())
            {
               return true;
            }

            if ((dataType.getType() != null)
                  && (dataType.getType().getId().equalsIgnoreCase(PredefinedConstants.DOCUMENT_DATA)))
            {
               return false;
            }
            if ((dataType.getType() != null)
                  && (dataType.getType().getId().equalsIgnoreCase(PredefinedConstants.STRUCTURED_DATA)))
            {
               return false;
            }
         }
         if (modelElement instanceof IExtensibleElement)
         {
            if (AttributeUtil.getAttributeValue((IExtensibleElement) modelElement,
                  IConnectionManager.URI_ATTRIBUTE_NAME) != null)
            {
               String uri = AttributeUtil.getAttributeValue(
                     (IExtensibleElement) modelElement,
                     IConnectionManager.URI_ATTRIBUTE_NAME);
               ModelType model = ModelUtils.findContainingModel(modelElement);
               if (model == null)
               {
                  return false;
               }
               Connection connection = (Connection) model.getConnectionManager()
                     .findConnection(uri);
               if (connection != null)
               {
                  String importString = connection.getAttribute("importByReference"); //$NON-NLS-1$
                  if (importString != null && importString.equalsIgnoreCase("false")) //$NON-NLS-1$
                  {
                     return false;
                  }
               }
               return true;
            }
         }
         if (modelElement instanceof Extensible)
         {
            if (ExtendedAttributeUtil.getAttributeValue((Extensible) modelElement,
                  IConnectionManager.URI_ATTRIBUTE_NAME) != null)
            {
               String uri = ExtendedAttributeUtil.getAttributeValue(
                     (Extensible) modelElement, IConnectionManager.URI_ATTRIBUTE_NAME);
               ModelType model = ModelUtils.findContainingModel(modelElement);
               if (model == null)
               {
                  return false;
               }
               Connection connection = (Connection) model.getConnectionManager()
                     .findConnection(uri);
               if (connection != null)
               {
                  String importString = connection.getAttribute("importByReference"); //$NON-NLS-1$
                  if (importString != null && importString.equalsIgnoreCase("false")) //$NON-NLS-1$
                  {
                     return false;
                  }
               }
               return true;
            }
         }
      }
      return false;
   }

}