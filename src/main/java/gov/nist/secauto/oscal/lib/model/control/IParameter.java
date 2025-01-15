/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control;

import gov.nist.secauto.oscal.lib.model.ParameterSelection;
import gov.nist.secauto.oscal.lib.model.Property;

import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IParameter {
  List<Property> getProps();

  ParameterSelection getSelect();

  @NonNull
  Stream<String> getParameterReferences();
}
