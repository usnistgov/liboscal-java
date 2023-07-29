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

import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IEntityItem {

  enum ItemType {
    ROLE(false),
    LOCATION(true),
    PARTY(true),
    GROUP(false),
    CONTROL(false),
    PART(false),
    PARAMETER(false),
    RESOURCE(true);

    private final boolean uuid;

    ItemType(boolean isUuid) {
      this.uuid = isUuid;
    }

    public boolean isUuid() {
      return uuid;
    }
  }

  /**
   * Get the identifier originally assigned to this entity.
   * <p>
   * If the identifier value was reassigned, the return value of this method will be different than
   * value returned by {@link #getIdentifier()}. In such cases, a call to
   * {@link #isIdentifierReassigned()} is expected to return {@code true}.
   * <p>
   * If the value was not reassigned, the return value of this method will be the same value returned
   * by {@link #getIdentifier()}. In this case, {@link #isIdentifierReassigned()} is expected to
   * return {@code false}.
   *
   * @return the original identifier value before reassignment
   */
  @NonNull
  String getOriginalIdentifier();

  /**
   * Get the entity's current identifier value.
   *
   * @return the identifier value
   */
  @NonNull
  String getIdentifier();

  /**
   * Determine if the identifier was reassigned.
   *
   * @return {@code true} if the identifier was reassigned, or {@code false} otherwise
   */
  boolean isIdentifierReassigned();

  @NonNull
  IModelNodeItem<?, ?> getInstance();

  void setInstance(@NonNull IModelNodeItem<?, ?> item);

  @NonNull
  <T> T getInstanceValue();

  @NonNull
  ItemType getItemType();

  URI getSource();

  int getReferenceCount();

  void incrementReferenceCount();

  int resetReferenceCount();
}
