// Sistermas Distribuidos
// Giuseppe Carmigniani
// Proyecto - segundo parcial

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class Consumidor {

  // este es el ip y el puerto donde esta el Reportero que recibe los mensajes
  public static int portReportero = 9999;
  public static String ipReportero = "127.0.0.1";

  // este el ip donde esta el factory RabbitMQ
  public static String ipFactory = "127.0.0.1";

  // Este es el nombre del trabajo en RabbitMQ
  private static final String TASK_QUEUE_NAME = "mensajes";

  public static void main(String[] argv) throws Exception {

    // Se crea un coneccion al factory con el ip del factory
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(ipFactory);

    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    // Se declara el nombre del trabajo para RabbitMQ para esperar los mensajes
    channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);

    System.out.println(" [*] Esperando a recibir mensajes.");

    // Esta funcion se llama cuando recibe un nuevo mensaje de RabbitMQ
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {

      // Aqui recibe el mensaje en formato String UTF-8
      String message = new String(delivery.getBody(), "UTF-8");

      // Muestra el mensaje en consola
      System.out.println(" [x] Mensaje recibido: '" + message + "'");

      // Se crea un String con el Hash code usando esta funcion enviandole el mensaje
      String messageHashCode = encryptThisString(message);

      System.out.println("--- SHA516 Hashcode generado: " + messageHashCode);
      System.out.println(" ");

      // Se envia el hashcode generado al reportero
      sendMessage(messageHashCode);
    
    };

    boolean autoAck = true;

    // Responde al servidor que completo el trabajo
    channel.basicConsume(
      TASK_QUEUE_NAME,
      autoAck,
      deliverCallback,
      consumerTag -> {}
    );
  }

  // Convierte el String enviado en un Hash SHA512

  public static String encryptThisString(String input) {
    try {
      // se seleciona el algoritmo SHA-512
      MessageDigest md = MessageDigest.getInstance("SHA-512");

      // se convierte el String a bytes
      byte[] messageDigest = md.digest(input.getBytes());

      // se convierte los bytes en un BigInteger
      BigInteger no = new BigInteger(1, messageDigest);

      // se convierte el BigInteger en un String hexadecimal
      String hashtext = no.toString(16);

      // se agregan ceros hasta convertir el String a 32 caracteres de tamaño
      while (hashtext.length() < 32) {
        hashtext = "0" + hashtext;
      }

      // se retorna el hastcode
      return hashtext;
      
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  // esta funcion envia el mensaje hashcode generado al reportero por medio e un Socket
  public static boolean sendMessage(String message) {
    try {
      // Se crea un Socket, con la IP del reportero, y el puerto
      Socket s = new Socket(ipReportero, portReportero);

      // Se crea un Stream que enviara la informacion al servidor
      DataOutputStream dout = new DataOutputStream(s.getOutputStream());

      // Se escribe el mensaje en formato UTF
      dout.writeUTF(message);

      // Se envia el mensaje
      dout.flush();

      // Se cierra el Stream
      dout.close();

      // Se cierra el puerto
      s.close();

      System.out.println(
        "--- El mensaje fue enviado al reportero correctamente."
      );
    } catch (Exception e) {
      // En caso de no poder enviar el mensaje al reportero, se muestra el
      // mensaje e intenta repetir el envio del mensaje al reportero cada 3 segundos
      // hasta que responda

      System.out.println(e);
      System.out.println(
        "No se pudo comunicar con el reportero, intentando nuevamente en 3 segundos..."
      );

      try {
        TimeUnit.SECONDS.sleep(3);
      } catch (Exception ex) {
        System.out.println(ex);
      }
      // Se llama a la funcion recursivamente si no se efectuo el envio del mensaje
      sendMessage(message);
      return false;
    }
    // En caso de enviarse el mensaje correctamente, termina la funcion y regresa
    // verdadero
    return true;
  }
}
