package com.dadaengineering.maven;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;

class IntrospectionResultSerializer {

    private final GraphQL graphql;
    private final ObjectMapper mapper;

    IntrospectionResultSerializer(GraphQL graphql, ObjectMapper mapper) {
        this.graphql = graphql;
        this.mapper = mapper;
    }

    byte[] serializer() throws JsonProcessingException {
        ExecutionResult result = graphql.execute(IntrospectionQuery.INTROSPECTION_QUERY);
        return mapper.writeValueAsBytes(result.getData());
    }
}
