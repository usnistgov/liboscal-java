/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
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
   * If the identifier value was reassigned, the return value of this method will
   * be different than value returned by {@link #getIdentifier()}. In such cases,
   * a call to {@link #isIdentifierReassigned()} is expected to return
   * {@code true}.
   * <p>
   * If the value was not reassigned, the return value of this method will be the
   * same value returned by {@link #getIdentifier()}. In this case,
   * {@link #isIdentifierReassigned()} is expected to return {@code false}.
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
   * @return {@code true} if the identifier was reassigned, or {@code false}
   *         otherwise
   */
  boolean isIdentifierReassigned();

  @NonNull
  IModelNodeItem<?, ?> getInstance();

  void setInstance(@NonNull IModelNodeItem<?, ?> item);

  @NonNull
  <T> T getInstanceValue();

  @NonNull
  ItemType getItemType();

  @NonNull
  URI getSource();

  int getReferenceCount();

  void incrementReferenceCount();

  int resetReferenceCount();
}
