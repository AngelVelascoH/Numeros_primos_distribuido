import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ServidorTCP {
    public static void main(String[] args) throws Exception {
        // Crear socket de escucha en el puerto 12345
        ServerSocket welcomeSocket = new ServerSocket(12345);
        System.out.println("Servidor listo para recibir conexiones...");

        while (true) {
            // Esperar por una conexión entrante
            Socket connectionSocket = welcomeSocket.accept();
            System.out.println("Cliente conectado desde " + connectionSocket.getInetAddress().getHostName());

            // Recibir número entero de 64 bits del cliente
            DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
            long numero = inFromClient.readLong();
            System.out.println("El cliente ha enviado el número " + numero);

            // Dividir el número en 3 intervalos y enviarlos a servidorA junto con el número original
            ExecutorService executor = Executors.newFixedThreadPool(3);
            long k = numero / 3;
            Intervalo intervalo1 = new Intervalo(2, k, numero);
            Intervalo intervalo2 = new Intervalo(k + 1, k * 2, numero);
            Intervalo intervalo3 = new Intervalo(2 * k + 1, numero - 1, numero);
            executor.execute(new ServidorAThread(intervalo1));
            executor.execute(new ServidorAThread(intervalo2));
            executor.execute(new ServidorAThread(intervalo3));
            executor.shutdown();

            // Esperar por las respuestas de servidorA
            String respuesta1 = ServidorAThread.respuestas.take();
            String respuesta2 = ServidorAThread.respuestas.take();
            String respuesta3 = ServidorAThread.respuestas.take();


            System.out.println("Respuestas recibidas: " + respuesta1 + ", " + respuesta2 + ", " + respuesta3);

            // Comprobar las respuestas recibidas y enviar la respuesta apropiada al cliente
            if (respuesta1.equals("NO DIVIDE") && respuesta2.equals("NO DIVIDE") && respuesta3.equals("NO DIVIDE")) {
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                outToClient.writeBytes("ES PRIMO\n");
                System.out.println("Enviando respuesta al cliente: ES PRIMO");
            } else {
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                outToClient.writeBytes("NO ES PRIMO\n");
                System.out.println("Enviando respuesta al cliente: NO ES PRIMO");
            }

            // Cerrar el socket de conexión
            connectionSocket.close();
        }
    }

    static class Intervalo {
        public long inicio;
        public long fin;
        public long numero;

        public Intervalo(long inicio, long fin, long numero) {
            this.inicio = inicio;
            this.fin = fin;
            this.numero = numero;
        }
    }

    static class ServidorAThread implements Runnable {
        public static BlockingQueue<String> respuestas = new LinkedBlockingQueue<>();
        private Intervalo intervalo;

        public ServidorAThread(Intervalo intervalo) {
            this.intervalo = intervalo;
        }

        @Override
        public void run() {
            try {
                // Conectar al servidorA y enviar el intervalo junto con el número original
                Socket clientSocket = new Socket("localhost", 5000);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeLong(intervalo.numero);
                outToServer.writeLong(intervalo.inicio);
                outToServer.writeLong(intervalo.fin);
                DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
                String respuesta = inFromServer.readUTF();
                respuestas.add(respuesta);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}