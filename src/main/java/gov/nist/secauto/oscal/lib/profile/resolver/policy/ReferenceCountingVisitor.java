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

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.model.common.datatype.markup.IMarkupText;
import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IMarkupItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Location;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Party;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.model.Role;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.Index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

public class ReferenceCountingVisitor implements IReferenceVisitor {
  private static final Logger LOGGER = LogManager.getLogger(ReferenceCountingVisitor.class);
  @NotNull
  private static final MetapathExpression PART_METAPATH
      = MetapathExpression.compile("part|part//part");
  @NotNull
  private static final MetapathExpression PARAM_MARKUP_METAPATH
      = MetapathExpression
          .compile("label|usage|constraint/(description|tests/remarks)|guideline/prose|select/choice|remarks");
  @NotNull
  private static final MetapathExpression ROLE_MARKUP_METAPATH
      = MetapathExpression.compile("title|description|remarks");
  @NotNull
  private static final MetapathExpression LOCATION_MARKUP_METAPATH
      = MetapathExpression.compile("title|remarks");
  @NotNull
  private static final MetapathExpression PARTY_MARKUP_METAPATH
      = MetapathExpression.compile("title|remarks");
  @NotNull
  private static final MetapathExpression RESOURCE_MARKUP_METAPATH
      = MetapathExpression.compile("title|description|remarks");

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

  public ReferenceCountingVisitor(@NotNull Index index, @NotNull URI source) {
    this.index = index;
    this.source = source;
  }

  @Override
  @NotNull
  public Index getIndex() {
    return index;
  }

  @NotNull
  protected URI getSource() {
    return source;
  }

  //
  // public void visitProfile(@NotNull Profile profile) {
  // // process children
  // Metadata metadata = profile.getMetadata();
  // if (metadata != null) {
  // visitMetadata(metadata);
  // }
  //
  // BackMatter backMatter = profile.getBackMatter();
  // if (backMatter != null) {
  // for (BackMatter.Resource resource : CollectionUtil.listOrEmpty(backMatter.getResources())) {
  // visitResource(resource);
  // }
  // }
  // }

  public void visitCatalog(@NotNull IDocumentNodeItem catalogItem) {

    // process children
    IRootAssemblyNodeItem rootItem = catalogItem.getRootAssemblyNodeItem();

    rootItem.getModelItemsByName("group").forEach(groupItem -> {
      visitGroup(groupItem);
    });

    rootItem.getModelItemsByName("control").forEach(controlItem -> {
      visitControl(controlItem);
    });
    index.getEntitiesByItemType(ItemType.ROLE).forEach(item -> resolveItem(item));
    index.getEntitiesByItemType(ItemType.LOCATION).forEach(item -> resolveItem(item));
    index.getEntitiesByItemType(ItemType.PARTY).forEach(item -> resolveItem(item));
    index.getEntitiesByItemType(ItemType.PARAMETER).forEach(item -> resolveItem(item));
    index.getEntitiesByItemType(ItemType.RESOURCE).forEach(item -> resolveItem(item));
  }

  private void resolveItem(EntityItem item) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Resolving {} identified as '{}'", item.getItemType().name(), item.getIdentifier());
    }
    item.markResolved();

    item.accept(this);
  }

  @Override
  public void visitRole(@NotNull IRequiredValueModelNodeItem item) {
    Role role = (Role) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.ROLE, ObjectUtils.notNull(role.getId()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(child));
      item.getModelItemsByName("link").forEach(child -> handleLink(child));
      evaluateToList(ROLE_MARKUP_METAPATH, item).forEach(child -> handleMarkup(child));
    }
  }

  @Override
  public void visitParty(@NotNull IRequiredValueModelNodeItem item) {
    Party party = (Party) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.PARTY, ObjectUtils.notNull(party.getUuid()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(child));
      item.getModelItemsByName("link").forEach(child -> handleLink(child));
      evaluateToList(PARTY_MARKUP_METAPATH, item).forEach(child -> handleMarkup(child));
    }
  }

  @Override
  public void visitLocation(@NotNull IRequiredValueModelNodeItem item) {
    Location location = (Location) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.LOCATION, ObjectUtils.notNull(location.getUuid()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(child));
      item.getModelItemsByName("link").forEach(child -> handleLink(child));
      evaluateToList(LOCATION_MARKUP_METAPATH, item).forEach(child -> handleMarkup(child));
    }
  }

  @Override
  public void visitResource(@NotNull IRequiredValueModelNodeItem item) {
    Resource resource = (Resource) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.RESOURCE, ObjectUtils.notNull(resource.getUuid()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(child));

      item.getModelItemsByName("citation").forEach(child -> {
        child.getModelItemsByName("text").forEach(citationChild -> handleMarkup(citationChild));
        child.getModelItemsByName("prop").forEach(citationChild -> handleProperty(citationChild));
        child.getModelItemsByName("link").forEach(citationChild -> handleLink(citationChild));
      });

      evaluateToList(RESOURCE_MARKUP_METAPATH, item).forEach(child -> handleMarkup(child));
    }
  }

  @Override
  public void visitParameter(@NotNull IRequiredValueModelNodeItem item) {
    Parameter parameter = (Parameter) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.PARAMETER, ObjectUtils.notNull(parameter.getId()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(child));
      item.getModelItemsByName("link").forEach(child -> handleLink(child));
      evaluateToList(PARAM_MARKUP_METAPATH, item).forEach(child -> handleMarkup(child));
    }
  }

  @NotNull
  @SuppressWarnings("unchecked")
  private <T extends INodeItem, R extends IRequiredValueModelNodeItem> List<@NotNull R> evaluateToList(
      @NotNull MetapathExpression metapath,
      @NotNull T item) {
    return ((ISequence<R>) metapath.evaluateAs(item, ResultType.SEQUENCE)).asList();
  }

  @Override
  public void visitGroup(@NotNull IRequiredValueModelNodeItem item) {
    CatalogGroup group = (CatalogGroup) item.getValue();
    String id = group.getId();

    boolean resolve;
    if (id == null) {
      resolve = true;
    } else {
      EntityItem entity = getIndex().getEntity(ItemType.GROUP, id);
      if (entity != null && !entity.isResolved()) {
        entity.markResolved();
        resolve = true;
      } else {
        resolve = false;
      }
    }

    if (resolve && getIndex().isSelected(group)) {
      // process children
      item.getModelItemsByName("title").forEach(child -> handleMarkup(child));
      item.getModelItemsByName("prop").forEach(child -> handleProperty(child));
      item.getModelItemsByName("link").forEach(child -> handleLink(child));
      visitParts(item);

      // only process these if the current group is selected, since the group will only be selected if a
      // child is selected
      item.getModelItemsByName("group").forEach(child -> visitGroup(child));
      item.getModelItemsByName("control").forEach(child -> visitControl(child));

      // skip parameters for now. These will be processed by a separate pass.
    }
  }

  @Override
  public void visitControl(@NotNull IRequiredValueModelNodeItem item) {
    Control control = (Control) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.CONTROL, ObjectUtils.notNull(control.getId()));

    if (!entity.isResolved()) {
      entity.markResolved();
      if (getIndex().isSelected(control)) {
        // process non-control, non-param children
        item.getModelItemsByName("title").forEach(child -> handleMarkup(child));
        item.getModelItemsByName("prop").forEach(child -> handleProperty(child));
        item.getModelItemsByName("link").forEach(child -> handleLink(child));
        visitParts(item);

        // skip parameters for now. These will be processed by a separate pass.
      }

      // Always process these, since we don't know if the child control is selected
      item.getModelItemsByName("control").forEach(child -> visitControl(child));
    }
  }

  protected void visitParts(@NotNull IRequiredValueModelNodeItem groupOrControlItem) {
    PART_METAPATH.evaluate(groupOrControlItem).asStream()
        .map(item -> (@NotNull IRequiredValueModelNodeItem) item)
        .forEachOrdered(partItem -> {
          visitPart(partItem);
        });
  }

  @Override
  public void visitPart(@NotNull IRequiredValueModelNodeItem item) {
    ControlPart part = (ControlPart) item.getValue();
    String id = part.getId();

    boolean resolve;
    if (id == null) {
      resolve = true;
    } else {
      EntityItem entity = getIndex().getEntity(ItemType.PART, id);
      if (entity != null && !entity.isResolved()) {
        entity.markResolved();
        resolve = true;
      } else {
        resolve = false;
      }
    }

    if (resolve) {
      item.getModelItemsByName("title").forEach(child -> handleMarkup(child));
      item.getModelItemsByName("prop").forEach(child -> handleProperty(child));
      item.getModelItemsByName("link").forEach(child -> handleLink(child));
      item.getModelItemsByName("prose").forEach(child -> handleMarkup(child));
      item.getModelItemsByName("part").forEach(child -> visitParts(child));
    }
  }

  @NotNull
  private void handleMarkup(@NotNull IRequiredValueModelNodeItem item) {
    IMarkupItem markupItem = (IMarkupItem) FnData.fnDataItem(item);
    IMarkupText markup = markupItem.getValue();
    if (markup != null) {
      handleMarkup(markup);
    }
  }

  @NotNull
  private void handleMarkup(@NotNull IMarkupText text) {
    for (Node node : CollectionUtil.toIterable(text.getNodesAsStream().iterator())) {
      if (node instanceof InsertAnchorNode) {
        handleInsert((InsertAnchorNode) node);
      } else if (node instanceof InlineLinkNode) {
        handleAnchor((InlineLinkNode) node);
      }
    }
  }

  @NotNull
  private void handleInsert(@NotNull InsertAnchorNode node) {
    boolean retval = INSERT_POLICY.handleReference(node, this);
    if (LOGGER.isWarnEnabled() && !retval) {
      LOGGER.atWarn().log("unsupported insert type '{}'", node.getType().toString());
    }
  }

  @NotNull
  private void handleAnchor(@NotNull InlineLinkNode node) {
    boolean result = ANCHOR_POLICY.handleReference(node, this);
    if (LOGGER.isWarnEnabled() && !result) {
      LOGGER.atWarn().log("unsupported anchor with href '{}'", node.getUrl().toString());
    }
  }

  @NotNull
  private void handleProperty(@NotNull IRequiredValueModelNodeItem item) {
    Property property = (Property) item.getValue();
    QName qname = property.getQName();

    IReferencePolicy<Property> policy = PROPERTY_POLICIES.get(qname);

    boolean result = policy != null && policy.handleReference(property, this);
    if (LOGGER.isWarnEnabled() && !result) {
      LOGGER.atWarn().log("unsupported property '{}'", property.getQName());
    }
  }

  @NotNull
  private void handleLink(@NotNull IRequiredValueModelNodeItem item) {
    Link link = (Link) item.getValue();
    IReferencePolicy<Link> policy = null;
    String rel = link.getRel();
    if (rel != null) {
      policy = LINK_POLICIES.get(rel);
    }

    boolean result = policy != null && policy.handleReference(link, this);
    if (LOGGER.isWarnEnabled() && !result) {
      LOGGER.atWarn().log("unsupported link rel '{}'", link.getRel());
    }
  }

  @SuppressWarnings("null")
  protected void incrementReferenceCount(@NotNull ItemType type, @NotNull UUID identifier) {
    incrementReferenceCount(type, identifier.toString());
  }

  protected void incrementReferenceCount(@NotNull ItemType type, @NotNull String identifier) {
    EntityItem item = getIndex().getEntity(type, identifier);
    if (item == null) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.atError().log("Unknown reference to {} '{}'", type.toString().toLowerCase(Locale.ROOT), identifier);
      }
    } else {
      item.incrementReferenceCount();
    }
  }
}
