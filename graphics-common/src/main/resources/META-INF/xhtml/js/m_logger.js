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
/**
 * @author Omkar.Patil
 */
define(function(){
	return {
		log: function(){ //deal upto 3 parameters, ignore rest
			try {
				switch (arguments.length) {
					case 1:
						console.log(arguments[0]);
						break;
					case 2:
						console.log(arguments[0], arguments[1]);
						break;
					case 3:
						console.log(arguments[0], arguments[1], arguments[2]);
						break;
				}
			}catch (e) {/* do nothing */}
		}		
	}
});