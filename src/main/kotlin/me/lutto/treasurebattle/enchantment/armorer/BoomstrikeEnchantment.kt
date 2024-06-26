package me.lutto.treasurebattle.enchantment.armorer

import me.lutto.treasurebattle.TreasureBattle
import me.lutto.treasurebattle.enchantment.CustomEnchantmentItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.random.Random

class BoomstrikeEnchantment(private val treasureBattle: TreasureBattle) : CustomEnchantmentItem(
    "boomstrike",
    "Boomstrike",
    EnchantmentTarget.WEARABLE) {

    init {
        treasureBattle.itemManager.registerEnchantmentItem(this)
    }

    private fun getEntityFromEntityDirection(entity: Entity, lookingLocation: Location): Location? {
        if (entity.world !== lookingLocation.world) return null

        val fromLocation: Location = entity.location
        val xDifference: Double = lookingLocation.x - fromLocation.x
        val yDifference: Double = lookingLocation.y - fromLocation.y
        val zDifference: Double = lookingLocation.z - fromLocation.z
        val distanceXZ = sqrt(xDifference * xDifference + zDifference * zDifference)
        val distanceY = sqrt(distanceXZ * distanceXZ + yDifference * yDifference)
        var yaw = Math.toDegrees(acos(xDifference / distanceXZ))
        val pitch = Math.toDegrees(acos(yDifference / distanceY)) - 90.0

        if (zDifference < 0.0) yaw += abs(180.0 - yaw) * 2.0

        val entityFromEntityVector: Location = entity.location
        entityFromEntityVector.yaw = (yaw - 90.0f).toFloat()
        entityFromEntityVector.pitch = (pitch - 90.0f).toFloat()
        return entityFromEntityVector
    }

    private fun explode(player: Player) {
        player.world.strikeLightningEffect(player.location)

        for (nearbyEntity in player.getNearbyEntities(5.0, 5.0, 5.0)) {
            if (nearbyEntity !is Player) return
            val nearbyPlayer: Player = nearbyEntity

            val armorStand: ArmorStand = player.world.spawnEntity(nearbyPlayer.location, EntityType.ARMOR_STAND) as ArmorStand
            armorStand.isInvisible = true

            val entityFromEntityDirection = getEntityFromEntityDirection(armorStand, player.location)
            armorStand.headPose = EulerAngle(
                Math.toRadians(entityFromEntityDirection!!.pitch.toDouble()), Math.toRadians(
                    entityFromEntityDirection.yaw.toDouble()
                ), 0.0
            )
            nearbyPlayer.velocity = Vector(armorStand.location.direction.x * -1.0, 1.0, armorStand.location.direction.z * -1.0)
            armorStand.remove()

            nearbyPlayer.damage(3.0)
        }
    }

    @EventHandler
    fun forTest(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        val player: Player = event.entity as Player
        if (!player.isBlocking) return
        if (event.finalDamage != 0.0) return
        val shield: ItemStack =
            if (!player.inventory.itemInOffHand.isEmpty) {
                player.inventory.itemInOffHand
            } else if (!player.inventory.itemInMainHand.isEmpty) {
                player.inventory.itemInMainHand
            } else return

        if (!treasureBattle.itemManager.isItem(shield, this.id)) return

        val randomInt: Int = Random.nextInt(1, 100)
        if (randomInt < 80) return

        explode(player)
    }

    override fun getMaterial(): Material = Material.SHIELD
    override fun getPairedEnchantment(): Enchantment = Enchantment.DURABILITY
    override fun getItemName(): Component = MiniMessage.miniMessage().deserialize("<gradient:#7a0000:#c70000>ʙᴏᴏᴍѕᴛʀɪᴋᴇ ѕʜɪᴇʟᴅ").decoration(TextDecoration.ITALIC, false)
    override fun getItemLore(): Component = MiniMessage.miniMessage().deserialize("Explosive defense!").color(NamedTextColor.DARK_GRAY)

}