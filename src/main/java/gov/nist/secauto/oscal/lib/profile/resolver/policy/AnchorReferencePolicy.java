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

import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.CharSubSequence;

import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Locale;

public class AnchorReferencePolicy
    extends AbstractCustomReferencePolicy<InlineLinkNode> {
  private static final Logger LOGGER = LogManager.getLogger(AnchorReferencePolicy.class);

  public AnchorReferencePolicy() {
    super(IIdentifierParser.FRAGMENT_PARSER);
  }

  @SuppressWarnings("null")
  @Override
  protected List<@NotNull ItemType> getEntityItemTypes(@NotNull InlineLinkNode link) {
    return List.of(ItemType.RESOURCE, ItemType.CONTROL, ItemType.GROUP, ItemType.PART);
  }

  @Override
  public String getReferenceText(@NotNull InlineLinkNode link) {
    return link.getUrl().toString();
  }

  @Override
  public void setReferenceText(@NotNull InlineLinkNode link, @NotNull String newValue) {
    link.setUrl(BasedSequence.of(newValue));
  }

  @Override
  protected void handleUnselected(@NotNull InlineLinkNode link, @NotNull EntityItem item,
      @NotNull IReferenceVisitor visitor) {
    URI linkHref = URI.create(link.getUrl().toString());
    URI sourceUri = item.getSource();

    URI resolved = sourceUri.resolve(linkHref);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.atTrace().log("remapping orphaned URI '{}' to '{}'", linkHref.toString(), resolved.toString());
    }
    link.setUrl(CharSubSequence.of(resolved.toString()));
  }

  @Override
  protected boolean handleIndexMiss(
      @NotNull InlineLinkNode reference,
      @NotNull List<@NotNull ItemType> itemTypes,
      @NotNull String identifier,
      @NotNull IReferenceVisitor visitor) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.atError().log(
          "the anchor should reference a {} identified by '{}', but the identifier was not found in the index.",
          itemTypes.stream()
              .map(en -> en.name().toLowerCase(Locale.ROOT))
              .collect(CustomCollectors.joiningWithOxfordComma("or")),
          identifier);
    }
    return true;
  }

  @Override
  protected boolean handleIdentifierNonMatch(@NotNull InlineLinkNode reference, @NotNull IReferenceVisitor visitor) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Ignoring URI '{}'", reference.getUrl().toStringOrNull());
    }

    return true;
  }
}
