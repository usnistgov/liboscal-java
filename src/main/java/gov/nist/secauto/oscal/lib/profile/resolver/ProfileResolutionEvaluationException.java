/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver;

public class ProfileResolutionEvaluationException
    extends IllegalStateException {

  /**
   * the serial version UID.
   */
  private static final long serialVersionUID = 1L;

  public ProfileResolutionEvaluationException() {
    // no arguments
  }

  public ProfileResolutionEvaluationException(String str) {
    super(str);
  }

  public ProfileResolutionEvaluationException(Throwable cause) {
    super(cause);
  }

  public ProfileResolutionEvaluationException(String message, Throwable cause) {
    super(message, cause);
  }
}
