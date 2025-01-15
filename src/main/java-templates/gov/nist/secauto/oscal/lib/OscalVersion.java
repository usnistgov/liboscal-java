/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib;

import gov.nist.secauto.metaschema.core.util.IVersionInfo;

/**
 * Provides version information for the underlying OSCAL implementation used by this library.
 */
public final class OscalVersion implements IVersionInfo {
  private static final String NAME = "oscal";
  private static final String BUILD_TIMESTAMP = "${timestamp}";
  private static final String COMMIT = "@oscal-git.commit.id.abbrev@";
  private static final String BRANCH = "@oscal-git.branch@";
  private static final String CLOSEST_TAG = "@oscal-git.closest.tag.name@";
  private static final String ORIGIN = "@oscal-git.remote.origin.url@";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getVersion() {
    return CLOSEST_TAG;
  }

  @Override
  public String getBuildTimestamp() {
    return BUILD_TIMESTAMP;
  }

  @Override
  public String getGitOriginUrl() {
    return ORIGIN;
  }

  @Override
  public String getGitCommit() {
    return COMMIT;
  }

  @Override
  public String getGitBranch() {
    return BRANCH;
  }

  @Override
  public String getGitClosestTag() {
    return CLOSEST_TAG;
  }
}
