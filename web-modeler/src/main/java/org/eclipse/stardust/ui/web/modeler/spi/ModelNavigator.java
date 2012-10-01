package org.eclipse.stardust.ui.web.modeler.spi;

import org.eclipse.emf.ecore.EObject;

public interface ModelNavigator<M extends EObject>
{
   EObject findProcessFromDiagramElement(EObject diagramElement);

   EObject findElementByUuid(M model, String uuid);

   EObject findElementByOid(M model, long oid);
}
