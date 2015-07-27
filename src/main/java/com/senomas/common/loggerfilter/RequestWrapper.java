package com.senomas.common.loggerfilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.input.TeeInputStream;

public class RequestWrapper extends HttpServletRequestWrapper {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    
    public RequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {
            private TeeInputStream tee = new TeeInputStream(RequestWrapper.super.getInputStream(), bos);

            @Override
            public int read() throws IOException {
                return tee.read();
            }
            
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return tee.read(b, off, len);
            }

			@Override
			public boolean isFinished() {
				try {
					return RequestWrapper.super.getInputStream().isFinished();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}

			@Override
			public boolean isReady() {
				try {
					return RequestWrapper.super.getInputStream().isReady();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}

			@Override
			public void setReadListener(ReadListener listener) {
				try {
					RequestWrapper.super.getInputStream().setReadListener(listener);
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
        };
    }
    
    public byte[] toByteArray() {
        return bos.toByteArray();
    }

}
