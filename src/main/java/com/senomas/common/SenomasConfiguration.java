package com.senomas.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.senomas")
public class SenomasConfiguration {
    
    private Compression compression = new Compression();
    private LoggerFilter httpLogger = new LoggerFilter();
    
    public Compression getCompression() {
        return compression;
    }
    
    public void setCompression(Compression compression) {
        this.compression = compression;
    }
    
    public LoggerFilter getHttpLogger() {
        return httpLogger;
    }
    
    public void setHttpLogger(LoggerFilter httpLogger) {
        this.httpLogger = httpLogger;
    }

    public static class Compression {
        private int order = 1;
        private List<String> excludes = new LinkedList<String>();
        
        public int getOrder() {
            return order;
        }
        
        public void setOrder(int order) {
            this.order = order;
        }
        
        public List<String> getExcludes() {
            return excludes;
        }
        
        public void setExcludes(List<String> excludes) {
            this.excludes = excludes;
        }
    }
    
    public static class LoggerFilter {
        private int order = 0;
        private Map<String, Object> path;

        public int getOrder() {
            return order;
        }
        
        public void setOrder(int order) {
            this.order = order;
        }
        
        public Map<String, Object> getPath() {
            return path;
        }
        
        public void setPath(Map<String, Object> path) {
            this.path = path;
        }
        
    }
}
