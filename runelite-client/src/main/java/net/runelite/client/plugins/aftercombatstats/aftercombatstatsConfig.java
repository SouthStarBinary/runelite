
package net.runelite.client.plugins.aftercombatstats;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("aftercombatstats")
public interface aftercombatstatsConfig extends Config  {

    @ConfigItem(
            keyName = "aftercombatstats",
            name = "Notifications of Combat",
            description = "Gives live notifications if not focused on client(Spammy)",
            position = 1
    )
    default boolean myCheckBox() {return true;}


}
