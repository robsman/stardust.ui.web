package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.xpdl2.SchemaTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationsType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.eclipse.stardust.ui.web.modeler.service.XsdSchemaUtils;

@Component
public class StructuredTypeChangeTracker implements ChangePostprocessor
{
   private Modification modification;

   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      modification = change;
      for (EObject candidate : change.getModifiedElements())
      {
         if (candidate instanceof TypeDeclarationType
               && (((TypeDeclarationType) candidate).getDataType() instanceof SchemaTypeType))
         {
            updateDerivedTypes((TypeDeclarationType) candidate);
         }
         if (candidate instanceof ModelType)
         {
            if ( !change.getRemovedElements().isEmpty())
            {
               for (EObject removedElement : change.getRemovedElements())
               {
                  if (removedElement instanceof TypeDeclarationType)
                  {
                     TypeDeclarationType decl = (TypeDeclarationType) removedElement;
                     removeReferingAccessPoints(decl);
                  }
               }
            }
         }
      }
   }

   private void updateDerivedTypes(TypeDeclarationType typeDeclaration)
   {
      EObject parent = typeDeclaration.eContainer();
      if (parent instanceof TypeDeclarationsType && typeDeclaration.getSchema() != null)
      {
         XSDTypeDefinition typeDef = getTypeDefinition(typeDeclaration);
         if (typeDef != null)
         {
            for (TypeDeclarationType decl : ((TypeDeclarationsType) parent).getTypeDeclaration())
            {
               if (decl.getDataType() instanceof SchemaTypeType)
               {
                  XSDTypeDefinition type = getTypeDefinition(decl);
                  if (type != null && typeDef.equals(type.getBaseType()))
                  {
                     modification.markAlsoModified(decl);
                  }
               }
            }
         }
      }
   }

   private XSDTypeDefinition getTypeDefinition(TypeDeclarationType typeDeclaration)
   {
      XSDSchema schema = typeDeclaration.getSchema();
      if (schema == null)
      {
         return null;
      }
      XSDNamedComponent component = XsdSchemaUtils.findNamedComponent(schema, typeDeclaration.getId());
      return component instanceof XSDTypeDefinition ? (XSDTypeDefinition) component
            : ((XSDElementDeclaration) component).getTypeDefinition();
   }

   private void removeReferingAccessPoints(TypeDeclarationType decl)
   {
      List<AccessPointType> removeList = new ArrayList<AccessPointType>();
      ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) decl.eContainer();
      ModelType model = (ModelType) changeDescription.getOldContainer(decl).eContainer();
      if (model != null)
      {
         for (ApplicationType application : model.getApplication())
         {
            if (application.getType() != null
                  && application.getType()
                        .getId()
                        .equals(
                              ModelerConstants.MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID))
            {
               for (AccessPointType accessPoint : application.getAccessPoint())
               {
                  if (accessPoint.getType()
                        .getId()
                        .equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
                  {
                     AttributeType dataType = AttributeUtil.getAttribute(accessPoint,
                           "carnot:engine:dataType");
                     if (dataType == null)
                     {
                        removeList.add(accessPoint);
                     }
                  }
               }
               application.getAccessPoint().removeAll(removeList);
            }
         }
      }
   }
}
