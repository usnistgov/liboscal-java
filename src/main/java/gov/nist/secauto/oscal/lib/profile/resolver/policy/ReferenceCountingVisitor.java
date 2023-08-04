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

import gov.nist.secauto.metaschema.core.datatype.markup.IMarkupString;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension.InsertAnchorNode;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IMarkupItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFieldNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.model.metadata.AbstractProperty;
import gov.nist.secauto.oscal.lib.model.metadata.IProperty;
import gov.nist.secauto.oscal.lib.profile.resolver.support.AbstractCatalogEntityVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ReferenceCountingVisitor
    extends AbstractCatalogEntityVisitor<ReferenceCountingVisitor.Context, Void>
    implements IReferenceVisitor<ReferenceCountingVisitor.Context> {
  private static final Logger LOGGER = LogManager.getLogger(ReferenceCountingVisitor.class);

  private static final ReferenceCountingVisitor SINGLETON = new ReferenceCountingVisitor();

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
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.OSCAL_NAMESPACE, "keep"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.RMF_NAMESPACE, "method"), PROPERTY_POLICY_IGNORE);
    PROPERTY_POLICIES.put(AbstractProperty.qname(IProperty.RMF_NAMESPACE, "aggregates"),
        PropertyReferencePolicy.create(IIdentifierParser.IDENTITY_PARSER, IEntityItem.ItemType.PARAMETER));

    LINK_POLICIES = new HashMap<>();
    LINK_POLICIES.put("source-profile", LINK_POLICY_IGNORE);
    LINK_POLICIES.put("citation", LinkReferencePolicy.create(IEntityItem.ItemType.RESOURCE));
    LINK_POLICIES.put("reference", LinkReferencePolicy.create(IEntityItem.ItemType.RESOURCE));
    LINK_POLICIES.put("related", LinkReferencePolicy.create(IEntityItem.ItemType.CONTROL));
    LINK_POLICIES.put("required", LinkReferencePolicy.create(IEntityItem.ItemType.CONTROL));
    LINK_POLICIES.put("corresp", LinkReferencePolicy.create(IEntityItem.ItemType.PART));
  }

  public static ReferenceCountingVisitor instance() {
    return SINGLETON;
  }

  public ReferenceCountingVisitor() {
    // visit everything except parts, roles, locations, parties, parameters, and
    // resources, which are
    // handled differently by this visitor
    super(ObjectUtils.notNull(EnumSet.complementOf(
        EnumSet.of(
            IEntityItem.ItemType.PART,
            IEntityItem.ItemType.ROLE,
            IEntityItem.ItemType.LOCATION,
            IEntityItem.ItemType.PARTY,
            IEntityItem.ItemType.PARAMETER,
            IEntityItem.ItemType.RESOURCE))));
  }

  @Override
  protected Void newDefaultResult(Context context) {
    // do nothing
    return null;
  }

  @Override
  protected Void aggregateResults(Void first, Void second, Context context) {
    // do nothing
    return null;
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
  // for (BackMatter.Resource resource :
  // CollectionUtil.listOrEmpty(backMatter.getResources())) {
  // visitResource(resource);
  // }
  // }
  // }

  public void visitCatalog(@NonNull IDocumentNodeItem catalogItem, @NonNull IIndexer indexer, @NonNull URI baseUri) {
    Context context = new Context(indexer, baseUri);
    visitCatalog(catalogItem, context);

    IIndexer index = context.getIndexer();
    // resolve the entities picked up by the original indexing operation
    // FIXME: Is this necessary?
    IIndexer.getReferencedEntitiesAsStream(index.getEntitiesByItemType(IEntityItem.ItemType.ROLE))
        .forEachOrdered(
            item -> resolveEntity(ObjectUtils.notNull(item), context, ReferenceCountingVisitor::resolveRole));
    IIndexer.getReferencedEntitiesAsStream(index.getEntitiesByItemType(IEntityItem.ItemType.LOCATION))
        .forEachOrdered(
            item -> resolveEntity(ObjectUtils.notNull(item), context,
                ReferenceCountingVisitor::resolveLocation));
    IIndexer.getReferencedEntitiesAsStream(index.getEntitiesByItemType(IEntityItem.ItemType.PARTY))
        .forEachOrdered(
            item -> resolveEntity(ObjectUtils.notNull(item), context,
                ReferenceCountingVisitor::resolveParty));
    IIndexer.getReferencedEntitiesAsStream(index.getEntitiesByItemType(IEntityItem.ItemType.PARAMETER))
        .forEachOrdered(
            item -> resolveEntity(ObjectUtils.notNull(item), context,
                ReferenceCountingVisitor::resolveParameter));
    IIndexer.getReferencedEntitiesAsStream(index.getEntitiesByItemType(IEntityItem.ItemType.RESOURCE))
        .forEachOrdered(
            item -> resolveEntity(ObjectUtils.notNull(item), context,
                ReferenceCountingVisitor::resolveResource));
  }

  @Override
  public Void visitGroup(
      IAssemblyNodeItem item,
      Void childResult,
      Context context) {
    IIndexer index = context.getIndexer();
    // handle the group if it is selected
    // a group will only be selected if it contains a descendant control that is
    // selected
    if (IIndexer.SelectionStatus.SELECTED.equals(index.getSelectionStatus(item))) {
      CatalogGroup group = ObjectUtils.requireNonNull((CatalogGroup) item.getValue());
      String id = group.getId();

      boolean resolve;
      if (id == null) {
        // always resolve a group without an identifier
        resolve = true;
      } else {
        IEntityItem entity = index.getEntity(IEntityItem.ItemType.GROUP, id, false);
        if (entity != null && !context.isResolved(entity)) {
          // only resolve if not already resolved
          context.markResolved(entity);
          resolve = true;
        } else {
          resolve = false;
        }
      }

      // resolve only if requested
      if (resolve) {
        resolveGroup(item, context);
      }
    }
    return null;
  }

  @Override
  public Void visitControl(
      IAssemblyNodeItem item,
      Void childResult,
      Context context) {
    IIndexer index = context.getIndexer();
    // handle the control if it is selected
    if (IIndexer.SelectionStatus.SELECTED.equals(index.getSelectionStatus(item))) {
      Control control = ObjectUtils.requireNonNull((Control) item.getValue());
      IEntityItem entity
          = context.getIndexer().getEntity(IEntityItem.ItemType.CONTROL, ObjectUtils.notNull(control.getId()), false);

      // the control must always appear in the index
      assert entity != null;

      if (!context.isResolved(entity)) {
        context.markResolved(entity);
        if (IIndexer.SelectionStatus.SELECTED.equals(context.getIndexer().getSelectionStatus(item))) {
          resolveControl(item, context);
        }
      }
    }
    return null;
  }

  @Override
  protected void visitParts(
      IAssemblyNodeItem groupOrControlItem,
      Context context) {
    // visits all descendant parts
    CHILD_PART_METAPATH.evaluate(groupOrControlItem).asStream()
        .map(item -> (IAssemblyNodeItem) item)
        .forEachOrdered(partItem -> {
          visitPart(ObjectUtils.notNull(partItem), groupOrControlItem, context);
        });
  }

  @Override
  protected void visitPart(
      IAssemblyNodeItem item,
      IAssemblyNodeItem groupOrControlItem,
      Context context) {
    assert context != null;

    ControlPart part = ObjectUtils.requireNonNull((ControlPart) item.getValue());
    String id = part.getId();

    boolean resolve;
    if (id == null) {
      // always resolve a part without an identifier
      resolve = true;
    } else {
      IEntityItem entity = context.getIndexer().getEntity(IEntityItem.ItemType.PART, id, false);
      if (entity != null && !context.isResolved(entity)) {
        // only resolve if not already resolved
        context.markResolved(entity);
        resolve = true;
      } else {
        resolve = false;
      }
    }

    if (resolve) {
      resolvePart(item, context);
    }
  }

  protected void resolveGroup(
      @NonNull IAssemblyNodeItem item,
      @NonNull Context context) {
    if (IIndexer.SelectionStatus.SELECTED.equals(context.getIndexer().getSelectionStatus(item))) {

      // process children
      item.getModelItemsByName("title")
          .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
      item.getModelItemsByName("prop")
          .forEach(child -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
      item.getModelItemsByName("link")
          .forEach(child -> handleLink(ObjectUtils.notNull((IAssemblyNodeItem) child), context));

      // always visit parts
      visitParts(item, context);

      // skip parameters for now. These will be processed by a separate pass.
    }
  }

  protected void resolveControl(
      @NonNull IAssemblyNodeItem item,
      @NonNull Context context) {
    // process non-control, non-param children
    item.getModelItemsByName("title")
        .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
    item.getModelItemsByName("prop")
        .forEach(child -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    item.getModelItemsByName("link")
        .forEach(child -> handleLink(ObjectUtils.notNull((IAssemblyNodeItem) child), context));

    // always visit parts
    visitParts(item, context);

    // skip parameters for now. These will be processed by a separate pass.
  }

  private static void resolveRole(@NonNull IEntityItem entity, @NonNull Context context) {
    IModelNodeItem<?, ?> item = entity.getInstance();
    item.getModelItemsByName("prop")
        .forEach(child -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    item.getModelItemsByName("link")
        .forEach(child -> handleLink(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    ROLE_MARKUP_METAPATH.evaluate(item).asList()
        .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
  }

  private static void resolveParty(@NonNull IEntityItem entity, @NonNull Context context) {
    IModelNodeItem<?, ?> item = entity.getInstance();
    item.getModelItemsByName("prop")
        .forEach(child -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    item.getModelItemsByName("link")
        .forEach(child -> handleLink(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    PARTY_MARKUP_METAPATH.evaluate(item).asList()
        .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
  }

  public static void resolveLocation(@NonNull IEntityItem entity, @NonNull Context context) {
    IModelNodeItem<?, ?> item = entity.getInstance();
    item.getModelItemsByName("prop")
        .forEach(child -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    item.getModelItemsByName("link")
        .forEach(child -> handleLink(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    LOCATION_MARKUP_METAPATH.evaluate(item).asList()
        .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
  }

  public static void resolveResource(@NonNull IEntityItem entity, @NonNull Context context) {
    IModelNodeItem<?, ?> item = entity.getInstance();

    item.getModelItemsByName("prop")
        .forEach(child -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) child), context));

    item.getModelItemsByName("citation").forEach(child -> {
      if (child != null) {
        child.getModelItemsByName("text")
            .forEach(citationChild -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) citationChild), context));
        child.getModelItemsByName("prop")
            .forEach(citationChild -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) citationChild), context));
        child.getModelItemsByName("link")
            .forEach(citationChild -> handleLink(ObjectUtils.notNull((IAssemblyNodeItem) citationChild), context));
      }
    });

    RESOURCE_MARKUP_METAPATH.evaluate(item).asList()
        .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
  }

  public static void resolveParameter(@NonNull IEntityItem entity, @NonNull Context context) {
    IModelNodeItem<?, ?> item = entity.getInstance();

    item.getModelItemsByName("prop")
        .forEach(child -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    item.getModelItemsByName("link")
        .forEach(child -> handleLink(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    PARAM_MARKUP_METAPATH.evaluate(item).asList()
        .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
  }

  private static void resolvePart(
      @NonNull IAssemblyNodeItem item,
      @NonNull Context context) {
    item.getModelItemsByName("title")
        .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
    item.getModelItemsByName("prop")
        .forEach(child -> handleProperty(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    item.getModelItemsByName("link")
        .forEach(child -> handleLink(ObjectUtils.notNull((IAssemblyNodeItem) child), context));
    item.getModelItemsByName("prose")
        .forEach(child -> handleMarkup(ObjectUtils.notNull((IFieldNodeItem) child), context));
    // item.getModelItemsByName("part").forEach(child ->
    // visitor.visitPart(ObjectUtils.notNull(child),
    // context));
  }

  private static void handleMarkup(
      @NonNull IFieldNodeItem item,
      @NonNull Context context) {
    IMarkupItem markupItem = (IMarkupItem) FnData.fnDataItem(item);
    IMarkupString<?> markup = markupItem.getValue();
    handleMarkup(item, markup, context);
  }

  private static void handleMarkup(
      @NonNull IFieldNodeItem contextItem,
      @NonNull IMarkupString<?> text,
      @NonNull Context context) {
    for (Node node : CollectionUtil.toIterable(
        ObjectUtils.notNull(text.getNodesAsStream().iterator()))) {
      if (node instanceof InsertAnchorNode) {
        handleInsert(contextItem, (InsertAnchorNode) node, context);
      } else if (node instanceof InlineLinkNode) {
        handleAnchor(contextItem, (InlineLinkNode) node, context);
      }
    }
  }

  private static void handleInsert(
      @NonNull IFieldNodeItem contextItem,
      @NonNull InsertAnchorNode node,
      @NonNull Context context) {
    boolean retval = INSERT_POLICY.handleReference(contextItem, node, context);
    if (LOGGER.isWarnEnabled() && !retval) {
      LOGGER.atWarn().log("Unsupported insert type '{}' at '{}'",
          node.getType().toString(),
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER));
    }
  }

  private static void handleAnchor(
      @NonNull IFieldNodeItem contextItem,
      @NonNull InlineLinkNode node,
      @NonNull Context context) {
    boolean result = ANCHOR_POLICY.handleReference(contextItem, node, context);
    if (LOGGER.isWarnEnabled() && !result) {
      LOGGER.atWarn().log("Unsupported anchor with href '{}' at '{}'",
          node.getUrl().toString(),
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER));
    }
  }

  private static void handleProperty(
      @NonNull IAssemblyNodeItem item,
      @NonNull Context context) {
    Property property = ObjectUtils.requireNonNull((Property) item.getValue());
    QName qname = property.getQName();

    IReferencePolicy<Property> policy = PROPERTY_POLICIES.get(qname);

    boolean result = policy != null && policy.handleReference(item, property, context);
    if (LOGGER.isWarnEnabled() && !result) {
      LOGGER.atWarn().log("Unsupported property '{}' at '{}'",
          property.getQName(),
          item.toPath(IPathFormatter.METAPATH_PATH_FORMATER));
    }
  }

  private static void handleLink(
      @NonNull IAssemblyNodeItem item,
      @NonNull Context context) {
    Link link = ObjectUtils.requireNonNull((Link) item.getValue());
    IReferencePolicy<Link> policy = null;
    String rel = link.getRel();
    if (rel != null) {
      policy = LINK_POLICIES.get(rel);
    }

    boolean result = policy != null && policy.handleReference(item, link, context);
    if (LOGGER.isWarnEnabled() && !result) {
      LOGGER.atWarn().log("unsupported link rel '{}' at '{}'",
          link.getRel(),
          item.toPath(IPathFormatter.METAPATH_PATH_FORMATER));
    }
  }

  protected void resolveEntity(
      @NonNull IEntityItem entity,
      @NonNull Context context,
      @NonNull BiConsumer<IEntityItem, Context> handler) {

    if (!context.isResolved(entity)) {
      context.markResolved(entity);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("Resolving {} identified as '{}'",
            entity.getItemType().name(),
            entity.getIdentifier());
      }

      if (!IIndexer.SelectionStatus.UNSELECTED
          .equals(context.getIndexer().getSelectionStatus(entity.getInstance()))) {
        // only resolve selected and unknown entities
        handler.accept(entity, context);
      }
    }
  }

  public void resolveEntity(
      @NonNull IEntityItem entity,
      @NonNull Context context) {
    resolveEntity(entity, context, (theEntity, theContext) -> entityDispatch(
        ObjectUtils.notNull(theEntity),
        ObjectUtils.notNull(theContext)));
  }

  protected void entityDispatch(@NonNull IEntityItem entity, @NonNull Context context) {
    IAssemblyNodeItem item = (IAssemblyNodeItem) entity.getInstance();
    switch (entity.getItemType()) {
    case CONTROL:
      resolveControl(item, context);
      break;
    case GROUP:
      resolveGroup(item, context);
      break;
    case LOCATION:
      resolveLocation(entity, context);
      break;
    case PARAMETER:
      resolveParameter(entity, context);
      break;
    case PART:
      resolvePart(item, context);
      break;
    case PARTY:
      resolveParty(entity, context);
      break;
    case RESOURCE:
      resolveResource(entity, context);
      break;
    case ROLE:
      resolveRole(entity, context);
      break;
    default:
      throw new UnsupportedOperationException(entity.getItemType().name());
    }
  }
  //
  // @Override
  // protected Void newDefaultResult(Object context) {
  // return null;
  // }
  //
  // @Override
  // protected Void aggregateResults(Object first, Object second, Object context)
  // {
  // return null;
  // }

  public static final class Context {
    @NonNull
    private final IIndexer indexer;
    @NonNull
    private final URI source;
    @NonNull
    private final Set<IEntityItem> resolvedEntities = new HashSet<>();

    private Context(@NonNull IIndexer indexer, @NonNull URI source) {
      this.indexer = indexer;
      this.source = source;
    }

    @NonNull
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intending to expose this field")
    public IIndexer getIndexer() {
      return indexer;
    }

    @Nullable
    public IEntityItem getEntity(@NonNull IEntityItem.ItemType itemType, @NonNull String identifier) {
      return getIndexer().getEntity(itemType, identifier);
    }

    @SuppressWarnings("unused")
    @NonNull
    private URI getSource() {
      return source;
    }

    public void markResolved(@NonNull IEntityItem entity) {
      resolvedEntities.add(entity);
    }

    public boolean isResolved(@NonNull IEntityItem entity) {
      return resolvedEntities.contains(entity);
    }

    public void incrementReferenceCount(
        @NonNull IModelNodeItem<?, ?> contextItem,
        @NonNull IEntityItem.ItemType type,
        @NonNull UUID identifier) {
      incrementReferenceCountInternal(
          contextItem,
          type,
          ObjectUtils.notNull(identifier.toString()),
          false);
    }

    public void incrementReferenceCount(
        @NonNull IModelNodeItem<?, ?> contextItem,
        @NonNull IEntityItem.ItemType type,
        @NonNull String identifier) {
      incrementReferenceCountInternal(
          contextItem,
          type,
          identifier,
          type.isUuid());
    }

    private void incrementReferenceCountInternal(
        @NonNull IModelNodeItem<?, ?> contextItem,
        @NonNull IEntityItem.ItemType type,
        @NonNull String identifier,
        boolean normalize) {
      IEntityItem item = getIndexer().getEntity(type, identifier, normalize);
      if (item == null) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.atError().log("Unknown reference to {} '{}' at '{}'",
              type.toString().toLowerCase(Locale.ROOT),
              identifier,
              contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER));
        }
      } else {
        item.incrementReferenceCount();
      }
    }
  }
}
