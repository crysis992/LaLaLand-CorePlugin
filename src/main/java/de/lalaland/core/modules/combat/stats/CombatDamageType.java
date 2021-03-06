package de.lalaland.core.modules.combat.stats;


import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of LaLaLand-CorePlugin and was created at the 16.11.2019
 *
 * LaLaLand-CorePlugin can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
@AllArgsConstructor
public enum CombatDamageType {

  PHYSICAL("Physischer Schaden", CombatStat.PHYSICAL_ARMOR, "§f" + (char) 0x2666),
  MYSTIC("Mystischer Schaden", CombatStat.MYSTIC_ARMOR, "§9" + (char) 0x1F7C6),
  BIO("Biologischer Schaden", CombatStat.BIO_ARMOR, "§2§l" + (char) 0x2623);

  @Getter
  private final String displayName;
  @Getter
  private final CombatStat defendingStat;
  @Getter
  private final String indicator;

  public static CombatDamageType ofBukkit(final DamageCause cause) {
    return VANILLA_MAPPINGS.get(cause);
  }

  private static final ImmutableMap<DamageCause, CombatDamageType> VANILLA_MAPPINGS =
      ImmutableMap.<DamageCause, CombatDamageType>builder()
          .put(DamageCause.BLOCK_EXPLOSION, CombatDamageType.PHYSICAL)
          .put(DamageCause.CONTACT, CombatDamageType.PHYSICAL)
          .put(DamageCause.CRAMMING, CombatDamageType.PHYSICAL)
          .put(DamageCause.CUSTOM, CombatDamageType.PHYSICAL)
          .put(DamageCause.DRAGON_BREATH, CombatDamageType.MYSTIC)
          .put(DamageCause.DROWNING, CombatDamageType.BIO)
          .put(DamageCause.DRYOUT, CombatDamageType.BIO)
          .put(DamageCause.ENTITY_ATTACK, CombatDamageType.PHYSICAL)
          .put(DamageCause.ENTITY_EXPLOSION, CombatDamageType.PHYSICAL)
          .put(DamageCause.ENTITY_SWEEP_ATTACK, CombatDamageType.PHYSICAL)
          .put(DamageCause.FALL, CombatDamageType.PHYSICAL)
          .put(DamageCause.FALLING_BLOCK, CombatDamageType.PHYSICAL)
          .put(DamageCause.FIRE, CombatDamageType.MYSTIC)
          .put(DamageCause.FIRE_TICK, CombatDamageType.MYSTIC)
          .put(DamageCause.FLY_INTO_WALL, CombatDamageType.PHYSICAL)
          .put(DamageCause.HOT_FLOOR, CombatDamageType.MYSTIC)
          .put(DamageCause.LAVA, CombatDamageType.MYSTIC)
          .put(DamageCause.LIGHTNING, CombatDamageType.MYSTIC)
          .put(DamageCause.MAGIC, CombatDamageType.MYSTIC)
          .put(DamageCause.MELTING, CombatDamageType.BIO)
          .put(DamageCause.POISON, CombatDamageType.BIO)
          .put(DamageCause.PROJECTILE, CombatDamageType.PHYSICAL)
          .put(DamageCause.STARVATION, CombatDamageType.BIO)
          .put(DamageCause.SUFFOCATION, CombatDamageType.BIO)
          .put(DamageCause.SUICIDE, CombatDamageType.BIO)
          .put(DamageCause.THORNS, CombatDamageType.MYSTIC)
          .put(DamageCause.VOID, CombatDamageType.MYSTIC)
          .put(DamageCause.WITHER, CombatDamageType.MYSTIC)
          .build();

}
