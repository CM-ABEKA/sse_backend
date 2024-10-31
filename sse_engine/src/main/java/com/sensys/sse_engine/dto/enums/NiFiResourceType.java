package com.sensys.sse_engine.dto.enums;

public enum NiFiResourceType {
    PROCESS_GROUP,
    PROCESSOR,
    FLOW,
    SYSTEM,
    CONTROLLER,
    COUNTERS,
    PROVENANCE,
    POLICIES,
    TENANTS,
    PROXY,
    RESOURCES,
    SITE_TO_SITE,
    PARAMETER_CONTEXTS,
    RESTRICTED_COMPONENTS,
    DATA,
    OPERATION,
    UNKNOWN;

    public static NiFiResourceType fromIdentifier(String identifier) {
        if (identifier == null) return UNKNOWN;
        
        if (identifier.startsWith("/process-groups")) return PROCESS_GROUP;
        if (identifier.startsWith("/processors")) return PROCESSOR;
        if (identifier.startsWith("/flow")) return FLOW;
        if (identifier.startsWith("/system")) return SYSTEM;
        if (identifier.startsWith("/controller")) return CONTROLLER;
        if (identifier.startsWith("/counters")) return COUNTERS;
        if (identifier.startsWith("/provenance")) return PROVENANCE;
        if (identifier.startsWith("/policies")) return POLICIES;
        if (identifier.startsWith("/tenants")) return TENANTS;
        if (identifier.startsWith("/proxy")) return PROXY;
        if (identifier.startsWith("/resources")) return RESOURCES;
        if (identifier.startsWith("/site-to-site")) return SITE_TO_SITE;
        if (identifier.startsWith("/parameter-contexts")) return PARAMETER_CONTEXTS;
        if (identifier.startsWith("/restricted-components")) return RESTRICTED_COMPONENTS;
        if (identifier.startsWith("/data")) return DATA;
        if (identifier.startsWith("/operation")) return OPERATION;
        
        return UNKNOWN;
    }
}