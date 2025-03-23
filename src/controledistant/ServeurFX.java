package controledistant;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServeurFX extends Application {
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private List<ClientHandler> clients = new ArrayList<>();
    private ObservableList<String> clientList = FXCollections.observableArrayList();
    private ObservableList<String> commandList = FXCollections.observableArrayList(); // Liste des commandes

    // Boutons de l'interface graphique
    private Button startButton;
    private Button stopButton;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Serveur de Contrôle à Distance");

        // Liste des clients connectés
        ListView<String> clientListView = new ListView<>(clientList);
        clientListView.setPrefHeight(200);

        // Liste des commandes exécutées
        ListView<String> commandListView = new ListView<>(commandList);
        commandListView.setPrefHeight(200);

        // Bouton pour démarrer le serveur
        startButton = new Button("Démarrer le serveur");
        startButton.setOnAction(e -> startServer(5001));

        // Bouton pour arrêter le serveur
        stopButton = new Button("Arrêter le serveur");
        stopButton.setOnAction(e -> stopServer());
        stopButton.setDisable(true); // Désactivé par défaut

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(clientListView, commandListView, startButton, stopButton);

        // Scene
        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startServer(int port) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                isRunning = true;
                System.out.println("Serveur démarré sur le port " + port);

                // Activer le bouton "Arrêter le serveur" et désactiver "Démarrer le serveur"
                Platform.runLater(() -> {
                    startButton.setDisable(true);
                    stopButton.setDisable(false);
                });

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connecté : " + clientSocket.getInetAddress());

                    // Ajouter le client à la liste
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    Platform.runLater(() -> clientList.add(clientSocket.getInetAddress().toString()));

                    // Démarrer un thread pour gérer le client
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Erreur lors de l'acceptation d'un client : " + e.getMessage());
                }
            }
        }).start();
    }

    private void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Serveur arrêté.");
            }

            // Fermer toutes les connexions client
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
            Platform.runLater(() -> {
                clientList.clear();
                commandList.clear(); // Effacer la liste des commandes
                startButton.setDisable(false);
                stopButton.setDisable(true);
            });
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt du serveur : " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.err.println("Erreur lors de la création des flux : " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                String command;
                while ((command = in.readLine()) != null) {
                    System.out.println("Commande reçue du client : " + command);

                    // Vérifier que le socket et l'adresse IP sont valides
                    if (socket != null && socket.getInetAddress() != null) {
                        String clientAddress = socket.getInetAddress().toString();
                        String commandEntry = "Client " + clientAddress + " : " + command;

                        // Mettre à jour l'interface graphique via Platform.runLater
                        Platform.runLater(() -> commandList.add(commandEntry));
                    } else {
                        System.err.println("Erreur : adresse IP du client non disponible.");
                    }

                    // Exécuter la commande et renvoyer le résultat
                    String result = executeCommand(command);
                    out.println(result);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la gestion du client : " + e.getMessage());
            } finally {
                close();
                Platform.runLater(() -> clientList.remove(socket.getInetAddress().toString()));
            }
        }

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
        output.append("END_OF_RESPONSE"); // Ajouter le marqueur de fin de réponse
        return output.toString();
    } catch (IOException e) {
        return "Erreur lors de l'exécution de la commande : " + e.getMessage() + "\nEND_OF_RESPONSE";
    }
}

        public void close() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket client : " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}