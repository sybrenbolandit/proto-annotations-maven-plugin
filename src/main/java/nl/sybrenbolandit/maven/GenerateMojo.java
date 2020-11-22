package nl.sybrenbolandit.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateMojo extends AbstractMojo {

    @Parameter(property = "inputFile", defaultValue = "${basedir}/src/main/proto/spec.proto")
    private File inputFile;

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/generated-resources")
    private File outputDirectory;

    public File getInputFile() {
        return inputFile;
    }

    public File getOutputDirectory() {
        return this.outputDirectory;
    }

    public void execute() {
        try {
            String proto = Files.readString(inputFile.toPath(), StandardCharsets.US_ASCII);

            String reponseObjectPattern = "\\brpc\\b.*\\s\\((.*?)\\)\\s.*";
            Pattern pattern = Pattern.compile(reponseObjectPattern);
            Matcher matcher = pattern.matcher(proto);

            if (matcher.find()) {
                String protoWithImport = addImportStatement(proto);
                String protoWithAnnotation = addAnnotation(protoWithImport, matcher.group(1));

                Files.createDirectories(outputDirectory.toPath());
                try (Writer writer = Files.newBufferedWriter(outputDirectory.toPath().resolve(inputFile.getName()), StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING)) {
                    writer.write(protoWithAnnotation);
                }
            } else {
                throw new IllegalStateException("Expected 'rpc' in protobuf service!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String addImportStatement(String proto) {
        String importStatement = "import \"google/api/annotations.proto\";";
        return proto.replaceFirst("\\bservice\\b\\s", importStatement + "\n\nservice ");
    }

    private String addAnnotation(String proto, String responseObjectName) {
        String annotation = String.format(
                "   option (google.api.http) = {\n" +
                        "       get: \"/%ss\"\n" +
                        "   };\n", responseObjectName.toLowerCase());


        return proto.replaceAll("\\brpc\\b\\s(.*?)\\{\\}", "rpc $1 {\n" + annotation + " }");
    }
}
