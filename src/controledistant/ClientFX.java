package controledistant;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javafx.application.Platform;

/**
 * ClientFX est une application JavaFX qui permet de se connecter à un serveur,
 * d'envoyer des commandes et de recevoir des réponses.
 */
public class ClientFX extends Application {
    private TextArea outputArea; // Zone d'affichage des résultats
    private TextField inputField; // Champ de saisie des commandes
    private Button sendButton; // Bouton "Envoyer"
    private Button connectButton; // Bouton "Se connecter"
    private Button disconnectButton; // Bouton "Se déconnecter"
    private Socket socket; // Socket pour la communication avec le serveur
    private PrintWriter out; // Flux de sortie pour envoyer des données au serveur
    private BufferedReader in; // Flux d'entrée pour recevoir des données du serveur
    private boolean isConnected = false; // Indicateur de connexion

    /**
     * Méthode principale pour démarrer l'application JavaFX.
     *
     * @param primaryStage La fenêtre principale de l'application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client de Contrôle à Distance");

        // Zone d'affichage des résultats
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(300);

        // Champ de saisie des commandes
        inputField = new TextField();
        inputField.setPromptText("Entrez une commande...");

        // Bouton "Envoyer"
        sendButton = new Button("Envoyer");
        sendButton.setOnAction(e -> sendCommand());
        sendButton.setDisable(true); // Désactivé par défaut

        // Bouton "Se connecter"
        connectButton = new Button("Se connecter");
        connectButton.setOnAction(e -> {
            connectButton.setDisable(true); // Désactiver le bouton pendant la connexion
            new Thread(this::connectToServer).start(); // Connexion dans un thread séparé
        });

        // Bouton "Se déconnecter"
        disconnectButton = new Button("Se déconnecter");
        disconnectButton.setOnAction(e -> disconnectFromServer());
        disconnectButton.setDisable(true); // Désactivé par défaut

        // Layout de l'interface utilisateur
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(outputArea, inputField, sendButton, connectButton, disconnectButton);

        // Création de la scène
        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Méthode pour établir une connexion avec le serveur.
     */
    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 5001); // Connexion au serveur
            socket.setSoTimeout(5000); // Timeout de 5 secondes
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            isConnected = true;

            // Mettre à jour l'interface utilisateur
            Platform.runLater(() -> {
                sendButton.setDisable(false);
                disconnectButton.setDisable(false);
                outputArea.appendText("Connecté au serveur.\n");
            });
        } catch (IOException e) {
            Platform.runLater(() -> {
                outputArea.appendText("Erreur de connexion au serveur : " + e.getMessage() + "\n");
                connectButton.setDisable(false); // Réactiver le bouton en cas d'erreur
            });
            e.printStackTrace();
        }
    }

    /**
     * Méthode pour se déconnecter du serveur.
     */
    private void disconnectFromServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            isConnected = false;

            // Mettre à jour l'interface utilisateur
            Platform.runLater(() -> {
                sendButton.setDisable(true);
                connectButton.setDisable(false);
                disconnectButton.setDisable(true);
                outputArea.appendText("Déconnecté du serveur.\n");
            });
        } catch (IOException e) {
            Platform.runLater(() -> outputArea.appendText("Erreur lors de la déconnexion : " + e.getMessage() + "\n"));
            e.printStackTrace();
        }
    }

    /**
     * Méthode pour envoyer une commande au serveur.
     */
    private void sendCommand() {
        if (!isConnected) {
            outputArea.appendText("Non connecté au serveur.\n");
            return;
        }

        String command = inputField.getText();
        if (command.isEmpty()) {
            outputArea.appendText("Veuillez entrer une commande.\n");
            return;
        }

        outputArea.appendText("Commande envoyée : " + command + "\n");

        // Utiliser un thread séparé pour envoyer la commande et recevoir la réponse
        new Thread(() -> {
            try {
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
                String finalResponse = responseBuilder.toString();
                Platform.runLater(() -> outputArea.appendText("Réponse du serveur :\n" + finalResponse + "\n"));
            } catch (SocketTimeoutException e) {
                Platform.runLater(() -> outputArea.appendText("Le serveur n'a pas répondu à temps.\n"));
            } catch (IOException e) {
                Platform.runLater(() -> outputArea.appendText("Erreur lors de l'envoi de la commande : " + e.getMessage() + "\n"));
                e.printStackTrace();
            }
        }).start();

        inputField.clear();
    }

    /**
     * Méthode principale pour lancer l'application.
     *
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        launch(args);
    }
}