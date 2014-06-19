package proxy;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.codec.binary.Base64;

public class DebugServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final Map<Class<?>, Object> instances = newHashMap();

  @Override
  public void init(final ServletConfig config) throws ServletException {
    // expose this to the selenium tests
    instances.put(Interface.class, theImplementation);
  }

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse rep) throws IOException {
    final InvocationRequest request = KryoUtils.deserialize(InvocationRequest.class, Base64.decodeBase64(req.getParameter("invocation")));
    try {
      request.type.getMethod(request.methodName, request.parameterTypes).invoke(instances.get(request.type), request.parameterValues);
      response = "invoked " + request.methodName;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    CacheUtils.addNoCaching(rep);
    rep.setContentType("text/html");
    rep.getWriter().print(response);
  }

  /** A DTO for the Selenium JVM's invocation request. */
  static class InvocationRequest {
    private final Class<?> type;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Object[] parameterValues;

    InvocationRequest(final Class<?> type, final String methodName, final Class<?>[] parameterTypes, final Object[] parameterValues) {
      this.type = type;
      this.methodName = methodName;
      this.parameterTypes = parameterTypes;
      this.parameterValues = parameterValues;
    }
  }

}
