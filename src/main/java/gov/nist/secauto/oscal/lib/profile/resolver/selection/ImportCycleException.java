/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

public class ImportCycleException
    extends Exception {

  /**
   * the serial version UUID.
   */
  private static final long serialVersionUID = 1L;

  public ImportCycleException() {
    // no message or cause
  }

  public ImportCycleException(String message) {
    super(message);
  }

  public ImportCycleException(Throwable cause) {
    super(cause);
  }

  public ImportCycleException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImportCycleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
