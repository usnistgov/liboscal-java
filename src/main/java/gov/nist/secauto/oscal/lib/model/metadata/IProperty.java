/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.metadata;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IProperty {
  @NonNull
  static URI normalizeNamespace(@Nullable URI namespace) {
    URI retval = namespace;
    if (retval == null) {
      retval = OSCAL_NAMESPACE;
    }
    return retval;
  }

  @SuppressWarnings("null")
  @NonNull
  URI OSCAL_NAMESPACE = URI.create("http://csrc.nist.gov/ns/oscal");
  @SuppressWarnings("null")
  @NonNull
  URI RMF_NAMESPACE = URI.create("http://csrc.nist.gov/ns/rmf");

  @Nullable
  String getName();

  @Nullable
  URI getNs();

  boolean isNamespaceEqual(@NonNull URI namespace);
}
