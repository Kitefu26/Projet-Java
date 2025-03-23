package controledistant;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1"; // Adresse IP du serveur
    private static final int SERVER_PORT = 5001; // Port du serveur

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
             Scanner scanner = new Scanner(System.in, "UTF-8")) {

            // Définir un timeout pour éviter les blocages
            socket.setSoTimeout(5000); // 5 secondes

            System.out.println("Connecté au serveur.");

            String command;
            while (true) {
                System.out.print("Commande à envoyer : ");
                command = scanner.nextLine();

                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }

                out.println(command); // Envoyer la commande au serveur

                // Lire la réponse du serveur
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    if (responseLine.equals("END_OF_RESPONSE")) {
                        // Fin de la réponse
                        break;
                    }
                    responseBuilder.append(responseLine).append("\n");
                }

                // Afficher la réponse complète
                System.out.println("Réponse du serveur :\n" + responseBuilder.toString());
            }

        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Le serveur n'a pas répondu à temps.");
        } catch (IOException e) {
            System.err.println("Erreur de connexion au serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}