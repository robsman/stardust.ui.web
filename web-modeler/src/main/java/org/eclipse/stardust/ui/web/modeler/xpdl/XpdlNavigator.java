package org.eclipse.stardust.ui.web.modeler.xpdl;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.model.xpdl.carnot.IGraphicalObject;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.spi.ModelFormat;
import org.eclipse.stardust.ui.web.modeler.spi.ModelNavigator;

@Service
@ModelFormat(ModelFormat.XPDL)
@Scope("singleton")
public class XpdlNavigator implements ModelNavigator<ModelType>
{
   @Override
   public EObject findProcessFromDiagramElement(EObject diagramElement)
   {
      assert (diagramElement instanceof IGraphicalObject);

      return ModelUtils.findContainingProcess(diagramElement);
   }

   @Override
   public EObject findElementByUuid(ModelType model, String uuid)
   {
      throw new UnsupportedOperationException("Not yet implemented.");
   }

   @Override
   public EObject findElementByOid(ModelType model, long oid)
   {
      throw new UnsupportedOperationException("Not yet implemented.");
   }
}
