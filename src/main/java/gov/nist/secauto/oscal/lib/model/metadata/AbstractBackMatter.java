/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.metadata;

import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;

import java.util.List;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractBackMatter implements IBackMatter {

  @Override
  public Resource getResourceByUuid(@NonNull UUID uuid) {
    List<Resource> resources = getResources();

    Resource retval = null;
    if (resources != null) {
      retval = resources.stream()
          .filter(resource -> {
            return uuid.equals(resource.getUuid());
          }).findFirst()
          .orElse(null);
    }
    return retval;
  }

}
