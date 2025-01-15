/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control.catalog;

import gov.nist.secauto.oscal.lib.model.ControlPart;

import java.util.List;

public interface ICatalogGroup extends IGroupContainer {
  List<ControlPart> getParts();
}
