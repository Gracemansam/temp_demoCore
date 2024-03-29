package com.lamicore.coredemo.upload;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/introspect")
public class IntrospectionController {

    private final RequestMappingHandlerMapping handlerMapping;

    public IntrospectionController(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/endpoints")
    @ResponseBody
    public List<String> getEndpoints() {
        return handlerMapping.getHandlerMethods()
                .keySet().stream()
                .map(key -> {
                    PatternsRequestCondition patternsCondition = key.getPatternsCondition();
                    return patternsCondition != null ? patternsCondition.getPatterns() : null;
                })
                .filter(Objects::nonNull) // Filter out null values
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
