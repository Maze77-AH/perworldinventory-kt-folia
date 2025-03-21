package me.ebonjaeger.perworldinventory.data

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.ebonjaeger.perworldinventory.ConsoleLogger
import me.ebonjaeger.perworldinventory.Group
import me.ebonjaeger.perworldinventory.configuration.PlayerSettings
import me.ebonjaeger.perworldinventory.configuration.PluginSettings
import me.ebonjaeger.perworldinventory.configuration.Settings
import me.ebonjaeger.perworldinventory.event.InventoryLoadCompleteEvent
import me.ebonjaeger.perworldinventory.service.BukkitService
import me.ebonjaeger.perworldinventory.service.EconomyService
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import org.bukkit.scheduler.BukkitTask
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import javax.inject.Inject
import org.bukkit.scheduler.BukkitScheduler

class ProfileManager @Inject constructor(private val bukkitService: BukkitService,
                                         private val dataSource: DataSource,
                                         private val settings: Settings,
                                         private val economyService: EconomyService,
                                         private val profileFactory: ProfileFactory)
{

    private val profileCache: Cache<ProfileKey, PlayerProfile> = CacheBuilder.newBuilder()
            .expireAfterAccess(settings.getProperty(PluginSettings.CACHE_DURATION).toLong(), TimeUnit.MINUTES)
            .maximumSize(settings.getProperty(PluginSettings.CACHE_MAX_LIMIT).toLong())
            .build()

    private val separateGameModes = settings.getProperty(PluginSettings.SEPARATE_GM_INVENTORIES)

    /**
     * Save a player in the database. Their profile will be cached for a period of time in order to save on disk I/O,
     * but they will still be saved immediately. Profiles will be saved asynchronously unless the server is shutting
     * down, in which case they wont be as plugins cannot schedule tasks in this state.
     *
     * @param player The player to store
     * @param group The group the player was in
     * @param gameMode The GameMode the player was in
     */
    fun addPlayerProfile(player: Player, group: Group, gameMode: GameMode)
    {
        val gm = when {
            separateGameModes -> gameMode
            else -> GameMode.SURVIVAL
        }

        val key = ProfileKey(player.uniqueId, group, gm)
        val profile = profileFactory.create(player)
        profileCache.put(key, profile)

        ConsoleLogger.fine("ProfileManager: Saving player '${player.name}' to database")
        ConsoleLogger.debug("ProfileManager: Key is: $key")

        if (!bukkitService.isShuttingDown())
        {
            fun runAsyncDelayed(plugin: Plugin, delay: Long, task: () -> Unit) {
                object : BukkitRunnable() {
                    override fun run() {
                        task()
                    }
                }.runTaskLaterAsynchronously(plugin, delay / 50)
            }
        } else
        {
            dataSource.savePlayer(key, profile)
        }
    }

    /**
     * Get a player's data for a given [Group] and [GameMode], and set it to the player.
     *
     * @param player The player to get stuff for
     * @param group The world group to load from
     * @param gameMode Which GameMode inventory to load
     */
    // TODO: This should return a PlayerProfile instead of just setting stuff
    fun getPlayerData(player: Player, group: Group, gameMode: GameMode)
    {
        val gm = when {
            separateGameModes -> gameMode
            else -> GameMode.SURVIVAL
        }

        val key = ProfileKey(player.uniqueId, group, gm)

        ConsoleLogger.fine("ProfileManager: Checking cache for player data for '${player.name}'")
        ConsoleLogger.debug("ProfileManager: Checking with key: $key")
        val cached = profileCache.getIfPresent(key)
        if (cached != null)
        {
            applyToPlayer(player, cached)

            val event = InventoryLoadCompleteEvent(player, group, gm)
            Bukkit.getPluginManager().callEvent(event)

            return
        }

        ConsoleLogger.fine("ProfileManager: Player '${player.name}' not in cache, loading from disk")
        val data = dataSource.getPlayer(key, player)
        if (data != null)
        {
            applyToPlayer(player, data)
        } else
        {
            applyDefaults(player)
        }

        val event = InventoryLoadCompleteEvent(player, group, gm)
        Bukkit.getPluginManager().callEvent(event)
    }

    /**
     * Invalidate all entries in the cache.
     *
     * This ensures that old entries aren't lingering around after a plugin
     * reload occurs, where groups and worlds might change.
     */
    fun invalidateCache()
    {
        profileCache.invalidateAll()
    }

    private fun applyToPlayer(player: Player, profile: PlayerProfile)
    {
        // Transfer simple properties
        for (value in PlayerProperty.values()) run {
            value.applyFromProfileToPlayerIfConfigured(profile, player, settings)
        }

        transferInventories(player, profile)
        transferHealth(player, profile)
        transferPotionEffects(player, profile)
        transferFlying(player, profile)

        economyService.setNewBalance(player, profile.balance)
    }

    private fun transferInventories(player: Player, profile: PlayerProfile)
    {
        if (settings.getProperty(PlayerSettings.LOAD_INVENTORY))
        {
            player.inventory.clear()
            player.inventory.contents = profile.inventory
            player.inventory.setArmorContents(profile.armor)
        }
        if (settings.getProperty(PlayerSettings.LOAD_ENDER_CHEST))
        {
            player.enderChest.clear()
            player.enderChest.contents = profile.enderChest
        }
    }

    private fun transferHealth(player: Player, profile: PlayerProfile)
    {
        if (!settings.getProperty(PlayerSettings.LOAD_HEALTH)) {
            return
        }

        player.getAttribute(Attribute.MAX_HEALTH)!!.baseValue = profile.maxHealth // Players have max health, this wont be null

        if (profile.health > 0 && profile.health <= profile.maxHealth) {
            player.health = profile.health
        } else {
            player.health = profile.maxHealth
        }
    }

    private fun transferPotionEffects(player: Player, profile: PlayerProfile)
    {
        if (settings.getProperty(PlayerSettings.LOAD_POTION_EFFECTS))
        {
            player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
            player.addPotionEffects(profile.potionEffects)
        }
    }

    /*
     * Flying needs some special handling because for some reason, it is
     * possible for players to not be able to fly when trying to set their
     * flight mode to true.
     *
     * If the player was flying when they're profile was saved, then it stands
     * to reason that they should be able to fly when going back to the same group.
     * Therefore, set their allow flight to true before setting their fly mode.
     */
    private fun transferFlying(player: Player, profile: PlayerProfile) {
        if (settings.getProperty(PlayerSettings.LOAD_FLYING)) {

            if (profile.isFlying && !player.allowFlight) {
                player.allowFlight = true
            }

            player.isFlying = profile.isFlying
        }
    }

    /**
     * Set a player to the base defaults, based on the plugin configuration.
     *
     * @param player The Player to affect
     */
    private fun applyDefaults(player: Player)
    {
        // Time for a massive line of if-statements . . .
        if (settings.getProperty(PlayerSettings.LOAD_INVENTORY))
        {
            player.inventory.clear()
        }
        if (settings.getProperty(PlayerSettings.LOAD_ENDER_CHEST))
        {
            player.enderChest.clear()
        }
        if (settings.getProperty(PlayerSettings.LOAD_EXHAUSTION))
        {
            player.exhaustion = PlayerDefaults.EXHAUSTION
        }
        if (settings.getProperty(PlayerSettings.LOAD_EXP))
        {
            player.exp = PlayerDefaults.EXPERIENCE
        }
        if (settings.getProperty(PlayerSettings.LOAD_HUNGER))
        {
            player.foodLevel = PlayerDefaults.FOOD_LEVEL
        }
        if (settings.getProperty(PlayerSettings.LOAD_HEALTH))
        {
            player.getAttribute(Attribute.MAX_HEALTH)!!.baseValue = PlayerDefaults.HEALTH // Players have max health, this wont be null
            player.health = PlayerDefaults.HEALTH
        }
        if (settings.getProperty(PlayerSettings.LOAD_LEVEL))
        {
            player.level = PlayerDefaults.LEVEL
        }
        if (settings.getProperty(PlayerSettings.LOAD_SATURATION))
        {
            player.saturation = PlayerDefaults.SATURATION
        }
        if (settings.getProperty(PlayerSettings.LOAD_FALL_DISTANCE))
        {
            player.fallDistance = PlayerDefaults.FALL_DISTANCE
        }
        if (settings.getProperty(PlayerSettings.LOAD_FIRE_TICKS))
        {
            player.fireTicks = PlayerDefaults.FIRE_TICKS
        }
        if (settings.getProperty(PlayerSettings.LOAD_MAX_AIR))
        {
            player.maximumAir = PlayerDefaults.MAXIMUM_AIR
        }
        if (settings.getProperty(PlayerSettings.LOAD_REMAINING_AIR))
        {
            player.remainingAir = PlayerDefaults.REMAINING_AIR
        }
        if (settings.getProperty(PlayerSettings.LOAD_POTION_EFFECTS))
        {
            player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
        }

        economyService.withdrawFromPlayer(player)
    }
}
