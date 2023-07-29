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

import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionEvaluationException;
import gov.nist.secauto.oscal.lib.profile.resolver.support.ICatalogVisitor;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A visitor used to process references.
 *
 * @param <T>
 *          the visitor context type used to pass state while visiting
 */
public interface IReferenceVisitor<T> extends ICatalogVisitor<T, Void> {

  /**
   * Visit the provided {@code item} representing an OSCAL {@link CatalogGroup} and handle any
   * enclosed references.
   *
   * @param item
   *          the Metapath node item containing reference nodes
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the reference
   */
  @Override
  Void visitGroup(@NonNull IAssemblyNodeItem item, Void childResult, T context);

  /**
   * Visit the provided {@code item} representing an OSCAL {@link Control} and handle any enclosed
   * references.
   *
   * @param item
   *          the Metapath node item containing reference nodes
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the reference
   */
  @Override
  Void visitControl(@NonNull IAssemblyNodeItem item, Void childResult, T context);
  //
  // /**
  // * Visit the provided {@code item} representing an OSCAL {@link Parameter} and handle any enclosed
  // * references.
  // *
  // * @param item
  // * the Metapath node item containing reference nodes
  // * @throws ProfileResolutionEvaluationException
  // * if there was an error handing the reference
  // */
  // void resolveParameter(@NonNull IModelNodeItem item);
  //
  // /**
  // * Visit the provided {@code item} representing an OSCAL {@link ControlPart} and handle any
  // enclosed
  // * references.
  // *
  // * @param item
  // * the Metapath node item containing reference nodes
  // * @throws ProfileResolutionEvaluationException
  // * if there was an error handing the reference
  // */
  // void resolvePart(@NonNull IModelNodeItem item, T context);
  //
  // /**
  // * Visit the provided {@code item} representing an OSCAL {@link Role} and handle any enclosed
  // * references.
  // *
  // * @param item
  // * the Metapath node item containing reference nodes
  // * @throws ProfileResolutionEvaluationException
  // * if there was an error handing the reference
  // */
  // void resolveRole(@NonNull IModelNodeItem item);
  //
  // /**
  // * Visit the provided {@code item} representing an OSCAL {@link Party} and handle any enclosed
  // * references.
  // *
  // * @param item
  // * the Metapath node item containing reference nodes
  // * @throws ProfileResolutionEvaluationException
  // * if there was an error handing the reference
  // */
  // void resolveParty(@NonNull IModelNodeItem item);
  //
  // /**
  // * Visit the provided {@code item} representing an OSCAL {@link Location} and handle any enclosed
  // * references.
  // *
  // * @param item
  // * the Metapath node item containing reference nodes
  // * @throws ProfileResolutionEvaluationException
  // * if there was an error handing the reference
  // */
  // void resolveLocation(@NonNull IModelNodeItem item);
  //
  // /**
  // * Visit the provided {@code item} representing an OSCAL {@link Resource} and handle any enclosed
  // * references.
  // *
  // * @param item
  // * the Metapath node item containing reference nodes
  // * @throws ProfileResolutionEvaluationException
  // * if there was an error handing the reference
  // */
  // void resolveResource(@NonNull IModelNodeItem item);
}
