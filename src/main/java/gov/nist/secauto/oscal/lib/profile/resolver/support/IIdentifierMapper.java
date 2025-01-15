/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IIdentifierMapper {
  @NonNull
  IIdentifierMapper IDENTITY = new IIdentifierMapper() {

    @Override
    public String mapRoleIdentifier(@NonNull String identifier) {
      return identifier;
    }

    @Override
    public String mapControlIdentifier(@NonNull String identifier) {
      return identifier;
    }

    @Override
    public String mapGroupIdentifier(@NonNull String identifier) {
      return identifier;
    }

    @Override
    public String mapParameterIdentifier(@NonNull String identifier) {
      return identifier;
    }

    @Override
    public @NonNull
    String mapPartIdentifier(@NonNull String identifier) {
      return identifier;
    }
  };

  @NonNull
  String mapRoleIdentifier(@NonNull String identifier);

  @NonNull
  String mapControlIdentifier(@NonNull String identifier);

  @NonNull
  String mapGroupIdentifier(@NonNull String identifier);

  @NonNull
  String mapParameterIdentifier(@NonNull String identifier);

  @NonNull
  String mapPartIdentifier(@NonNull String identifier);

  @NonNull
  default String mapByItemType(
      @NonNull IEntityItem.ItemType itemType,
      @NonNull String identifier) {
    String retval;
    switch (itemType) {
    case CONTROL:
      retval = mapControlIdentifier(identifier);
      break;
    case GROUP:
      retval = mapGroupIdentifier(identifier);
      break;
    case PARAMETER:
      retval = mapParameterIdentifier(identifier);
      break;
    case PART:
      retval = mapPartIdentifier(identifier);
      break;
    case ROLE:
      retval = mapRoleIdentifier(identifier);
      break;
    case LOCATION:
    case PARTY:
    case RESOURCE:
      retval = identifier;
      break;
    default:
      throw new UnsupportedOperationException("Unsupported item type: " + itemType.name());
    }
    return retval;
  }
}
