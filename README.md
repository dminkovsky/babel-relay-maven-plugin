# babel-relay-maven-plugin

This Maven plugin generates a JSON representation of your [graphql-java](https://github.com/graphql-java/graphql-java) schema
for use with the [babel-relay-plugin](https://github.com/facebook/relay/tree/master/scripts/babel-relay-plugin).

## Example

First implement a [`GraphQLSchemaSupplier`](src/main/java/com/dadaengineering/maven/GraphQLSchemaSupplier.java). This class must
have a public no-args constructor.

```java
package com.example;

public class MyGraphQLSchemaSupplier implements GraphQLSchemaSupplier {
    @Override
    public GraphQLSchema get() {
        return newSchema()
            .query(newObject()
                .name("Query")
                .field(newFieldDefinition()
                    .name("field")
                    .type(GraphQLString)
                    .build())
                .build())
            .build();
    }
}
```

Then configure the plugin:

```xml
<project>
    <properties>
        <version.babel-relay-plugin>1.0-SNAPSHOT</version.babel-relay-plugin>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.dadaengineering</groupId>
            <artifactId>babel-relay-maven-plugin</artifactId>
            <version>${version.babel-relay-maven-plugin}</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>com.dadaengineering</groupId>
                <artifactId>babel-relay-maven-plugin</artifactId>
                <version>${version.babel-relay-maven-plugin}</version>
                <executions>
                    <execution>
                        <phase>compile</phase>
                        <goals>
                            <goal>build</goal>
                        </goals>
                        <configuration>
                            <schemaSupplier>com.example.MyGraphQLSchemaSupplier</schemaSupplier>
                            <jsonDest>${project.basedir}/webapp/schema.json</jsonDest>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

The above configuration binds this plugin's `build` goal to execute during compilation. So `mvn compile` will
 produce the introspection result JSON output in `${project.basedir}/webapp/schema.json`.

