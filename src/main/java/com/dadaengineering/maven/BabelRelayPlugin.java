package com.dadaengineering.maven;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import graphql.GraphQL;
import graphql.execution.SimpleExecutionStrategy;
import graphql.schema.GraphQLSchema;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Mojo(name = "build")
public class BabelRelayPlugin extends AbstractMojo {

    private static final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "build.schemaSupplier")
    private String schemaSupplier;

    @Parameter(property = "build.jsonDest")
    private String jsonDest;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        assertParameter(schemaSupplier, "schemaSupplier");
        assertParameter(jsonDest, "jsonDest");
        ClassLoader loader = buildClassLoader();
        GraphQLSchemaSupplier plugin = getSupplier(loader);
        GraphQLSchema schema = plugin.getSchema();
        IntrospectionResultSerializer serializer = new IntrospectionResultSerializer(new GraphQL(schema, new SimpleExecutionStrategy()), mapper);
        byte[] json = getBytes(serializer);
        Path path = Paths.get(jsonDest);
        writeJson(json, path);
    }

    private void assertParameter(Object param, String name) throws MojoFailureException {
        if (param == null) {
            fail("Required parameter '%s' is not configured.", name);
        }
    }

    private void writeJson(byte[] json, Path path) throws MojoFailureException {
        try {
            Files.delete(path);
        }
        catch (NoSuchFileException e) {
            // this is fine
        }
        catch (IOException e) {
            fail("Failed attempting to delete schema JSON at '%s'", jsonDest);
        }
        try {
            Files.write(path, json, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            fail("Failed attempting to write schema JSON to '%s'", jsonDest);
        }
    }

    private byte[] getBytes(IntrospectionResultSerializer serializer) throws MojoFailureException {
        byte[] serializer1 = null;
        try {
            serializer1 = serializer.serializer();
        } catch (JsonProcessingException e) {
            fail("Failed attempting to serialize introspection results.");
        }
        return serializer1;
    }

    private GraphQLSchemaSupplier getSupplier(ClassLoader loader) throws MojoFailureException {
        GraphQLSchemaSupplier plugin = null;
        try {
            plugin = loadClass(loader).asSubclass(GraphQLSchemaSupplier.class)
              .newInstance();
        } catch (InstantiationException e) {
            fail("Failed attempting to instantiate '%s'.", schemaSupplier);
        } catch (IllegalAccessException e) {
            fail("Failed attempting to access '%s'.", schemaSupplier);
        }
        return plugin;
    }

    private Class<?> loadClass(ClassLoader loader) throws MojoFailureException {
        Class<?> aClass1 = null;
        try {
            aClass1 = loader.loadClass(schemaSupplier);
        } catch (ClassNotFoundException e) {
            fail(String.format("Failed attempting to load class '%s'.", schemaSupplier));
        }
        return aClass1;
    }

    // http://maven.apache.org/guides/mini/guide-maven-classloading.html
    // http://stackoverflow.com/questions/871708/maven-plugin-cant-load-class
    private ClassLoader buildClassLoader() {
        try {
            List<String> classpathElements = project.getCompileClasspathElements();
            classpathElements.add(project.getBuild().getOutputDirectory() );
            classpathElements.add(project.getBuild().getTestOutputDirectory() );

            URL urls[] = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                urls[i] = new File(classpathElements.get(i)).toURI().toURL();
            }

            return new URLClassLoader(urls, getClass().getClassLoader() );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void fail(String format, Object... objs) throws MojoFailureException {
        throw new MojoFailureException(String.format(format, objs));
    }

}
