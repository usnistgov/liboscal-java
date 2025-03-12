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

package gov.nist.secauto.oscal.lib.profile.resolver.alter;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.model.control.catalog.ICatalogVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionEvaluationException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class AddVisitor implements ICatalogVisitor<Boolean, AddVisitor.Context> {
  public enum TargetType {
    CONTROL("control", Control.class),
    PARAM("param", Parameter.class),
    PART("part", ControlPart.class);

    @NonNull
    private static final Map<Class<?>, TargetType> CLASS_TO_TYPE;
    @NonNull
    private static final Map<String, TargetType> NAME_TO_TYPE;
    @NonNull
    private final String fieldName;
    @NonNull
    private final Class<?> clazz;

    static {
      {
        Map<Class<?>, TargetType> map = new ConcurrentHashMap<>();
        for (TargetType type : values()) {
          map.put(type.getClazz(), type);
        }
        CLASS_TO_TYPE = CollectionUtil.unmodifiableMap(map);
      }

      {
        Map<String, TargetType> map = new ConcurrentHashMap<>();
        for (TargetType type : values()) {
          map.put(type.fieldName(), type);
        }
        NAME_TO_TYPE = CollectionUtil.unmodifiableMap(map);
      }
    }

    /**
     * Get the target type associated with the provided {@code clazz}.
     *
     * @param clazz
     *          the class to identify the target type for
     * @return the associated target type or {@code null} if the class is not
     *         associated with a target type
     */
    @Nullable
    public static TargetType forClass(@NonNull Class<?> clazz) {
      Class<?> target = clazz;
      TargetType retval;
      // recurse over parent classes to find a match
      do {
        retval = CLASS_TO_TYPE.get(target);
      } while (retval == null && (target = target.getSuperclass()) != null);
      return retval;
    }

    /**
     * Get the target type associated with the provided field {@code name}.
     *
     * @param name
     *          the field name to identify the target type for
     * @return the associated target type or {@code null} if the name is not
     *         associated with a target type
     */
    @Nullable
    public static TargetType forFieldName(@Nullable String name) {
      return name == null ? null : NAME_TO_TYPE.get(name);
    }

    TargetType(@NonNull String fieldName, @NonNull Class<?> clazz) {
      this.fieldName = fieldName;
      this.clazz = clazz;
    }

    /**
     * Get the field name associated with the target type.
     *
     * @return the name
     */
    public String fieldName() {
      return fieldName;
    }

    /**
     * Get the bound class associated with the target type.
     *
     * @return the class
     */
    public Class<?> getClazz() {
      return clazz;
    }
  }

  public enum Position {
    BEFORE,
    AFTER,
    STARTING,
    ENDING;

    @NonNull
    private static final Map<String, Position> NAME_TO_POSITION;

    static {
      Map<String, Position> map = new ConcurrentHashMap<>();
      for (Position position : values()) {
        map.put(position.name().toLowerCase(Locale.ROOT), position);
      }
      NAME_TO_POSITION = CollectionUtil.unmodifiableMap(map);
    }

    /**
     * Get the position associated with the provided {@code name}.
     *
     * @param name
     *          the name to identify the position for
     * @return the associated position or {@code null} if the name is not associated
     *         with a position
     */
    @Nullable
    public static Position forName(@Nullable String name) {
      return name == null ? null : NAME_TO_POSITION.get(name);
    }
  }

  @NonNull
  private static final AddVisitor INSTANCE = new AddVisitor();
  private static final Map<TargetType, Set<TargetType>> APPLICABLE_TARGETS;

  static {
    APPLICABLE_TARGETS = new EnumMap<>(TargetType.class);
    APPLICABLE_TARGETS.put(TargetType.CONTROL, Set.of(TargetType.CONTROL, TargetType.PARAM, TargetType.PART));
    APPLICABLE_TARGETS.put(TargetType.PARAM, Set.of(TargetType.PARAM));
    APPLICABLE_TARGETS.put(TargetType.PART, Set.of(TargetType.PART));
  }

  private static Set<TargetType> getApplicableTypes(@NonNull TargetType type) {
    return APPLICABLE_TARGETS.getOrDefault(type, CollectionUtil.emptySet());
  }

  /**
   * Apply the add directive.
   *
   * @param control
   *          the control target
   * @param position
   *          the position to apply the content or {@code null}
   * @param byId
   *          the identifier of the target or {@code null}
   * @param title
   *          a title to set
   * @param params
   *          parameters to add
   * @param props
   *          properties to add
   * @param links
   *          links to add
   * @param parts
   *          parts to add
   * @return {@code true} if the modification was made or {@code false} otherwise
   * @throws ProfileResolutionEvaluationException
   *           if a processing error occurred during profile resolution
   */
  public static boolean add(
      @NonNull Control control,
      @Nullable Position position,
      @Nullable String byId,
      @Nullable MarkupLine title,
      @NonNull List<Parameter> params,
      @NonNull List<Property> props,
      @NonNull List<Link> links,
      @NonNull List<ControlPart> parts) {
    return INSTANCE.visitControl(
        control,
        new Context(
            control,
            position == null ? Position.ENDING : position,
            byId,
            title,
            params,
            props,
            links,
            parts));
  }

  @Override
  public Boolean visitCatalog(Catalog catalog, Context context) {
    // not required
    throw new UnsupportedOperationException("not needed");
  }

  @Override
  public Boolean visitGroup(CatalogGroup group, Context context) {
    // not required
    throw new UnsupportedOperationException("not needed");
  }

  /**
   * If the add applies to the current object, then apply the child objects.
   * <p>
   * An add applies if:
   * <ol>
   * <li>the {@code targetItem} supports all of the children</li>
   * <li>the context matches if:
   * <ul>
   * <li>the target item's id matches the "by-id"; or</li>
   * <li>the "by-id" is not defined and the target item is the control matching
   * the target context</li>
   * </ul>
   * </li>
   * </ol>
   *
   * @param <T>
   *          the type of the {@code targetItem}
   * @param targetItem
   *          the current target to process
   * @param titleConsumer
   *          a consumer to apply a title to or {@code null} if the object has no
   *          title field
   * @param paramsSupplier
   *          a supplier for the child {@link Parameter} collection
   * @param propsSupplier
   *          a supplier for the child {@link Property} collection
   * @param linksSupplier
   *          a supplier for the child {@link Link} collection
   * @param partsSupplier
   *          a supplier for the child {@link ControlPart} collection
   * @param context
   *          the add context
   * @return {@code true} if a modification was made or {@code false} otherwise
   */
  private static <T> boolean handleCurrent(
      @NonNull T targetItem,
      @Nullable Consumer<MarkupLine> titleConsumer,
      @Nullable Supplier<? extends List<Parameter>> paramsSupplier,
      @Nullable Supplier<? extends List<Property>> propsSupplier,
      @Nullable Supplier<? extends List<Link>> linksSupplier,
      @Nullable Supplier<? extends List<ControlPart>> partsSupplier,
      @NonNull Context context) {
    boolean retval = false;
    Position position = context.getPosition();
    if (context.appliesTo(targetItem) && !context.isSequenceTargeted(targetItem)) {
      // the target item is the target of the add
      MarkupLine newTitle = context.getTitle();
      if (newTitle != null) {
        assert titleConsumer != null;
        titleConsumer.accept(newTitle);
      }

      handleCollection(position, context.getParams(), paramsSupplier);
      handleCollection(position, context.getProps(), propsSupplier);
      handleCollection(position, context.getLinks(), linksSupplier);
      handleCollection(position, context.getParts(), partsSupplier);
      retval = true;
    }
    return retval;
  }

  private static <T> void handleCollection(
      @NonNull Position position,
      @NonNull List<T> newItems,
      @Nullable Supplier<? extends List<T>> originalCollectionSupplier) {
    if (originalCollectionSupplier != null) {
      List<T> oldItems = originalCollectionSupplier.get();
      if (!newItems.isEmpty()) {
        if (Position.STARTING.equals(position)) {
          oldItems.addAll(0, newItems);
        } else { // ENDING
          oldItems.addAll(newItems);
        }
      }
    }
  }

  // private static <T> void handleChild(
  // @NonNull TargetType itemType,
  // @NonNull Supplier<? extends List<T>> collectionSupplier,
  // @Nullable Consumer<T> handler,
  // @NonNull Context context) {
  // boolean handleChildren = !Collections.disjoint(context.getTargetItemTypes(),
  // getApplicableTypes(itemType));
  // if (handleChildren && handler != null) {
  // // if the child item type is applicable and there is a handler, iterate over
  // children
  // Iterator<T> iter = collectionSupplier.get().iterator();
  // while (iter.hasNext()) {
  // T item = iter.next();
  // if (item != null) {
  // handler.accept(item);
  // }
  // }
  // }
  // }

  private static <T> boolean handleChild(
      @NonNull TargetType itemType,
      @NonNull Supplier<? extends List<T>> originalCollectionSupplier,
      @NonNull Supplier<? extends List<T>> newItemsSupplier,
      @Nullable Function<T, Boolean> handler,
      @NonNull Context context) {

    // determine if this child type can match
    boolean isItemTypeMatch = context.isMatchingType(itemType);

    Set<TargetType> applicableTypes = getApplicableTypes(itemType);
    boolean descendChild = handler != null && !Collections.disjoint(context.getTargetItemTypes(), applicableTypes);

    boolean retval = false;
    if (isItemTypeMatch || descendChild) {
      // if the item type is applicable, attempt to match by id
      List<T> collection = originalCollectionSupplier.get();
      ListIterator<T> iter = collection.listIterator();
      boolean deferred = false;
      while (iter.hasNext()) {
        T item = ObjectUtils.requireNonNull(iter.next());

        if (isItemTypeMatch && context.appliesTo(item) && context.isSequenceTargeted(item)) {
          // if id match, inject the new items into the collection
          switch (context.getPosition()) {
          case AFTER: {
            newItemsSupplier.get().forEach(iter::add);
            retval = true;
            break;
          }
          case BEFORE: {
            iter.previous();
            List<T> adds = newItemsSupplier.get();
            adds.forEach(iter::add);
            item = iter.next();
            retval = true;
            break;
          }
          case STARTING:
          case ENDING:
            deferred = true;
            break;
          default:
            throw new UnsupportedOperationException(context.getPosition().name().toLowerCase(Locale.ROOT));
          }
        }

        if (descendChild) {
          assert handler != null;

          // handle child items since they are applicable to the search criteria
          retval = retval || handler.apply(item);
        }
      }

      if (deferred) {
        List<T> newItems = newItemsSupplier.get();
        if (Position.ENDING.equals(context.getPosition())) {
          collection.addAll(newItems);
          retval = true;
        } else if (Position.STARTING.equals(context.getPosition())) {
          collection.addAll(0, newItems);
          retval = true;
        }
      }
    }
    return retval;
  }

  @Override
  public Boolean visitControl(Control control, Context context) {
    assert context != null;

    if (control.getParams() == null) {
      control.setParams(new LinkedList<>());
    }

    if (control.getProps() == null) {
      control.setProps(new LinkedList<>());
    }

    if (control.getLinks() == null) {
      control.setLinks(new LinkedList<>());
    }

    if (control.getParts() == null) {
      control.setParts(new LinkedList<>());
    }

    boolean retval = handleCurrent(
        control,
        control::setTitle,
        control::getParams,
        control::getProps,
        control::getLinks,
        control::getParts,
        context);

    // visit params
    retval = retval || handleChild(
        TargetType.PARAM,
        control::getParams,
        context::getParams,
        child -> visitParameter(ObjectUtils.notNull(child), context),
        context);

    // visit parts
    retval = retval || handleChild(
        TargetType.PART,
        control::getParts,
        context::getParts,
        child -> visitPart(child, context),
        context);

    // visit control children
    for (Control childControl : CollectionUtil.listOrEmpty(control.getControls())) {
      Set<TargetType> applicableTypes = getApplicableTypes(TargetType.CONTROL);
      if (!Collections.disjoint(context.getTargetItemTypes(), applicableTypes)) {
        retval = retval || visitControl(ObjectUtils.requireNonNull(childControl), context);
      }
    }
    return retval;
  }

  @Override
  public Boolean visitParameter(Parameter parameter, Context context) {
    assert context != null;
    if (parameter.getProps() == null) {
      parameter.setProps(new LinkedList<>());
    }

    if (parameter.getLinks() == null) {
      parameter.setLinks(new LinkedList<>());
    }

    return handleCurrent(
        parameter,
        null,
        null,
        parameter::getProps,
        parameter::getLinks,
        null,
        context);
  }

  /**
   * Visit the control part.
   *
   * @param part
   *          the bound part object
   * @param context
   *          the visitor context
   * @return {@code true} if the removal was applied or {@code false} otherwise
   */
  public boolean visitPart(ControlPart part, Context context) {
    assert context != null;
    if (part.getProps() == null) {
      part.setProps(new LinkedList<>());
    }

    if (part.getLinks() == null) {
      part.setLinks(new LinkedList<>());
    }

    if (part.getParts() == null) {
      part.setParts(new LinkedList<>());
    }

    boolean retval = handleCurrent(
        part,
        null,
        null,
        part::getProps,
        part::getLinks,
        part::getParts,
        context);

    return retval || handleChild(
        TargetType.PART,
        part::getParts,
        context::getParts,
        child -> visitPart(child, context),
        context);
  }

  static class Context {
    @NonNull
    private static final Set<TargetType> TITLE_TYPES = ObjectUtils.notNull(
        Set.of(TargetType.CONTROL, TargetType.PART));
    @NonNull
    private static final Set<TargetType> PARAM_TYPES = ObjectUtils.notNull(
        Set.of(TargetType.CONTROL, TargetType.PARAM));
    @NonNull
    private static final Set<TargetType> PROP_TYPES = ObjectUtils.notNull(
        Set.of(TargetType.CONTROL, TargetType.PARAM, TargetType.PART));
    @NonNull
    private static final Set<TargetType> LINK_TYPES = ObjectUtils.notNull(
        Set.of(TargetType.CONTROL, TargetType.PARAM, TargetType.PART));
    @NonNull
    private static final Set<TargetType> PART_TYPES = ObjectUtils.notNull(
        Set.of(TargetType.CONTROL, TargetType.PART));

    @NonNull
    private final Control control;
    @NonNull
    private final Position position;
    @Nullable
    private final String byId;
    @Nullable
    private final MarkupLine title;
    @NonNull
    private final List<Parameter> params;
    @NonNull
    private final List<Property> props;
    @NonNull
    private final List<Link> links;
    @NonNull
    private final List<ControlPart> parts;
    @NonNull
    private final Set<TargetType> targetItemTypes;

    public Context(
        @NonNull Control control,
        @NonNull Position position,
        @Nullable String byId,
        @Nullable MarkupLine title,
        @NonNull List<Parameter> params,
        @NonNull List<Property> props,
        @NonNull List<Link> links,
        @NonNull List<ControlPart> parts) {

      Set<TargetType> targetItemTypes = ObjectUtils.notNull(EnumSet.allOf(TargetType.class));

      List<String> additionObjects = new LinkedList<>();

      boolean sequenceTarget = true;
      if (title != null) {
        targetItemTypes.retainAll(TITLE_TYPES);
        additionObjects.add("title");
        sequenceTarget = false;
      }

      if (!params.isEmpty()) {
        targetItemTypes.retainAll(PARAM_TYPES);
        additionObjects.add("param");
      }

      if (!props.isEmpty()) {
        targetItemTypes.retainAll(PROP_TYPES);
        additionObjects.add("prop");
        sequenceTarget = false;
      }

      if (!links.isEmpty()) {
        targetItemTypes.retainAll(LINK_TYPES);
        additionObjects.add("link");
        sequenceTarget = false;
      }

      if (!parts.isEmpty()) {
        targetItemTypes.retainAll(PART_TYPES);
        additionObjects.add("part");
      }

      if (Position.BEFORE.equals(position) || Position.AFTER.equals(position)) {
        if (!sequenceTarget) {
          throw new ProfileResolutionEvaluationException(
              "When using position before or after, one collection of parameters or parts can be specified."
                  + " Other additions must not be used.");
        }
        if (sequenceTarget && !params.isEmpty() && parts.isEmpty()) {
          targetItemTypes.retainAll(Set.of(TargetType.PARAM));
        } else if (sequenceTarget && !parts.isEmpty() && params.isEmpty()) {
          targetItemTypes.retainAll(Set.of(TargetType.PART));
        } else {
          throw new ProfileResolutionEvaluationException(
              "When using position before or after, only one collection of parameters or parts can be specified.");
        }
      }

      if (targetItemTypes.isEmpty()) {
        throw new ProfileResolutionEvaluationException("No parent object supports the requested objects to add: " +
            additionObjects.stream().collect(CustomCollectors.joiningWithOxfordComma("or")));
      }

      this.control = control;
      this.position = position;
      this.byId = byId;
      this.title = title;
      this.params = params;
      this.props = props;
      this.links = links;
      this.parts = parts;
      this.targetItemTypes = CollectionUtil.unmodifiableSet(targetItemTypes);
    }

    public Control getControl() {
      return control;
    }

    @NonNull
    public Position getPosition() {
      return position;
    }

    @Nullable
    public String getById() {
      return byId;
    }

    @Nullable
    public MarkupLine getTitle() {
      return title;
    }

    @NonNull
    public List<Parameter> getParams() {
      return params;
    }

    @NonNull
    public List<Property> getProps() {
      return props;
    }

    @NonNull
    public List<Link> getLinks() {
      return links;
    }

    @NonNull
    public List<ControlPart> getParts() {
      return parts;
    }

    @NonNull
    public Set<TargetType> getTargetItemTypes() {
      return targetItemTypes;
    }

    public boolean isMatchingType(@NonNull TargetType type) {
      return getTargetItemTypes().contains(type);
    }

    public <T> boolean isSequenceTargeted(T targetItem) {
      TargetType objectType = TargetType.forClass(targetItem.getClass());
      return (Position.BEFORE.equals(position) || Position.AFTER.equals(position))
          && (TargetType.PARAM.equals(objectType) && isMatchingType(TargetType.PARAM)
              || TargetType.PART.equals(objectType) && isMatchingType(TargetType.PART));
    }

    protected boolean checkValue(@Nullable String actual, @Nullable String expected) {
      return expected == null || expected.equals(actual);
    }

    /**
     * Determine if the provided {@code obj} is the target of the add.
     *
     * @param obj
     *          the current object
     * @return {@code true} if the current object applies or {@code false} otherwise
     */
    public boolean appliesTo(@NonNull Object obj) {
      TargetType objectType = TargetType.forClass(obj.getClass());

      boolean retval = objectType != null && isMatchingType(objectType);
      if (retval) {
        assert objectType != null;

        // check other criteria
        String actualId = null;

        switch (objectType) {
        case CONTROL: {
          Control control = (Control) obj;
          actualId = control.getId();
          break;
        }
        case PARAM: {
          Parameter param = (Parameter) obj;
          actualId = param.getId();
          break;
        }
        case PART: {
          ControlPart part = (ControlPart) obj;
          actualId = part.getId() == null ? null : part.getId();
          break;
        }
        default:
          throw new UnsupportedOperationException(objectType.fieldName());
        }

        String byId = getById();
        if (getById() == null && TargetType.CONTROL.equals(objectType)) {
          retval = getControl().equals(obj);
        } else {
          retval = byId != null && byId.equals(actualId);
        }
      }
      return retval;
    }
  }
}
