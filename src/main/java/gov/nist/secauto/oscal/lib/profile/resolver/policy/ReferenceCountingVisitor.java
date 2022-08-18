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
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IMarkupItem;
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
import gov.nist.secauto.oscal.lib.model.metadata.AbstractProperty;
import gov.nist.secauto.oscal.lib.model.metadata.IProperty;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.Index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ReferenceCountingVisitor implements IReferenceVisitor { // NOPMD - ok
  private static final Logger LOGGER = LogManager.getLogger(ReferenceCountingVisitor.class);
  @NonNull
  private static final MetapathExpression PART_METAPATH
      = MetapathExpression.compile("part|part//part");
  @NonNull
  private static final MetapathExpression PARAM_MARKUP_METAPATH
      = MetapathExpression
          .compile("label|usage|constraint/(description|tests/remarks)|guideline/prose|select/choice|remarks");
  @NonNull
  private static final MetapathExpression ROLE_MARKUP_METAPATH
      = MetapathExpression.compile("title|description|remarks");
  @NonNull
  private static final MetapathExpression LOCATION_MARKUP_METAPATH
      = MetapathExpression.compile("title|remarks");
  @NonNull
  private static final MetapathExpression PARTY_MARKUP_METAPATH
      = MetapathExpression.compile("title|remarks");
  @NonNull
  private static final MetapathExpression RESOURCE_MARKUP_METAPATH
      = MetapathExpression.compile("title|description|remarks");

  @NonNull
  private static final IReferencePolicy<Property> PROPERTY_POLICY_IGNORE = IReferencePolicy.ignore();
  @NonNull
  private static final IReferencePolicy<Link> LINK_POLICY_IGNORE = IReferencePolicy.ignore();

  @NonNull
  private static final Map<QName, IReferencePolicy<Property>> PROPERTY_POLICIES;
  @NonNull
  private static final Map<String, IReferencePolicy<Link>> LINK_POLICIES;
  @NonNull
  private static final InsertReferencePolicy INSERT_POLICY = new InsertReferencePolicy();
  @NonNull
  private static final AnchorReferencePolicy ANCHOR_POLICY = new AnchorReferencePolicy();

  static {
    PROPERTY_POLICIES = new HashMap<>();
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.OSCAL_NAMESPACE, "resolution-tool"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.OSCAL_NAMESPACE, "label"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.OSCAL_NAMESPACE, "sort-id"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.OSCAL_NAMESPACE, "alt-label"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.OSCAL_NAMESPACE, "alt-identifier"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.OSCAL_NAMESPACE, "method"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.RMF_NAMESPACE, "method"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.RMF_NAMESPACE, "aggregates"),
        PropertyReferencePolicy.create(IIdentifierParser.IDENTITY_PARSER, ItemType.PARAMETER));

    LINK_POLICIES = new HashMap<>();
    LINK_POLICIES.put("source-profile", LINK_POLICY_IGNORE);
    LINK_POLICIES.put("citation", LinkReferencePolicy.create(ItemType.RESOURCE));
    LINK_POLICIES.put("reference", LinkReferencePolicy.create(ItemType.RESOURCE));
    LINK_POLICIES.put("related", LinkReferencePolicy.create(ItemType.CONTROL));
    LINK_POLICIES.put("required", LinkReferencePolicy.create(ItemType.CONTROL));
    LINK_POLICIES.put("corresp", LinkReferencePolicy.create(ItemType.PART));
  }

  @NonNull
  private final Index index;
  @NonNull
  private final URI source;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intending to store this parameter")
  public ReferenceCountingVisitor(@NonNull Index index, @NonNull URI source) {
    this.index = index;
    this.source = source;
  }

  @Override
  @NonNull
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intending to expose this field")
  public Index getIndex() {
    return index;
  }

  @NonNull
  protected URI getSource() {
    return source;
  }

  //
  // public void visitProfile(@NonNull Profile profile) {
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

  public void visitCatalog(@NonNull IDocumentNodeItem catalogItem) {

    // process children
    IRootAssemblyNodeItem rootItem = catalogItem.getRootAssemblyNodeItem();

    rootItem.getModelItemsByName("group").forEach(groupItem -> {
      visitGroup(ObjectUtils.notNull(groupItem));
    });

    rootItem.getModelItemsByName("control").forEach(controlItem -> {
      visitControl(ObjectUtils.notNull(controlItem));
    });
    index.getEntitiesByItemType(ItemType.ROLE).forEach(item -> resolveItem(ObjectUtils.notNull(item)));
    index.getEntitiesByItemType(ItemType.LOCATION).forEach(item -> resolveItem(ObjectUtils.notNull(item)));
    index.getEntitiesByItemType(ItemType.PARTY).forEach(item -> resolveItem(ObjectUtils.notNull(item)));
    index.getEntitiesByItemType(ItemType.PARAMETER).forEach(item -> resolveItem(ObjectUtils.notNull(item)));
    index.getEntitiesByItemType(ItemType.RESOURCE).forEach(item -> resolveItem(ObjectUtils.notNull(item)));
  }

  private void resolveItem(@NonNull EntityItem item) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Resolving {} identified as '{}'", item.getItemType().name(), item.getIdentifier());
    }
    item.markResolved();

    item.accept(this);
  }

  @Override
  public void visitRole(@NonNull IRequiredValueModelNodeItem item) {
    Role role = (Role) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.ROLE, ObjectUtils.notNull(role.getId()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(ObjectUtils.notNull(child)));
      item.getModelItemsByName("link").forEach(child -> handleLink(ObjectUtils.notNull(child)));
      ROLE_MARKUP_METAPATH.evaluate(item).asList()
          .forEach(child -> handleMarkup(ObjectUtils.notNull((IRequiredValueModelNodeItem) child)));
    }
  }

  @Override
  public void visitParty(@NonNull IRequiredValueModelNodeItem item) {
    Party party = (Party) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.PARTY, ObjectUtils.notNull(party.getUuid()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(ObjectUtils.notNull(child)));
      item.getModelItemsByName("link").forEach(child -> handleLink(ObjectUtils.notNull(child)));
      PARTY_MARKUP_METAPATH.evaluate(item).asList()
          .forEach(child -> handleMarkup(ObjectUtils.notNull((IRequiredValueModelNodeItem) child)));
    }
  }

  @Override
  public void visitLocation(@NonNull IRequiredValueModelNodeItem item) {
    Location location = (Location) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.LOCATION, ObjectUtils.notNull(location.getUuid()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(ObjectUtils.notNull(child)));
      item.getModelItemsByName("link").forEach(child -> handleLink(ObjectUtils.notNull(child)));
      LOCATION_MARKUP_METAPATH.evaluate(item).asList()
          .forEach(child -> handleMarkup(ObjectUtils.notNull((IRequiredValueModelNodeItem) child)));
    }
  }

  @Override
  public void visitResource(@NonNull IRequiredValueModelNodeItem item) {
    Resource resource = (Resource) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.RESOURCE, ObjectUtils.notNull(resource.getUuid()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(ObjectUtils.notNull(child)));

      item.getModelItemsByName("citation").forEach(child -> {
        if (child != null) {
          child.getModelItemsByName("text").forEach(citationChild -> handleMarkup(ObjectUtils.notNull(citationChild)));
          child.getModelItemsByName("prop")
              .forEach(citationChild -> handleProperty(ObjectUtils.notNull(citationChild)));
          child.getModelItemsByName("link").forEach(citationChild -> handleLink(ObjectUtils.notNull(citationChild)));
        }
      });

      RESOURCE_MARKUP_METAPATH.evaluate(item).asList()
          .forEach(child -> handleMarkup(ObjectUtils.notNull((IRequiredValueModelNodeItem) child)));
    }
  }

  @Override
  public void visitParameter(@NonNull IRequiredValueModelNodeItem item) {
    Parameter parameter = (Parameter) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.PARAMETER, ObjectUtils.notNull(parameter.getId()));

    if (!entity.isResolved()) {
      entity.markResolved();

      item.getModelItemsByName("prop").forEach(child -> handleProperty(ObjectUtils.notNull(child)));
      item.getModelItemsByName("link").forEach(child -> handleLink(ObjectUtils.notNull(child)));
      PARAM_MARKUP_METAPATH.evaluate(item).asList()
          .forEach(child -> handleMarkup(ObjectUtils.notNull((IRequiredValueModelNodeItem) child)));
    }
  }

  @Override
  public void visitGroup(@NonNull IRequiredValueModelNodeItem item) {
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
      item.getModelItemsByName("title").forEach(child -> handleMarkup(ObjectUtils.notNull(child)));
      item.getModelItemsByName("prop").forEach(child -> handleProperty(ObjectUtils.notNull(child)));
      item.getModelItemsByName("link").forEach(child -> handleLink(ObjectUtils.notNull(child)));
      visitParts(item);

      // only process these if the current group is selected, since the group will only be selected if a
      // child is selected
      item.getModelItemsByName("group").forEach(child -> visitGroup(ObjectUtils.notNull(child)));
      item.getModelItemsByName("control").forEach(child -> visitControl(ObjectUtils.notNull(child)));

      // skip parameters for now. These will be processed by a separate pass.
    }
  }

  @Override
  public void visitControl(@NonNull IRequiredValueModelNodeItem item) {
    Control control = (Control) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.CONTROL, ObjectUtils.notNull(control.getId()));

    if (!entity.isResolved()) {
      entity.markResolved();
      if (getIndex().isSelected(control)) {
        // process non-control, non-param children
        item.getModelItemsByName("title").forEach(child -> handleMarkup(ObjectUtils.notNull(child)));
        item.getModelItemsByName("prop").forEach(child -> handleProperty(ObjectUtils.notNull(child)));
        item.getModelItemsByName("link").forEach(child -> handleLink(ObjectUtils.notNull(child)));
        visitParts(item);

        // skip parameters for now. These will be processed by a separate pass.
      }

      // Always process these, since we don't know if the child control is selected
      item.getModelItemsByName("control").forEach(child -> visitControl(ObjectUtils.notNull(child)));
    }
  }

  protected void visitParts(@NonNull IRequiredValueModelNodeItem groupOrControlItem) {
    PART_METAPATH.evaluate(groupOrControlItem).asStream()
        .map(item -> (IRequiredValueModelNodeItem) item)
        .forEachOrdered(partItem -> {
          visitPart(ObjectUtils.notNull(partItem));
        });
  }

  @Override
  public void visitPart(@NonNull IRequiredValueModelNodeItem item) {
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
      item.getModelItemsByName("title").forEach(child -> handleMarkup(ObjectUtils.notNull(child)));
      item.getModelItemsByName("prop").forEach(child -> handleProperty(ObjectUtils.notNull(child)));
      item.getModelItemsByName("link").forEach(child -> handleLink(ObjectUtils.notNull(child)));
      item.getModelItemsByName("prose").forEach(child -> handleMarkup(ObjectUtils.notNull(child)));
      item.getModelItemsByName("part").forEach(child -> visitParts(ObjectUtils.notNull(child)));
    }
  }

  private void handleMarkup(@NonNull IRequiredValueModelNodeItem item) {
    IMarkupItem markupItem = (IMarkupItem) FnData.fnDataItem(item);
    IMarkupText markup = markupItem.getValue();
    handleMarkup(markup);
  }

  private void handleMarkup(@NonNull IMarkupText text) {
    for (Node node : CollectionUtil.toIterable(text.getNodesAsStream().iterator())) {
      if (node instanceof InsertAnchorNode) {
        handleInsert((InsertAnchorNode) node);
      } else if (node instanceof InlineLinkNode) {
        handleAnchor((InlineLinkNode) node);
      }
    }
  }

  private void handleInsert(@NonNull InsertAnchorNode node) {
    boolean retval = INSERT_POLICY.handleReference(node, this);
    if (LOGGER.isWarnEnabled() && !retval) {
      LOGGER.atWarn().log("unsupported insert type '{}'", node.getType().toString());
    }
  }

  private void handleAnchor(@NonNull InlineLinkNode node) {
    boolean result = ANCHOR_POLICY.handleReference(node, this);
    if (LOGGER.isWarnEnabled() && !result) {
      LOGGER.atWarn().log("unsupported anchor with href '{}'", node.getUrl().toString());
    }
  }

  private void handleProperty(@NonNull IRequiredValueModelNodeItem item) {
    Property property = (Property) item.getValue();
    QName qname = property.getQName();

    IReferencePolicy<Property> policy = PROPERTY_POLICIES.get(qname);

    boolean result = policy != null && policy.handleReference(property, this);
    if (LOGGER.isWarnEnabled() && !result) {
      LOGGER.atWarn().log("unsupported property '{}'", property.getQName());
    }
  }

  private void handleLink(@NonNull IRequiredValueModelNodeItem item) {
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

  protected void incrementReferenceCount(@NonNull ItemType type, @NonNull UUID identifier) {
    incrementReferenceCount(type, ObjectUtils.notNull(identifier.toString()));
  }

  protected void incrementReferenceCount(@NonNull ItemType type, @NonNull String identifier) {
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
