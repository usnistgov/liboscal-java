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

import gov.nist.secauto.metaschema.core.model.xml.IModulePostProcessor;
import gov.nist.secauto.metaschema.databind.DefaultBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingMatcher;
import gov.nist.secauto.oscal.lib.model.AssessmentPlan;
import gov.nist.secauto.oscal.lib.model.AssessmentResults;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.ComponentDefinition;
import gov.nist.secauto.oscal.lib.model.PlanOfActionAndMilestones;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.SystemSecurityPlan;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class OscalBindingContext
    extends DefaultBindingContext {
  @NonNull
  public static final String NS_OSCAL = "http://csrc.nist.gov/ns/oscal/1.0";
  @NonNull
  private static final OscalBindingContext SINGLETON = new OscalBindingContext();

  @NonNull
  public static OscalBindingContext instance() {
    return SINGLETON;
  }

  /**
   * Construct a new OSCAL-flavored binding context with custom constraints.
   *
   * @param modulePostProcessors
   *          a list of module post processors to call after loading a module
   */
  public OscalBindingContext(@NonNull List<IModulePostProcessor> modulePostProcessors) {
    super(modulePostProcessors);
    registerBindingMatcher(new Matcher());
  }

  /**
   * Construct a new OSCAL-flavored binding context.
   */
  protected OscalBindingContext() {
    registerBindingMatcher(new Matcher());
  }

  @NonNull
  public Catalog loadCatalog(@NonNull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(Catalog.class, url);
  }

  @NonNull
  public Catalog loadCatalog(@NonNull Path path) throws IOException {
    return newBoundLoader().load(Catalog.class, path);
  }

  @NonNull
  public Catalog loadCatalog(@NonNull File file) throws IOException {
    return newBoundLoader().load(Catalog.class, file);
  }

  @NonNull
  public Profile loadProfile(@NonNull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(Profile.class, url);
  }

  @NonNull
  public Profile loadProfile(@NonNull Path path) throws IOException {
    return newBoundLoader().load(Profile.class, path);
  }

  @NonNull
  public Profile loadProfile(@NonNull File file) throws IOException {
    return newBoundLoader().load(Profile.class, file);
  }

  @NonNull
  public SystemSecurityPlan loadSystemSecurityPlan(@NonNull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(SystemSecurityPlan.class, url);
  }

  @NonNull
  public SystemSecurityPlan loadSystemSecurityPlan(@NonNull Path path) throws IOException {
    return newBoundLoader().load(SystemSecurityPlan.class, path);
  }

  @NonNull
  public SystemSecurityPlan loadSystemSecurityPlan(@NonNull File file) throws IOException {
    return newBoundLoader().load(SystemSecurityPlan.class, file);
  }

  @NonNull
  public ComponentDefinition loadComponentDefinition(@NonNull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(ComponentDefinition.class, url);
  }

  @NonNull
  public ComponentDefinition loadComponentDefinition(@NonNull Path path) throws IOException {
    return newBoundLoader().load(ComponentDefinition.class, path);
  }

  @NonNull
  public ComponentDefinition loadComponentDefinition(@NonNull File file) throws IOException {
    return newBoundLoader().load(ComponentDefinition.class, file);
  }

  @NonNull
  public AssessmentPlan loadAssessmentPlan(@NonNull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(AssessmentPlan.class, url);
  }

  @NonNull
  public AssessmentPlan loadAssessmentPlan(@NonNull Path path) throws IOException {
    return newBoundLoader().load(AssessmentPlan.class, path);
  }

  @NonNull
  public AssessmentPlan loadAssessmentPlan(@NonNull File file) throws IOException {
    return newBoundLoader().load(AssessmentPlan.class, file);
  }

  @NonNull
  public AssessmentResults loadAssessmentResults(@NonNull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(AssessmentResults.class, url);
  }

  @NonNull
  public AssessmentResults loadAssessmentResults(@NonNull Path path) throws IOException {
    return newBoundLoader().load(AssessmentResults.class, path);
  }

  @NonNull
  public AssessmentResults loadAssessmentResults(@NonNull File file) throws IOException {
    return newBoundLoader().load(AssessmentResults.class, file);
  }

  @NonNull
  public PlanOfActionAndMilestones loadPlanOfActionAndMilestones(@NonNull URL url)
      throws IOException, URISyntaxException {
    return newBoundLoader().load(PlanOfActionAndMilestones.class, url);
  }

  @NonNull
  public PlanOfActionAndMilestones loadPlanOfActionAndMilestones(@NonNull Path path) throws IOException {
    return newBoundLoader().load(PlanOfActionAndMilestones.class, path);
  }

  @NonNull
  public PlanOfActionAndMilestones loadPlanOfActionAndMilestones(@NonNull File file) throws IOException {
    return newBoundLoader().load(PlanOfActionAndMilestones.class, file);
  }

  private static final class Matcher implements IBindingMatcher {
    @Override
    public Class<?> getBoundClassForXmlQName(QName startElementQName) {
      Class<?> clazz = null;
      if (NS_OSCAL.equals(startElementQName.getNamespaceURI())) {
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
