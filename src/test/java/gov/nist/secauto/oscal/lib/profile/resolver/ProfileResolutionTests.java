/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.DefaultBoundLoader;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.ImportCycleException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.xmlunit.assertj3.XmlAssert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import edu.umd.cs.findbugs.annotations.NonNull;

class ProfileResolutionTests {
  private static final String XSLT_PATH = "src/test/resources/profile-test-helper.xsl";
  private static final String PROFILE_UNIT_TEST_PATH
      = "oscal/src/specifications/profile-resolution/profile-resolution-examples";
  private static final String JUNIT_TEST_PATH = "src/test/resources";
  private static final String PROFILE_EXPECTED_PATH = PROFILE_UNIT_TEST_PATH + "/output-expected";

  private static Processor processor;
  private static XsltExecutable compairisonXslt;

  @BeforeAll
  static void setup() throws SaxonApiException {
    processor = new Processor(false);
    XsltCompiler comp = processor.newXsltCompiler();
    compairisonXslt = comp.compile(new StreamSource(new File(XSLT_PATH)));
  }

  public static Processor getProcessor() {
    return processor;
  }

  public static XsltExecutable getCompairisonXslt() {
    return compairisonXslt;
  }

  public static ProfileResolver newProfileResolver(@NonNull URI baseUri) {
    DynamicContext context = new DynamicContext(OscalBindingContext.OSCAL_STATIC_METAPATH_CONTEXT);
    context.setDocumentLoader(new DefaultBoundLoader(OscalBindingContext.instance()));
    return new ProfileResolver(context, (uri, src) -> baseUri.resolve(uri));
  }

  private static Catalog resolveProfile(@NonNull Path profilePath)
      throws FileNotFoundException, IOException, ProfileResolutionException, URISyntaxException {
    return resolveProfile(ObjectUtils.notNull(profilePath.toUri().toURL()));
  }

  private static Catalog resolveProfile(@NonNull File profileFile)
      throws FileNotFoundException, IOException, ProfileResolutionException, URISyntaxException {
    return resolveProfile(ObjectUtils.notNull(profileFile.toURI().toURL()));
  }

  private static Catalog resolveProfile(@NonNull URL profileUrl)
      throws IOException, ProfileResolutionException, URISyntaxException {
    return (Catalog) INodeItem.toValue(
        newProfileResolver(ObjectUtils.notNull(profileUrl.toURI())).resolve(profileUrl));
  }

  /**
   * Transform the source to normalize content for test comparison.
   *
   * @param source
   *          the source to normalize
   * @return the transformed content
   * @throws SaxonApiException
   *           if an error occurs while performing the transformation
   */
  private static String transformXml(Source source) throws SaxonApiException {
    net.sf.saxon.s9api.Serializer out = getProcessor().newSerializer();
    out.setOutputProperty(net.sf.saxon.s9api.Serializer.Property.METHOD, "xml");
    // out.setOutputProperty(net.sf.saxon.s9api.Serializer.Property.INDENT, "yes");
    StringWriter writer = new StringWriter();
    out.setOutputWriter(writer);
    XsltTransformer trans = compairisonXslt.load();
    trans.setSource(source);
    trans.setDestination(out);
    trans.transform();

    return writer.toString();
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/profile-tests.csv", numLinesToSkip = 1)
  void test(String profileName) throws IOException, SaxonApiException, URISyntaxException {
    performTest(profileName);
  }

  @Test
  void testSingle() throws IOException, SaxonApiException, URISyntaxException {
    performTest("modify-adds");
  }

  void performTest(String profileName) throws IOException, SaxonApiException, URISyntaxException {
    String profileLocation = String.format("%s/%s_profile.xml", PROFILE_UNIT_TEST_PATH, profileName);

    File profileFile = new File(profileLocation);

    Catalog catalog = null;
    try {
      catalog = resolveProfile(profileFile);
    } catch (ProfileResolutionException ex) {
      fail(String.format("Resolution of profile '%s' failed. %s", profileFile.getAbsolutePath(),
          ex.getLocalizedMessage()));
    }
    assert catalog != null;

    Assertions.assertThat(catalog.getUuid()).isNotNull();
    Assertions.assertThat(catalog.getMetadata()).isNotNull();
    Assertions.assertThat(catalog.getMetadata().getLastModified()).matches(a -> ZoneOffset.UTC.equals(a.getOffset()));
    Assertions.assertThat(catalog.getMetadata().getLinks()).filteredOn("rel", "source-profile").extracting("href")
        .hasSize(1);
    Assertions.assertThat(catalog.getMetadata().getProps()).filteredOn("name", "resolution-tool").extracting("value")
        .hasSize(1);

    ISerializer<Catalog> serializer = OscalBindingContext.instance().newSerializer(Format.XML, Catalog.class);
    StringWriter writer = new StringWriter();
    serializer.serialize(catalog, writer);

    // OscalBindingContext.instance().newSerializer(Format.YAML,
    // Catalog.class).serialize(catalog,
    // System.out);

    // System.out.println("Pre scrub: " + writer.getBuffer().toString());

    String actual = transformXml(new StreamSource(new StringReader(writer.getBuffer().toString())));
    // System.out.println("Post scrub: "+actual);

    String expectedPath = String.format("%s/%s_profile_RESOLVED.xml", PROFILE_EXPECTED_PATH, profileName);
    String expected = transformXml(new StreamSource(new File(expectedPath)));

    XmlAssert.assertThat(actual).and(expected).ignoreWhitespace().ignoreElementContentWhitespace().areIdentical();
  }

  @Test
  void testBrokenLink() {
    String profileLocation = String.format("%s/broken_profile.xml", PROFILE_UNIT_TEST_PATH);

    File profileFile = new File(profileLocation);

    assertThrows(FileNotFoundException.class, () -> {
      resolveProfile(profileFile);
    });
  }

  @Test
  void testCircularLink() {
    String profileLocation = String.format("%s/circular_profile.xml", PROFILE_UNIT_TEST_PATH);

    File profileFile = new File(profileLocation);

    IOException exceptionThrown = assertThrows(IOException.class, () -> {
      resolveProfile(profileFile);
    });

    MatcherAssert.assertThat(exceptionThrown.getCause(), CoreMatchers.instanceOf(ImportCycleException.class));
  }

  @Test
  void testOscalVersion() throws IOException, ProfileResolutionException, URISyntaxException {
    Path profileFile = Paths.get(JUNIT_TEST_PATH, "content/test-oscal-version-profile.xml");
    assert profileFile != null;
    Catalog catalog = resolveProfile(profileFile);
    assertNotNull(catalog);
    assertEquals("1.0.4", catalog.getMetadata().getOscalVersion());
  }

  @Test
  void testImportResourceRelativeLink() throws IOException, ProfileResolutionException, URISyntaxException {
    Path profilePath = Paths.get(JUNIT_TEST_PATH, "content/profile-relative-links-resource.xml");
    assert profilePath != null;
    Catalog resolvedCatalog = resolveProfile(profilePath);
    assertNotNull(resolvedCatalog);
  }

  @Test
  @Disabled
  void testRemove() throws IOException, ProfileResolutionException, URISyntaxException {
    URL url = new URL(
        "https://raw.githubusercontent.com/GSA/fedramp-automation/2229f10cc0b143410522026b793f4947eebb0872/dist/content/baselines/rev4/xml/FedRAMP_rev4_LI-SaaS-baseline_profile.xml");

    Catalog resolvedCatalog = resolveProfile(url);
    assertNotNull(resolvedCatalog);
  }

  @Test
  void testArsModerateProfile() throws IOException, ProfileResolutionException, URISyntaxException {
    URL url = new URL(
        "https://raw.githubusercontent.com/CMSgov/ars-machine-readable/4850049d550772672be855c5551a727aa57eb1bd/ODP-extract/ars-5.0-moderate-profile.xml");

    Catalog resolvedCatalog = resolveProfile(url);

    assertNotNull(resolvedCatalog);
  }

  @Test
  void testMultipleImports() throws IOException, ProfileResolutionException, URISyntaxException {
    File file = new File("src/test/resources/content/oscal-cli-60-profile.json");
    Catalog resolvedCatalog = resolveProfile(file);
    assertAll(
        () -> assertNotNull(resolvedCatalog),
        () -> assertEquals(1,
            resolvedCatalog.getControls().stream().filter(control -> "sc-19".equals(control.getId())).count()));
  }
}
