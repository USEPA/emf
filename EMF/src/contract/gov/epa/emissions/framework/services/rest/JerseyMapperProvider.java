package gov.epa.emissions.framework.services.rest;

import java.text.SimpleDateFormat;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamWriteConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class JerseyMapperProvider implements ContextResolver<ObjectMapper> {
    private static ObjectMapper apiMapper = new ObjectMapper();
    @Override
    public ObjectMapper getContext(Class<?> type) {
        apiMapper.getFactory().setStreamWriteConstraints(StreamWriteConstraints.builder().maxNestingDepth(100000).build());
        apiMapper.getFactory().setStreamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(100000).build());
        apiMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        apiMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        apiMapper.setSerializationInclusion(Include.NON_NULL);
        apiMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        return apiMapper;
    }
}