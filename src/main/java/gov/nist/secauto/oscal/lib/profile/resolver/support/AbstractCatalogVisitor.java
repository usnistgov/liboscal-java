/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalModelConstants;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Used to visit a catalog containing groups and controls.
 *
 * @param <T>
 *          the type of the state object used to pass calling context
 *          information
 * @param <R>
 *          the type of the result for visiting a collection of groups and/or
 *          controls
 */
public abstract class AbstractCatalogVisitor<T, R> implements ICatalogVisitor<T, R> {

  protected abstract R newDefaultResult(T state);

  protected abstract R aggregateResults(R first, R second, T state);

  protected R visitCatalog(@NonNull IDocumentNodeItem catalogDocument, T state) {
    return catalogDocument.modelItems().reduce(
        newDefaultResult(state),
        (result, catalogOrGroup) -> visitGroupContainer(
            ObjectUtils.requireNonNull((IAssemblyNodeItem) catalogOrGroup), result, state),
        (result1, result2) -> aggregateResults(result1, result2, state));
  }

  /**
   * Visit the child groups and controls (in that order) of a given catalog or
   * group container.
   *
   * @param catalogOrGroup
   *          the catalog or group Metapath item currently being visited
   * @param initialResult
   *          the initial result value to use when aggregating child results
   * @param state
   *          the current visitor state
   * @return a meaningful result of the given type
   */
  protected R visitGroupContainer(
      @NonNull IAssemblyNodeItem catalogOrGroup,
      R initialResult,
      T state) {
    R result = catalogOrGroup.getModelItemsByName(OscalModelConstants.QNAME_GROUP).stream()
        .map(groupItem -> {
          return visitGroupItem(
              ObjectUtils.requireNonNull((IAssemblyNodeItem) groupItem),
              state);
        })
        .reduce(initialResult, (first, second) -> aggregateResults(first, second, state));
    return visitControlContainer(catalogOrGroup, result, state);
  }

  /**
   * Called when visiting a group.
   * <p>
   * This method will first visit the group's children, then the group itself.
   *
   * @param group
   *          the group Metapath item to visit
   * @param state
   *          the current visitor state
   * @return a meaningful result of the given type
   */
  protected R visitGroupItem(@NonNull IAssemblyNodeItem group, T state) {
    R childResult = visitGroupContainer(group, newDefaultResult(state), state);
    return visitGroupInternal(group, childResult, state);
  }

  /**
   * Called when visiting a group after visiting it's children.
   *
   * @param group
   *          the group Metapath item currently being visited
   * @param childResult
   *          the result of visiting the group's children
   * @param state
   *          the current visitor state
   * @return a meaningful result of the given type
   */
  protected R visitGroupInternal(
      @NonNull IAssemblyNodeItem group,
      R childResult,
      T state) {
    return visitGroup(group, childResult, state);
  }

  /**
   * Visit the child controls (in that order) of a given catalog, group, or
   * control container.
   *
   * @param catalogOrGroupOrControl
   *          the catalog, group, or control Metapath item currently being visited
   * @param initialResult
   *          the initial result value to use when aggregating child results
   * @param state
   *          the current visitor state
   * @return a meaningful result of the given type
   */
  protected R visitControlContainer(
      @NonNull IAssemblyNodeItem catalogOrGroupOrControl,
      R initialResult,
      T state) {
    return catalogOrGroupOrControl.getModelItemsByName(OscalModelConstants.QNAME_CONTROL).stream()
        .map(control -> {
          return visitControlItem(ObjectUtils.requireNonNull((IAssemblyNodeItem) control), state);
        })
        .reduce(initialResult, (first, second) -> aggregateResults(first, second, state));
  }

  /**
   * Called when visiting a control.
   * <p>
   * This method will first visit the control's children, then the control itself.
   *
   * @param control
   *          the control Metapath item to visit
   * @param state
   *          the current visitor state
   * @return a meaningful result of the given type
   */
  protected R visitControlItem(@NonNull IAssemblyNodeItem control, T state) {
    R childResult = visitControlContainer(control, newDefaultResult(state), state);
    return visitControlInternal(control, childResult, state);
  }

  /**
   * Called when visiting a control after visiting it's children.
   *
   * @param control
   *          the Metapath item for the control currently being visited
   * @param childResult
   *          the result of visiting the control's children
   * @param state
   *          the calling context information
   * @return a meaningful result of the given type
   */
  protected R visitControlInternal(@NonNull IAssemblyNodeItem control, R childResult, T state) {
    return visitControl(control, childResult, state);
  }
}
