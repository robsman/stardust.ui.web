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

package org.eclipse.stardust.ui.web.modeler.marshaling;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;

import com.google.gson.JsonObject;

/**
 * 
 * @author Marc.Gille
 *
 */
public class ModelElementUnmarshaller {
	private static ModelElementUnmarshaller instance;
	
	private Map<Class, String[]> symbolPropertiesMap;
	private Map<Class, String[]> modelElementPropertiesMap;
	private Map<Class, String[]> modelElementReferencePropertiesMap;

	/**
	 * 
	 * @return
	 */
	public static synchronized ModelElementUnmarshaller getInstance()
	{
		if (instance == null)
		{
			instance = new ModelElementUnmarshaller();
		}
		
		return instance;
	}

	/**
	 * 
	 */
	public ModelElementUnmarshaller() {
		super();

		symbolPropertiesMap = new HashMap<Class, String[]>();
		modelElementPropertiesMap = new HashMap<Class, String[]>();
		modelElementReferencePropertiesMap = new HashMap<Class, String[]>();

		symbolPropertiesMap.put(ActivitySymbolType.class, new String[]{ "x", "y" });
		modelElementPropertiesMap.put(ActivitySymbolType.class, new String[]{ "name", "description" });
		modelElementReferencePropertiesMap.put(ActivitySymbolType.class, new String[]{ "application", "subprocess" });
	}

	/**
	 * 
	 * @param element
	 * @param json
	 */
	public void populateFromJson(IModelElement element,
			JsonObject json) {
		IModelElement symbol = null;
		IModelElement modelElement = null;
		String[] symbolProperties = null;
		String[] modelElementProperties = null;
		JsonObject symbolJson = null;
		JsonObject modelElementJson = null;

		if (modelElement instanceof ProcessDefinitionType) {
			symbol = null;
			modelElement = element;
			symbolJson = null;
			modelElementJson = json;
			modelElementProperties = modelElementPropertiesMap.get(ProcessDefinitionType.class);
			symbolProperties = null;
		}
		else if (element instanceof ActivitySymbolType) {
			symbol = element;
			modelElement = ((ActivitySymbolType)element).getActivity();
			symbolJson = json;
			modelElementJson = json.getAsJsonObject("modelElement");
			symbolProperties = symbolPropertiesMap.get(ActivitySymbolType.class);
			modelElementProperties = modelElementPropertiesMap.get(ActivitySymbolType.class);
		}

		if (symbol != null)
		{
			for (String property : symbolProperties)
			{
				mapProperty(symbol, symbolJson, property);
			}			
		}

		if (modelElement != null)
		{
			for (String property : modelElementProperties)
			{
				mapProperty(modelElement, modelElementJson, property);
			}
		} 
	}

	/**
	 * 
	 * @param targetElement
	 * @param request
	 * @param property
	 */
	private void mapProperty(IModelElement targetElement, JsonObject request,
			String property) {
		if (request.has(property)) {
			System.out.println("Setting property " + property + " of value "
					+ request.get(property) + " on object " + targetElement);

			try {
				// TODO Boolean
				Method getter = targetElement.getClass().getMethod(
						"get" + ("" + property.charAt(0)).toUpperCase()
								+ property.substring(1), new Class[] {});
				Method setter = targetElement.getClass()
						.getMethod(
								"set" + ("" + property.charAt(0)).toUpperCase()
										+ property.substring(1),
								getter.getReturnType());

				if (String.class.isAssignableFrom(getter.getReturnType())) {
					if (request.get(property) != null) {
						System.out.println("Invoking " + setter.getName()
								+ " with property value "
								+ request.get(property).getAsString());
						setter.invoke(targetElement, request.get(property)
								.getAsString());
					} else {
						System.out.println("Invoking " + setter.getName()
								+ " with null");
						setter.invoke(targetElement, new Object[]{null});
					}
				}

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("No value for property " + property);
		}
	}

}
