/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control.catalog;

import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Parameter;

import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IControlContainer {

  List<Control> getControls();

  /**
   * Add a new {@link Control} item to the end of the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  boolean addControl(@NonNull Control item);

  /**
   * Remove the first matching {@link Control} item from the underlying
   * collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  boolean removeControl(@NonNull Control item);

  List<Parameter> getParams();

  /**
   * Add a new {@link Parameter} item to the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  boolean addParam(@NonNull Parameter item);

  /**
   * Remove the first matching {@link Parameter} item from the underlying
   * collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  boolean removeParam(@NonNull Parameter item);

  /**
   * Get the parameter identifiers referenced in the object's context, but not by
   * their child objects.
   *
   * @return a stream of identifiers
   */
  Stream<String> getReferencedParameterIds();
}
