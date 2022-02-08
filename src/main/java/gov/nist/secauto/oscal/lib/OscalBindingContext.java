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

package gov.nist.secauto.oscal.lib;

import gov.nist.secauto.metaschema.binding.DefaultBindingContext;
import gov.nist.secauto.metaschema.binding.IBindingMatcher;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.oscal.lib.model.AssessmentPlan;
import gov.nist.secauto.oscal.lib.model.AssessmentResults;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.ComponentDefinition;
import gov.nist.secauto.oscal.lib.model.PlanOfActionAndMilestones;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.SystemSecurityPlan;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.namespace.QName;

public class OscalBindingContext
    extends DefaultBindingContext {
  private static final OscalBindingContext INSTANCE = new OscalBindingContext();

  @NotNull
  public static OscalBindingContext instance() {
    return INSTANCE;
  }

  /**
   * Construct a new OSCAL-flavored binding context.
   */
  protected OscalBindingContext() {
    registerBindingMatcher(new Matcher());
  }

  @NotNull
  public Catalog loadCatalog(@NotNull File file) throws BindingException, FileNotFoundException, IOException {
    return newBoundLoader().load(Catalog.class, file);
  }

  @NotNull
  public Profile loadProfile(@NotNull File file) throws BindingException, FileNotFoundException, IOException {
    return newBoundLoader().load(Profile.class, file);
  }

  @NotNull
  public SystemSecurityPlan loadSystemSecurityPlan(@NotNull File file)
      throws BindingException, FileNotFoundException, IOException {
    return newBoundLoader().load(SystemSecurityPlan.class, file);
  }

  @NotNull
  public ComponentDefinition loadComponentDefinition(@NotNull File file)
      throws BindingException, FileNotFoundException, IOException {
    return newBoundLoader().load(ComponentDefinition.class, file);
  }

  @NotNull
  public AssessmentPlan loadAssessmentPlan(@NotNull File file)
      throws BindingException, FileNotFoundException, IOException {
    return newBoundLoader().load(AssessmentPlan.class, file);
  }

  @NotNull
  public AssessmentResults loadAssessmentResults(@NotNull File file)
      throws BindingException, FileNotFoundException, IOException {
    return newBoundLoader().load(AssessmentResults.class, file);
  }

  @NotNull
  public PlanOfActionAndMilestones loadPlanOfActionAndMilestones(@NotNull File file)
      throws BindingException, FileNotFoundException, IOException {
    return newBoundLoader().load(PlanOfActionAndMilestones.class, file);
  }

  private class Matcher implements IBindingMatcher {

    @Override
    public Class<?> getBoundClassForXmlQName(QName startElementQName) {
      Class<?> clazz = null;
      if ("http://csrc.nist.gov/ns/oscal/1.0".equals(startElementQName.getNamespaceURI())) {
        switch (startElementQName.getLocalPart()) {
        case "catalog":
          clazz = Catalog.class;
          break;
        case "profile":
          clazz = Profile.class;
          break;
        case "system-security-plan":
          clazz = SystemSecurityPlan.class;
          break;
        case "component-definition":
          clazz = ComponentDefinition.class;
          break;
        case "assessment-plan":
          clazz = AssessmentPlan.class;
          break;
        case "assessment-results":
          clazz = AssessmentResults.class;
          break;
        case "plan-of-action-and-milestones":
          clazz = PlanOfActionAndMilestones.class;
          break;
        default:
          throw new UnsupportedOperationException("Unrecognized element name: " + startElementQName.toString());
        }
      }
      return clazz;
    }

    @Override
    public Class<?> getBoundClassForJsonName(String name) {
      Class<?> retval;
      switch (name) {
      case "catalog":
        retval = Catalog.class;
        break;
      case "profile":
        retval = Profile.class;
        break;
      case "system-security-plan":
        retval = SystemSecurityPlan.class;
        break;
      case "component-definition":
        retval = ComponentDefinition.class;
        break;
      case "assessment-plan":
        retval = AssessmentPlan.class;
        break;
      case "assessment-results":
        retval = AssessmentResults.class;
        break;
      case "plan-of-action-and-milestones":
        retval = PlanOfActionAndMilestones.class;
        break;
      default:
        throw new UnsupportedOperationException("Unrecognized field name: " + name);
      }
      return retval;
    }

  }
}
