// Sistermas Distribuidos
// Giuseppe Carmigniani
// Proyecto - segundo parcial

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Productor {

  // Este es el nombre del trabajo en RabbitMQ
  private static final String QUEUE_NAME = "mensajes";

  // el contador de linear enviadas
  public static int linesSent = 0;

  // La ip para el servidor de RabbitMQ
  public static String factoryIp = "127.0.0.1";

  public static void main(String[] argv) throws Exception {
    // Primero se le el archivo texto-10000 el cual tiene 10,000 lineas de texto
    BufferedReader in = new BufferedReader(new FileReader("texto-10000.txt"));

    String str;

    // Se crea un ArrayList de Strings con cada linea del texto
    List<String> list = new ArrayList<String>();
    while ((str = in.readLine()) != null) {
      list.add(str);
    }

    // Se transforma la lista en un Array de Strings
    String[] stringArr = list.toArray(new String[0]);

    // Se establece la coneccion al servidor RabbitMQ (el factory)
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(factoryIp);

    try (
      // Se intenta la conexion
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel()
    ) {
      // se crea una cola de trabajo para los mensajes
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      // se itera  en el array de los Strings
      // enviando un mensaje por cada linea del documento de texto al servidor
      for (var i = 0; i < stringArr.length; i++) {
        channel.basicPublish(
          "",
          QUEUE_NAME,
          null,
          stringArr[i].getBytes(StandardCharsets.UTF_8)
        );

        System.out.println(" [x] Sent '" + stringArr[i] + "'");

        // Al enviar el mensaje se suma en contador de mensajes enviados
        linesSent++;
      }

      // Al enviar todos los mensajes, se mustra la cantidad enviada
      System.out.println("Se enviaron " + linesSent + " lineas en total.");
    }
  }
}
