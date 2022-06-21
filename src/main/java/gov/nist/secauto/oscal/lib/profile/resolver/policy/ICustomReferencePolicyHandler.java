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

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICustomReferencePolicyHandler<TYPE> {
  @NotNull
  ICustomReferencePolicyHandler<?> IGNORE_INDEX_MISS_POLICY = new AbstractIndexMissPolicyHandler<>() {
    @Override
    public boolean handleIndexMiss(
        @NotNull ICustomReferencePolicy<Object> policy,
        @NotNull Object type,
        @NotNull List<@NotNull ItemType> itemTypes,
        @NotNull String identifier,
        @NotNull IReferenceVisitor visitor) {
      // do nothing
      return true;
    }
  };

  /**
   * A callback used to handle the case where an identifier could not be parsed from the reference
   * text.
   * 
   * @param policy
   *          the reference policy for this reference
   * @param reference
   *          the reference object
   * @param visitor
   *          the reference visitor used to resolve referenced objects
   * @return {@code true} if the reference is considered handled, or {@code false} otherwise
   */
  default boolean handleIdentifierNonMatch(
      @NotNull ICustomReferencePolicy<TYPE> policy,
      @NotNull TYPE reference,
      @NotNull IReferenceVisitor visitor) {
    return false;
  }

  /**
   * A callback used to handle the case where an identifier could be parsed from the reference text,
   * but the index didn't contain a matching entity.
   * 
   * @param policy
   *          the reference policy for this reference
   * @param reference
   *          the reference object
   * @param itemTypes
   *          the item types that were checked
   * @param identifier
   *          the parsed identifier
   * @param visitor
   *          the reference visitor used to resolve referenced objects
   * @return {@code true} if the reference is considered handled, or {@code false} otherwise
   */
  default boolean handleIndexMiss(
      @NotNull ICustomReferencePolicy<TYPE> policy,
      @NotNull TYPE reference,
      @NotNull List<@NotNull ItemType> itemTypes,
      @NotNull String identifier,
      @NotNull IReferenceVisitor visitor) {
    return false;
  }

  /**
   * A callback used to handle the case where an identifier could be parsed and the index contains a
   * matching entity.
   * 
   * @param policy
   *          the reference policy for this reference
   * @param reference
   *          the reference object
   * @param item
   *          the entity that is referenced
   * @param visitor
   *          the reference visitor used to resolve referenced objects
   * @return {@code true} if the reference is considered handled, or {@code false} otherwise
   */
  default boolean handleIndexHit(
      @NotNull ICustomReferencePolicy<TYPE> policy,
      @NotNull TYPE reference,
      @NotNull EntityItem item,
      @NotNull IReferenceVisitor visitor) {
    return false;
  }
}
