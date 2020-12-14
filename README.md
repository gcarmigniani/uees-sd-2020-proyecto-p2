# Sistemas Distribuidos
# Giuseppe Carmigniani
# Proyecto segundo parcial

## Instalacion

Para ejectuar el programa, es necesario tener el Java JRE y JDK en todas las maquinas que se utilizen, para lo cual se puede usar los siguientes comandos

```
sudo apt install default-jre
sudo apt install default-jdk
```

Ademas es necesario installar RabbitMQ en uno de las maquinas, para hacerlo se pueden seguir las instrucciones en este link para el sistema operativo especifico

https://www.rabbitmq.com/download.html


## Compilacion

El programa consiste en 3 archivos java, el Productor, Consumidor y Reportero, los cuales pueden ser ejecutandos en la misma maquina, o en multiples maquina para aprovecar la capacidad de distribuir mensajes de RabbitMQ

Para compilar los programas, dentro de la terminal se ubica en la carpeta del proyecto y se utiliza el siguiente codigo

```
javac -cp amqp-client-5.7.1.jar Productor.java Consumidor.java Reportero.java
```

Esto compilara los 3 archivos en tipo .class de java para ser ejecutado

Es necesario que antes de ejecutar los programar, el servicio de RabbitMQ se encuentre activo de lo contrario no se podran ejecutar la cola de trabajos ni la comunicacion entre los clientes



## Ejecucion

Con el servicio de RabbitMQ activo, Es necesario ejecutar los 3 programas, los cuales pueden ser distribuidos en varios sistemas dentro de una misma red local que se discutira en el proximo segmento.

El primer programa a ejecutar es el **Reportero**, con el siguiente codigo

```
java -cp ".:amqp-client-5.7.1.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar" Reportero
```

Luego el programa de **Consumidor** con el siguiente codigo

```
java -cp ".:amqp-client-5.7.1.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar" Consumidor
```

Cuando el consumidor este listo para recibir los mensajes, se ejecuta el programa de **Productor** con el siguiente codigo

```
java -cp ".:amqp-client-5.7.1.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar" Productor
```

Automaticamente el Productor leera el archivo **"texto-1000.txt"** que se encuentra en la carpeta, y enviara cada linea al servidor RabbitMQ como un mensaje

RabbitMQ despues enviara cada mensaje a los Consumidores activos de manera distribuida, estos Consumidores generaran hashcodes de tipo SHA512 de cada mensaje recibido y los enviaran por medio se un Socket al Reportero que estara esperando los mensajes Hash

El Reportero contara el tiempo transcurrido entre que recibe el primer y ultimo mensaje y mostrara el tiempo que tomo en recibir todos los mensajes y la cantidad de mensajes recibidos para confirmar que no se perdio ningun mensaje

## Uso en Sistemas Distribuidos

En caso de querer ejecturar los programas en diferentes clientes dentro de una misma red local, se lo puede hacer configurando variables de direcciones ip dentro de los archivos .java

Dentro de **Productor.java**, la variable **factoryIp** debe apuntar a la ip donde el servidor RabbitMQ se esta ejecutando

Dentro de **Consumidor.java**, la variable **ipReportero** debe apuntar a la ip donde se esta ejecutando el **Reportero**, y el **portReportero** el puerto para recibit los mensajes por Socket. Ademas la variable **ipFactory** debe apuntar a la ip donde el servidor RabbitMQ se esta ejecutando

Finalmente en **Reportero.java**, solo es necesario cambiar la variable **puerto** al puerto que se utilizara para recibir los mensajes de los Consumidores por socket.

Es posible ejecutar multiples instancias de **Consumidor** al mismo tiempo, lo que agilitara el tiempo de procesamiento de mensajes en caso de que las instancias de Consumidores se encuentres en diferentes sistemas
