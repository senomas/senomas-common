package com.senomas.common.loggerfilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.output.TeeOutputStream;

import com.senomas.common.U;

public class ResponseWrapper extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    StringBuilder sb = new StringBuilder();

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
    }
    
    @Override
    public void setHeader(String name, String value) {
        sb.append("   setHeader "+name+": '"+value+"'\n");
        super.setHeader(name, value);
    }
    
    @Override
    public void setDateHeader(String name, long date) {
        sb.append("   setHeader "+name+": "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date(date))).append('\n');
        super.setDateHeader(name, date);
    }
    
    @Override
    public void setIntHeader(String name, int value) {
        sb.append("   setHeader "+name+": "+value).append('\n');
        super.setIntHeader(name, value);
    }
    
    @Override
    public void addHeader(String name, String value) {
        sb.append("   addHeader "+name+": '"+value+"'\n");
        super.addHeader(name, value);
    }
    
    @Override
    public void addDateHeader(String name, long date) {
        sb.append("   addHeader "+name+": "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date(date))).append('\n');
        super.addDateHeader(name, date);
    }
    
    @Override
    public void addIntHeader(String name, int value) {
        sb.append("   addHeader "+name+": "+value).append('\n');
        super.addIntHeader(name, value);
    }
    
    @Override
    public void reset() {
        sb.append("   reset\n");
        super.reset();
    }
    
    @Override
    public void resetBuffer() {
        sb.append("   resetBuffer\n");
        super.resetBuffer();
    }
    
    @Override
    public void addCookie(Cookie cookie) {
        sb.append("   addCookie "+U.dump(cookie));
        super.addCookie(cookie);
    }
    
    @Override
    public void sendError(int sc) throws IOException {
        sb.append("   sendError ").append(sc).append('\n');
        super.sendError(sc);
    }
    
    @Override
    public void sendError(int sc, String msg) throws IOException {
        sb.append("   sendError ").append(sc).append(" '").append(msg).append("'\n");
        super.sendError(sc, msg);
    }
    
    @Override
    public void sendRedirect(String location) throws IOException {
        sb.append("   sendRedirect ").append(location).append('\n');
        super.sendRedirect(location);
    }
    
    @Override
    public void setCharacterEncoding(String charset) {
        sb.append("   setCharacterEncoding ").append(charset).append('\n');
        super.setCharacterEncoding(charset);
    }
    
    @Override
    public void setContentLength(int len) {
        sb.append("   setContentLength ").append(len).append('\n');
        super.setContentLength(len);
    }
    
    @Override
    public void setContentType(String type) {
        sb.append("   setContentType ").append(type).append('\n');
        super.setContentType(type);
    }
    
    @Override
    public void setStatus(int sc) {
        sb.append("   setStatus ").append(sc).append('\n');
        super.setStatus(sc);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public void setStatus(int sc, String sm) {
        sb.append("   setStatus ").append(sc).append(" '").append(sm).append("'\n");
        super.setStatus(sc, sm);
    }
    
    protected TeeOutputStream getTeeOutputStream() throws IOException {
        return new TeeOutputStream(ResponseWrapper.super.getOutputStream(), bos);
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            TeeOutputStream tee = getTeeOutputStream();
            
            @Override
            public void write(int b) throws IOException {
                tee.write(b);
            }
            
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                tee.write(b, off, len);
            }
            
            @Override
            public void flush() throws IOException {
                tee.flush();
            }
            
            @Override
            public void close() throws IOException {
                tee.close();
            }

			@Override
			public boolean isReady() {
				try {
					return ResponseWrapper.super.getOutputStream().isReady();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}

			@Override
			public void setWriteListener(WriteListener listener) {
				try {
					ResponseWrapper.super.getOutputStream().setWriteListener(listener);
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
        };
    }
    
    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(getTeeOutputStream());
    }
    
    public String getHeaders() {
        return sb.toString();
    }
    
    public byte[] toByteArray() {
        return bos.toByteArray();
    }

}
