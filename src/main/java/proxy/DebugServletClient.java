package proxy;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import proxy.DebugServlet.InvocationRequest;

/** Shuffles calls from the Selenium/JUnit JVM off to {@link DebugServlet}. */
@SuppressWarnings("deprecation")
public class DebugServletClient {

  private final static DefaultHttpClient client = new DefaultHttpClient();
  private static final int webappPort = new Integer(System.getProperty("webapp.port", "8888"));

  @SuppressWarnings("unchecked")
  public static <T> T getProxy(final Class<T> type) {
    return (T) Enhancer.create(type, new Handler(type));
  }

  private static class Handler implements MethodInterceptor {
    private final Class<?> type;
    private final List<String> methodsToIgnore = newArrayList(
      "finalize",
      "toString",
      "hashCode",
      "equals",
      "notify",
      "notifyAll",
      "wait");

    private Handler(final Class<?> type) {
      this.type = type;
    }

    @Override
    public synchronized Object intercept(
        final Object object,
        final Method method,
        final Object[] args,
        final MethodProxy proxy) throws Throwable {
      if (methodsToIgnore.contains(method.getName())) {
        return method.invoke(object, args);
      }
      final InvocationRequest request = new InvocationRequest(type, method.getName(), method.getParameterTypes(), args);
      final String data = Base64.encodeBase64URLSafeString(KryoUtils.serialize(request));
      final HttpGet req = new HttpGet("http://localhost:" + webappPort + "/debug?invocation=" + data);
      try {
        final HttpResponse res = client.execute(req);
        if (res.getStatusLine().getStatusCode() != 200) {
          throw new RuntimeException("DebugServlet failed to invoke "
            + method.getName()
            + " on a "
            + object.getClass().getSimpleName());
        }
        EntityUtils.consume(res.getEntity());
        return null; // for now we don't bother returning anything
      } finally {
        req.abort();
      }
    }
  }

}
