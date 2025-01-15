/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.metapath.IMetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.IMetapathExpression.ResultType;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.metadata.IProperty;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem.ItemType;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IIndexer {
  enum SelectionStatus {
    SELECTED,
    UNSELECTED,
    UNKNOWN;
  }

  IMetapathExpression HAS_PROP_KEEP_METAPATH = IMetapathExpression.compile(
      "prop[@name='keep' and has-oscal-namespace('" + IProperty.OSCAL_NAMESPACE + "')]/@value = 'always'",
      OscalBindingContext.OSCAL_STATIC_METAPATH_CONTEXT);

  Predicate<IEntityItem> KEEP_ENTITY_PREDICATE = entity -> entity.getReferenceCount() > 0
      || (Boolean) ObjectUtils
          .notNull(HAS_PROP_KEEP_METAPATH.evaluateAs(entity.getInstance(), ResultType.BOOLEAN));

  static boolean isReferencedEntity(@NonNull IEntityItem entity) {
    return KEEP_ENTITY_PREDICATE.test(entity);
  }

  /**
   * Keep entities that have a reference count greater than zero or are required
   * to be kept based on the "keep"="always property.
   *
   * @param entities
   *          the entity items to filter
   * @return the entities that pass the filter
   */
  static Stream<IEntityItem> getReferencedEntitiesAsStream(@NonNull Collection<IEntityItem> entities) {
    return entities.stream().filter(KEEP_ENTITY_PREDICATE);
  }

  /**
   * Keep entities that have a reference count of zero or are not required to be
   * kept based on the "keep"="always property.
   *
   * @param entities
   *          the entity items to filter
   * @return the entities that pass the filter
   */
  static Stream<IEntityItem> getUnreferencedEntitiesAsStream(@NonNull Collection<IEntityItem> entities) {
    return entities.stream().filter(KEEP_ENTITY_PREDICATE.negate());
  }

  /**
   * Generates a stream of distinct items that have a reference count greater than
   * zero or are required to be kept based on the "keep"="always property.
   * <p>
   * Distinct items are determined based on the item's key using the provided
   * {@code keyMapper}.
   *
   * @param <T>
   *          the item type
   * @param <K>
   *          the key type
   * @param resolvedItems
   *          a series of previously resolved items to add to prepend to the
   *          stream
   * @param importedEntityItems
   *          a collection of new items to filter then append to the stream
   * @param keyMapper
   *          the key mapping function to determine the item's key
   * @return the resulting series of items with duplicate items with the same key
   *         removed
   */
  // TODO: Is this the right name for this method?
  static <T, K> Stream<T> filterDistinct(
      @NonNull Stream<T> resolvedItems,
      @NonNull Collection<IEntityItem> importedEntityItems,
      @NonNull Function<? super T, ? extends K> keyMapper) {
    @SuppressWarnings("unchecked")
    Stream<T> importedStream = getReferencedEntitiesAsStream(importedEntityItems)
        .map(entity -> (T) entity.getInstanceValue());

    return CustomCollectors.distinctByKey(
        ObjectUtils.notNull(Stream.concat(resolvedItems, importedStream)),
        keyMapper,
        (key, value1, value2) -> value2);
  }

  static void logIndex(@NonNull IIndexer indexer, @NonNull Level logLevel) {
    Logger logger = LogManager.getLogger();

    Set<INodeItem> indexedItems = new HashSet<>();
    if (logger.isEnabled(logLevel)) {
      for (ItemType itemType : ItemType.values()) {
        assert itemType != null;
        for (IEntityItem item : indexer.getEntitiesByItemType(itemType)) {
          INodeItem nodeItem = item.getInstance();
          indexedItems.add(nodeItem);
          logger.atLevel(logLevel).log("{} {}: selected: {}, reference count: {}",
              itemType.name(),
              item.isIdentifierReassigned() ? item.getIdentifier() + "(" + item.getOriginalIdentifier() + ")"
                  : item.getIdentifier(),
              indexer.getSelectionStatus(nodeItem),
              item.getReferenceCount());
        }
      }
    }

    for (Map.Entry<INodeItem, SelectionStatus> entry : indexer.getSelectionStatusMap().entrySet()) {
      INodeItem nodeItem = entry.getKey();
      if (!indexedItems.contains(nodeItem)) {
        Object value = nodeItem.getValue();
        logger.atLevel(logLevel).log("{}: {}", value == null ? "(null)" : value.getClass().getName(), entry.getValue());
      }
    }
  }

  @NonNull
  IEntityItem addRole(@NonNull IModelNodeItem<?, ?> role);

  @NonNull
  IEntityItem addLocation(@NonNull IModelNodeItem<?, ?> location);

  @NonNull
  IEntityItem addParty(@NonNull IModelNodeItem<?, ?> party);

  @Nullable
  IEntityItem addGroup(@NonNull IModelNodeItem<?, ?> group);

  @NonNull
  IEntityItem addControl(@NonNull IModelNodeItem<?, ?> control);

  @NonNull
  IEntityItem addParameter(@NonNull IModelNodeItem<?, ?> parameter);

  @Nullable
  IEntityItem addPart(@NonNull IModelNodeItem<?, ?> part);

  @NonNull
  IEntityItem addResource(@NonNull IModelNodeItem<?, ?> resource);

  @NonNull
  Collection<IEntityItem> getEntitiesByItemType(@NonNull IEntityItem.ItemType itemType);

  @Nullable
  default IEntityItem getEntity(@NonNull IEntityItem.ItemType itemType, @NonNull UUID identifier) {
    return getEntity(itemType, ObjectUtils.notNull(identifier.toString()), false);
  }

  /**
   * Lookup an item of the given {@code itemType} having the given
   * {@code identifier}.
   * <p>
   * Will normalize the case of a UUID-based identifier.
   *
   * @param itemType
   *          the type of item to search for
   * @param identifier
   *          the identifier to lookup
   * @return the matching item or {@code null} if no match was found
   */
  @Nullable
  default IEntityItem getEntity(@NonNull IEntityItem.ItemType itemType, @NonNull String identifier) {
    return getEntity(itemType, identifier, itemType.isUuid());
  }

  /**
   * Lookup an item of the given {@code itemType} having the given
   * {@code identifier}.
   * <p>
   * Will normalize the case of a UUID-based the identifier when requested.
   *
   * @param itemType
   *          the type of item to search for
   * @param identifier
   *          the identifier to lookup
   * @param normalize
   *          {@code true} if the identifier case should be normalized or
   *          {@code false} otherwise
   * @return the matching item or {@code null} if no match was found
   */
  @Nullable
  IEntityItem getEntity(@NonNull IEntityItem.ItemType itemType, @NonNull String identifier, boolean normalize);

  boolean removeItem(@NonNull IEntityItem entity);

  boolean isSelected(@NonNull IEntityItem entity);

  Map<INodeItem, SelectionStatus> getSelectionStatusMap();

  @NonNull
  SelectionStatus getSelectionStatus(@NonNull INodeItem item);

  void setSelectionStatus(@NonNull INodeItem item, @NonNull SelectionStatus selectionStatus);

  void resetSelectionStatus();

  void append(@NonNull IIndexer result);

  /**
   * Get a copy of the entity map.
   *
   * @return the copy
   */
  @NonNull
  Map<ItemType, Map<String, IEntityItem>> getEntities();
}
