package com.headissue.badges;

/**
 * @author Jens Wilke
 */

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

/**
 * Easy performance optimizer for serving static files. Sets caching header
 * and does compression.
 *
 * @author Jens Wilke; created: 2013-07-19
 */
public class CompressFilter extends HttpServlet {

  public final int maxAge = 27 * 60 / 2;
  public final int compressionLevel = 9;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    /*-
    System.err.println("pathInfo=" + req.getPathInfo());
    System.err.println("pathTranslated=" + req.getPathTranslated());
    System.err.println("requestUrl=" + req.getRequestURL());
    System.err.println("servletPath=" + req.getServletPath());
    System.err.println("requestURI=" + req.getRequestURI());
    -*/
    String pi = req.getPathInfo();
    // System.err.println("dispatch to: " + pi);
    RequestDispatcher rd = req.getRequestDispatcher(pi);
    String _acceptEncoding = req.getHeader("Accept-Encoding");
    boolean _doCompress = _acceptEncoding != null && _acceptEncoding.contains("gzip");
    // always run through the compression filter, since that also set the caching flags
    rd.forward(req, new CompressionHttpServletResponseWrapper(_doCompress, res));
  }

  @Override
  public void destroy() {

  }

  class CompressionHttpServletResponseWrapper extends HttpServletResponseWrapper {


    private ServletResponseGZIPOutputStream gzipStream;
    private ServletOutputStream outputStream;
    private PrintWriter printWriter;


    public CompressionHttpServletResponseWrapper(boolean _doCompress, HttpServletResponse response)
      throws IOException {
      super(response);
      gzipStream = new ServletResponseGZIPOutputStream(_doCompress, response, response.getOutputStream());
    }

    @Override
    public void flushBuffer() throws IOException {
      if (printWriter != null) {
        printWriter.flush();
      }
      if (outputStream != null) {
        outputStream.flush();
      }
      flushBuffer();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      if (printWriter != null) {
        throw new IllegalStateException("printWriter already defined");
      }
      if (outputStream == null) {
        outputStream = gzipStream;
      }
      return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
      if (outputStream != null)
        throw new IllegalStateException("printWriter already defined");
      if (printWriter == null) {
        printWriter =
          new PrintWriter(new OutputStreamWriter(gzipStream, getCharacterEncoding()));
      }
      return printWriter;
    }

    @Override
    public void setDateHeader(String name, long date) {
    }

    @Override
    public void addDateHeader(String name, long date) {
    }

    @Override
    public void setHeader(String name, String value) {
    }

    @Override
    public void addHeader(String name, String value) {
    }

    @Override
    public void setIntHeader(String name, int value) {
    }

    @Override
    public void addIntHeader(String name, int value) {
    }

    @Override
    public void setContentLength(int len) {
      // Do nothing
    }

  }

  class ServletResponseGZIPOutputStream extends ServletOutputStream {

    ServletOutputStream output;
    ByteArrayOutputStream rawData;
    ByteArrayOutputStream compressedData;
    GZIPOutputStream gzipStream;
    HttpServletResponse response;
    boolean doCompress;

    public ServletResponseGZIPOutputStream(boolean _doCompress, HttpServletResponse res, ServletOutputStream o) throws IOException {
      output = o;
      response = res;
      compressedData = new ByteArrayOutputStream();
      rawData = new ByteArrayOutputStream();
      gzipStream = new GZIPOutputStream(compressedData) {
        {
          def.setLevel(compressionLevel);
        }
      };
      doCompress = _doCompress;
    }

    public byte[] getCompressedData() {
      return compressedData.toByteArray();
    }

    public byte[] getRawData() {
      return rawData.toByteArray();
    }

    @Override
    public void close() throws IOException {
      if (response.getStatus() == 200) {
        // null out everything....
        /*- does not help!
        response.setHeader("Set-Cookie", null);
        response.setHeader("Accept-Ranges", null);
        response.setHeader("ETag", null);
        response.setHeader("Last-Modified", null);
        -*/
        response.setHeader("Cache-Control", "max-age=" + maxAge);

      }
      gzipStream.close();
      rawData.close();
      // output.write(rawData.toByteArray());
      byte[] _bytesCompressed = compressedData.toByteArray();
      byte[] _bytesRaw = rawData.toByteArray();
      // System.out.println("raw: " + _bytesRaw.length + ", compressed: " + _bytesCompressed.length);
      // always check, an png or jpeg might not yield a better compression
      if (_bytesCompressed.length < _bytesRaw.length && doCompress) {
        response.setContentLength(_bytesCompressed.length);
        response.addHeader("Content-Encoding", "gzip");
        response.addHeader("Vary", "Accept-Encoding");
        output.write(_bytesCompressed);
        // System.out.println("Sending compessed raw: " + _bytesRaw.length + ", compressed: " + _bytesCompressed.length);
      } else {
        response.setContentLength(_bytesRaw.length);
        output.write(_bytesRaw);
      }
      output.close();
    }

    @Override
    public void flush() throws IOException {
      // real action happens at close();
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
      gzipStream.write(b, off, len);
      rawData.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
      gzipStream.write(b);
      rawData.write(b);
    }

    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      output.setWriteListener(writeListener);
    }

  }

  static class Request extends HttpServletRequestWrapper {
    Request(HttpServletRequest request) {
      super(request);
    }
  }

  static class Response implements HttpServletResponse {

    int statusCode;
    String errorString;
    String statusMessage;
    String characterEncoding;
    byte[] output;
    MyOutputStream outputStream;
    PrintWriter writer;
    String contentType;
    Locale locale;

    public byte[] getData() throws IOException {
      flushBuffer();
      if (writer != null) {
        writer.close();
      }
      if (outputStream != null) {
        outputStream.close();
        outputStream.innerOut.toByteArray();
      }
      return null;
    }

    public void addCookie(Cookie cookie) { }

    public boolean containsHeader(String name) { return false; }

    public String encodeURL(String url) { return null; }

    public String encodeRedirectURL(String url) { return null; }

    public String encodeUrl(String url) { return null; }

    public String encodeRedirectUrl(String url) { return null; }

    public void sendError(int sc, String msg) throws IOException {
      statusCode = sc;
      errorString = msg;
    }

    public void sendError(int sc) throws IOException {
      statusCode = sc;
    }

    public void sendRedirect(String location) throws IOException {
    }

    public void setDateHeader(String name, long date) {
    }

    public void addDateHeader(String name, long date) {
    }

    public void setHeader(String name, String value) {
    }

    public void addHeader(String name, String value) {
    }

    public void setIntHeader(String name, int value) {
    }

    public void addIntHeader(String name, int value) {
    }

    public void setStatus(int sc) {
      statusCode = sc;
    }

    public void setStatus(int sc, String sm) {
      statusCode = sc;
      statusMessage = sm;
    }

    public int getStatus() {
      return statusCode;
    }

    public String getHeader(String name) {
      return null;
    }

    public Collection<String> getHeaders(String name) {
      return null;
    }

    public Collection<String> getHeaderNames() {
      return null;
    }

    public String getCharacterEncoding() {
      return characterEncoding;
    }

    public String getContentType() {
      return contentType;
    }

    public ServletOutputStream getOutputStream() throws IOException {
      if (outputStream == null) {
        resetBuffer();
      }
      return outputStream;
    }

    public PrintWriter getWriter() throws IOException {
      if (writer == null) {
        if (outputStream == null) {
          resetBuffer();
        }
        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getOutputStream(), characterEncoding)));
      }
      return writer;
    }

    public void setCharacterEncoding(String v) {
      characterEncoding = v;
    }

    public void setContentLength(int len) {
    }

    public void setContentLengthLong(long len) {
    }

    public void setContentType(String type) {
      contentType = type;
    }

    public void setBufferSize(int size) {
    }

    public int getBufferSize() {
      return 4711;
    }

    public void flushBuffer() throws IOException {
      if (writer != null) { writer.flush(); }
      if (outputStream != null) { outputStream.flush(); }
    }

    public void resetBuffer() {
      outputStream = new MyOutputStream();
      outputStream.innerOut = new ByteArrayOutputStream();
      writer = null;
    }

    public boolean isCommitted() {
      return false;
    }

    public void reset() {
    }

    public void setLocale(Locale loc) {
      locale = loc;
    }

    public Locale getLocale() {
      return locale;
    }
  }

  static class MyOutputStream extends ServletOutputStream {

    ByteArrayOutputStream innerOut;

    public boolean isReady() {
      return true;
    }

    @Override
    public void write(int b) throws IOException {
      innerOut.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      innerOut.write(b, off, len);
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
  }

}
