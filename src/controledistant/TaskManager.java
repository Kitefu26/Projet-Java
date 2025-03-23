
package controledistant;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

/**
 * TaskManager gère l'exécution des tâches en arrière-plan et sur l'interface utilisateur.
 */
public class TaskManager {
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Exécute une tâche en arrière-plan.
     *
     * @param task La tâche à exécuter.
     */
    public static void executeBackgroundTask(Runnable task) {
        threadPool.execute(task);
    }

    /**
     * Exécute une tâche sur le thread de l'interface utilisateur.
     *
     * @param task La tâche à exécuter.
     */
    public static void executeUITask(Runnable task) {
        Platform.runLater(task);
    }

    /**
     * Arrête le pool de threads.
     */
    public static void shutdown() {
        threadPool.shutdown();
    }
}