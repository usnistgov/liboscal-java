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
