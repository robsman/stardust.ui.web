package org.eclipse.stardust.ui.web.modeler.marshaling;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

public interface ModelUnmarshaller
{

   void populateFromJson(EObject modelElement, JsonObject jto);

}
