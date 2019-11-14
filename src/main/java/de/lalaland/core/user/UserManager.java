package de.lalaland.core.user;

import de.lalaland.core.CorePlugin;
import de.lalaland.core.user.listener.PlayerJoinQuit;
import de.lalaland.core.user.task.RemoveOfflineUserThread;
import de.lalaland.core.user.task.SaveUserDataThread;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;

/*******************************************************
 * Copyright (C) 2015-2019 Piinguiin neuraxhd@gmail.com
 *
 * This file is part of CorePlugin and was created at the 13.11.2019
 *
 * CorePlugin can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class UserManager {

  private final CorePlugin corePlugin;
  @Getter
  private final Object2ObjectOpenHashMap<UUID, User> cachedUsers;

  /**
   * Instantiates a new User manager.
   *
   * @param corePlugin the core plugin
   */
  public UserManager(final CorePlugin corePlugin) {
    this.corePlugin = corePlugin;
    cachedUsers = new Object2ObjectOpenHashMap<>();
    corePlugin.registerListener(new PlayerJoinQuit(corePlugin));
    addAllOnlinePlayerToCache(); // in case of reload
    final RemoveOfflineUserThread removeOfflineUserThread = new RemoveOfflineUserThread(corePlugin);
    removeOfflineUserThread.run();
    final SaveUserDataThread saveUserDataThread = new SaveUserDataThread(corePlugin);
    saveUserDataThread.run();
  }

  private void addAllOnlinePlayerToCache() {
    Bukkit.getOnlinePlayers().forEach(player -> addUserToCache(player.getUniqueId()));
  }

  /**
   * Add user to the cache.
   *
   * @param uuid the uuid
   */
  public void addUserToCache(final UUID uuid) {

    if (isCached(uuid)) {
      return;
    }

    getCachedUsers().put(uuid, new User(corePlugin, uuid));
  }

  /**
   * Remove user from the cache.
   *
   * @param uuid the uuid
   */
  public void removeUserFromCache(final UUID uuid) {

    if (!isCached(uuid)) {
      return;
    }

    final User user = new User(corePlugin, uuid);
    user.save();

    getCachedUsers().remove(uuid);
  }

  /**
   * Gets user.
   *
   * @param uuid the uuid of the player
   * @return the user class of the player
   */
  public User getUser(final UUID uuid) {

    if (!isCached(uuid)) {
      addUserToCache(uuid);
    }

    return getCachedUsers().get(uuid);
  }

  private boolean isCached(final UUID uuid) {
    return cachedUsers.containsKey(uuid);
  }


}
