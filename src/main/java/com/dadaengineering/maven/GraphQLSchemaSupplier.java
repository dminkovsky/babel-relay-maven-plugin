package com.dadaengineering.maven;

import graphql.schema.GraphQLSchema;

public interface GraphQLSchemaSupplier {
    GraphQLSchema get();
}
