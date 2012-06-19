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
package com.infinity.bpm.rt.persistence.jpa.test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Robert.Sauer
 */
@Entity(name = "portal_persistence_jpa_test")
public class TestEntity
{

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long oid;

   @Column
   private String name;

   @Column
   private String value;
}
