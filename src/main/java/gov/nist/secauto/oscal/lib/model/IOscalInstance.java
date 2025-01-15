/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model;

import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;

import java.util.UUID;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IOscalInstance {
  UUID getUuid();

  Metadata getMetadata();

  BackMatter getBackMatter();

  Resource getResourceByUuid(@NonNull UUID id);
}
