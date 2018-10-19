
package net.runelite.client.plugins.test;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("test")
public interface  testConfig extends Config  {

    @ConfigItem(
            keyName = "test",
            name = "Notifications of Combat",
            description = "Gives live notifications if not focused on client(Spammy)",
            position = 1
    )
    default boolean myCheckBox() {return true;}

    @ConfigItem(keyName = "input", name = "Insert Input", description = "Input a option", position = 2)
    default String myInput() {return "";}

}
