package com.samet.turkcefactions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;

public class Klan extends JavaPlugin implements CommandExecutor, Listener {

    public static class ShopItem {
        public String displayName;
        public Material material;
        public int amount;
        public double buyPrice;

        public ShopItem(String displayName, Material material, int amount, double buyPrice) {
            this.displayName = displayName;
            this.material = material;
            this.amount = amount;
            this.buyPrice = buyPrice;
        }
    }

    private final List<ShopItem> shopItems = new ArrayList<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        initShopItems();
        getServer().getPluginManager().registerEvents(this, this);

        registerCommand("klan");
        registerCommand("arsa");
        registerCommand("kit");
        registerCommand("kc");
        registerCommand("shop");
        registerCommand("market");
        registerCommand("bakiye");
        registerCommand("para");
        registerCommand("pay");

        Bukkit.getScheduler().runTaskTimer(this, this::updateAllScoreboards, 20L, 60L);
        getLogger().info("§aTurkce Factions Chest-GUI Eklentisi Aktif!");
    }

    private void registerCommand(String name) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(this);
        }
    }

    private void initShopItems() {
        shopItems.add(new ShopItem("§aTas x64", Material.STONE, 64, 100));
        shopItems.add(new ShopItem("§aCimen x64", Material.GRASS_BLOCK, 64, 150));
        shopItems.add(new ShopItem("§aMese Odunu x64", Material.OAK_LOG, 64, 200));
        shopItems.add(new ShopItem("§aObsidyen x16", Material.OBSIDIAN, 16, 500));
        shopItems.add(new ShopItem("§bElmas x5", Material.DIAMOND, 5, 1000));
        shopItems.add(new ShopItem("§fDemir Kule x16", Material.IRON_INGOT, 16, 300));
        shopItems.add(new ShopItem("§eAltin Kule x16", Material.GOLD_INGOT, 16, 600));
        shopItems.add(new ShopItem("§aZümrüt x5", Material.EMERALD, 5, 800));
        shopItems.add(new ShopItem("§cBiftek x32", Material.COOKED_BEEF, 32, 120));
        shopItems.add(new ShopItem("§eAltin Elma x2", Material.GOLDEN_APPLE, 2, 800));
        shopItems.add(new ShopItem("§dElmas Kilic", Material.DIAMOND_SWORD, 1, 1500));
        shopItems.add(new ShopItem("§dElmas Kazma", Material.DIAMOND_PICKAXE, 1, 1500));
    }

    public double getBalance(Player player) {
        String uuid = player.getUniqueId().toString();
        if (!getConfig().contains("ekonomi." + uuid)) {
            getConfig().set("ekonomi." + uuid, 1000.0);
            saveConfig();
        }
        return getConfig().getDouble("ekonomi." + uuid);
    }

    public void setBalance(Player player, double amount) {
        String uuid = player.getUniqueId().toString();
        getConfig().set("ekonomi." + uuid, Math.max(0, amount));
        saveConfig();
        updateScoreboard(player);
    }

    public void addBalance(Player player, double amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public boolean withdrawBalance(Player player, double amount) {
        double current = getBalance(player);
        if (current >= amount) {
            setBalance(player, current - amount);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateScoreboard(event.getPlayer());
    }

    public void updateAllScoreboards() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateScoreboard(p);
        }
    }

    public void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("factions_board", Criteria.DUMMY, "§6§lMINEFACTIONS");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String uuid = player.getUniqueId().toString();
        String klan = getConfig().getString("oyuncular." + uuid + ".klan", "§cYok");
        double bakiye = getBalance(player);

        obj.getScore("§1 ").setScore(7);
        obj.getScore("§fOyuncu: §e" + player.getName()).setScore(6);
        obj.getScore("§fBakiye: §a$" + String.format("%.0f", bakiye)).setScore(5);
        obj.getScore("§2 ").setScore(4);
        obj.getScore("§fKlaniniz: §b" + klan).setScore(3);
        obj.getScore("§3 ").setScore(2);
        obj.getScore("§eplay.sunucu.com").setScore(1);

        player.setScoreboard(board);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komutlar sadece oyunda kullanilabilir!");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        if (cmd.equals("bakiye") || cmd.equals("para")) {
            player.sendMessage("§a[Ekonomi] §fMevcut Bakiyeniz: §a$" + String.format("%.2f", getBalance(player)));
            return true;
        }

        if (cmd.equals("pay")) {
            if (args.length < 2) {
                player.sendMessage("§cKullanim: /pay <oyuncu> <miktar>");
                return true;
            }
            Player hedef = Bukkit.getPlayer(args[0]);
            if (hedef == null || !hedef.isOnline()) {
                player.sendMessage("§cOyuncu bulunamadi!");
                return true;
            }
            try {
                double miktar = Double.parseDouble(args[1]);
                if (miktar <= 0) return true;
                if (withdrawBalance(player, miktar)) {
                    addBalance(hedef, miktar);
                    player.sendMessage("§a[Ekonomi] §e" + hedef.getName() + " §fisimli oyuncuya §a$" + miktar + " §fgonderildi.");
                    hedef.sendMessage("§a[Ekonomi] §e" + player.getName() + " §fsize §a$" + miktar + " §fgonderdi.");
                } else {
                    player.sendMessage("§cYetersiz bakiye!");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cGecerli bir sayi girin!");
            }
            return true;
        }

        switch (cmd) {
            case "klan" -> openKlanMenu(player);
            case "arsa" -> openArsaMenu(player);
            case "kit" -> openKitMenu(player);
            case "shop", "market" -> openMarketMenu(player);
            case "kc" -> {
                if (args.length == 0) {
                    player.sendMessage("§cKullanim: /kc <mesaj>");
                } else {
                    String msg = String.join(" ", args);
                    player.sendMessage("§b[Klan Chat] §f" + player.getName() + ": " + msg);
                }
            }
        }

        return true;
    }

    // --- MARKET MENÜSÜ ---
    public void openMarketMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8=== §aSunucu Marketi §8===");

        for (int i = 0; i < shopItems.size() && i < 27; i++) {
            ShopItem item = shopItems.get(i);
            inv.setItem(i, createGuiItem(
                    item.material,
                    item.displayName,
                    "§7Miktar: §e" + item.amount,
                    "§7Fiyat: §a$" + item.buyPrice,
                    "",
                    "§eSatin almak icin tiklayin!"
            ));
        }

        player.openInventory(inv);
    }

    // --- KLAN MENÜSÜ ---
    public void openKlanMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8=== §bKlan Menusu §8===");

        inv.setItem(11, createGuiItem(Material.ANVIL, "§aKlan Olustur", "§7Kendi klaninizi kurun.", "§7Gereksinim: §a$1000"));
        inv.setItem(13, createGuiItem(Material.PAPER, "§bKlan Bilgisi", "§7Mevcut klaninizin durumunu gorun."));
        inv.setItem(15, createGuiItem(Material.REDSTONE, "§cKlandan Ayril", "§7Mevcut klaninizdan cikis yapin."));

        player.openInventory(inv);
    }

    // --- ARSA MENÜSÜ ---
    public void openArsaMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8=== §eArsa Menusu §8===");

        inv.setItem(11, createGuiItem(Material.GRASS_BLOCK, "§aArsa Al", "§7Bulundugunuz alani satin alin.", "§7Fiyat: §a$500"));
        inv.setItem(15, createGuiItem(Material.COMPASS, "§eArsama Git", "§7Arsaniza isinlanin."));

        player.openInventory(inv);
    }

    // --- KİT MENÜSÜ ---
    public void openKitMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8=== §dKit Menusu §8===");

        inv.setItem(11, createGuiItem(Material.IRON_SWORD, "§aOyuncu Kiti", "§7Temel baslangic ekipmanlari."));
        inv.setItem(15, createGuiItem(Material.DIAMOND_SWORD, "§bVIP Kiti", "§7Ozel VIP ekipmanlari."));

        player.openInventory(inv);
    }

    // --- SANDIK TIKLAMA OLAYLARI (EVENTS) ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();

        if (title.contains("Sunucu Marketi") || title.contains("Klan Menusu") || title.contains("Arsa Menusu") || title.contains("Kit Menusu")) {
            event.setCancelled(true); // Eşyaların envanterden alınmasını engeller

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slot = event.getSlot();

            // MARKET TIKLAMA
            if (title.contains("Sunucu Marketi")) {
                if (slot < shopItems.size()) {
                    ShopItem shopItem = shopItems.get(slot);
                    if (withdrawBalance(player, shopItem.buyPrice)) {
                        player.getInventory().addItem(new ItemStack(shopItem.material, shopItem.amount));
                        player.sendMessage("§a[Market] §e" + shopItem.displayName + " §fbasariyla satin alindi! §a-$" + shopItem.buyPrice);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§c[Market] Yetersiz bakiye! Bu urun icin §a$" + shopItem.buyPrice + " §cgerekiyor.");
                    }
                }
            }

            // KLAN TIKLAMA
            else if (title.contains("Klan Menusu")) {
                if (slot == 11) {
                    player.closeInventory();
                    player.sendMessage("§e[Klan] Klan olusturmak icin sohbetten komut yazin: §b/klan olustur <isim>");
                } else if (slot == 13) {
                    player.closeInventory();
                    String uuid = player.getUniqueId().toString();
                    String klan = getConfig().getString("oyuncular." + uuid + ".klan", "Klaniniz yok");
                    player.sendMessage("§b[Klan] Mevcut Klaniniz: §e" + klan);
                } else if (slot == 15) {
                    player.closeInventory();
                    player.sendMessage("§c[Klan] Klandan ayrildiniz.");
                }
            }

            // ARSA TIKLAMA
            else if (title.contains("Arsa Menusu")) {
                if (slot == 11) {
                    player.closeInventory();
                    if (withdrawBalance(player, 500)) {
                        player.sendMessage("§a[Arsa] Bulundugunuz arsa basariyla satin alindi!");
                    } else {
                        player.sendMessage("§c[Arsa] Arsa almak icin $500 gerekiyor.");
                    }
                } else if (slot == 15) {
                    player.closeInventory();
                    player.sendMessage("§e[Arsa] Arsaniza isinlandiniz!");
                }
            }

            // KİT TIKLAMA
            else if (title.contains("Kit Menusu")) {
                if (slot == 11) {
                    player.closeInventory();
                    player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
                    player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
                    player.sendMessage("§a[Kit] Baslangic kiti alindi!");
                } else if (slot == 15) {
                    player.closeInventory();
                    player.sendMessage("§c[Kit] VIP kitini almak icin VIP olmalisiniz.");
                }
            }
        }
    }
}
