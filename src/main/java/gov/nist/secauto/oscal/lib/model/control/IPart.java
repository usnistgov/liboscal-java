/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension.InsertAnchorNode;
import gov.nist.secauto.oscal.lib.model.ControlPart;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IPart {
  MarkupMultiline getProse();

  List<ControlPart> getParts();

  @NonNull
  Stream<InsertAnchorExtension.InsertAnchorNode> getInserts(@NonNull Predicate<InsertAnchorNode> filter);
}
