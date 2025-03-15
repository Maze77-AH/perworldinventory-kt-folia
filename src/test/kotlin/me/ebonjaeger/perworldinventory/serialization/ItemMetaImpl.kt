package me.ebonjaeger.perworldinventory.serialization

import com.google.common.collect.Multimap
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.components.CustomModelDataComponent
import org.bukkit.inventory.meta.components.EquippableComponent
import org.bukkit.inventory.meta.components.FoodComponent
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent
import org.bukkit.inventory.meta.components.ToolComponent
import org.bukkit.inventory.meta.components.UseCooldownComponent
import org.bukkit.inventory.meta.tags.CustomItemTagContainer
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataAdapterContext
import net.md_5.bungee.api.chat.BaseComponent
import net.kyori.adventure.text.Component
import java.util.*
import org.bukkit.persistence.PersistentDataType

// Define CustomModelDataComponent as a placeholder
class CustomModelDataComponent
class DummyPersistentDataContainer : PersistentDataContainer {
    override fun readFromBytes(p0: ByteArray, p1: Boolean) {
        throw NotImplementedError("Not implemented")
    }
    override fun serializeToBytes(): ByteArray {
        throw NotImplementedError("Not implemented")
    }
    override fun getAdapterContext(): PersistentDataAdapterContext {
        throw NotImplementedError("Not implemented")
    }
    // Backing map using a Pair of (NamespacedKey, PersistentDataType) as the key.
    private val data = mutableMapOf<Pair<NamespacedKey, PersistentDataType<*, *>>, Any?>()

    override fun <P : Any, C : Any> get(key: NamespacedKey, type: PersistentDataType<P, C>): C? {
        @Suppress("UNCHECKED_CAST")
        return data[Pair(key, type)] as? C
    }

    override fun <P : Any, C : Any> getOrDefault(
        key: NamespacedKey,
        type: PersistentDataType<P, C>,
        defaultValue: C
    ): C {
        return get(key, type) ?: defaultValue
    }

    override operator fun <P : Any, C : Any> set(
        key: NamespacedKey,
        type: PersistentDataType<P, C>,
        value: C
    ) {
        data[Pair(key, type)] = value
    }

    override fun remove(key: NamespacedKey) {
        data.keys.filter { it.first == key }.forEach { data.remove(it) }
    }

    override fun <P : Any, C : Any> has(key: NamespacedKey, type: PersistentDataType<P, C>): Boolean {
        return data.containsKey(Pair(key, type))
    }

    override fun has(key: NamespacedKey): Boolean {
        return data.keys.any { it.first == key }
    }

    override fun isEmpty(): Boolean = data.isEmpty()

    override fun getKeys(): MutableSet<NamespacedKey> =
        data.keys.map { it.first }.toMutableSet()

    override fun copyTo(destination: PersistentDataContainer, clear: Boolean) {
        // Iterate over each entry in our backing map and copy it to the destination.
        for ((keyPair, value) in data) {
            val (key, type) = keyPair
            // Suppress unchecked cast warnings; assume that our value has the correct type.
            @Suppress("UNCHECKED_CAST")
            destination[key, type as PersistentDataType<Any, Any>] = value as Any
        }
        if (clear) {
            data.clear()
        }
    }

    fun toContainer(): PersistentDataContainer = this
}

class ItemMetaTestImpl : ItemMeta, Damageable {
    override fun setMaxDamage(p0: Int?) {
        // No-op for testing
    }
    override fun getMaxDamage(): Int {
        return 0 // or your desired implementation
    }
    override fun hasMaxDamage(): Boolean {
        return false // or your desired implementation
    }
    override fun resetDamage() {
        // No-op for testing
    }
    override fun hasDamageValue(): Boolean {
        throw NotImplementedError("not implemented")
    }
    override fun getAttributeModifiers(attribute: Attribute): MutableCollection<AttributeModifier>? {
        throw NotImplementedError("Not implemented")
    }
    override fun getAttributeModifiers(slot: EquipmentSlot): Multimap<Attribute, AttributeModifier> {
        throw NotImplementedError("Not implemented")
    }
    override fun getAttributeModifiers(): Multimap<Attribute, AttributeModifier>? {
        throw NotImplementedError("Not implemented")
    }
    
    val providedMap: MutableMap<String, Any>

    private var version = 0

    /**
     * Default constructor.
     */
    constructor() {
        this.providedMap = mutableMapOf()
    }

    /**
     * Deserialization constructor, as used in
     * [org.bukkit.configuration.serialization.ConfigurationSerialization.getConstructor].
     */
    constructor(map: MutableMap<String, Any>) {
        this.providedMap = map
    }

    override fun setEnchantmentGlintOverride(override: Boolean?) {
        // No-op implementation for testing purposes.
    }    

    override fun getEnchantmentGlintOverride(): Boolean {
        return false;
    }

    override fun hasEnchantmentGlintOverride(): Boolean {
        return false;
    }

    override fun setItemModel(key: NamespacedKey?) {
        // No-op for testing purposes; or store the value if needed.
    }

    override fun getItemModel(): NamespacedKey? {
        return null
    }    

    override fun hasItemModel(): Boolean {
        // For testing, return false (or true if that makes sense for your tests)
        return false
    }    

    override fun setTooltipStyle(tooltipStyle: NamespacedKey?) {
        // No-op implementation for testing purposes
    }

    override fun getTooltipStyle(): NamespacedKey? {
        return null
    }

    override fun hasTooltipStyle(): Boolean {
        return false
    }    

    override fun setHideTooltip(hide: Boolean) {
        throw NotImplementedError("Not implemented")
    }    

    override fun isHideTooltip(): Boolean {
        throw NotImplementedError("Not implemented")
    }    

    override fun removeEnchantments() {
        throw NotImplementedError("Not implemented")
    }    

    override fun setEnchantable(enchantable: Int?) {
        throw NotImplementedError("Not implemented")
    }    

    override fun hasEnchantable(): Boolean {
        // Return a default value or your own logic.
        return false
    }    

    override fun getEnchantable(): Int {
        // You can either throw an exception if this should never be called in tests:
        throw NotImplementedError("Not implemented")
        
        // Or return a default value if you need a value for testing:
        // return 0
    }    

    override fun setCustomModelDataComponent(component: org.bukkit.inventory.meta.components.CustomModelDataComponent?) {
        throw NotImplementedError("Not implemented")
    }

    override fun getCustomModelDataComponent(): org.bukkit.inventory.meta.components.CustomModelDataComponent {
        throw NotImplementedError("not implemented")
    }

    override fun setLoreComponents(p0: MutableList<Array<BaseComponent>>?) {
        throw NotImplementedError("not implemented")
    }

    override fun getLoreComponents(): MutableList<Array<BaseComponent>>? {
        throw NotImplementedError("not implemented")
    }

    override fun lore(p0: MutableList<out Component>?): Unit {
        throw NotImplementedError("not implemented")
    }

    override fun lore(): MutableList<Component>? {
        throw NotImplementedError("not implemented")
    }

    override fun setItemName(name: String?) {
        throw NotImplementedError("Not implemented")
    }    

    override fun getItemName(): String {
        throw NotImplementedError("Not implemented")
    }    

    override fun setDisplayNameComponent(components: Array<out BaseComponent?>?) {
        throw NotImplementedError("not implemented")
    }

    override fun customName(value: Component?) {
        // Implement the setter logic here or throw an exception if not used
        throw NotImplementedError("not implemented")
    }

    override fun getDisplayNameComponent(): Array<BaseComponent> {
        // Implement logic here or simply throw an exception if not used
        throw NotImplementedError("not implemented")
    }

    override fun serialize(): MutableMap<String, Any> =
            HashMap(this.providedMap)

    override fun clone(): ItemMetaTestImpl {
        val mapClone = HashMap(providedMap)
        return ItemMetaTestImpl(mapClone)
    }

    override fun setDisplayName(name: String?) {
        throw NotImplementedError("not implemented")
    }

    override fun itemName(component: Component?) {
        throw NotImplementedError("not implemented")
    }

    override fun itemName(): Component {
        throw NotImplementedError("not implemented")
    }

    override fun getLore(): MutableList<String> {
        throw NotImplementedError("not implemented")
    }

    override fun setLore(lore: MutableList<String>?) {
        throw NotImplementedError("not implemented")
    }

    override fun customName(): Component? {
        throw NotImplementedError("not implemented")
    }  

    override fun hasEnchants(): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun setLocalizedName(name: String?) {
        throw NotImplementedError("not implemented")
    }

    override fun hasLore(): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun addItemFlags(vararg itemFlags: ItemFlag) {
        throw NotImplementedError("not implemented")
    }

    override fun hasDisplayName(): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun getItemFlags(): MutableSet<ItemFlag> {
        throw NotImplementedError("not implemented")
    }

    override fun setUnbreakable(unbreakable: Boolean) {
        throw NotImplementedError("not implemented")
    }

    override fun getDisplayName(): String {
        throw NotImplementedError("not implemented")
    }

    override fun getEnchants(): MutableMap<Enchantment, Int> {
        throw NotImplementedError("not implemented")
    }

    override fun getLocalizedName(): String {
        throw NotImplementedError("not implemented")
    }

    override fun isUnbreakable(): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun removeItemFlags(vararg itemFlags: ItemFlag) {
        throw NotImplementedError("not implemented")
    }    

    override fun hasLocalizedName(): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun getDamage(): Int
    {
        return 0
    }

    override fun hasDamage(): Boolean
    {
        throw NotImplementedError("not implemented")
    }

    override fun setDamage(damage: Int)
    {
        throw NotImplementedError("not implemented")
    }

    override fun addEnchant(ench: Enchantment, level: Int, ignoreLevelRestriction: Boolean): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun hasConflictingEnchant(ench: Enchantment): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun getCustomModelData(): Int {
        throw NotImplementedError("not implemented")
    }

    override fun setAttributeModifiers(attributeModifiers: Multimap<Attribute, AttributeModifier>?) {
        throw NotImplementedError("not implemented")
    }

    override fun removeAttributeModifier(attribute: Attribute): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun removeAttributeModifier(slot: EquipmentSlot): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun removeAttributeModifier(attribute: Attribute, modifier: AttributeModifier): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun hasCustomName(): Boolean {
        throw NotImplementedError("not implemented")
    }    

    override fun hasCustomModelData(): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun addAttributeModifier(attribute: Attribute, modifier: AttributeModifier): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun getEnchantLevel(ench: Enchantment): Int {
        throw NotImplementedError("not implemented")
    }

    override fun setVersion(version: Int) {
        this.version = version
    }

    override fun hasEnchant(ench: Enchantment): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun setCustomModelData(data: Int?) {
        throw NotImplementedError("not implemented")
    }

    override fun hasAttributeModifiers(): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun getCustomTagContainer(): CustomItemTagContainer {
        throw NotImplementedError("not implemented")
    }

    override fun hasItemName(): Boolean {
        throw NotImplementedError("not implemented")
    }    

    override fun hasItemFlag(flag: ItemFlag): Boolean {
        throw NotImplementedError("not implemented")
    }

    override fun removeEnchant(ench: Enchantment): Boolean {
        throw NotImplementedError("not implemented")
    }

    // Glider
    override fun isGlider(): Boolean {
        return false
    }

    override fun setGlider(glider: Boolean) {
        // No-op for testing
    }

    // Fire-resistant (deprecated)
    @Deprecated("Use getDamageResistant() instead", ReplaceWith("getDamageResistant()"))
    override fun isFireResistant(): Boolean {
        return false
    }

    @Deprecated("Use setDamageResistant() instead", ReplaceWith("setDamageResistant(tag)"))
    override fun setFireResistant(fireResistant: Boolean) {
        // No-op for testing
    }

    // Damage resistance
    override fun hasDamageResistant(): Boolean {
        return false
    }

    override fun getDamageResistant(): Tag<DamageType>? {
        return null
    }

    override fun setDamageResistant(tag: Tag<DamageType>?) {
        // No-op for testing
    }

    // Max stack size
    override fun hasMaxStackSize(): Boolean {
        return false
    }

    override fun getMaxStackSize(): Int {
        return 64 // default value
    }

    override fun setMaxStackSize(max: Int?) {
        // No-op for testing
    }

    // Rarity
    override fun hasRarity(): Boolean {
        return false
    }

    override fun getRarity(): ItemRarity {
        throw NotImplementedError("Not implemented")
    }

    override fun setRarity(rarity: ItemRarity?) {
        // No-op for testing
    }

    // Use remainder
    override fun hasUseRemainder(): Boolean {
        return false
    }

    override fun getUseRemainder(): ItemStack? {
        return null
    }

    override fun setUseRemainder(remainder: ItemStack?) {
        // No-op for testing
    }

    // Use cooldown
    override fun hasUseCooldown(): Boolean {
        return false
    }

    override fun getUseCooldown(): UseCooldownComponent {
        throw NotImplementedError("Not implemented")
    }

    override fun setUseCooldown(cooldown: UseCooldownComponent?) {
        // No-op for testing
    }

    // Food
    override fun hasFood(): Boolean {
        return false
    }

    override fun getFood(): FoodComponent {
        throw NotImplementedError("Not implemented")
    }

    override fun setFood(food: FoodComponent?) {
        // No-op for testing
    }

    // Tool
    override fun hasTool(): Boolean {
        return false
    }

    override fun getTool(): ToolComponent {
        throw NotImplementedError("Not implemented")
    }

    override fun setTool(tool: ToolComponent?) {
        // No-op for testing
    }

    // Equippable
    override fun hasEquippable(): Boolean {
        return false
    }

    override fun getEquippable(): EquippableComponent {
        throw NotImplementedError("Not implemented")
    }

    override fun setEquippable(equippable: EquippableComponent?) {
        // No-op for testing
    }

    // Jukebox playable
    override fun hasJukeboxPlayable(): Boolean {
        return false
    }

    override fun getJukeboxPlayable(): JukeboxPlayableComponent {
        throw NotImplementedError("Not implemented")
    }

    override fun setJukeboxPlayable(jukeboxPlayable: JukeboxPlayableComponent?) {
        // No-op for testing
    }

    // Rarity (legacy getters/setters already implemented above)
    override fun getAsString(): String {
        return "{}"
    }

    override fun getAsComponentString(): String {
        return "[]"
    }

    @Deprecated("This API is unsupported and will be replaced", ReplaceWith("emptySet()"))
    override fun getCanDestroy(): Set<org.bukkit.Material> = emptySet()

    @Deprecated("This API is unsupported and will be replaced")
    override fun setCanDestroy(canDestroy: Set<org.bukkit.Material>) { /* No-op */ }

    @Deprecated("This API is unsupported and will be replaced", ReplaceWith("emptySet()"))
    override fun getCanPlaceOn(): Set<org.bukkit.Material> = emptySet()

    @Deprecated("This API is unsupported and will be replaced")
    override fun setCanPlaceOn(canPlaceOn: Set<org.bukkit.Material>) { /* No-op */ }

    @Deprecated("This API is unsupported and will be replaced", ReplaceWith("emptySet()"))
    override fun getDestroyableKeys(): Set<com.destroystokyo.paper.Namespaced> = emptySet()

    @Deprecated("This API is unsupported and will be replaced")
    override fun setDestroyableKeys(canDestroy: Collection<com.destroystokyo.paper.Namespaced>) { /* No-op */ }

    @Deprecated("This API is unsupported and will be replaced", ReplaceWith("emptySet()"))
    override fun getPlaceableKeys(): Set<com.destroystokyo.paper.Namespaced> = emptySet()

    @Deprecated("This API is unsupported and will be replaced")
    override fun setPlaceableKeys(canPlaceOn: Collection<com.destroystokyo.paper.Namespaced>) { /* No-op */ }

    @Deprecated("This API is unsupported and will be replaced")
    override fun hasPlaceableKeys(): Boolean = false

    @Deprecated("This API is unsupported and will be replaced")
    override fun hasDestroyableKeys(): Boolean = false

    override fun getPersistentDataContainer(): PersistentDataContainer = DummyPersistentDataContainer()
}
