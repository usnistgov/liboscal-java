/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.datatype.adapter.UuidAdapter;
import gov.nist.secauto.metaschema.core.metapath.IMetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.IMetapathExpression.ResultType;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Metadata.Location;
import gov.nist.secauto.oscal.lib.model.Metadata.Party;
import gov.nist.secauto.oscal.lib.model.Metadata.Role;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolver;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem.ItemType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class BasicIndexer implements IIndexer {
  private static final Logger LOGGER = LogManager.getLogger(ProfileResolver.class);
  private static final IMetapathExpression CONTAINER_METAPATH
      = IMetapathExpression.compile("(ancestor::control|ancestor::group)[1]",
          OscalBindingContext.OSCAL_STATIC_METAPATH_CONTEXT);

  @NonNull
  private final Map<IEntityItem.ItemType, Map<String, IEntityItem>> entityTypeToIdentifierToEntityMap;
  @NonNull
  private Map<INodeItem, SelectionStatus> nodeItemToSelectionStatusMap;

  @Override
  public void append(@NonNull IIndexer other) {
    for (ItemType itemType : ItemType.values()) {
      assert itemType != null;
      for (IEntityItem entity : other.getEntitiesByItemType(itemType)) {
        assert entity != null;
        addItem(entity);
      }
    }

    this.nodeItemToSelectionStatusMap.putAll(other.getSelectionStatusMap());
  }

  public BasicIndexer() {
    this.entityTypeToIdentifierToEntityMap = new EnumMap<>(IEntityItem.ItemType.class);
    this.nodeItemToSelectionStatusMap = new ConcurrentHashMap<>();
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // needed
  public BasicIndexer(IIndexer other) {
    // copy entity map
    this.entityTypeToIdentifierToEntityMap = other.getEntities();

    // copy selection map
    this.nodeItemToSelectionStatusMap = new ConcurrentHashMap<>(other.getSelectionStatusMap());
  }

  @Override
  public void setSelectionStatus(@NonNull INodeItem item, @NonNull SelectionStatus selectionStatus) {
    nodeItemToSelectionStatusMap.put(item, selectionStatus);
  }

  @Override
  public Map<INodeItem, SelectionStatus> getSelectionStatusMap() {
    return CollectionUtil.unmodifiableMap(nodeItemToSelectionStatusMap);
  }

  @Override
  public SelectionStatus getSelectionStatus(@NonNull INodeItem item) {
    SelectionStatus retval = nodeItemToSelectionStatusMap.get(item);
    return retval == null ? SelectionStatus.UNKNOWN : retval;
  }

  @Override
  public void resetSelectionStatus() {
    nodeItemToSelectionStatusMap = new ConcurrentHashMap<>();
  }

  @Override
  public boolean isSelected(@NonNull IEntityItem entity) {
    boolean retval;
    switch (entity.getItemType()) {
    case CONTROL:
    case GROUP:
      retval = IIndexer.SelectionStatus.SELECTED.equals(getSelectionStatus(entity.getInstance()));
      break;
    case PART: {
      IModelNodeItem<?, ?> instance = entity.getInstance();
      IIndexer.SelectionStatus status = getSelectionStatus(instance);
      if (IIndexer.SelectionStatus.UNKNOWN.equals(status)) {
        // lookup the status if not known
        IModelNodeItem<?, ?> containerItem = CONTAINER_METAPATH.evaluateAs(instance, ResultType.ITEM);
        assert containerItem != null;
        status = getSelectionStatus(containerItem);

        // cache the status
        setSelectionStatus(instance, status);
      }
      retval = IIndexer.SelectionStatus.SELECTED.equals(status);
      break;
    }
    case PARAMETER:
    case LOCATION:
    case PARTY:
    case RESOURCE:
    case ROLE:
      // always "selected"
      retval = true;
      break;
    default:
      throw new UnsupportedOperationException(entity.getItemType().name());
    }
    return retval;
  }

  @Override
  public Map<ItemType, Map<String, IEntityItem>> getEntities() {
    // make a copy
    Map<ItemType, Map<String, IEntityItem>> copy = entityTypeToIdentifierToEntityMap.entrySet().stream()
        .map(entry -> {
          ItemType key = entry.getKey();
          Map<String, IEntityItem> oldMap = entry.getValue();

          Map<String, IEntityItem> newMap = oldMap.entrySet().stream()
              .collect(Collectors.toMap(
                  Map.Entry::getKey,
                  Map.Entry::getValue,
                  (key1, key2) -> key1,
                  LinkedHashMap::new)); // need ordering
          assert newMap != null;
          // use a synchronized map to ensure thread safety
          return Map.entry(key, Collections.synchronizedMap(newMap));
        })
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (key1, key2) -> key1,
            ConcurrentHashMap::new));

    assert copy != null;
    return copy;
  }

  @Override
  @NonNull
  // TODO: rename to getEntitiesForItemType
  public Collection<IEntityItem> getEntitiesByItemType(@NonNull IEntityItem.ItemType itemType) {
    Map<String, IEntityItem> entityGroup = entityTypeToIdentifierToEntityMap.get(itemType);
    return entityGroup == null ? CollectionUtil.emptyList() : ObjectUtils.notNull(entityGroup.values());
  }
  //
  // public EntityItem getEntity(@NonNull ItemType itemType, @NonNull UUID
  // identifier) {
  // return getEntity(itemType, ObjectUtils.notNull(identifier.toString()),
  // false);
  // }
  //
  // public EntityItem getEntity(@NonNull ItemType itemType, @NonNull String
  // identifier) {
  // return getEntity(itemType, identifier, itemType.isUuid());
  // }

  @Override
  public IEntityItem getEntity(@NonNull ItemType itemType, @NonNull String identifier, boolean normalize) {
    Map<String, IEntityItem> entityGroup = entityTypeToIdentifierToEntityMap.get(itemType);
    String normalizedIdentifier = normalize ? normalizeIdentifier(identifier) : identifier;
    return entityGroup == null ? null : entityGroup.get(normalizedIdentifier);
  }

  protected IEntityItem addItem(@NonNull IEntityItem item) {
    IEntityItem.ItemType type = item.getItemType();

    @SuppressWarnings("PMD.UseConcurrentHashMap") // need ordering
    Map<String, IEntityItem> entityGroup = entityTypeToIdentifierToEntityMap.computeIfAbsent(
        type,
        key -> Collections.synchronizedMap(new LinkedHashMap<>()));
    IEntityItem oldEntity = entityGroup.put(item.getIdentifier(), item);

    if (oldEntity != null && LOGGER.isWarnEnabled()) {
      LOGGER.atWarn().log("Duplicate {} found with identifier {} in index.",
          oldEntity.getItemType().name().toLowerCase(Locale.ROOT),
          oldEntity.getIdentifier());
    }
    return oldEntity;
  }

  @NonNull
  protected IEntityItem addItem(@NonNull AbstractEntityItem.Builder builder) {
    IEntityItem retval = builder.build();
    addItem(retval);
    return retval;
  }

  @Override
  public boolean removeItem(@NonNull IEntityItem entity) {
    IEntityItem.ItemType type = entity.getItemType();
    Map<String, IEntityItem> entityGroup = entityTypeToIdentifierToEntityMap.get(type);

    boolean retval = false;
    if (entityGroup != null) {
      retval = entityGroup.remove(entity.getIdentifier(), entity);

      // remove if present
      nodeItemToSelectionStatusMap.remove(entity.getInstance());

      if (retval) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.atDebug().log("Removing {} '{}' from index.", type.name(), entity.getIdentifier());
        }
      } else if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("The {} entity '{}' was not found in the index to remove.",
            type.name(),
            entity.getIdentifier());
      }
    }
    return retval;
  }

  @Override
  public IEntityItem addRole(IModelNodeItem<?, ?> item) {
    Role role = ObjectUtils.requireNonNull((Role) item.getValue());
    String identifier = ObjectUtils.requireNonNull(role.getId());

    return addItem(newBuilder(item, ItemType.ROLE, identifier));
  }

  @Override
  public IEntityItem addLocation(IModelNodeItem<?, ?> item) {
    Location location = ObjectUtils.requireNonNull((Location) item.getValue());
    UUID identifier = ObjectUtils.requireNonNull(location.getUuid());

    return addItem(newBuilder(item, ItemType.LOCATION, identifier));
  }

  @Override
  public IEntityItem addParty(IModelNodeItem<?, ?> item) {
    Party party = ObjectUtils.requireNonNull((Party) item.getValue());
    UUID identifier = ObjectUtils.requireNonNull(party.getUuid());

    return addItem(newBuilder(item, ItemType.PARTY, identifier));
  }

  @Override
  public IEntityItem addGroup(IModelNodeItem<?, ?> item) {
    CatalogGroup group = ObjectUtils.requireNonNull((CatalogGroup) item.getValue());
    String identifier = group.getId();
    return identifier == null ? null : addItem(newBuilder(item, ItemType.GROUP, identifier));
  }

  @Override
  public IEntityItem addControl(IModelNodeItem<?, ?> item) {
    Control control = ObjectUtils.requireNonNull((Control) item.getValue());
    String identifier = ObjectUtils.requireNonNull(control.getId());
    return addItem(newBuilder(item, ItemType.CONTROL, identifier));
  }

  @Override
  public IEntityItem addParameter(IModelNodeItem<?, ?> item) {
    Parameter parameter = ObjectUtils.requireNonNull((Parameter) item.getValue());
    String identifier = ObjectUtils.requireNonNull(parameter.getId());

    return addItem(newBuilder(item, ItemType.PARAMETER, identifier));
  }

  @Override
  public IEntityItem addPart(IModelNodeItem<?, ?> item) {
    ControlPart part = ObjectUtils.requireNonNull((ControlPart) item.getValue());
    String identifier = part.getId();

    return identifier == null ? null : addItem(newBuilder(item, ItemType.PART, identifier));
  }

  @Override
  public IEntityItem addResource(IModelNodeItem<?, ?> item) {
    Resource resource = ObjectUtils.requireNonNull((Resource) item.getValue());
    UUID identifier = ObjectUtils.requireNonNull(resource.getUuid());

    return addItem(newBuilder(item, ItemType.RESOURCE, identifier));
  }

  @NonNull
  protected final AbstractEntityItem.Builder newBuilder(
      @NonNull IModelNodeItem<?, ?> item,
      @NonNull ItemType itemType,
      @NonNull UUID identifier) {
    return newBuilder(item, itemType, ObjectUtils.notNull(identifier.toString()));
  }

  /**
   * Create a new builder with the provided info.
   * <p>
   * This method can be overloaded to support applying additional data to the
   * returned builder.
   * <p>
   * When working with identifiers that are case insensitve, it is important to
   * ensure that the identifiers are normalized to lower case.
   *
   * @param item
   *          the Metapath node to associate with the entity
   * @param itemType
   *          the type of entity
   * @param identifier
   *          the entity's identifier
   * @return the entity builder
   */
  @NonNull
  protected AbstractEntityItem.Builder newBuilder(
      @NonNull IModelNodeItem<?, ?> item,
      @NonNull ItemType itemType,
      @NonNull String identifier) {
    return new AbstractEntityItem.Builder()
        .instance(item, itemType)
        .originalIdentifier(identifier)
        .source(ObjectUtils.requireNonNull(item.getBaseUri(), "item must have an associated URI"));
  }

  /**
   * Lower case UUID-based identifiers and leave others unmodified.
   *
   * @param identifier
   *          the identifier
   * @return the resulting normalized identifier
   */
  @NonNull
  public String normalizeIdentifier(@NonNull String identifier) {
    return UuidAdapter.UUID_PATTERN.matcher(identifier).matches()
        ? ObjectUtils.notNull(identifier.toLowerCase(Locale.ROOT))
        : identifier;
  }
  //
  // private static class ItemGroup {
  // @NonNull
  // private final ItemType itemType;
  // Map<String, IEntityItem> idToEntityMap;
  //
  // public ItemGroup(@NonNull ItemType itemType) {
  // this.itemType = itemType;
  // this.idToEntityMap = new LinkedHashMap<>();
  // }
  //
  // public IEntityItem getEntity(@NonNull String identifier) {
  // return idToEntityMap.get(identifier);
  // }
  //
  // @SuppressWarnings("null")
  // @NonNull
  // public Collection<IEntityItem> getEntities() {
  // return idToEntityMap.values();
  // }
  //
  // public IEntityItem add(@NonNull IEntityItem entity) {
  // assert itemType.equals(entity.getItemType());
  // return idToEntityMap.put(entity.getOriginalIdentifier(), entity);
  // }
  // }
}
