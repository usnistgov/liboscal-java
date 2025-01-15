/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib;

import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.DefaultBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.SimpleModuleLoaderStrategy;
import gov.nist.secauto.oscal.lib.model.AssessmentPlan;
import gov.nist.secauto.oscal.lib.model.AssessmentResults;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.ComponentDefinition;
import gov.nist.secauto.oscal.lib.model.OscalCompleteModule;
import gov.nist.secauto.oscal.lib.model.PlanOfActionAndMilestones;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.SystemSecurityPlan;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class OscalBindingContext
    extends DefaultBindingContext {

  @NonNull
  public static final StaticContext OSCAL_STATIC_METAPATH_CONTEXT = StaticContext.builder()
      .defaultModelNamespace(OscalModelConstants.NS_OSCAL)
      .build();
  private static final Lazy<OscalBindingContext> SINGLETON = Lazy.lazy(OscalBindingContext::new);

  @NonNull
  public static OscalBindingContext instance() {
    return ObjectUtils.notNull(SINGLETON.get());
  }

  /**
   * Get a new builder that can produce a new, configured OSCAL-flavored binding
   * context.
   *
   * @return the builder
   * @since 2.0.0
   */
  public static IBindingContext.BindingContextBuilder builder() {
    return new IBindingContext.BindingContextBuilder(OscalBindingContext::new);
  }

  /**
   * Get a new OSCAL-flavored {@link IBindingContext} instance, which can be used
   * to load information that binds a model to a set of Java classes.
   *
   * @return a new binding context
   * @since 2.0.0
   */
  @NonNull
  public static OscalBindingContext newInstance() {
    return new OscalBindingContext();
  }

  /**
   * Get a new OSCAL-flavored {@link IBindingContext} instance, which can be used
   * to load information that binds a model to a set of Java classes.
   *
   * @param strategy
   *          the loader strategy to use when loading Metaschema modules
   * @return a new binding context
   * @since 2.0.0
   */
  @NonNull
  public static OscalBindingContext newInstance(@NonNull IBindingContext.IModuleLoaderStrategy strategy) {
    return new OscalBindingContext(strategy);
  }

  /**
   * Construct a new OSCAL-flavored binding context.
   */
  protected OscalBindingContext() {
    this(new SimpleModuleLoaderStrategy());
  }

  /**
   * Construct a new OSCAL-flavored binding context.
   *
   * @param strategy
   *          the behavior class to use for loading Metaschema modules
   * @since 2.0.0
   */
  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // false positive
  public OscalBindingContext(@NonNull IBindingContext.IModuleLoaderStrategy strategy) {
    super(strategy);
    registerModule(OscalCompleteModule.class);
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
}
