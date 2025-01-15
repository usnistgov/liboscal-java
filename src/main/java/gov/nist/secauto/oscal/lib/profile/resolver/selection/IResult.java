/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Parameter;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IResult {

  void promoteParameter(@NonNull Parameter param);

  void promoteControl(@NonNull Control control);

  void applyTo(@NonNull Catalog parent);

  void applyTo(@NonNull CatalogGroup parent);

  void applyTo(@NonNull Control parent);
}
