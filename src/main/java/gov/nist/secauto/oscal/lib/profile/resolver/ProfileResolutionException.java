/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ProfileResolutionException
    extends Exception {

  /**
   * the serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Create a new profile resolution exception with the provided {@code message}.
   *
   * @param message
   *          a description of the error that occurred
   */
  public ProfileResolutionException(String message) {
    super(message);
  }

  /**
   * Create a new profile resolution exception with the provided {@code message}
   * based on the provided {@code cause}.
   *
   * @param message
   *          a description of the error that occurred
   * @param cause
   *          the initial cause of the exception
   */
  public ProfileResolutionException(String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}
