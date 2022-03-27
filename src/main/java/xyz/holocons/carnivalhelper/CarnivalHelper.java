package xyz.holocons.carnivalhelper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CarnivalHelper extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        this.getCommand("carnival").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Command not allowed to be used on console!"));
            return false;
        }

        if (!player.hasPermission("carnivalhelper.use")) {
            player.sendMessage(Component.text("You do not have permission!"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Invalid usage!"));
            return false;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!isValidCurrency(heldItem)) {
            player.sendMessage(Component.text("The item you are holding is not a yagoold! Super yagoolds are not" +
                    " fixable/upgradable.")
                    .append(Component.newline())
                    .append(Component.text("Please contact staff through discord if you think this is wrong."))
            );
            return true;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("There is not enough free space in your inventory!");
            return true;
        }

        // Fix command - Fix broken/invalid yagoolds
        switch (args[0].toLowerCase()) {
            /*
            Fixes any gold nugget that has 3 lores (no matter the text) to a valid yagoold. Used for villager trading
             */
            case "fix" -> {
                ItemStack validCurrency = makeValidCurrency();
                validCurrency.setAmount(heldItem.getAmount());
                player.getInventory().setItemInMainHand(validCurrency);
                player.sendMessage("Yagoolds has been fixed!");
                this.getLogger().info("Replaced " + player.getName() + " " + validCurrency.getAmount() + " yagoold.");
                return true;
            }
            /*
            Executes scrates commands. One key is equal to one yagoold.
             */
            case "claim" -> {
                int amount = heldItem.getAmount();
                this.getLogger().info("Removing yagoold from " + player.getName());
                player.getInventory().setItemInMainHand(null);
                String strCommand = "scrates givekey carnivalCrate " + player.getName() + " " + amount + " -v";
                this.getLogger().info("Executing command [" + strCommand + "]");
                Bukkit.dispatchCommand(this.getServer().getConsoleSender(), strCommand);
                this.getLogger().info("Executed command!");
                return true;
            }
            /*
            Upgrades gold nuggets to iron nuggets.
             */
            case "upgrade" -> {
                ItemStack validSuperCurrency = makeValidSuperCurrency();

                int amount = heldItem.getAmount() / 4;
                int remainder = heldItem.getAmount() % 4;

                if (amount < 1) {
                    player.sendMessage(Component.text("You don't have enough yagoold!", NamedTextColor.RED));
                    return true;
                }

                validSuperCurrency.setAmount(amount);
                heldItem.setAmount(remainder);

                player.getInventory().setItemInMainHand(heldItem);
                var failedItems = player.getInventory().addItem(validSuperCurrency);
                if (!failedItems.isEmpty()) {
                    failedItems.forEach((slot, item) -> {
                        this.getLogger().warning("Item " + item.getType() + " of amount " + item.getAmount() + " was " +
                                "not able to be added to player " + player.getName() + "!");
                    });
                    player.sendMessage("There were some items that we were not able to add to your inventory." +
                            " Please contact staff for help.");
                }
                player.sendMessage("Yagoolds has been upgraded!");
                this.getLogger().info("Replaced " + player.getName() + " " + validSuperCurrency.getAmount() + " yagoold.");
                return true;
            }
        }

        return false;
    }

    private ItemStack makeValidCurrency() {
        ItemStack itemStack = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.displayName(Component.text("Yagoold", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        itemMeta.lore(List.of(
                Component.text("Unlock gacha crate and custom"),
                Component.text("items at the carnival using"),
                Component.text("this item!")
        ));

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    private ItemStack makeValidSuperCurrency() {
        ItemStack itemStack = new ItemStack(Material.IRON_NUGGET);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.addEnchant(Enchantment.LURE, 1, false);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        itemMeta.displayName(Component.text("Super Yagoold", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        itemMeta.lore(List.of(
                Component.text("Unlock gacha crate and custom"),
                Component.text("items at the carnival using"),
                Component.text("this item!")
        ));

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /*
    Checks if an itemStack is a valid currency. A valid currency is just a gold nugget with 3 lore lines. It
    does not check for the name or the content of the lore.
     */
    private boolean isValidCurrency(ItemStack itemStack) {
        var itemMeta = itemStack.getItemMeta();
        return itemStack.getType() == Material.GOLD_NUGGET && itemMeta.hasLore() && itemMeta.lore().size() == 3;
    }

}