package me.lutto.treasurebattle.manager

import me.lutto.treasurebattle.TreasureBattle
import me.lutto.treasurebattle.enchantment.CustomEnchantmentItem
import me.lutto.treasurebattle.instance.CustomItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

class ItemManager(private val treasureBattle: TreasureBattle) {

    private var itemList: MutableList<CustomItem> = mutableListOf()

    init {
        createInstaFurnace()
    }

    private fun createInstaFurnace() {
        val item = ItemStack(Material.FURNACE, 1)
        val meta = item.itemMeta
        meta.displayName(MiniMessage.miniMessage().deserialize("<obfuscated>A</obfuscated> <gold>ɪɴѕᴛᴀ ꜰᴜʀɴᴀᴄᴇ <white><obfuscated>A"))
        item.setItemMeta(meta)

        val customItem = CustomItem("insta_furnace", item)
        itemList.add(customItem)

        val shapedRecipe = ShapedRecipe(NamespacedKey.minecraft(customItem.getId()), item)
        if (Bukkit.getServer().getRecipe(NamespacedKey.minecraft(customItem.getId())) != null) {
            Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(customItem.getId()))
        }
        shapedRecipe.shape(
            "SSS",
            "SCS",
            "SSS"
        )
        shapedRecipe.setIngredient('S', Material.COBBLESTONE)
        shapedRecipe.setIngredient('C', Material.COAL)
        Bukkit.getServer().addRecipe(shapedRecipe)
    }

    fun registerEnchantmentItem(customEnchantment: CustomEnchantmentItem) {
        val item = ItemStack(customEnchantment.getMaterial(), 1)
        val itemMeta = item.itemMeta

        val key = NamespacedKey(treasureBattle, "custom_enchantment")
        itemMeta.persistentDataContainer[key, PersistentDataType.STRING] = customEnchantment.id

        itemMeta.displayName(customEnchantment.getItemName())
        itemMeta.lore(listOf(Component.text("${customEnchantment.name} I", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), customEnchantment.getItemLore()))
        itemMeta.addEnchant(customEnchantment.getPairedEnchantment(), customEnchantment.getPairedEnchantmentLevel(), false)
        item.setItemMeta(itemMeta)

        val customItem = CustomItem(customEnchantment.id, item)
        itemList.add(customItem)
    }

    fun isItem(item: ItemStack, id: String): Boolean {
        val key = NamespacedKey(treasureBattle, "custom_enchantment")
        if (item.itemMeta == null) return false
        if (!item.itemMeta.persistentDataContainer.has(key)) return false
        if (item.itemMeta.persistentDataContainer[key, PersistentDataType.STRING] != id) return false
        return true
    }

    fun getItem(id: String): CustomItem? {
        for (customItem in itemList) {
            if (customItem.getId() == id) return customItem
        }
        return null
    }

    fun getItemList(): MutableList<CustomItem> = itemList

}
