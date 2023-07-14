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

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Visits a catalog document and its children as designated.
 * <p>
 * This implementation is stateless. The {@code T} parameter can be used to convey state as needed.
 *
 * @param <T>
 *          the state type
 * @param <R>
 *          the result type
 */
public abstract class AbstractCatalogEntityVisitor<T, R>
    extends AbstractCatalogVisitor<T, R> {
  @NonNull
  public static final MetapathExpression CHILD_PART_METAPATH
      = MetapathExpression.compile("part|part//part");
  @NonNull
  private static final MetapathExpression BACK_MATTER_RESOURCES_METAPATH
      = MetapathExpression.compile("back-matter/resource");
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
   * Create a new visitor that will visit the item types identified by {@code itemTypesToVisit}.
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

    IRootAssemblyNodeItem root = catalogDocument.getRootAssemblyNodeItem();
    visitMetadata(root, state);
    visitBackMatter(root, state);
    return result;
  }

  @Override
  protected R visitGroupContainer(IRequiredValueModelNodeItem catalogOrGroup, R initialResult, T state) {
    R retval;
    if (Collections.disjoint(getItemTypesToVisit(), GROUP_CONTAINER_TYPES)) {
      retval = initialResult;
    } else {
      retval = super.visitGroupContainer(catalogOrGroup, initialResult, state);
    }
    return retval;
  }

  @Override
  protected R visitControlContainer(IRequiredValueModelNodeItem catalogOrGroupOrControl, R initialResult, T state) {
    R retval;
    if (Collections.disjoint(getItemTypesToVisit(), CONTROL_CONTAINER_TYPES)) {
      retval = initialResult;
    } else {
      // first descend to all control container children
      retval = super.visitControlContainer(catalogOrGroupOrControl, initialResult, state);

      // handle parameters
      if (isVisitedItemType(IEntityItem.ItemType.PARAMETER)) {
        retval = catalogOrGroupOrControl.getModelItemsByName("param").stream()
            .map(paramItem -> {
              return visitParameter(ObjectUtils.requireNonNull(paramItem), catalogOrGroupOrControl, state);
            })
            .reduce(retval, (first, second) -> aggregateResults(first, second, state));
      } // TODO Auto-generated method stub
    }
    return retval;
  }

  protected void visitParts(@NonNull IRequiredValueModelNodeItem groupOrControlItem, T state) {
    // handle parts
    if (isVisitedItemType(IEntityItem.ItemType.PART)) {
      CHILD_PART_METAPATH.evaluate(groupOrControlItem).asStream()
          .map(item -> (IRequiredValueModelNodeItem) item)
          .forEachOrdered(partItem -> {
            visitPart(ObjectUtils.requireNonNull(partItem), groupOrControlItem, state);
          });
    }
  }

  @Override
  protected R visitGroupInternal(@NonNull IRequiredValueModelNodeItem item, R childResult, T state) {
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
  protected R visitControlInternal(IRequiredValueModelNodeItem item, R childResult, T state) {
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
   * Can be overridden by classes extending this interface to support processing of the visited
   * object.
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
      @NonNull IRequiredValueModelNodeItem item,
      @NonNull IRequiredValueModelNodeItem catalogOrGroupOrControl,
      T state) {
    // do nothing
    return newDefaultResult(state);
  }

  /**
   * Called when visiting a part.
   * <p>
   * Can be overridden by classes extending this interface to support processing of the visited
   * object.
   * 
   * @param item
   *          the Metapath item for the part
   * @param groupOrControl
   *          the part's parent Metapath item
   * @param state
   *          the calling context information
   */
  protected void visitPart( // NOPMD noop default
      @NonNull IRequiredValueModelNodeItem item,
      @NonNull IRequiredValueModelNodeItem groupOrControl,
      T state) {
    // do nothing
  }

  /**
   * Called when visiting the "metadata" section of an OSCAL document.
   * <p>
   * Visits each contained role, location, and party.
   * 
   * @param rootItem
   *          the root Metaschema node item containing the "metadata" node
   * @param state
   *          the calling context information
   */
  protected void visitMetadata(@NonNull IRootAssemblyNodeItem rootItem, T state) {
    rootItem.getModelItemsByName("metadata").forEach(metadataItem -> {
      if (isVisitedItemType(IEntityItem.ItemType.ROLE)) {
        metadataItem.getModelItemsByName("role").forEach(roleItem -> {
          visitRole(ObjectUtils.requireNonNull(roleItem), metadataItem, state);
        });
      }

      if (isVisitedItemType(IEntityItem.ItemType.LOCATION)) {
        metadataItem.getModelItemsByName("location").forEach(locationItem -> {
          visitLocation(ObjectUtils.requireNonNull(locationItem), metadataItem, state);
        });
      }

      if (isVisitedItemType(IEntityItem.ItemType.PARTY)) {
        metadataItem.getModelItemsByName("party").forEach(partyItem -> {
          visitParty(ObjectUtils.requireNonNull(partyItem), metadataItem, state);
        });
      }
    });
  }

  /**
   * Called when visiting a role in the "metadata" section of an OSCAL document.
   * <p>
   * Can be overridden by classes extending this interface to support processing of the visited
   * object.
   * 
   * @param item
   *          the role Metaschema node item which is a child of the "metadata" node
   * @param metadataItem
   *          the "metadata" Metaschema node item containing the role
   * @param state
   *          the calling context information
   */
  protected void visitRole( // NOPMD noop default
      @NonNull IRequiredValueModelNodeItem item,
      @NonNull IRequiredValueModelNodeItem metadataItem,
      T state) {
    // do nothing
  }

  /**
   * Called when visiting a location in the "metadata" section of an OSCAL document.
   * <p>
   * Can be overridden by classes extending this interface to support processing of the visited
   * object.
   * 
   * @param item
   *          the location Metaschema node item which is a child of the "metadata" node
   * @param metadataItem
   *          the "metadata" Metaschema node item containing the location
   * @param state
   *          the calling context information
   */
  protected void visitLocation( // NOPMD noop default
      @NonNull IRequiredValueModelNodeItem item,
      @NonNull IRequiredValueModelNodeItem metadataItem,
      T state) {
    // do nothing
  }

  /**
   * Called when visiting a party in the "metadata" section of an OSCAL document.
   * <p>
   * Can be overridden by classes extending this interface to support processing of the visited
   * object.
   * 
   * @param item
   *          the party Metaschema node item which is a child of the "metadata" node
   * @param metadataItem
   *          the "metadata" Metaschema node item containing the party
   * @param state
   *          the calling context information
   */
  protected void visitParty( // NOPMD noop default
      @NonNull IRequiredValueModelNodeItem item,
      @NonNull IRequiredValueModelNodeItem metadataItem,
      T state) {
    // do nothing
  }

  /**
   * Called when visiting the "back-matter" section of an OSCAL document.
   * <p>
   * Visits each contained resource.
   * 
   * @param rootItem
   *          the root Metaschema node item containing the "back-matter" node
   * @param state
   *          the calling context information
   */
  protected void visitBackMatter(@NonNull IRootAssemblyNodeItem rootItem, T state) {
    if (isVisitedItemType(IEntityItem.ItemType.RESOURCE)) {
      BACK_MATTER_RESOURCES_METAPATH.evaluate(rootItem).asStream()
          .map(item -> (IRequiredValueModelNodeItem) item)
          .forEachOrdered(resourceItem -> {
            visitResource(ObjectUtils.requireNonNull(resourceItem), rootItem, state);
          });
    }
  }

  /**
   * Called when visiting a resource in the "back-matter" section of an OSCAL document.
   * <p>
   * Can be overridden by classes extending this interface to support processing of the visited
   * object.
   * 
   * @param item
   *          the resource Metaschema node item which is a child of the "metadata" node
   * @param backMatterItem
   *          the resource Metaschema node item containing the party
   * @param state
   *          the calling context information
   */
  protected void visitResource( // NOPMD noop default
      @NonNull IRequiredValueModelNodeItem item,
      @NonNull IRootAssemblyNodeItem backMatterItem,
      T state) {
    // do nothing
  }
}
