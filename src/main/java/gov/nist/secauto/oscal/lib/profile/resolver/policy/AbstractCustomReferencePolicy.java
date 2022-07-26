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

import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractCustomReferencePolicy<TYPE> implements ICustomReferencePolicy<TYPE> {
  private static final Logger LOGGER = LogManager.getLogger(AbstractCustomReferencePolicy.class);

  @Nonnull
  private final IIdentifierParser identifierParser;

  protected AbstractCustomReferencePolicy(
      @Nonnull IIdentifierParser identifierParser) {
    this.identifierParser = identifierParser;
  }

  @Override
  @Nonnull
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
  @Nonnull
  protected abstract List<@Nonnull ItemType> getEntityItemTypes(@Nonnull TYPE reference);

  protected boolean handleIndexHit(
      @Nonnull TYPE reference,
      @Nonnull EntityItem item,
      @Nonnull IReferenceVisitor visitor) {

    if (item.isSelected(visitor.getIndex())) {
      if (item.getReferenceCount() == 0 && !item.isResolved()) {
        // this referenced item will need to be resolved
        item.accept(visitor);
      }
      item.incrementReferenceCount();

      if (item.isIdentifierChanged()) {
        String referenceText = ObjectUtils.notNull(getReferenceText(reference));
        String newReferenceText = getIdentifierParser().update(referenceText, item.getIdentifier());
        setReferenceText(reference, newReferenceText);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.atDebug().log("Mapping {} reference '{}' to '{}'.", item.getItemType().name(), referenceText,
              newReferenceText);
        }
      }
      handleSelected(reference, item, visitor);
    } else {
      handleUnselected(reference, item, visitor);
    }
    return true;
  }

  protected void handleUnselected( // NOPMD - intentional
      @Nonnull TYPE reference,
      @Nonnull EntityItem item,
      @Nonnull IReferenceVisitor visitor) {
    // do nothing by default
  }

  protected void handleSelected( // NOPMD - intentional
      @Nonnull TYPE reference,
      @Nonnull EntityItem item,
      @Nonnull IReferenceVisitor visitor) {
    // do nothing by default
  }

  protected boolean handleIndexMiss(
      @Nonnull TYPE reference,
      @Nonnull List<@Nonnull ItemType> itemTypes,
      @Nonnull String identifier,
      @Nonnull IReferenceVisitor visitor) {
    // provide no handler by default
    return false;
  }

  protected boolean handleIdentifierNonMatch(
      @Nonnull TYPE reference,
      @Nonnull IReferenceVisitor visitor) {
    // provide no handler by default
    return false;
  }

  @Override
  public boolean handleReference(@Nonnull TYPE type, @Nonnull IReferenceVisitor visitor) {
    String referenceText = getReferenceText(type);

    // if the reference text does not exist, ignore the reference; otherwise, handle it.
    return referenceText == null || handleIdentifier(type, getIdentifierParser().parse(referenceText), visitor);
  }

  protected boolean handleIdentifier(
      @Nonnull TYPE type,
      @Nullable String identifier,
      @Nonnull IReferenceVisitor visitor) {
    boolean retval;
    if (identifier == null) {
      retval = handleIdentifierNonMatch(type, visitor);
    } else {
      List<@Nonnull ItemType> itemTypes = getEntityItemTypes(type);
      EntityItem item = null;
      for (ItemType itemType : itemTypes) {
        item = visitor.getIndex().getEntity(itemType, identifier);
        if (item != null) {
          break;
        }
      }

      if (item == null) {
        retval = handleIndexMiss(type, itemTypes, identifier, visitor);
      } else {
        retval = handleIndexHit(type, item, visitor);
      }
    }
    return retval;
  }
}
