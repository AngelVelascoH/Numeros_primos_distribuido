import java.io.*;
import java.net.*;

public class ServidorA {

    public static void main(String[] args) throws IOException {
        int puerto = 5000;
        ServerSocket servidor = new ServerSocket(puerto);
        servidor.setReuseAddress(true);

        while (true) {
            System.out.println("Esperando conexión del servidor TCP...");

            // Esperar a que se conecte el servidor TCP
            Socket socketTcp = servidor.accept();
            System.out.println("Conexión establecida con el servidor TCP.");

            // Crear y ejecutar un nuevo hilo para manejar la conexión
            ServidorAThread hilo = new ServidorAThread(socketTcp);
            Thread t = new Thread(hilo);
            t.start();
        }
    }

    static class ServidorAThread implements Runnable {
        private Socket socketTcp;

        public ServidorAThread(Socket socketTcp) {
            this.socketTcp = socketTcp;
        }

        @Override
        public void run() {
            try {
                // Obtener los datos enviados por el servidor TCP
                DataInputStream entrada = new DataInputStream(socketTcp.getInputStream());
                long numero = entrada.readLong();
                long inicio = entrada.readLong();
                long fin = entrada.readLong();
                System.out.println("El servidor TCP ha enviado el número " + numero + " y el intervalo [" + inicio + ", " + fin + "].");

                // Verificar si el número es divisible en el intervalo dado
                boolean divisible = false;
                for (long i = inicio; i <= fin; i++) {
                    if (numero % i == 0) {
                        divisible = true;
                        break;
                    }
                }
                System.out.println("El número " + numero + " es divisible en el intervalo [" + inicio + ", " + fin + "]?   " + divisible);

                // Enviar respuesta al servidor TCP
                DataOutputStream salida = new DataOutputStream(socketTcp.getOutputStream());
                if (divisible) {
                    salida.writeUTF("DIVIDE");
                } else {
                    salida.writeUTF("NO DIVIDE");
                }

                // Cerrar conexiones
                entrada.close();
                salida.close();
                socketTcp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}