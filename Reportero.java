// Sistermas Distribuidos
// Giuseppe Carmigniani
// Proyecto - segundo parcial

import java.io.*;
import java.net.*;

public class Reportero {

  // este puerto se utilizara para la comunicacion
  public static int puerto = 9999;
  public static int hashCount = 0;
  public static boolean countStarted = false;

  public static long timeStart = 0;
  public static long timeEnd = 0;

  public static int readingTimeout = 5;

  public static void main(String[] args) {
    waitWorkerMessage();
  }

  public static void waitWorkerMessage() {
    try {
      // Se crea un ServerSocket para esperar los mensajes
      ServerSocket ss = new ServerSocket(puerto);
      System.out.println("Esperando a recibir datos en el puerto: 9999\n");

      try {
        while (true) {
          Socket s = ss.accept();

          // Se crea un data inputStream que espera los datos
          DataInputStream dis = new DataInputStream(s.getInputStream());

          // Se asigna el mensaje recibido a un Stringe de formato UTF
          String message = (String) dis.readUTF();

          // El timeout para saber cuando se ha terminado el envio de mensajes
          // se lo inicia una vez que llege el primer mensaje, de ahi en
          // adelante si hay mas de 5 segundos sin recibir un mensaje,
          // da por terminado el proceso de recibir los mensajes
          // Ademas tambien toma el tiempo que llego el primer mensaje
          // para calcular al final en tiempo total tomado

          if (!countStarted) {
            countStarted = true;
            timeStart = System.currentTimeMillis();
            ss.setSoTimeout(readingTimeout * 1000);
          }

          // Se muestra en Hash recibido y se suma al contado de hash recibidos
          System.out.println(message);
          System.out.println(" ");
          hashCount++;

          System.out.println("Hash recibidos: " + hashCount);
        }
      } catch (SocketTimeoutException e) {
        // Cuando llega el timeout de no recibir mas mensajes se termina la espera
        // de los mensajes y se calcula el tiempo transcurrido entre el primer
        // mensaje y el ultimo, menos el tiempo del timeout que espera al final

        System.out.println("Se llego al timeout " + e);
        timeEnd = System.currentTimeMillis();
        ss.close();
        long elapsedTime = timeEnd - timeStart - readingTimeout;

        // Se muestra la cantidad de hash recibidos para confirmar que se
        // recibieron todos los hash y tambien muestra el tiempo que tomo
        // en recibir todos los mensajes
        System.out.println("Se obtuvieron " + hashCount + " Hashes");
        System.out.println(
          "Y tomo " + (double) (elapsedTime) / 1000 + " Segundos"
        );
      }

      // Se cierra el puerto del servidor
      ss.close();

      System.out.println("Fin de la ejecucion, cerrando el servidor");
      //waitWorkerMessage();

    } catch (Exception e) {
      System.out.println("Hubo un error al crear el servidor");
      System.out.println(e);
    }
  }
}
