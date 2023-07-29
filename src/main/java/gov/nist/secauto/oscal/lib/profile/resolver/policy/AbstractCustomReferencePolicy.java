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

import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionEvaluationException;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class AbstractCustomReferencePolicy<TYPE> implements ICustomReferencePolicy<TYPE> {
  private static final Logger LOGGER = LogManager.getLogger(AbstractCustomReferencePolicy.class);

  @NonNull
  private final IIdentifierParser identifierParser;

  protected AbstractCustomReferencePolicy(
      @NonNull IIdentifierParser identifierParser) {
    this.identifierParser = identifierParser;
  }

  @Override
  @NonNull
  public IIdentifierParser getIdentifierParser() {
    return identifierParser;
  }

  /**
   * Get the possible item types that can be searched in the order in which the identifier will be
   * looked up.
   * <p>
   * The {@code reference} object is provided to allow for context sensitive item type tailoring.
   *
   * @param reference
   *          the reference object
   * @return a list of item types to search for
   */
  @NonNull
  protected abstract List<IEntityItem.ItemType> getEntityItemTypes(@NonNull TYPE reference);

  /**
   * Handle an index hit.
   *
   * @param contextItem
   *          the node containing the identifier reference
   * @param reference
   *          the identifier reference object generating the hit
   * @param item
   *          the referenced item
   * @param visitorContext
   *          the reference visitor state, which can be used for further processing
   * @return {@code true} if the hit was handled or {@code false} otherwise
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the index hit
   */
  protected boolean handleIndexHit(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull TYPE reference,
      @NonNull IEntityItem item,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {

    if (visitorContext.getIndexer().isSelected(item)) {
      if (!visitorContext.isResolved(item)) {
        // this referenced item will need to be resolved
        ReferenceCountingVisitor.instance().resolveEntity(item, visitorContext);
      }
      item.incrementReferenceCount();

      if (item.isIdentifierReassigned()) {
        String referenceText = ObjectUtils.notNull(getReferenceText(reference));
        String newReferenceText = getIdentifierParser().update(referenceText, item.getIdentifier());
        setReferenceText(reference, newReferenceText);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.atDebug().log("Mapping {} reference '{}' to '{}'.", item.getItemType().name(), referenceText,
              newReferenceText);
        }
      }
      handleSelected(contextItem, reference, item, visitorContext);
    } else {
      handleUnselected(contextItem, reference, item, visitorContext);
    }
    return true;
  }

  /**
   * Handle an index hit against an item related to an unselected control.
   * <p>
   * Subclasses can override this method to perform extra processing.
   *
   * @param contextItem
   *          the node containing the identifier reference
   * @param reference
   *          the identifier reference object generating the hit
   * @param item
   *          the referenced item
   * @param visitorContext
   *          the reference visitor, which can be used for further processing
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the index hit
   */
  protected void handleUnselected( // NOPMD noop default
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull TYPE reference,
      @NonNull IEntityItem item,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    // do nothing by default
  }

  /**
   * Handle an index hit against an item related to an selected control.
   * <p>
   * Subclasses can override this method to perform extra processing.
   *
   * @param contextItem
   *          the node containing the identifier reference
   * @param reference
   *          the identifier reference object generating the hit
   * @param item
   *          the referenced item
   * @param visitorContext
   *          the reference visitor state, which can be used for further processing
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the index hit
   */
  protected void handleSelected( // NOPMD noop default
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull TYPE reference,
      @NonNull IEntityItem item,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    // do nothing by default
  }

  /**
   * Handle an index miss for a reference. This occurs when the referenced item was not found in the
   * index.
   * <p>
   * Subclasses can override this method to perform extra processing.
   *
   * @param contextItem
   *          the node containing the identifier reference
   * @param reference
   *          the identifier reference object generating the hit
   * @param itemTypes
   *          the possible item types for this reference
   * @param identifier
   *          the parsed identifier
   * @param visitorContext
   *          the reference visitor state, which can be used for further processing
   * @return {@code true} if the reference is handled by this method or {@code false} otherwise
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the index miss
   */
  protected boolean handleIndexMiss(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull TYPE reference,
      @NonNull List<IEntityItem.ItemType> itemTypes,
      @NonNull String identifier,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    // provide no handler by default
    return false;
  }

  /**
   * Handle the case where the identifier was not a syntax match for an expected identifier. This can
   * occur when the reference is malformed, using an unrecognized syntax.
   * <p>
   * Subclasses can override this method to perform extra processing.
   *
   * @param contextItem
   *          the node containing the identifier reference
   * @param reference
   *          the identifier reference object generating the hit
   * @param visitorContext
   *          the reference visitor state, which can be used for further processing
   * @return {@code true} if the reference is handled by this method or {@code false} otherwise
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the index miss due to a non match
   */
  protected boolean handleIdentifierNonMatch(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull TYPE reference,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    // provide no handler by default
    return false;
  }

  @Override
  public boolean handleReference(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull TYPE type,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    String referenceText = getReferenceText(type);

    // if the reference text does not exist, ignore the reference; otherwise, handle it.
    return referenceText == null
        || handleIdentifier(contextItem, type, getIdentifierParser().parse(referenceText), visitorContext);
  }

  /**
   * Handle the provided {@code identifier} for a given {@code type} of reference.
   *
   * @param contextItem
   *          the node containing the identifier reference
   * @param type
   *          the item type of the reference
   * @param identifier
   *          the identifier
   * @param visitorContext
   *          the reference visitor state, which can be used for further processing
   * @return {@code true} if the reference is handled by this method or {@code false} otherwise
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the reference
   */
  protected boolean handleIdentifier(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull TYPE type,
      @Nullable String identifier,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    boolean retval;
    if (identifier == null) {
      retval = handleIdentifierNonMatch(contextItem, type, visitorContext);
    } else {
      List<IEntityItem.ItemType> itemTypes = getEntityItemTypes(type);
      IEntityItem item = null;
      for (IEntityItem.ItemType itemType : itemTypes) {
        assert itemType != null;

        item = visitorContext.getEntity(itemType, identifier);
        if (item != null) {
          break;
        }
      }

      if (item == null) {
        retval = handleIndexMiss(contextItem, type, itemTypes, identifier, visitorContext);
      } else {
        retval = handleIndexHit(contextItem, type, item, visitorContext);
      }
    }
    return retval;
  }
}
