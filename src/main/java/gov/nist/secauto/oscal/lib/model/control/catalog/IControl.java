/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control.catalog;

import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;

import java.util.List;

public interface IControl extends IControlContainer {

  String getId();

  List<ControlPart> getParts();

  Control getParentControl();

  void setParentControl(Control parent);
}
