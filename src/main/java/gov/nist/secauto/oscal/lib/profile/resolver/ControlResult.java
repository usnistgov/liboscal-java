/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.oscal.lib.profile.resolver;

import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Parameter;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class ControlResult {

  private final List<Parameter> promotedParameters;
  private final List<Control> promotedControls;

  public ControlResult() {
    this.promotedParameters = new LinkedList<>();
    this.promotedControls = new LinkedList<>();
  }

  public List<Parameter> getPromotedParameters() {
    return promotedParameters;
  }

  public List<Control> getPromotedControls() {
    return promotedControls;
  }

  public void promoteParameter(Parameter param) {
    promotedParameters.add(param);
  }

  public void promoteControl(Control control) {
    promotedControls.add(control);
  }

  public void apply(@NotNull Catalog catalog) {
    List<Parameter> promotedParams = getPromotedParameters();
    if (!promotedParams.isEmpty()) {
      List<Parameter> params = catalog.getParams();
      if (params == null) {
        params = new LinkedList<>();
        catalog.setParams(params);
      }

      for (Parameter param : promotedParams) {
        params.add(param);
      }
    }

    List<Control> promotedControls = getPromotedControls();
    if (!promotedControls.isEmpty()) {
      List<Control> controls = catalog.getControls();
      if (controls == null) {
        controls = new LinkedList<>();
        catalog.setControls(controls);
      }

      for (Control childControl : promotedControls) {
        controls.add(childControl);
      }
    }
  }

  public void apply(@NotNull CatalogGroup group) {
    List<Parameter> promotedParams = getPromotedParameters();
    if (!promotedParams.isEmpty()) {
      List<Parameter> params = group.getParams();
      if (params == null) {
        params = new LinkedList<>();
        group.setParams(params);
      }

      for (Parameter param : promotedParams) {
        params.add(param);
      }
    }

    List<Control> promotedControls = getPromotedControls();
    if (!promotedControls.isEmpty()) {
      List<Control> controls = group.getControls();
      if (controls == null) {
        controls = new LinkedList<>();
        group.setControls(controls);
      }

      for (Control childControl : promotedControls) {
        controls.add(childControl);
      }
    }
  }

  public void apply(@NotNull Control control) {
    List<Parameter> promotedParams = getPromotedParameters();
    if (!promotedParams.isEmpty()) {
      List<Parameter> params = control.getParams();
      if (params == null) {
        params = new LinkedList<>();
        control.setParams(params);
      }

      for (Parameter param : promotedParams) {
        params.add(param);
      }
    }

    List<Control> promotedControls = getPromotedControls();
    if (!promotedControls.isEmpty()) {
      List<Control> controls = control.getControls();
      if (controls == null) {
        controls = new LinkedList<>();
        control.setControls(controls);
      }

      for (Control childControl : promotedControls) {
        controls.add(childControl);
      }
    }
  }

  public void append(ControlResult that) {
    promotedParameters.addAll(that.getPromotedParameters());
    promotedControls.addAll(that.getPromotedControls());
  }
}
