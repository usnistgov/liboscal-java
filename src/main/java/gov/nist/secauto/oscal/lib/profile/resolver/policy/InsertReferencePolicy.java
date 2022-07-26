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

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import com.vladsch.flexmark.util.sequence.BasedSequence;

import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nonnull;

import java.util.List;
import java.util.Locale;

public class InsertReferencePolicy
    extends AbstractCustomReferencePolicy<InsertAnchorNode> {
  private static final Logger LOGGER = LogManager.getLogger(InsertReferencePolicy.class);

  public InsertReferencePolicy() {
    super(IIdentifierParser.IDENTITY_PARSER);
  }

  @Override
  protected List<@Nonnull ItemType> getEntityItemTypes(@Nonnull InsertAnchorNode insert) {
    String type = insert.getType().toString();

    List<@Nonnull ItemType> itemTypes;
    if ("param".equals(type)) {
      itemTypes = CollectionUtil.singletonList(ItemType.PARAMETER);
    } else {
      throw new UnsupportedOperationException("unrecognized insert type: " + type);
    }
    return itemTypes;
  }

  @Override
  public String getReferenceText(@Nonnull InsertAnchorNode insert) {
    return insert.getIdReference().toString();
  }

  @Override
  public void setReferenceText(@Nonnull InsertAnchorNode insert, @Nonnull String newReference) {
    insert.setIdReference(BasedSequence.of(newReference));
  }

  @Override
  protected boolean handleIndexMiss(
      @Nonnull InsertAnchorNode insert,
      @Nonnull List<@Nonnull ItemType> itemTypes,
      @Nonnull String identifier,
      @Nonnull IReferenceVisitor visitor) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.atError().log(
          "the '{}' insert should reference a '{}' identified by '{}'. The index did not contain the identifier.",
          insert.getType().toString(),
          itemTypes.stream()
              .map(type -> type.name().toLowerCase(Locale.ROOT))
              .collect(CustomCollectors.joiningWithOxfordComma("or")),
          identifier);
    }
    return true;
  }

}
