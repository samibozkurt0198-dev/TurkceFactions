import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class Klan extends JavaPlugin implements CommandExecutor, Listener {

    @Override
    public void onEnable() {
        if (getCommand("klan") != null) {
            getCommand("klan").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("§aTurkce Factions Eklentisi Aktif!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komut sadece oyunda kullanilabilir!");
            return true;
        }

        Inventory gui = Bukkit.createInventory(null, 27, "§8=== §bKlan Yonetim Menusu §8===");

        ItemStack klanKur = new ItemStack(Material.NETHER_STAR);
        ItemMeta kurMeta = klanKur.getItemMeta();
        if (kurMeta != null) {
            kurMeta.setDisplayName("§a§lKlan Kur");
            kurMeta.setLore(Collections.singletonList("§7Yeni bir klan olustur."));
            klanKur.setItemMeta(kurMeta);
        }

        ItemStack klanBilgi = new ItemStack(Material.BOOK);
        ItemMeta bilgiMeta = klanBilgi.getItemMeta();
        if (bilgiMeta != null) {
            bilgiMeta.setDisplayName("§e§lKlan Bilgileri");
            bilgiMeta.setLore(Collections.singletonList("§7Mevcut klanini gor."));
            klanBilgi.setItemMeta(bilgiMeta);
        }

        gui.setItem(11, klanKur);
        gui.setItem(15, klanBilgi);

        player.openInventory(gui);
        return true;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§8=== §bKlan Yonetim Menusu §8===")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

            Player player = (Player) event.getWhoClicked();
            String itemIsmi = event.getCurrentItem().getItemMeta().getDisplayName();

            if (itemIsmi.equals("§a§lKlan Kur")) {
                player.closeInventory();
                player.sendMessage("§a[Klan] §fKlan kurmak icin: §e/klan kur <Isim>");
            } else if (itemIsmi.equals("§e§lKlan Bilgileri")) {
                player.closeInventory();
                player.sendMessage("§e[Klan] §fHenuz bir klaniniz yok.");
            }
        }
    }
}
