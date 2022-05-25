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

import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.model.common.datatype.markup.IMarkupText;
import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.oscal.lib.model.BackMatter;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Location;
import gov.nist.secauto.oscal.lib.model.Metadata;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.ParameterConstraint;
import gov.nist.secauto.oscal.lib.model.ParameterGuideline;
import gov.nist.secauto.oscal.lib.model.Party;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.model.ResponsibleParty;
import gov.nist.secauto.oscal.lib.model.Role;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolver.ResolutionData;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.AnchorReferencePolicy;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.IIdentifierParser;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.IReferencePolicy;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.InsertReferencePolicy;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.LinkReferencePolicy;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.PropertyReferencePolicy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

public class ReferenceCountingVisitor {
  private static final Logger LOGGER = LogManager.getLogger(ImportCatalogVisitor.class);

  @NotNull
  private static final IReferencePolicy<Property> PROPERTY_POLICY_IGNORE = IReferencePolicy.ignore();
  @NotNull
  private static final IReferencePolicy<Link> LINK_POLICY_IGNORE = IReferencePolicy.ignore();

  @NotNull
  private static final Map<QName, IReferencePolicy<Property>> PROPERTY_POLICIES;
  @NotNull
  private static final Map<String, IReferencePolicy<Link>> LINK_POLICIES;
  @NotNull
  private static final InsertReferencePolicy INSERT_POLICY = new InsertReferencePolicy();
  @NotNull
  private static final AnchorReferencePolicy ANCHOR_POLICY = new AnchorReferencePolicy();

  static {
    PROPERTY_POLICIES = new HashMap<>();
    PROPERTY_POLICIES.put(Property.qname(Property.OSCAL_NAMESPACE, "resolution-tool"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(Property.qname(Property.OSCAL_NAMESPACE, "label"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(Property.qname(Property.OSCAL_NAMESPACE, "sort-id"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(Property.qname(Property.OSCAL_NAMESPACE, "alt-label"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(Property.qname(Property.OSCAL_NAMESPACE, "alt-identifier"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(Property.qname(Property.RMF_NAMESPACE, "method"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(Property.qname(Property.RMF_NAMESPACE, "aggregates"),
        PropertyReferencePolicy.create(IIdentifierParser.IDENTITY_PARSER, ItemType.PARAMETER));

    LINK_POLICIES = new HashMap<>();
    LINK_POLICIES.put("source-profile", LINK_POLICY_IGNORE);
    LINK_POLICIES.put("citation", LinkReferencePolicy.create(ItemType.RESOURCE));
    LINK_POLICIES.put("reference", LinkReferencePolicy.create(ItemType.RESOURCE));
    LINK_POLICIES.put("related", LinkReferencePolicy.create(ItemType.CONTROL));
    LINK_POLICIES.put("required", LinkReferencePolicy.create(ItemType.CONTROL));
  }

  @NotNull
  private final Index index;
  @NotNull
  private final URI source;
  @NotNull
  private final Map<@NotNull String, Parameter> inScopeParameters;

  public ReferenceCountingVisitor(@NotNull ResolutionData data) {
    this.index = data.getIndex();
    this.source = data.getProfileUri();
    this.inScopeParameters = new LinkedHashMap<>();
  }

  @NotNull
  public Index getIndex() {
    return index;
  }

  @NotNull
  public URI getSource() {
    return source;
  }

  private void registerInScopeParameters(@NotNull List<Parameter> parameters) {
    parameters.stream().forEachOrdered(param -> {
      String id = param.getId();
      if (id != null && param != null) {
        inScopeParameters.put(id, param);
      }
    });
  }

  private void unregisterInScopeParameters(@NotNull List<Parameter> parameters) {
    parameters.stream().forEachOrdered(param -> {
      String id = param.getId();
      if (id != null) {
        inScopeParameters.remove(id);
      }
    });
  }

  public void visitProfile(@NotNull Profile profile) {
    // process children
    Metadata metadata = profile.getMetadata();
    if (metadata != null) {
      visitMetadata(metadata);
    }

    BackMatter backMatter = profile.getBackMatter();
    if (backMatter != null) {
      for (BackMatter.Resource resource : CollectionUtil.listOrEmpty(backMatter.getResources())) {
        visitResource(resource);
      }
    }
  }

  @SuppressWarnings("null")
  public void visitCatalog(@NotNull Catalog catalog) {

    // track in-scope parameters
    List<Parameter> parameters = CollectionUtil.listOrEmpty(catalog.getParams());
    registerInScopeParameters(parameters);

    // process children
    Metadata metadata = catalog.getMetadata();
    if (metadata != null) {
      visitMetadata(metadata);
    }

    // BackMatter backMatter = catalog.getBackMatter();
    // if (backMatter != null) {
    // for (BackMatter.Resource resource : CollectionUtil.listOrEmpty(backMatter.getResources())) {
    // visitResource(resource);
    // }
    // }

    for (Iterator<CatalogGroup> iter = CollectionUtil.listOrEmpty(catalog.getGroups()).iterator(); iter.hasNext();) {
      CatalogGroup child = iter.next();
      visitGroup(child);
    }

    for (Iterator<Control> iter = CollectionUtil.listOrEmpty(catalog.getControls()).iterator(); iter.hasNext();) {
      Control child = iter.next();
      visitControl(child);
    }

    for (Iterator<Parameter> parameterIter = CollectionUtil.listOrEmpty(catalog.getParams()).iterator(); parameterIter
        .hasNext();) {

      Parameter childParam = parameterIter.next();

      visitParameter(childParam);
    }

    // remove unused parameters
    for (Iterator<Parameter> parameterIter
        = CollectionUtil.listOrEmpty(catalog.getParams()).iterator(); parameterIter.hasNext();) {
      Parameter childParam = parameterIter.next();

      EntityItem item = getIndex().getEntity(ItemType.PARAMETER, childParam.getId());
      if (item == null || item.getReferenceCount() == 0) {
        LOGGER.atTrace().log("Removing parameter '{}'", childParam.getId());
        parameterIter.remove();
      }
    }

    // remove parameters from in-scope parameters
    unregisterInScopeParameters(parameters);
  }

  public void visitMetadata(@NotNull Metadata metadata) {
    visitLinkedNodes(metadata.getTitle());

    for (Role role : CollectionUtil.listOrEmpty(metadata.getRoles())) {
      visitRole(role);
    }

    for (Location location : CollectionUtil.listOrEmpty(metadata.getLocations())) {
      visitLocation(location);
    }

    for (Party party : CollectionUtil.listOrEmpty(metadata.getParties())) {
      visitParty(party);
    }

    visitProperties(metadata.getProps());
    visitLinks(metadata.getLinks());
    visitLinkedNodes(metadata.getRemarks());
    visitResponsibleParties(metadata.getResponsibleParties());
  }

  protected void visitProperties(List<Property> properties) {
    for (Property property : CollectionUtil.listOrEmpty(properties)) {
      handleProperty(property);
      visitLinkedNodes(property.getRemarks());
    }
  }

  protected void visitLinks(List<Link> links) {
    for (Link link : CollectionUtil.listOrEmpty(links)) {
      handleLink(link);
      visitLinkedNodes(link.getText());
    }
  }

  public void visitRole(@NotNull Role role) {
//    if ("always".equals(Property.getValue(role.getProps(), Property.qname(Property.OSCAL_NAMESPACE, "keep"), "never"))) {
//      incrementReferenceCount(ItemType.ROLE, role.getId());
//    }
    
    visitLinkedNodes(role.getTitle());
    visitLinkedNodes(role.getDescription());
    visitProperties(role.getProps());
    visitLinks(role.getLinks());
    visitLinkedNodes(role.getRemarks());
  }

  private void visitLocation(@NotNull Location location) {
    visitLinkedNodes(location.getTitle());
    visitProperties(location.getProps());
    visitLinks(location.getLinks());
    visitLinkedNodes(location.getRemarks());
  }

  protected void visitResponsibleParties(List<ResponsibleParty> responsibleParties) {
    for (ResponsibleParty responsibleParty : CollectionUtil.listOrEmpty(responsibleParties)) {
      visitResponsibleParty(responsibleParty);
    }
  }

  @SuppressWarnings("null")
  protected void incrementReferenceCount(@NotNull ItemType type, @NotNull UUID identifier) {
    incrementReferenceCount(type, identifier.toString());
  }

  protected void incrementReferenceCount(@NotNull ItemType type, @NotNull String identifier) {
    EntityItem item = getIndex().getEntity(type, identifier);
    if (item == null) {
      LOGGER.atError().log("Unknown reference to {} '{}'", type.toString().toLowerCase(), identifier);
    } else {
      item.incrementReferenceCount();
    }
  }

  @SuppressWarnings("null")
  protected void visitParty(@NotNull Party party) {
    visitProperties(party.getProps());
    visitLinks(party.getLinks());

    for (UUID uuid : CollectionUtil.listOrEmpty(party.getLocationUuids())) {
      incrementReferenceCount(EntityItem.ItemType.LOCATION, uuid);
    }
    visitLinkedNodes(party.getRemarks());
  }

  @SuppressWarnings("null")
  protected void visitResponsibleParty(@NotNull ResponsibleParty responsibleParty) {
    for (UUID uuid : CollectionUtil.listOrEmpty(responsibleParty.getPartyUuids())) {
      incrementReferenceCount(EntityItem.ItemType.PARTY, uuid);
    }
    visitProperties(responsibleParty.getProps());
    visitLinks(responsibleParty.getLinks());
    visitLinkedNodes(responsibleParty.getRemarks());
  }

  private void visitResource(@NotNull Resource resource) {
    EntityItem item = getIndex().addResource(resource, getSource());
    if (item != null) {
      String entityType = item.getItemType().toString().toLowerCase();
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("The current {} '{}' in '{}' collides with the existing {} '{}' in '{}'. Using the current one.",
            entityType,
            resource.getUuid(),
            getSource(),
            entityType,
            item.getIdentifier(),
            item.getSource());
      }
    }
  }

  private void visitParameter(@NotNull Parameter parameter) {
    visitProperties(parameter.getProps());
    visitLinks(parameter.getLinks());
    visitLinkedNodes(parameter.getLabel());
    visitLinkedNodes(parameter.getUsage());

    for (ParameterConstraint constraint : CollectionUtil.listOrEmpty(parameter.getConstraints())) {
      visitLinkedNodes(constraint.getDescription());
      for (ParameterConstraint.Test test : CollectionUtil.listOrEmpty(constraint.getTests())) {
        visitLinkedNodes(test.getRemarks());
      }
    }

    for (ParameterGuideline guideline : CollectionUtil.listOrEmpty(parameter.getGuidelines())) {
      visitLinkedNodes(guideline.getProse());
    }
    visitLinkedNodes(parameter.getRemarks());
  }

  public void visitGroup(@NotNull CatalogGroup group) {
    // track in-scope parameters
    List<Parameter> parameters = CollectionUtil.listOrEmpty(group.getParams());
    registerInScopeParameters(parameters);

    // process children
    visitLinkedNodes(group.getTitle());

    for (Iterator<Parameter> parameterIter = CollectionUtil.listOrEmpty(group.getParams()).iterator(); parameterIter
        .hasNext();) {

      @SuppressWarnings("null")
      @NotNull
      Parameter childParam = parameterIter.next();

      visitParameter(childParam);
    }
    visitProperties(group.getProps());
    visitLinks(group.getLinks());
    for (ControlPart part : CollectionUtil.listOrEmpty(group.getParts())) {
      visitPart(part);
    }

    for (Iterator<CatalogGroup> iter = CollectionUtil.listOrEmpty(group.getGroups()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      CatalogGroup child = iter.next();
      visitGroup(child);
    }

    for (Iterator<Control> iter = CollectionUtil.listOrEmpty(group.getControls()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      Control child = iter.next();
      visitControl(child);
    }

    // remove unused parameters
    for (Iterator<Parameter> parameterIter
        = CollectionUtil.listOrEmpty(group.getParams()).iterator(); parameterIter.hasNext();) {
      Parameter childParam = parameterIter.next();

      EntityItem item = getIndex().getEntity(ItemType.PARAMETER, childParam.getId());
      if (item.getReferenceCount() == 0) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.atTrace().log("Removing parameter '{}'", childParam.getId());
        }
        parameterIter.remove();
      }
    }

    unregisterInScopeParameters(parameters);
  }

  private void visitPart(ControlPart part) {
    visitLinkedNodes(part.getTitle());
    visitProperties(part.getProps());
    visitLinkedNodes(part.getProse());
    for (ControlPart child : CollectionUtil.listOrEmpty(part.getParts())) {
      visitPart(child);
    }
    visitLinks(part.getLinks());
  }

  public void visitControl(@NotNull Control control) {
    // track in-scope parameters
    List<Parameter> parameters = new LinkedList<>(CollectionUtil.listOrEmpty(control.getParams()));
    registerInScopeParameters(parameters);

    for (Iterator<Parameter> parameterIter
        = CollectionUtil.listOrEmpty(control.getParams()).iterator(); parameterIter.hasNext();) {
      Parameter childParam = parameterIter.next();

      visitParameter(childParam);
    }
    visitProperties(control.getProps());
    visitLinks(control.getLinks());

    for (ControlPart part : CollectionUtil.listOrEmpty(control.getParts())) {
      visitPart(part);
    }

    for (Iterator<Control> controlIter = CollectionUtil.listOrEmpty(control.getControls()).iterator(); controlIter
        .hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      Control child = controlIter.next();
      visitControl(child);

    }

    // remove unused parameters
    for (Iterator<Parameter> parameterIter
        = CollectionUtil.listOrEmpty(control.getParams()).iterator(); parameterIter.hasNext();) {
      Parameter childParam = parameterIter.next();

      EntityItem item = getIndex().getEntity(ItemType.PARAMETER, childParam.getId());
      if (item == null || item.getReferenceCount() == 0) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.atTrace().log("Removing parameter '{}'", childParam.getId());
        }
        parameterIter.remove();
      }
    }

    unregisterInScopeParameters(parameters);

    // // append in-scope parameters
    // List<Parameter> parameters = listOrEmpty(control.getParams());
    // parameters.stream().forEachOrdered(param -> inScopeParameters.put(param.getId(), param));
    //
    // controlStack.push(control);
    //
    // // determine matching controls
    // Result matchingChildren = listOrEmpty(control.getControls()).stream()
    // .filter(Objects::nonNull)
    // .map(child -> {
    // Result result = visitControl(child, filter);
    // return result;
    // }).reduce((resultA, resultB) -> {
    // return resultA.aggregate(resultB);
    // }).orElse(null);
    //
    // Result retval;
    // if (isMatch) {
    // // assign the resulting children to this control
    // control.setControls(matchingChildren.collect(Collectors.toList()));
    // control.getControls().forEach(child -> child.setParentControl(control));
    // // return this control as the child
    // retval = Stream.of(control);
    // } else {
    // // push the children up since this control is not a match
    // retval = matchingChildren;
    // }
    //
    // controlStack.pop();
    //
    // // remove in-scope parameters
    // parameters.stream().forEachOrdered(param -> inScopeParameters.remove(param.getId()));
    //
    // return retval;
  }

  private void visitLinkedNodes(IMarkupText text) {
    if (text != null) {
      for (Node node : CollectionUtil.toIterable(text.getNodesAsStream().iterator())) {
        if (node instanceof InsertAnchorNode) {
          handleInsert((InsertAnchorNode) node);
        } else if (node instanceof InlineLinkNode) {
          handleAnchor((InlineLinkNode) node);
        }
      }
    }
  }

  private void handleProperty(@NotNull Property property) {
    QName qname = property.getQName();

    IReferencePolicy<Property> policy = PROPERTY_POLICIES.get(qname);

    boolean handled = false;
    if (policy != null) {
      handled = policy.handleReference(property, index);
    }

    if (!handled && LOGGER.isWarnEnabled()) {
      LOGGER.atWarn().log("unsupported property '{}'", property.getQName());
    }
  }

  private void handleLink(Link link) {
    IReferencePolicy<Link> policy = null;
    String rel = link.getRel();
    if (rel != null) {
      policy = LINK_POLICIES.get(rel);
    }

    boolean handled = false;
    if (policy != null) {
      handled = policy.handleReference(link, index);
    }

    if (!handled && LOGGER.isWarnEnabled()) {
      LOGGER.atWarn().log("unsupported link rel '{}'", link.getRel());
    }
  }

  private void handleInsert(@NotNull InsertAnchorNode node) {
    if (!INSERT_POLICY.handleReference(node, index) && LOGGER.isWarnEnabled()) {
      LOGGER.atWarn().log("unsupported insert type '{}'", node.getType().toString());
    }
  }

  private void handleAnchor(@NotNull InlineLinkNode node) {
    if (!ANCHOR_POLICY.handleReference(node, index) && LOGGER.isWarnEnabled()) {
      LOGGER.atWarn().log("unsupported anchor with href '{}'", node.getUrl().toString());
    }
  }
}
