/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Used to visit a catalog containing groups and controls.
 *
 * @param <T>
 *          the type of the context object used to pass calling context
 *          information
 * @param <R>
 *          the type of the result for visiting a collection of groups and/or
 *          controls
 */
public interface ICatalogVisitor<T, R> {

  /**
   * Called when visiting a group.
   * <p>
   * Can be overridden by classes extending this interface to support processing
   * of the visited object.
   *
   * @param group
   *          the Metapath item for the group
   * @param childResult
   *          the result of evaluating the group's children
   * @param state
   *          the calling context information
   * @return a meaningful result of the given type
   */
  default R visitGroup(@NonNull IAssemblyNodeItem group, R childResult, T state) {
    // do nothing by default
    return childResult;
  }

  /**
   * Called when visiting a control.
   * <p>
   * Can be overridden by classes extending this interface to support processing
   * of the visited object.
   *
   * @param control
   *          the Metapath item for the control
   * @param childResult
   *          the result of evaluating the control's children
   * @param state
   *          the calling context information
   * @return a meaningful result of the given type
   */
  default R visitControl(@NonNull IAssemblyNodeItem control, R childResult, T state) {
    // do nothing by default
    return childResult;
  }
}
