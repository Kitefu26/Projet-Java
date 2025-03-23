Projet : Contrôle d'Ordinateur à Distance en Java

Description

Ce projet est une application client-serveur permettant le contrôle à distance d'un ordinateur via des commandes textuelles, similaire à SSH. Le client envoie une commande au serveur, qui l'exécute localement et renvoie la réponse.

Fonctionnalités

Client JavaFX : Interface graphique permettant de se connecter au serveur et d'envoyer des commandes.

Serveur Java : Reçoit les commandes, les exécute et renvoie les résultats.

Communication TCP/IP : Utilisation des sockets pour échanger les commandes et réponses.

Gestion des threads : Le serveur gère plusieurs clients simultanément.

Prérequis

Java 8 ou version ultérieure

NetBeans (ou tout autre IDE compatible)

XAMPP (si utilisation d'une base de données MySQL en option)

Installation

Cloner le dépôt Git :

git clone (https://github.com/Kitefu26/Projet-Java.git)
cd Projet-Java

Compiler et exécuter le serveur :

javac controledistant/Serveur.java
java controledistant.Serveur

Compiler et exécuter le client JavaFX :

javac controledistant/ClientFX.java
java controledistant.ClientFX

Explication des classes

ClientFX.java (Côté Client)

Interface graphique pour envoyer des commandes au serveur.

Gestion de la connexion et des déconnexions.

Affichage des réponses du serveur.

Client.java (Client en mode console)

Client fonctionnant en ligne de commande.

Envoi des commandes au serveur et affichage des résultats.

Serveur.java (Côté Serveur)

Ecoute les connexions entrantes via un ServerSocket.

Exécute les commandes reçues et envoie les résultats aux clients.

Gère plusieurs clients avec des threads.

ServeurFX.java (Serveur avec Interface Graphique)

Affiche la liste des clients connectés.

Journalise les commandes reçues et exécutées.

Permet de démarrer et arrêter le serveur via une interface JavaFX.

TaskManager.java

Gère l'exécution des tâches en arrière-plan et sur le thread de l'interface utilisateur.

Exécution

Lancer le serveur en premier.

Ouvrir plusieurs clients et tester l'envoi de commandes.

Observer l'exécution des commandes et les résultats affichés.

Améliorations possibles

Ajout d'une authentification des utilisateurs.

Chiffrement des communications avec SSL/TLS.

Possibilité d'envoyer des fichiers entre client et serveur.

Auteur

Projet réalisé dans le cadre du module Java Avancé à l'Ecole Supérieure Polytechnique de Dakar.
