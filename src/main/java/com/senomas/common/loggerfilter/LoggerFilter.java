package com.senomas.common.loggerfilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.senomas.common.SenomasConfiguration;
import com.senomas.common.U;

@Component
@EnableConfigurationProperties(SenomasConfiguration.class)
public class LoggerFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerFilter.class);
    
    public static final int FLAG_OFF = 0;
    public static final int FLAG_BASIC = 1;
    public static final int FLAG_HEADER = 2;
    public static final int FLAG_SESSION = 4;
    public static final int FLAG_REQUEST = 8;
    public static final int FLAG_RESPONSE = 16;
    public static final int FLAG_REQUEST_PRETTY = 32;
    public static final int FLAG_RESPONSE_PRETTY = 64;
    public static final int FLAG_ALL = 0xFFFF;
    
    private static long counter = 1;

    FilterConfig cfg;
    List<Matcher> matchers = new LinkedList<Matcher>();
    
    @Autowired
    private SenomasConfiguration config;
    
    @Bean
    public FilterRegistrationBean loggerFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        if (config != null && config.getHttpLogger() != null && config.getHttpLogger().getPath() != null) {
            LOG.debug("init LoggerFilter");
            for (Entry<String, Object> me : config.getHttpLogger().getPath().entrySet()) {
                String vs[] = ((String) me.getValue()).split(",");
                int flag = 0;
                for (String v : vs) {
                    if ("OFF".equalsIgnoreCase(v)) {
                        flag = FLAG_OFF;
                    } else if ("BASIC".equalsIgnoreCase(v)) {
                        flag |= FLAG_BASIC;
                    } else if ("HEADER".equalsIgnoreCase(v)) {
                        flag |= FLAG_HEADER;
                    } else if ("SESSION".equalsIgnoreCase(v)) {
                        flag |= FLAG_SESSION;
                    } else if ("REQUEST".equalsIgnoreCase(v)) {
                        flag |= FLAG_REQUEST;
                    } else if ("RESPONSE".equalsIgnoreCase(v)) {
                        flag |= FLAG_RESPONSE;
                    } else if ("REQUEST_PRETTY".equalsIgnoreCase(v)) {
                        flag |= FLAG_REQUEST_PRETTY;
                    } else if ("RESPONSE_PRETTY".equalsIgnoreCase(v)) {
                        flag |= FLAG_RESPONSE_PRETTY;
                    } else if ("ALL".equalsIgnoreCase(v)) {
                        flag |= FLAG_ALL;
                    } else {
                        throw new RuntimeException("Not supported flag '"+v+"' in "+me.getKey()+" = "+me.getValue());
                    }
                }
                matchers.add(new Matcher(me.getKey(), flag));
                if (LOG.isDebugEnabled()) LOG.debug("Logging '"+me.getKey()+"' "+me.getValue()+"    "+Integer.toHexString(flag).toUpperCase());
            }
            Collections.sort(matchers, new Comparator<Matcher>() {

                public int compare(Matcher o1, Matcher o2) {
                    return Integer.compare(o2.path.length(), o1.path.length());
                }
    
            });
            registrationBean.setOrder(config.getHttpLogger().getOrder());
        }
        registrationBean.setFilter(this);
        return registrationBean;
    }
    

    public void init(FilterConfig filterConfig) throws ServletException {
        this.cfg = filterConfig;
    }

    public void destroy() {}

    public int getFlag(String path) {
        for (Matcher m : matchers) {
            if (path.startsWith(m.path)) { return m.flag; }
        }
        return 0;
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest) {
            HttpServletRequest hreq = (HttpServletRequest) req;
            HttpServletResponse hresp = (HttpServletResponse) resp;
            String qstr = hreq.getQueryString();
            String url = hreq.getRequestURI() + (qstr != null ? '?' + qstr : "");
            int flag = getFlag(url);
            if (flag > 0) {
                final long reqid  = counter++;
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append("Request [").append(reqid).append("]  ").append(url);
                    if ((flag & FLAG_HEADER) != 0) {
                        sb.append("\nHeaders:");
                        for (Enumeration<?> e = hreq.getHeaderNames(); e.hasMoreElements(); ) {
                            String str = (String) e.nextElement();
                            Enumeration<?> e2 = hreq.getHeaders(str);
                            if (e2.hasMoreElements()) {
                                Object v1 = e2.nextElement();
                                if (e2.hasMoreElements()) {
                                    sb.append("\n   "+str+":\n      '"+v1+"'");
                                    do {
                                        sb.append("\n      '"+e2.nextElement()+"'");
                                    } while (e2.hasMoreElements());
                                } else {
                                    sb.append("\n   "+str+": '"+v1+"'");
                                }
                            }
                        }
                        HttpSession session = hreq.getSession(false);
                        if (session != null) {
                            sb.append("\nSession id: "+session.getId());
                        }
                        sb.append("\nRemote user: "+hreq.getRemoteUser());
                    }
    
                    doLog(sb.toString());
                    
                    HttpServletRequest xreq = ((flag & FLAG_REQUEST) != 0) ? new RequestWrapper(hreq) : hreq;
                    HttpServletResponse xres = ((flag & FLAG_HEADER) != 0 || (flag & FLAG_RESPONSE) != 0 || (flag & FLAG_RESPONSE_PRETTY) != 0) ? new ResponseWrapper(hresp) : hresp;
                    
                    chain.doFilter(xreq, xres);
    
                    if (xreq instanceof RequestWrapper) {
                        byte bb[] = ((RequestWrapper) xreq).toByteArray();
                        if (bb.length > 0) {
                            doLog("Request ["+reqid+"]  "+url+"\nBody len: "+bb.length+"\n"+U.toString(bb));
                        }
                    }
                    
                    if (xres instanceof ResponseWrapper) {
                        sb.setLength(0);
                        sb.append("Response [").append(reqid).append("]  ").append(url).append('\n');
                        if ((flag & FLAG_HEADER) != 0) {
                            sb.append("Headers:\n").append(((ResponseWrapper) xres).getHeaders());
                        }
                        if ((flag & FLAG_RESPONSE) != 0 || (flag & FLAG_RESPONSE_PRETTY) != 0) {
                            byte bb[] = ((ResponseWrapper) xres).toByteArray();
                            if (bb.length > 0) {
                                if ((flag & FLAG_RESPONSE_PRETTY) != 0) {
                                    String ct = ((ResponseWrapper) xres).getContentType();
                                    if (ct != null && ct.startsWith("application/json")) {
                                        sb.append("Body prettified len: ").append(bb.length).append('\n');
                                        ObjectMapper om = new ObjectMapper();
                                        om.enable(SerializationFeature.INDENT_OUTPUT);
                                        Object json = om.readValue(bb, Object.class);
                                        sb.append(om.writeValueAsString(json));
                                    } else {
                                        sb.append("Body len: ").append(bb.length).append('\n');
                                        sb.append(U.toString(bb));
                                    }
                                } else {
                                    sb.append("Body len: ").append(bb.length).append('\n');
                                    sb.append(U.toString(bb));
                                }
                            }
                        } else {
                            byte bb[] = ((ResponseWrapper) xres).toByteArray();
                            sb.append("Body len: ").append(bb.length);
                        }
                        doLog(sb.toString());
                    }
                    
                    return;
                } catch (IOException e) {
                    LOG.error("Error  ["+reqid+"]  "+url+"\n"+e.getMessage(), e);
                    throw e;
                } catch (ServletException e) {
                    LOG.error("Error  ["+reqid+"]  "+url+"\n"+e.getMessage(), e);
                    throw e;
                }
            }
        }
        chain.doFilter(req, resp);
    }

    protected void doLog(String str) {
        LOG.info(str);
    }

    static class Matcher {
        String path;
        int flag;

        public Matcher(String path, int flag) {
            this.path = path;
            this.flag = flag;
        }
        
        public String getPath() {
            return path;
        }
        
        public int getFlag() {
            return flag;
        }
        
    }
}
