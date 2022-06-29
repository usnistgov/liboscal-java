/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.oscal.lib.profile.resolver;

import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;

import org.jetbrains.annotations.NotNull;

public interface IIdentifierMapper {
  @NotNull
  IIdentifierMapper IDENTITY = new IIdentifierMapper() {

    @Override
    public String mapRoleIdentifier(@NotNull String identifier) {
      return identifier;
    }

    @Override
    public String mapControlIdentifier(@NotNull String identifier) {
      return identifier;
    }

    @Override
    public String mapGroupIdentifier(@NotNull String identifier) {
      return identifier;
    }

    @Override
    public String mapParameterIdentifier(@NotNull String identifier) {
      return identifier;
    }

    @Override
    public @NotNull String mapPartIdentifier(@NotNull String identifier) {
      return identifier;
    }
  };

  @NotNull
  String mapRoleIdentifier(@NotNull String identifier);

  @NotNull
  String mapControlIdentifier(@NotNull String identifier);

  @NotNull
  String mapGroupIdentifier(@NotNull String identifier);

  @NotNull
  String mapParameterIdentifier(@NotNull String identifier);

  @NotNull
  String mapPartIdentifier(@NotNull String identifier);

  @NotNull
  default String mapByItemType(@NotNull ItemType itemType, @NotNull String identifier) { // NOPMD - intentional
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
