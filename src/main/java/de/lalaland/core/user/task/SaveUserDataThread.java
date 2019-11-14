package de.lalaland.core.user.task;

import de.lalaland.core.CorePlugin;
import de.lalaland.core.user.User;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*******************************************************
 * Copyright (C) 2015-2019 Piinguiin neuraxhd@gmail.com
 *
 * This file is part of CorePlugin and was created at the 14.11.2019
 *
 * CorePlugin can not be copied and/or distributed without the express
 * permission of the owner.
 *
 *******************************************************/
public class SaveUserDataThread implements Runnable {

  private final CorePlugin corePlugin;
  private final int interval;

  /**
   * Instantiates a new Remove offline user thread. Remove User classes stored in cache when they
   * are not in use.
   *
   * @param corePlugin the core plugin
   */
  public SaveUserDataThread(final CorePlugin corePlugin) {
    this.corePlugin = corePlugin;
    interval = corePlugin.getCoreConfig().getUserSaveInterval();
  }

  @Override
  public void run() {

    final Object2ObjectOpenHashMap<UUID, User> cachedUsers = corePlugin.getUserManager()
        .getCachedUsers();

    if (cachedUsers.isEmpty()) {
      return;
    }

    for (final User user : cachedUsers.values()) {

      if (user.isUpdate()) {
        user.save();
      }

    }

    try {
      Thread.sleep(TimeUnit.MINUTES.toMillis(interval));
    } catch (final InterruptedException e) {
      corePlugin.getCoreLogger().error(e.getMessage());
    }

  }


}
