/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control.catalog;

import gov.nist.secauto.oscal.lib.model.CatalogGroup;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IGroupContainer extends IControlContainer {

  List<CatalogGroup> getGroups();

  /**
   * Add a new {@link CatalogGroup} item to the end of the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  boolean addGroup(@NonNull CatalogGroup item);

  /**
   * Remove the first matching {@link CatalogGroup} item from the underlying
   * collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  boolean removeGroup(@NonNull CatalogGroup item);
}
