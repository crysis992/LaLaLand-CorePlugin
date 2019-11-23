package de.lalaland.core.modules.combat.stats;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import de.lalaland.core.modules.combat.items.StatItem;
import de.lalaland.core.utils.common.UtilMath;
import de.lalaland.core.utils.common.UtilPlayer;
import de.lalaland.core.utils.holograms.MovingHologram;
import de.lalaland.core.utils.holograms.impl.HologramManager;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of LaLaLand-CorePlugin and was created at the 16.11.2019
 *
 * LaLaLand-CorePlugin can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class CombatDamageListener implements Listener {

  private static final int HOLOGRAM_LIFE_TICKS = 30;
  private static final Vector BASE_HOLOGRAM_VELOCITY = new Vector(0, 0.075, 0);
  private static final Vector BASE_SCALAR_XZ = new Vector(1, 0, 1);
  private static final ImmutableMap<DamageCause, Double> ENVIRONMENTAL_BASE_PERCENTAGE = ImmutableMap.<DamageCause, Double>builder()
      .put(DamageCause.BLOCK_EXPLOSION, 25D)
      .put(DamageCause.CONTACT, 2.5D)
      .put(DamageCause.CRAMMING, 15D)
      .put(DamageCause.DRAGON_BREATH, 20D)
      .put(DamageCause.DROWNING, 33.33D)
      .put(DamageCause.DRYOUT, 50D)
      .put(DamageCause.ENTITY_EXPLOSION, 25D)
      .put(DamageCause.FALL, 12.5D)
      .put(DamageCause.FALLING_BLOCK, 15D)
      .put(DamageCause.FIRE, 17.5D)
      .put(DamageCause.FIRE_TICK, 10D)
      .put(DamageCause.FLY_INTO_WALL, 50D)
      .put(DamageCause.HOT_FLOOR, 9D)
      .put(DamageCause.LAVA, 20D)
      .put(DamageCause.LIGHTNING, 50D)
      .put(DamageCause.MAGIC, 15D)
      .put(DamageCause.MELTING, 33.33D)
      .put(DamageCause.POISON, 7.5D)
      .put(DamageCause.PROJECTILE, 10D)
      .put(DamageCause.STARVATION, 50D)
      .put(DamageCause.SUFFOCATION, 45D)
      .put(DamageCause.SUICIDE, 100D)
      .put(DamageCause.THORNS, 7.5D)
      .put(DamageCause.VOID, 50D)
      .put(DamageCause.WITHER, 8.5D)
      .build();

  public CombatDamageListener(final CombatStatManager combatStatManager,
      final HologramManager hologramManager) {
    this.combatStatManager = combatStatManager;
    critChanceRandom = ThreadLocalRandom.current();
    this.hologramManager = hologramManager;
  }

  private final CombatStatManager combatStatManager;
  private final ThreadLocalRandom critChanceRandom;
  private final HologramManager hologramManager;

  @EventHandler
  public void onDamage(final EntityDamageEvent event) {
    if (event instanceof EntityDamageByEntityEvent) {
      return;
    }
    final Entity defender = event.getEntity();
    if (!(defender instanceof LivingEntity)) {
      return;
    }
    final LivingEntity defenderLiving = (LivingEntity) defender;
    double damage = event.getDamage() * ENVIRONMENTAL_BASE_PERCENTAGE.get(event.getCause());
    damage *= (100D / defenderLiving.getHealth());
    final CombatStatHolder holderDefender = combatStatManager
        .getCombatStatHolder(defender.getUniqueId());
    damage = DamageEvaluator
        .calculateDamage(holderDefender, damage, CombatDamageType.ofBukkit(event.getCause()));

    event.setDamage(0);

    double healthLeft = defenderLiving.getHealth() - damage;
    if (healthLeft < 0) {
      healthLeft = 0;
    }

    defenderLiving.setHealth(healthLeft);

  }

  @EventHandler
  public void onHeal(final EntityRegainHealthEvent event) {
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    createHealingHologram((LivingEntity) event.getEntity(), UtilMath.cut(event.getAmount(), 1));
  }

  @EventHandler
  public void onCombat(final EntityDamageByEntityEvent event) {
    Entity attacker = event.getDamager();
    final Entity defender = event.getEntity();

    boolean isRanged = false;

    if (attacker instanceof Projectile) {
      isRanged = true;
      final ProjectileSource source = ((Projectile) attacker).getShooter();
      if (source instanceof Entity) {
        attacker = (Entity) source;
      } else {
        //TODO Evaluate random projectiles
      }
    }

    if (!(attacker instanceof LivingEntity && defender instanceof LivingEntity)) {
      return;
    }

    final LivingEntity defenderLiving = (LivingEntity) defender;
    final LivingEntity attackerLiving = (LivingEntity) attacker;
    final CombatStatHolder attackHolder = combatStatManager.getCombatStatHolder(attackerLiving);
    final CombatStatHolder defenceHolder = combatStatManager.getCombatStatHolder(defenderLiving);
    final boolean isPlayerAttacker = attacker instanceof Player;

    event.setDamage(0);
    double damage = attackHolder
        .getStatValue(isRanged ? CombatStat.RANGE_DAMAGE : CombatStat.MEELE_DAMAGE);

    final boolean crit = attackHolder.getStatValue(CombatStat.CRIT_CHANCE) >= critChanceRandom
        .nextDouble(0, 100);

    if (crit) {
      final double dmgMulti = (1D / 100D) * attackHolder.getStatValue(CombatStat.CRIT_DAMAGE);
      damage *= dmgMulti;
    }

    damage = DamageEvaluator
        .calculateDamage(defenceHolder, damage, CombatDamageType.ofBukkit(event.getCause()));

    ItemStack attackItem = attackerLiving.getActiveItem();
    if (isPlayerAttacker) {
      final float multi = UtilPlayer.getAttackCooldown((Player) attacker);
      if (multi < 0.9F) {
        damage *= 0.05;
      } else {
        damage *= multi;
      }
      attackItem = ((Player) attacker).getInventory().getItemInMainHand();
    }
    if (attackItem != null && attackItem.getType() != Material.AIR) {
      final StatItem statItem = StatItem.of(attackItem);
      if (statItem.isItemStatComponent()) {
        final Integer durability = statItem.getDurability();
        if (durability != null) {
          Preconditions.checkState(durability >= 0, "Item durability is below 0");
          if (durability == 0) {
            damage *= 0.025;
          } else if (durability > 0) {
            final int leftDurability = durability - 1;
            if (leftDurability == 0) {
              // TODO effect for breaking tool
            }
            statItem.setDurability(leftDurability);
            if (isPlayerAttacker) {
              ((Player) attacker).getInventory().setItemInMainHand(statItem.getItemStack());
            } else {
              attackerLiving.getEquipment().setItemInMainHand(statItem.getItemStack());
            }
          }
        }
      }
    }

    damage = UtilMath.cut(damage, 1);

    if (isPlayerAttacker) {
      final Player attackerPlayer = (Player) attacker;
      createDamageHologram(attackerPlayer, defender, crit, damage);
    }

    double healthLeft = defenderLiving.getHealth() - damage;
    if (healthLeft <= 0) {
      healthLeft = 0;
    }
    defenderLiving.setHealth(healthLeft);

  }

  private void createDamageHologram(final Player attackerPlayer, final Entity def,
      final boolean crit, final double dmg) {
    final String holoMsg = (crit ? "§c" : "§e") + dmg;
    final Location defLoc = def.getLocation().clone().add(0, 0.5, 0);
    final Vector directionAdjustVec = attackerPlayer.getLocation().getDirection().clone()
        .multiply(BASE_SCALAR_XZ).normalize().multiply(0.05);
    final Vector holoVel = BASE_HOLOGRAM_VELOCITY.clone().add(directionAdjustVec);
    final MovingHologram moving = hologramManager
        .createMovingHologram(defLoc, holoVel, HOLOGRAM_LIFE_TICKS);
    moving.getHologram().appendTextLine(holoMsg);
  }

  private void createHealingHologram(final LivingEntity livingDefender, final double heal) {
    final String holoMsg = "§a+" + heal;
    final Location defLoc = livingDefender.getLocation().clone().add(0, 0.5, 0);
    final Vector holoVel = BASE_HOLOGRAM_VELOCITY.clone().multiply(1.2);
    final MovingHologram moving = hologramManager
        .createMovingHologram(defLoc, holoVel, HOLOGRAM_LIFE_TICKS);
    moving.getHologram().appendTextLine(holoMsg);
  }

}
