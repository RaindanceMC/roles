package de.raindancer.modules.roles.util;

import de.raindancer.core.platform.util.Scheduling;
import de.raindancer.modules.roles.RolesServices;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Choosing, giving and paying for roles write the network's documents: that is not for the server's threads. */
public final class OffThread {

    private OffThread() {
    }

    /** Runs {@code work} off the server's threads. */
    public static void run(RolesServices live, Runnable work) {
        Scheduling.async(live.plugin(), work);
    }

    /**
     * Staff database work, and what it says once done: on the player's own thread. The console and RCON only collect
     * what is said while the command runs, so for them the command waits for the work (a moment; it is a staff
     * command) rather than answering into nothing.
     */
    public static <T> void then(RolesServices live, CommandSender sender, Supplier<T> work, Consumer<T> say) {
        if (sender instanceof Player player) {
            Scheduling.async(live.plugin(), () -> {
                T done = work.get();
                Scheduling.entity(live.plugin(), player, () -> say.accept(done));
            });
            return;
        }
        CompletableFuture<T> done = new CompletableFuture<>();
        Scheduling.async(live.plugin(), () -> {
            try {
                done.complete(work.get());
            } catch (RuntimeException failed) {
                done.completeExceptionally(failed);
            }
        });
        try {
            say.accept(done.get(15, TimeUnit.SECONDS));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException failed) {
            live.messages().send(sender, "roles.not-saved");
        }
    }
}
