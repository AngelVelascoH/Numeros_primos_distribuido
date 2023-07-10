import java.io.*;
import java.net.*;

public class ClienteTCP {
    public static void main(String[] args) throws Exception {
        // Solicitar número entero al usuario y validar la entrada
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        long numero = 0;
        boolean entradaValida = false;
        while (!entradaValida) {
            System.out.print("Introduce un número entero de 64 bits: ");
            try {
                numero = Long.parseLong(inFromUser.readLine());
                entradaValida = true;
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, introduce un número entero de máximo 64 bits.");
            }
        }

        // Conectar al servidor y enviar el número entero
        Socket clientSocket = null;
        while (true) {
            try {
                clientSocket = new Socket("localhost", 12345);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeLong(numero);
                break;
            } catch (ConnectException e) {
                System.out.println("No se pudo conectar con el servidor. Intentando de nuevo en 5 segundos...");
                Thread.sleep(5000); // Esperar 5 segundos antes de intentar de nuevo
            }
        }

        // Recibir la cadena del servidor y mostrarla en pantalla
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String cadena = inFromServer.readLine();
        System.out.println("El numero" + numero + cadena);

        // Cerrar el socket
        clientSocket.close();
    }
}
