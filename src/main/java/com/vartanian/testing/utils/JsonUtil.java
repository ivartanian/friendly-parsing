package com.vartanian.testing.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public JsonNode fromJsonToTree(String tokenRequest) throws IOException {

        if (tokenRequest == null){
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(tokenRequest);

    }

    public JsonNode getJsonElement(JsonNode jsonNode, String key) throws IOException {

        if (key == null || jsonNode == null){
            return null;
        }

        JsonNode resultNode = jsonNode.get(key);
        if (resultNode == null){
            return null;
        }
        return resultNode;

    }

}
