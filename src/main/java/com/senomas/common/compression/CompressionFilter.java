package com.senomas.common.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.senomas.common.SenomasConfiguration;

@Component
@EnableConfigurationProperties(SenomasConfiguration.class)
public class CompressionFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(CompressionFilter.class);

    FilterConfig cfg;
    List<String> excludes = new LinkedList<String>();

    @Autowired
    private SenomasConfiguration config;
    
    public CompressionFilter() {
        LOG.debug("init");
    }

    @Bean
    public FilterRegistrationBean compressionFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        if (LOG.isDebugEnabled()) LOG.debug("init CompressionFilter");
        for (String p : config.getCompression().getExcludes()) {
            excludes.add(p);
            if (LOG.isDebugEnabled()) LOG.debug("exclude '"+p+'\'');
        }
        registrationBean.setFilter(this);
        registrationBean.setOrder(config.getCompression().getOrder());
        return registrationBean;
    }
    
    public void init(FilterConfig filterConfig) throws ServletException {
        this.cfg = filterConfig;
    }
    
	public void destroy() {
	}

    public boolean excluded(String path) {
        for (String ex : excludes) {
            if (path.startsWith(ex)) return true;
        }
        return false;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest hreq = (HttpServletRequest) request;
            HttpServletResponse hresp = (HttpServletResponse) response;
            String acceptEncoding = hreq.getHeader(HttpHeaders.ACCEPT_ENCODING);
            String huri = hreq.getRequestURI();
            if (acceptEncoding != null && acceptEncoding.indexOf("gzip") >= 0 && !excluded(huri)) {
                URL res = Thread.currentThread().getContextClassLoader().getResource("public" + huri);
                String resStr = res != null ? res.toExternalForm() : null;
                if (resStr != null && resStr.startsWith("file:") && !resStr.endsWith(".gz")) {
                    try {
                        File f = new File(res.toURI());
                        String fcp = f.getCanonicalPath();
                        if (f.isFile() && f.length() > 1024) {
                            int ix = fcp.lastIndexOf("/public/");
                            String fzp = ix > 0 ? fcp.substring(0, ix) + "/temp/" + fcp.substring(ix+8) : fcp;
                            File fz = new File(fzp + ".gz");
                            fz.getParentFile().mkdirs();
                            if (fz.exists() && fz.lastModified() > f.lastModified()) {
                                if (fz.length() < f.length()) {
                                    hresp.setContentType(cfg.getServletContext().getMimeType(hreq.getRequestURI()));
                                    hresp.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                                    hresp.addDateHeader(HttpHeaders.LAST_MODIFIED, f.lastModified());
                                    hresp.addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fz.length()));
                                    // write zip file
                                    InputStream in = null;
                                    OutputStream out = hresp.getOutputStream();
                                    try {
                                        in = new FileInputStream(fz);
                                        byte buffer[] = new byte[1024];
                                        int len;
                                        while ((len = in.read(buffer)) > 0) {
                                            out.write(buffer, 0, len);
                                        }
                                        out.flush();
                                    } finally {
                                        if (in != null) {
                                            try {
                                                in.close();
                                            } catch (Exception e) {
                                                LOG.warn(e.getMessage(), e);
                                            }
                                        }
                                    }
                                    return;
                                } else {
                                    chain.doFilter(request, response);
                                    return;
                                }
                            } else {
                                if (LOG.isDebugEnabled()) LOG.debug("COMPRESS TO " + fz.getCanonicalPath());
                                synchronized (this) {
                                    GZIPOutputStream gzos = null;
                                    FileInputStream in = null;
                                    try {
                                        gzos = new GZIPOutputStream(new FileOutputStream(fz));
                                        in = new FileInputStream(f);
                                        int len;
                                        byte buffer[] = new byte[1024];
                                        while ((len = in.read(buffer)) > 0) {
                                            gzos.write(buffer, 0, len);
                                        }
                                        gzos.finish();
                                    } finally {
                                        if (in != null) {
                                            try {
                                                in.close();
                                            } catch (Exception e) {
                                                LOG.warn(e.getMessage(), e);
                                            }
                                        }
                                        if (gzos != null) {
                                            try {
                                                gzos.close();
                                            } catch (Exception e) {
                                                LOG.warn(e.getMessage(), e);
                                            }
                                        }
                                    }
                                }
                                if (fz.length() < f.length()) {
                                    hresp.setContentType(cfg.getServletContext().getMimeType(hreq.getRequestURI()));
                                    hresp.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                                    hresp.addDateHeader(HttpHeaders.LAST_MODIFIED, f.lastModified());
                                    hresp.addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fz.length()));
                                    // write zip file
                                    InputStream in = null;
                                    OutputStream out = hresp.getOutputStream();
                                    try {
                                        in = new FileInputStream(fz);
                                        byte buffer[] = new byte[1024];
                                        int len;
                                        while ((len = in.read(buffer)) > 0) {
                                            out.write(buffer, 0, len);
                                        }
                                        out.flush();
                                    } finally {
                                        if (in != null) {
                                            try {
                                                in.close();
                                            } catch (Exception e) {
                                                LOG.warn(e.getMessage(), e);
                                            }
                                        }
                                    }
                                    return;
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

}
