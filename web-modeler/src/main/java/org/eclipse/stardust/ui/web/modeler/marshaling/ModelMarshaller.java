package org.eclipse.stardust.ui.web.modeler.marshaling;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface ModelMarshaller
{
   JsonElement toJson(EObject element);

   JsonObject toModelJson(EObject model);

   JsonObject  toProcessDiagramJson(EObject model, String processId);
}
