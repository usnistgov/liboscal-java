/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.IResourceLocation;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.ConstraintValidationFinding;
import gov.nist.secauto.metaschema.core.model.validation.AbstractValidationResultProcessor;
import gov.nist.secauto.metaschema.core.model.validation.IValidationFinding;
import gov.nist.secauto.metaschema.core.model.validation.IValidationResult;
import gov.nist.secauto.metaschema.core.model.validation.JsonSchemaContentValidator;
import gov.nist.secauto.metaschema.core.model.validation.JsonSchemaContentValidator.JsonValidationFinding;
import gov.nist.secauto.metaschema.core.model.validation.XmlSchemaContentValidator;
import gov.nist.secauto.metaschema.core.model.validation.XmlSchemaContentValidator.XmlValidationFinding;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext.ISchemaValidationProvider;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingModuleLoader;
import gov.nist.secauto.metaschema.schemagen.ISchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.ISchemaGenerator.SchemaFormat;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import javax.xml.transform.stream.StreamSource;

import edu.umd.cs.findbugs.annotations.NonNull;

class OscalValidationTest {
  private static final Logger LOGGER = LogManager.getLogger(OscalValidationTest.class);

  @Test
  void testValidateOscalProfileXml() throws MetaschemaException, IOException, URISyntaxException {
    Path generationDir = Paths.get("target/generated-modules");
    Files.createDirectories(generationDir);

    IBindingContext bindingContext = IBindingContext.builder()
        .compilePath(ObjectUtils.notNull(Files.createTempDirectory(generationDir, "modules-")))
        .build();

    IBindingModuleLoader loader = bindingContext.newModuleLoader();
    IModule module = loader.load(new URI(
        "https://raw.githubusercontent.com/usnistgov/OSCAL/refs/heads/main/src/metaschema/oscal_profile_metaschema.xml"));

    // ModuleLoader loader = new ModuleLoader();
    // loader.allowEntityResolution();
    // IModule module = loader.load(new URI(
    // "https://raw.githubusercontent.com/usnistgov/OSCAL/refs/heads/main/src/metaschema/oscal_profile_metaschema.xml"));

    IValidationResult validationResult = bindingContext.validate(
        new URI(
            "https://raw.githubusercontent.com/GSA/fedramp-automation/refs/heads/develop/src/validations/constraints/content/fedramp-tailoring-profile.xml"),
        Format.XML,
        new ValidationProvider(module),
        null);

    if (validationResult.isPassing()) {
      LOGGER.info("The resource is valid.");
    } else {
      LOGGER.info("Validation identified the following issues:");
      new LoggingValidationHandler().handleResults(validationResult);
    }
    assertTrue(validationResult.isPassing());
  }

  private static final class ValidationProvider implements ISchemaValidationProvider {
    @NonNull
    private final IModule module;

    public ValidationProvider(@NonNull IModule module) {
      this.module = module;
    }

    @Override
    public XmlSchemaContentValidator getXmlSchemas(
        @NonNull URL targetResource,
        @NonNull IBindingContext bindingContext) throws IOException, SAXException {
      IMutableConfiguration<SchemaGenerationFeature<?>> schemaGenerationConfig
          = new DefaultConfiguration<>();

      // schemaGenerationConfig.enableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
      // schemaGenerationConfig.enableFeature(SchemaGenerationFeature.INLINE_CHOICE_DEFINITIONS);

      try (StringWriter writer = new StringWriter()) {
        ISchemaGenerator.generateSchema(module, writer, SchemaFormat.XML, schemaGenerationConfig);
        try (Reader reader = new StringReader(writer.toString())) {
          return new XmlSchemaContentValidator(
              ObjectUtils.notNull(List.of(new StreamSource(reader))));
        }
      }
    }

    @Override
    public JsonSchemaContentValidator getJsonSchema(
        @NonNull JSONObject json,
        @NonNull IBindingContext bindingContext) throws IOException {
      IMutableConfiguration<SchemaGenerationFeature<?>> configuration = new DefaultConfiguration<>();

      try (StringWriter writer = new StringWriter()) {
        ISchemaGenerator.generateSchema(module, writer, SchemaFormat.JSON, configuration);
        return new JsonSchemaContentValidator(
            new JSONObject(new JSONTokener(writer.toString())));
      }
    }
  }

  private static final class LoggingValidationHandler
      extends AbstractValidationResultProcessor {

    private <T extends IValidationFinding> void handleFinding(
        @NonNull T finding,
        @NonNull Function<T, CharSequence> formatter) {

      switch (finding.getSeverity()) {
      case CRITICAL:
      case ERROR:
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(formatter.apply(finding), finding.getCause());
        }
        break;
      case WARNING:
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn(formatter.apply(finding), finding.getCause());
        }
        break;
      case INFORMATIONAL:
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info(formatter.apply(finding), finding.getCause());
        }
        break;
      default:
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug(formatter.apply(finding), finding.getCause());
        }
        break;
      }
    }

    @Override
    protected void handleJsonValidationFinding(JsonValidationFinding finding) {
      handleFinding(finding, this::getMessage);
    }

    @Override
    protected void handleXmlValidationFinding(XmlValidationFinding finding) {
      handleFinding(finding, this::getMessage);
    }

    @Override
    protected void handleConstraintValidationFinding(ConstraintValidationFinding finding) {
      handleFinding(finding, this::getMessage);
    }

    @NonNull
    private CharSequence getMessage(JsonValidationFinding finding) {
      StringBuilder builder = new StringBuilder();
      builder.append('[')
          .append(finding.getCause().getPointerToViolation())
          .append("] ")
          .append(finding.getMessage());

      URI documentUri = finding.getDocumentUri();
      if (documentUri != null) {
        builder.append(" [")
            .append(documentUri.toString())
            .append(']');
      }
      return builder;
    }

    @NonNull
    private CharSequence getMessage(XmlValidationFinding finding) {
      StringBuilder builder = new StringBuilder();

      builder.append(finding.getMessage())
          .append(" [");

      URI documentUri = finding.getDocumentUri();
      if (documentUri != null) {
        builder.append(documentUri.toString());
      }

      SAXParseException ex = finding.getCause();
      builder.append(finding.getMessage())
          .append('{')
          .append(ex.getLineNumber())
          .append(',')
          .append(ex.getColumnNumber())
          .append("}]");
      return builder;
    }

    @NonNull
    private CharSequence getMessage(@NonNull ConstraintValidationFinding finding) {
      StringBuilder builder = new StringBuilder();
      builder.append('[')
          .append(finding.getTarget().getMetapath())
          .append(']');

      String id = finding.getIdentifier();
      if (id != null) {
        builder.append(' ')
            .append(id);
      }

      builder.append(' ')
          .append(finding.getMessage());

      URI documentUri = finding.getTarget().getBaseUri();
      IResourceLocation location = finding.getLocation();
      if (documentUri != null || location != null) {
        builder.append(" [");
      }

      if (documentUri != null) {
        builder.append(documentUri.toString());
      }

      if (location != null) {
        builder.append('{')
            .append(location.getLine())
            .append(',')
            .append(location.getColumn())
            .append('}');
      }
      if (documentUri != null || location != null) {
        builder.append(']');
      }
      return builder;
    }
  }
}
