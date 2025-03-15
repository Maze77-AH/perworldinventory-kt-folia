package me.ebonjaeger.perworldinventory.service

import me.ebonjaeger.perworldinventory.PerWorldInventory
import me.ebonjaeger.perworldinventory.Utils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.scheduler.BukkitTask
import org.bukkit.plugin.Plugin
import javax.inject.Inject
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import org.bukkit.scheduler.BukkitRunnable
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

/**
 * Service for functions around the server the plugin is running on (scheduling tasks, etc.).
 *
 * @param plugin the plugin instance
 */
class BukkitService @Inject constructor(private val plugin: PerWorldInventory)
{

    private val scheduler = plugin.server.scheduler

    fun isShuttingDown() =
        plugin.isShuttingDown

    fun getOfflinePlayers(): Array<out OfflinePlayer> =
            Bukkit.getOfflinePlayers()

    fun runRepeatingTaskAsynchronously(plugin: Plugin, task: Runnable, delay: Long, period: Long): ScheduledTask =
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, Consumer { task.run() }, delay, period, java.util.concurrent.TimeUnit.MILLISECONDS)

    fun runTaskAsynchronously(plugin: Plugin, task: () -> Unit): ScheduledTask =
        Bukkit.getAsyncScheduler().runDelayed(plugin, Consumer { task() }, 0L, java.util.concurrent.TimeUnit.MILLISECONDS)
        

    /**
     * Run a task that may or may not be asynchronous depending on the
     * parameter passed to this function.
     *
     * @param task The task to run
     * @param async If the task should be run asynchronously
     */
    fun runTaskOptionallyAsynchronously(task: () -> Unit, async: Boolean): CompletableFuture<Unit> {
        return if (async) {
            CompletableFuture.runAsync(task).thenApply { Unit }
        } else {
            // Run synchronously on the main thread.
            fun runRepeatingTaskAsynchronously(plugin: Plugin, delay: Long, period: Long, task: Runnable): ScheduledTask {
                return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, Consumer { task.run() }, delay, period, java.util.concurrent.TimeUnit.MILLISECONDS)
            }
            CompletableFuture.completedFuture(Unit)
        }
    }
    
    

    fun runTask(plugin: Plugin, task: () -> Unit): BukkitTask =
        Bukkit.getScheduler().runTask(plugin, Runnable { task() })
}
