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

import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.Index;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.IReferencePolicy.NonMatchPolicy;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractReferencePolicy<TYPE> implements IReferencePolicy<TYPE> {

  @NotNull
  private final IIdentifierParser identifierParser;
  @NotNull
  private final NonMatchPolicy nonMatchPolicy;

  @SuppressWarnings("null")
  protected AbstractReferencePolicy(@NotNull IIdentifierParser identifierParser,
      @NotNull NonMatchPolicy nonMatchPolicy) {
    this.identifierParser = Objects.requireNonNull(identifierParser, "identifierParser");
    this.nonMatchPolicy = Objects.requireNonNull(nonMatchPolicy, "nonMatchPolicy");
  }

  @NotNull
  public IIdentifierParser getIdentifierParser() {
    return identifierParser;
  }

  @NotNull
  protected abstract ItemType getEntityItemType(@NotNull TYPE type);

  protected abstract String getReference(@NotNull TYPE type);

  protected boolean handleMatch(EntityItem item, @NotNull TYPE type, @NotNull Index index) {
    item.incrementReferenceCount();
    return true;
  }

  protected boolean handleNonMatch(@NotNull TYPE type, @NotNull ItemType itemType, @NotNull String identifier,
      @NotNull Index index) {
    boolean retval = false;
    switch (nonMatchPolicy) {
    case IGNORE:
      retval = true;
      break;
    case ERROR:
      retval = handleNonMatchError(type, itemType, identifier, index);
      break;
    case WARN:
      retval = handleNonMatchWarning(type, itemType, identifier, index);
      break;
    default:
      break;
    }
    return retval;
  }

  protected abstract boolean handleNonMatchWarning(@NotNull TYPE type, @NotNull ItemType itemType,
      @NotNull String identifier,
      @NotNull Index index);

  protected abstract boolean handleNonMatchError(@NotNull TYPE type, @NotNull ItemType itemType,
      @NotNull String identifier, @NotNull Index index);

  public boolean handleReference(@NotNull TYPE type, @NotNull Index index) {
    String reference = getReference(type);

    boolean handled = false;
    if (reference != null) {
      Pair<@NotNull Boolean, @NotNull String> result = getIdentifierParser().match(reference);
      if (result.getLeft()) {
        @SuppressWarnings("null")
        String identifier = result.getRight();
  
        ItemType itemType = getEntityItemType(type);
        EntityItem item = index.getEntity(itemType, identifier);
        if (item != null) {
          handled = handleMatch(item, type, index);
        } else {
          // enforce the non-match policy
          handled = handleNonMatch(type, itemType, identifier, index);
        }
      } // else ignore
    }

    return handled;
  }
}
