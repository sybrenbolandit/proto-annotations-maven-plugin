package nl.sybrenbolandit.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GenerateMojoTest {
    @Rule
    public MojoRule rule =
            new MojoRule() {
                @Override
                protected void before() throws Throwable {
                }

                @Override
                protected void after() {
                }
            };

    @Test
    public void generationGraphQLTest() throws Exception {
        File pom = new File("src/test/resources/test-project");
        assertNotNull(pom);
        assertTrue(pom.exists());
        assertTrue(new File(pom, "pom.xml").exists());

        GenerateMojo generateMojo = (GenerateMojo) rule.lookupConfiguredMojo(pom, "generate");
        assertNotNull(generateMojo);

        generateMojo.execute();

        File outputFile = generateMojo.getOutputDirectory().toPath().resolve("test.proto").toFile();
        assertTrue(outputFile.exists());
        String protoContent = Files.readString(outputFile.toPath());
        assertTrue(protoContent.contains("get: \"/cats\""));
    }
}
