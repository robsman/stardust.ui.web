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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationsType;
//import org.eclipse.stardust.modeling.data.structured.Structured_Messages;
import org.eclipse.stardust.modeling.repository.common.Connection;
import org.eclipse.stardust.modeling.validation.IModelElementValidator;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.stardust.modeling.validation.ValidationException;

import com.infinity.bpm.thirdparty.emf.common.util.URI;

public class StructuredDataValidator implements IModelElementValidator
{
   public Issue[] validate(IModelElement element) throws ValidationException
   {
      List<Issue> issues = new ArrayList<Issue>();
      DataType data = (DataType) element;
      AttributeType attribute = AttributeUtil.getAttribute((IExtensibleElement) element, "carnot:connection:uri"); //$NON-NLS-1$
      if (data.getExternalReference() != null)
      {
         // Validation for external references takes place in the ReferencedModelElementValidator
         return null;
      }

      ModelType model = ModelUtils.findContainingModel(data);
      String typeId = AttributeUtil.getAttributeValue(data, StructuredDataConstants.TYPE_DECLARATION_ATT);
      if (StringUtils.isEmpty(typeId))
      {
         issues.add(new Issue(Issue.ERROR, element, data.getId() + ": No structured type assigned",
               StructuredDataConstants.TYPE_DECLARATION_ATT));
      }
      else
      {
         TypeDeclarationsType declarations = null;
         if(attribute != null)
         {
            String uri = attribute.getValue();
            URI aRealUri = URI.createURI(uri);
            Connection connection = (Connection) model.getConnectionManager()
                  .findConnection(uri);
            if (connection.getAttribute("importByReference") != null //$NON-NLS-1$
                  && !"false".equals(connection.getAttribute("importByReference"))) //$NON-NLS-1$ //$NON-NLS-2$
            {

               EObject o = model.getConnectionManager().find(
                     aRealUri.scheme().toString() + "://" + aRealUri.authority() + "/"); //$NON-NLS-1$ //$NON-NLS-2$
               ModelType referencedModel = (ModelType) Reflect.getFieldValue(o, "eObject"); //$NON-NLS-1$

               declarations = referencedModel.getTypeDeclarations();
            }
            else
            {
               declarations = model.getTypeDeclarations();
            }
         }
         else
         {
            declarations = model.getTypeDeclarations();
         }

         TypeDeclarationType type = null;
         if(declarations != null)
         {
            type = declarations.getTypeDeclaration(typeId);
         }

         if (type == null)
         {
            String message = "Invalid type declaration:" + typeId;
            // TODO: check other types when implemented
            issues.add(new Issue(Issue.WARNING, element, message,
                  StructuredDataConstants.TYPE_DECLARATION_ATT));
         }
      }
      return issues.toArray(new Issue[issues.size()]);
   }
}