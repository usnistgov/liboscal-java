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

import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension.InsertAnchorNode;
import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public class InsertReferencePolicy
    extends AbstractCustomReferencePolicy<InsertAnchorNode> {
  private static final Logger LOGGER = LogManager.getLogger(InsertReferencePolicy.class);

  public InsertReferencePolicy() {
    super(IIdentifierParser.IDENTITY_PARSER);
  }

  @Override
  protected List<IEntityItem.ItemType> getEntityItemTypes(@NonNull InsertAnchorNode insert) {
    String type = insert.getType().toString();

    List<IEntityItem.ItemType> itemTypes;
    if ("param".equals(type)) {
      itemTypes = CollectionUtil.singletonList(IEntityItem.ItemType.PARAMETER);
    } else {
      throw new UnsupportedOperationException("unrecognized insert type: " + type);
    }
    return itemTypes;
  }

  @Override
  public String getReferenceText(@NonNull InsertAnchorNode insert) {
    return insert.getIdReference().toString();
  }

  @Override
  public void setReferenceText(@NonNull InsertAnchorNode insert, @NonNull String newReference) {
    insert.setIdReference(ObjectUtils.notNull(BasedSequence.of(newReference)));
  }

  @Override
  protected boolean handleIndexMiss(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull InsertAnchorNode insert,
      @NonNull List<IEntityItem.ItemType> itemTypes,
      @NonNull String identifier,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.atError().log(
          "The '{}' insert at '{}' should reference a '{}' identified by '{}'."
              + " The index did not contain the identifier.",
          insert.getType().toString(),
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER),
          itemTypes.stream()
              .map(type -> type.name().toLowerCase(Locale.ROOT))
              .collect(CustomCollectors.joiningWithOxfordComma("or")),
          identifier);
    }
    return true;
  }

}
