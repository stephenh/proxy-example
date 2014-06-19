package proxy;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;

public class KryoUtils {

  // Use KryoReflectionFactorySupport for handling classes without a 0-arg cstr
  private static final Kryo kryo = new KryoReflectionFactorySupport();

  public static byte[] serialize(final Object instance) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Output output = new Output(out);
    kryo.writeObject(output, instance);
    output.close();
    return out.toByteArray();
  }

  public static <T> T deserialize(final Class<T> type, final byte[] bytes) {
    final Input input = new Input(bytes);
    return kryo.readObject(input, type);
  }

}
