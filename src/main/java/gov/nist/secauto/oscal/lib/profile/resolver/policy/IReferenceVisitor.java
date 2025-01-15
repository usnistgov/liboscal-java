/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
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
   * Visit the provided {@code item} representing an OSCAL {@link CatalogGroup}
   * and handle any enclosed references.
   *
   * @param item
   *          the Metapath node item containing reference nodes
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the reference
   */
  @Override
  Void visitGroup(@NonNull IAssemblyNodeItem item, Void childResult, T context);

  /**
   * Visit the provided {@code item} representing an OSCAL {@link Control} and
   * handle any enclosed references.
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
  // * Visit the provided {@code item} representing an OSCAL {@link Parameter} and
  // handle any enclosed
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
  // * Visit the provided {@code item} representing an OSCAL {@link ControlPart}
  // and handle any
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
  // * Visit the provided {@code item} representing an OSCAL {@link Role} and
  // handle any enclosed
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
  // * Visit the provided {@code item} representing an OSCAL {@link Party} and
  // handle any enclosed
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
  // * Visit the provided {@code item} representing an OSCAL {@link Location} and
  // handle any enclosed
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
  // * Visit the provided {@code item} representing an OSCAL {@link Resource} and
  // handle any enclosed
  // * references.
  // *
  // * @param item
  // * the Metapath node item containing reference nodes
  // * @throws ProfileResolutionEvaluationException
  // * if there was an error handing the reference
  // */
  // void resolveResource(@NonNull IModelNodeItem item);
}
