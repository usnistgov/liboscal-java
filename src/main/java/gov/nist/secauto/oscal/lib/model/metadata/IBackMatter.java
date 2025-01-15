/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.metadata;

import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;

import java.util.List;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IBackMatter {
  List<Resource> getResources();

  @Nullable
  Resource getResourceByUuid(@NonNull UUID uuid);
}
