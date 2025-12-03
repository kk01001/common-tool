package io.github.kk01001.graphql.examples.directive;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

import java.util.Locale;

public class UppercaseDirective implements SchemaDirectiveWiring {

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> env) {
        GraphQLFieldDefinition field = env.getElement();
        GraphQLFieldsContainer parentType = env.getFieldsContainer();
        
        if (parentType instanceof graphql.schema.GraphQLObjectType) {
            graphql.schema.GraphQLObjectType objectType = (graphql.schema.GraphQLObjectType) parentType;
            DataFetcher<?> originalFetcher = env.getCodeRegistry().getDataFetcher(objectType, field);
            DataFetcher<?> dataFetcher = (dataFetchingEnvironment) -> {
                Object originalResult = originalFetcher.get(dataFetchingEnvironment);
                if (originalResult instanceof String) {
                    return ((String) originalResult).toUpperCase(Locale.ROOT);
                }
                return originalResult;
            };
            env.getCodeRegistry().dataFetcher(objectType, field, dataFetcher);
        }
        return field;
    }
}
