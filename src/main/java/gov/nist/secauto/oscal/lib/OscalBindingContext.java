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
import gov.nist.secauto.metaschema.model.common.constraint.IConstraintSet;
import gov.nist.secauto.oscal.lib.model.AssessmentPlan;
import gov.nist.secauto.oscal.lib.model.AssessmentResults;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.ComponentDefinition;
import gov.nist.secauto.oscal.lib.model.PlanOfActionAndMilestones;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.SystemSecurityPlan;

import javax.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

import javax.xml.namespace.QName;

public class OscalBindingContext
    extends DefaultBindingContext {
  @Nonnull
  private static final OscalBindingContext INSTANCE = new OscalBindingContext();

  @Nonnull
  public static OscalBindingContext instance() {
    return INSTANCE;
  }

  /**
   * Construct a new OSCAL-flavored binding context with custom constraints.
   * 
   * @param constraintSets
   *          a set of additional constraints to apply
   */
  public OscalBindingContext(@Nonnull Set<@Nonnull IConstraintSet> constraintSets) {
    super(constraintSets);
    registerBindingMatcher(new Matcher());
  }

  /**
   * Construct a new OSCAL-flavored binding context.
   */
  protected OscalBindingContext() {
    registerBindingMatcher(new Matcher());
  }

  @Nonnull
  public Catalog loadCatalog(@Nonnull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(Catalog.class, url);
  }

  @Nonnull
  public Catalog loadCatalog(@Nonnull Path path) throws IOException {
    return newBoundLoader().load(Catalog.class, path);
  }

  @SuppressWarnings("null")
  @Nonnull
  public Catalog loadCatalog(@Nonnull File file) throws IOException {
    return loadCatalog(file.toPath());
  }

  @Nonnull
  public Profile loadProfile(@Nonnull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(Profile.class, url);
  }

  @Nonnull
  public Profile loadProfile(@Nonnull Path path) throws IOException {
    return newBoundLoader().load(Profile.class, path);
  }

  @Nonnull
  public Profile loadProfile(@Nonnull File file) throws IOException {
    return newBoundLoader().load(Profile.class, file);
  }

  @Nonnull
  public SystemSecurityPlan loadSystemSecurityPlan(@Nonnull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(SystemSecurityPlan.class, url);
  }

  @Nonnull
  public SystemSecurityPlan loadSystemSecurityPlan(@Nonnull Path path) throws IOException {
    return newBoundLoader().load(SystemSecurityPlan.class, path);
  }

  @Nonnull
  public SystemSecurityPlan loadSystemSecurityPlan(@Nonnull File file) throws IOException {
    return newBoundLoader().load(SystemSecurityPlan.class, file);
  }

  @Nonnull
  public ComponentDefinition loadComponentDefinition(@Nonnull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(ComponentDefinition.class, url);
  }

  @Nonnull
  public ComponentDefinition loadComponentDefinition(@Nonnull Path path) throws IOException {
    return newBoundLoader().load(ComponentDefinition.class, path);
  }

  @Nonnull
  public ComponentDefinition loadComponentDefinition(@Nonnull File file) throws IOException {
    return newBoundLoader().load(ComponentDefinition.class, file);
  }

  @Nonnull
  public AssessmentPlan loadAssessmentPlan(@Nonnull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(AssessmentPlan.class, url);
  }

  @Nonnull
  public AssessmentPlan loadAssessmentPlan(@Nonnull Path path) throws IOException {
    return newBoundLoader().load(AssessmentPlan.class, path);
  }

  @Nonnull
  public AssessmentPlan loadAssessmentPlan(@Nonnull File file) throws IOException {
    return newBoundLoader().load(AssessmentPlan.class, file);
  }

  @Nonnull
  public AssessmentResults loadAssessmentResults(@Nonnull URL url) throws IOException, URISyntaxException {
    return newBoundLoader().load(AssessmentResults.class, url);
  }

  @Nonnull
  public AssessmentResults loadAssessmentResults(@Nonnull Path path) throws IOException {
    return newBoundLoader().load(AssessmentResults.class, path);
  }

  @Nonnull
  public AssessmentResults loadAssessmentResults(@Nonnull File file) throws IOException {
    return newBoundLoader().load(AssessmentResults.class, file);
  }

  @Nonnull
  public PlanOfActionAndMilestones loadPlanOfActionAndMilestones(@Nonnull URL url)
      throws IOException, URISyntaxException {
    return newBoundLoader().load(PlanOfActionAndMilestones.class, url);
  }

  @Nonnull
  public PlanOfActionAndMilestones loadPlanOfActionAndMilestones(@Nonnull Path path) throws IOException {
    return newBoundLoader().load(PlanOfActionAndMilestones.class, path);
  }

  @Nonnull
  public PlanOfActionAndMilestones loadPlanOfActionAndMilestones(@Nonnull File file) throws IOException {
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
