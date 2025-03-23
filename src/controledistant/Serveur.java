package controledistant;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serveur est une application qui écoute les connexions des clients,
 * reçoit des commandes et renvoie les résultats.
 */
public class Serveur {
    private ServerSocket serverSocket; // Socket serveur
    private boolean isRunning = false; // Indicateur d'état du serveur
    private ExecutorService threadPool; // Pool de threads pour gérer les clients

    /**
     * Démarre le serveur sur le port spécifié.
     *
     * @param port Le port sur lequel le serveur écoute.
     */
    public void start(int port) {
        try {
            // Créer un socket serveur TCP
            serverSocket = new ServerSocket(port);
            isRunning = true;
            threadPool = Executors.newCachedThreadPool(); // Pool de threads dynamique
            System.out.println("Serveur démarré sur le port " + port);

            // Accepter les connexions des clients
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connecté : " + clientSocket.getInetAddress());

                    // Créer un nouveau thread pour gérer ce client
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Erreur lors de l'acceptation d'un client : " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Arrête le serveur et libère les ressources.
     */
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdown(); // Arrêter le pool de threads
            }
            System.out.println("Serveur arrêté.");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt du serveur : " + e.getMessage());
        }
    }

    /**
     * ClientHandler gère la communication avec un client spécifique.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String command;
                while ((command = in.readLine()) != null) {
                    System.out.println("Commande reçue du client " + socket.getInetAddress() + " : " + command);
                    String result = executeCommand(command);
                    out.println(result); // Envoyer la réponse
                    out.println("END_OF_RESPONSE"); // Marqueur de fin de réponse
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la gestion du client " + socket.getInetAddress() + " : " + e.getMessage());
            } finally {
                try {
                    socket.close();
                    System.out.println("Client déconnecté : " + socket.getInetAddress());
                } catch (IOException e) {
                    System.err.println("Erreur lors de la fermeture du socket client : " + e.getMessage());
                }
            }
        }

        /**
         * Exécute une commande système et retourne le résultat.
         *
         * @param command La commande à exécuter.
         * @return Le résultat de la commande.
         */
        private String executeCommand(String command) {
            try {
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                Process process = builder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                return output.toString();
            } catch (IOException e) {
                return "Erreur lors de l'exécution de la commande : " + e.getMessage();
            }
        }
    }

    /**
     * Méthode principale pour démarrer le serveur.
     *
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        Serveur serveur = new Serveur();
        serveur.start(5001); // Port TCP (5001)
    }
}