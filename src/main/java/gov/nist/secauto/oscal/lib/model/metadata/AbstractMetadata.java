/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.metadata;

import gov.nist.secauto.oscal.lib.model.Metadata.Party;

import java.util.List;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractMetadata implements IMetadata {

  @Override
  public Party getPartyByUuid(@NonNull UUID uuid) {
    List<Party> parties = getParties();

    Party retval = null;
    if (parties != null) {
      retval = parties.stream()
          .filter(party -> {
            return uuid.equals(party.getUuid());
          }).findFirst()
          .orElse(null);
    }
    return retval;
  }
}
