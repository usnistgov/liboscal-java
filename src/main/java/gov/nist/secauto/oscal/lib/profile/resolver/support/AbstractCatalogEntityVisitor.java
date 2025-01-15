/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.metapath.IMetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.OscalModelConstants;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Visits a catalog document and its children as designated.
 * <p>
 * This implementation is stateless. The {@code T} parameter can be used to
 * convey state as needed.
 *
 * @param <T>
 *          the state type
 * @param <R>
 *          the result type
 */
public abstract class AbstractCatalogEntityVisitor<T, R>
    extends AbstractCatalogVisitor<T, R> {
  @NonNull
  public static final IMetapathExpression CHILD_PART_METAPATH
      = IMetapathExpression.compile("part|part//part",
          OscalBindingContext.OSCAL_STATIC_METAPATH_CONTEXT);
  @NonNull
  private static final IMetapathExpression BACK_MATTER_RESOURCES_METAPATH
      = IMetapathExpression.compile("back-matter/resource",
          OscalBindingContext.OSCAL_STATIC_METAPATH_CONTEXT);
  @NonNull
  private static final Set<IEntityItem.ItemType> GROUP_CONTAINER_TYPES
      = ObjectUtils.notNull(EnumSet.of(
          IEntityItem.ItemType.GROUP,
          IEntityItem.ItemType.CONTROL,
          IEntityItem.ItemType.PARAMETER,
          IEntityItem.ItemType.PART));
  @NonNull
  private static final Set<IEntityItem.ItemType> CONTROL_CONTAINER_TYPES
      = ObjectUtils.notNull(EnumSet.of(
          IEntityItem.ItemType.CONTROL,
          IEntityItem.ItemType.PARAMETER,
          IEntityItem.ItemType.PART));
  @NonNull
  private final Set<IEntityItem.ItemType> itemTypesToVisit;

  /**
   * Create a new visitor that will visit the item types identified by
   * {@code itemTypesToVisit}.
   *
   * @param itemTypesToVisit
   *          the item type the visitor will visit
   */
  public AbstractCatalogEntityVisitor(@NonNull Set<IEntityItem.ItemType> itemTypesToVisit) {
    this.itemTypesToVisit = CollectionUtil.unmodifiableSet(itemTypesToVisit);
  }

  public Set<IEntityItem.ItemType> getItemTypesToVisit() {
    return CollectionUtil.unmodifiableSet(itemTypesToVisit);
  }

  protected boolean isVisitedItemType(@NonNull IEntityItem.ItemType type) {
    return itemTypesToVisit.contains(type);
  }

  @Override
  public R visitCatalog(IDocumentNodeItem catalogDocument, T state) {
    R result = super.visitCatalog(catalogDocument, state);

    catalogDocument.modelItems().forEachOrdered(item -> {
      IRootAssemblyNodeItem root = ObjectUtils.requireNonNull((IRootAssemblyNodeItem) item);
      visitMetadata(root, state);
      visitBackMatter(root, state);
    });
    return result;
  }

  @Override
  protected R visitGroupContainer(IAssemblyNodeItem catalogOrGroup, R initialResult, T state) {
    R retval;
    if (Collections.disjoint(getItemTypesToVisit(), GROUP_CONTAINER_TYPES)) {
      retval = initialResult;
    } else {
      retval = super.visitGroupContainer(catalogOrGroup, initialResult, state);
    }
    return retval;
  }

  @Override
  protected R visitControlContainer(IAssemblyNodeItem catalogOrGroupOrControl, R initialResult, T state) {
    R retval;
    if (Collections.disjoint(getItemTypesToVisit(), CONTROL_CONTAINER_TYPES)) {
      retval = initialResult;
    } else {
      // first descend to all control container children
      retval = super.visitControlContainer(catalogOrGroupOrControl, initialResult, state);

      // handle parameters
      if (isVisitedItemType(IEntityItem.ItemType.PARAMETER)) {
        retval = catalogOrGroupOrControl.getModelItemsByName(OscalModelConstants.QNAME_PARAM).stream()
            .map(paramItem -> visitParameter(
                ObjectUtils.requireNonNull((IAssemblyNodeItem) paramItem),
                catalogOrGroupOrControl,
                state))
            .reduce(retval, (first, second) -> aggregateResults(first, second, state));
      }
    }
    return retval;
  }

  protected void visitParts(@NonNull IAssemblyNodeItem groupOrControlItem, T state) {
    // handle parts
    if (isVisitedItemType(IEntityItem.ItemType.PART)) {
      CHILD_PART_METAPATH.evaluate(groupOrControlItem).stream()
          .map(item -> (IAssemblyNodeItem) item)
          .forEachOrdered(partItem -> {
            visitPart(ObjectUtils.requireNonNull(partItem), groupOrControlItem, state);
          });
    }
  }

  @Override
  protected R visitGroupInternal(@NonNull IAssemblyNodeItem item, R childResult, T state) {
    if (isVisitedItemType(IEntityItem.ItemType.PART)) {
      visitParts(item, state);
    }

    R retval = childResult;
    if (isVisitedItemType(IEntityItem.ItemType.GROUP)) {
      retval = visitGroup(item, retval, state);
    }
    return retval;
  }

  @Override
  protected R visitControlInternal(IAssemblyNodeItem item, R childResult, T state) {
    if (isVisitedItemType(IEntityItem.ItemType.PART)) {
      visitParts(item, state);
    }

    R retval = childResult;
    if (isVisitedItemType(IEntityItem.ItemType.CONTROL)) {
      retval = visitControl(item, retval, state);
    }
    return retval;
  }

  /**
   * Called when visiting a parameter.
   * <p>
   * Can be overridden by classes extending this interface to support processing
   * of the visited object.
   *
   * @param item
   *          the Metapath item for the parameter
   * @param catalogOrGroupOrControl
   *          the parameter's parent Metapath item
   * @param state
   *          the calling context information
   * @return a meaningful result of the given type
   */
  protected R visitParameter(
      @NonNull IAssemblyNodeItem item,
      @NonNull IAssemblyNodeItem catalogOrGroupOrControl,
      T state) {
    // do nothing
    return newDefaultResult(state);
  }

  /**
   * Called when visiting a part.
   * <p>
   * Can be overridden by classes extending this interface to support processing
   * of the visited object.
   *
   * @param item
   *          the Metapath item for the part
   * @param groupOrControl
   *          the part's parent Metapath item
   * @param state
   *          the calling context information
   */
  protected void visitPart( // NOPMD noop default
      @NonNull IAssemblyNodeItem item,
      @NonNull IAssemblyNodeItem groupOrControl,
      T state) {
    // do nothing
  }

  /**
   * Called when visiting the "metadata" section of an OSCAL document.
   * <p>
   * Visits each contained role, location, and party.
   *
   * @param rootItem
   *          the root Module node item containing the "metadata" node
   * @param state
   *          the calling context information
   */
  protected void visitMetadata(@NonNull IRootAssemblyNodeItem rootItem, T state) {
    rootItem.getModelItemsByName(OscalModelConstants.QNAME_METADATA).stream()
        .map(metadataItem -> (IAssemblyNodeItem) metadataItem)
        .forEach(metadataItem -> {
          if (isVisitedItemType(IEntityItem.ItemType.ROLE)) {
            metadataItem.getModelItemsByName(OscalModelConstants.QNAME_ROLE).stream()
                .map(roleItem -> (IAssemblyNodeItem) roleItem)
                .forEachOrdered(roleItem -> {
                  visitRole(ObjectUtils.requireNonNull(roleItem), metadataItem, state);
                });
          }

          if (isVisitedItemType(IEntityItem.ItemType.LOCATION)) {
            metadataItem.getModelItemsByName(OscalModelConstants.QNAME_LOCATION).stream()
                .map(locationItem -> (IAssemblyNodeItem) locationItem)
                .forEachOrdered(locationItem -> {
                  visitLocation(ObjectUtils.requireNonNull(locationItem), metadataItem, state);
                });
          }

          if (isVisitedItemType(IEntityItem.ItemType.PARTY)) {
            metadataItem.getModelItemsByName(OscalModelConstants.QNAME_PARTY).stream()
                .map(partyItem -> (IAssemblyNodeItem) partyItem)
                .forEachOrdered(partyItem -> {
                  visitParty(ObjectUtils.requireNonNull(partyItem), metadataItem, state);
                });
          }
        });
  }

  /**
   * Called when visiting a role in the "metadata" section of an OSCAL document.
   * <p>
   * Can be overridden by classes extending this interface to support processing
   * of the visited object.
   *
   * @param item
   *          the role Module node item which is a child of the "metadata" node
   * @param metadataItem
   *          the "metadata" Module node item containing the role
   * @param state
   *          the calling context information
   */
  protected void visitRole( // NOPMD noop default
      @NonNull IAssemblyNodeItem item,
      @NonNull IAssemblyNodeItem metadataItem,
      T state) {
    // do nothing
  }

  /**
   * Called when visiting a location in the "metadata" section of an OSCAL
   * document.
   * <p>
   * Can be overridden by classes extending this interface to support processing
   * of the visited object.
   *
   * @param item
   *          the location Module node item which is a child of the "metadata"
   *          node
   * @param metadataItem
   *          the "metadata" Module node item containing the location
   * @param state
   *          the calling context information
   */
  protected void visitLocation( // NOPMD noop default
      @NonNull IAssemblyNodeItem item,
      @NonNull IAssemblyNodeItem metadataItem,
      T state) {
    // do nothing
  }

  /**
   * Called when visiting a party in the "metadata" section of an OSCAL document.
   * <p>
   * Can be overridden by classes extending this interface to support processing
   * of the visited object.
   *
   * @param item
   *          the party Module node item which is a child of the "metadata" node
   * @param metadataItem
   *          the "metadata" Module node item containing the party
   * @param state
   *          the calling context information
   */
  protected void visitParty( // NOPMD noop default
      @NonNull IAssemblyNodeItem item,
      @NonNull IAssemblyNodeItem metadataItem,
      T state) {
    // do nothing
  }

  /**
   * Called when visiting the "back-matter" section of an OSCAL document.
   * <p>
   * Visits each contained resource.
   *
   * @param rootItem
   *          the root Module node item containing the "back-matter" node
   * @param state
   *          the calling context information
   */
  protected void visitBackMatter(@NonNull IRootAssemblyNodeItem rootItem, T state) {
    if (isVisitedItemType(IEntityItem.ItemType.RESOURCE)) {
      BACK_MATTER_RESOURCES_METAPATH.evaluate(rootItem).stream()
          .map(item -> (IAssemblyNodeItem) item)
          .forEachOrdered(resourceItem -> {
            visitResource(ObjectUtils.requireNonNull(resourceItem), rootItem, state);
          });
    }
  }

  /**
   * Called when visiting a resource in the "back-matter" section of an OSCAL
   * document.
   * <p>
   * Can be overridden by classes extending this interface to support processing
   * of the visited object.
   *
   * @param resource
   *          the resource Module node item which is a child of the "metadata"
   *          node
   * @param backMatter
   *          the resource Module node item containing the party
   * @param state
   *          the calling context information
   */
  protected void visitResource( // NOPMD noop default
      @NonNull IAssemblyNodeItem resource,
      @NonNull IRootAssemblyNodeItem backMatter,
      T state) {
    // do nothing
  }
}
