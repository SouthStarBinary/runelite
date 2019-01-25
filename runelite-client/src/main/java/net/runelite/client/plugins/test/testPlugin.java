package net.runelite.client.plugins.test;

//import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import net.runelite.client.eventbus.Subscribe;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import net.runelite.api.*;

import static net.runelite.api.AnimationID.*;

import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/*
TODO
***Only for NPCS and single combat for now***
See if there is another way to check if NPC is dead other than GameTick
Add config option for combat feed
Look into the start up method, see if can use for resetting fields. Maybe just a reset method?
Look into formatting the messages
***BUG: Catches the damage being done to another npc that didn't come from local player
 */

@PluginDescriptor(
        name = "After Combat Stats",
        description = "After a fight gives you your highest, lowest, average, and total hits",
        tags = {"health", "hitpoints", "notifications", "prayer"}
)
public class testPlugin extends Plugin {

    @Inject
    private Notifier notifier;

    @Inject
    private Client client;

    @Inject
    private testConfig config;

    @Provides
    testConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(testConfig.class);
    }


    private NPC targetNPC;
    private boolean lastIsDead = false;
    private int intHighestHit = 0;
    private int intLowestHit = 99;
    private int totalDamage = 0;
    private int intTotalHits = 0;

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        final Actor source = event.getActor();
        final boolean isNpc = source instanceof NPC;
        final NPC npc;

        //if you want both player and target hits:
        //if event.getActor() != client.getLocalPlayer() && !isNpc

        /*
        Base Case
        if there is no previous target, return
         */
        if (targetNPC == null) {
            return;
        }

        //BASE CASE
        //if false then source is not an NPC return early
        if (!isNpc)
        {
            return;
        }

        //BASE CASE
        //if it is an NPC, check to see if it is the npc we are fighting(by id)
        //if not return early
        if (isNpc) {
            npc = (NPC)source;
            if (npc.getId() != targetNPC.getId()) {
                return;
            }
        }

        //at this point we have our targeted npc.
        final Hitsplat hitsplat = event.getHitsplat();//the actual hitsplat to get averg highest and lowest

        if (hitsplat.getAmount() > intHighestHit) {//highest hit
            intHighestHit = hitsplat.getAmount();
        }
        if (hitsplat.getAmount() < intLowestHit && hitsplat.getAmount() != 0) {//lowest hit
            intLowestHit = hitsplat.getAmount();
        }

        if (hitsplat.getAmount() != 0) {//counter for total times hit
            intTotalHits += 1;
        }

        totalDamage += hitsplat.getAmount();//adds up total damage done

        //combat feed after every hitsplatapplied
        client.addChatMessage(ChatMessageType.EXAMINE_ITEM, event.getActor().getName(),
                   event.getActor().getName() + " was hit for: " + hitsplat.getAmount(),"");

        //notification - possibly remove
        if (config.myCheckBox()) {
            notifier.notify(event.getActor().getName() + " was hit for: " + hitsplat.getAmount());
        }

    }

    /*
    Checks the target local player interacted with and checks
    to see if it was a NPC. If it is and you can attack it,
    set field value as the targeted npc.
     */
    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {

        final Actor source = event.getSource();//gets the source of the interaction (who caused it)

        /*
        Base Case
        if not local player return
         */
        if (source != client.getLocalPlayer()) {
            return;
        }

        final Actor target = event.getTarget();//gets the target of the interaction

        final boolean isNPC = target instanceof NPC;//checks to see if the target is a NPC

        if (!isNPC) {//if the target is not an NPC return
            return;
        }

        final NPC npc = (NPC)target;//cast the target as an npc
        final NPCComposition npcComposition = npc.getComposition();//get what makes up the NPC (for their listed actions)
        List<String> actions = Arrays.asList(npcComposition.getActions());

        //if one of the interactions is attack then player likely attempted combat
        if (actions.contains("Attack")) {
            client.addChatMessage(ChatMessageType.EXAMINE_ITEM, client.getUsername(),
                    "You have entered COMBAT","");
            targetNPC = npc;
            lastIsDead = false;
            intTotalHits = 0;
            intLowestHit = 99;
            intHighestHit = 0;
            totalDamage = 0;
        }

    }

    //isDead() method only seems to work on a game tick event. Doesn't work on hitsplatapplied event.
    @Subscribe
    public void onGameTick (GameTick event) {

        //if target npc is not null, is dead and the last npc you fought is dead as well (prevents constant spam every game tick)
        if (targetNPC != null && targetNPC.isDead() && lastIsDead == false) {
            client.addChatMessage(ChatMessageType.EXAMINE_ITEM, "",
                    targetNPC.getName() + " is dead","");
            lastIsDead = true;
            client.addChatMessage(ChatMessageType.EXAMINE_ITEM,
                    "",  "Your Stats - " +
                            " Highest Hit: " + intHighestHit +
                            " Lowest Hit: " + intLowestHit +
                            " Hit on Average: " + totalDamage / intTotalHits +
                    " Total Damage Dealt: " + totalDamage,"");
        }


    }

}
