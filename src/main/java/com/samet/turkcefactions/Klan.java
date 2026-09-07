package com.samet.turkcefactions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
        public double sellPrice;
        public String category;

        public ShopItem(String displayName, Material material, int amount, double buyPrice, double sellPrice, String category) {
            this.displayName = displayName;
            this.material = material;
            this.amount = amount;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.category = category;
        }
    }

    private final List<ShopItem> shopItems = new ArrayList<>();
    private final Map<UUID, Integer> lastPlotId = new HashMap<>();
    private final Random random = new Random();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        initAdvancedShop();
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
        registerCommand("sat");
        registerCommand("rutbe");
        registerCommand("arena");
        registerCommand("tag");
        registerCommand("vip");
        registerCommand("maden");
        registerCommand("buyu");
        registerCommand("kasa");

        Bukkit.getScheduler().runTaskTimer(this, this::updateAllScoreboards, 20L, 60L);
        getLogger().info("§aTurkce Factions v5.0 (Maden, Büyü, Kasa, Kit) Aktif!");
    }

    private void registerCommand(String name) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(this);
        }
    }

    private void initAdvancedShop() {
        shopItems.add(new ShopItem("§aTas x64", Material.STONE, 64, 100, 20, "Bloklar"));
        shopItems.add(new ShopItem("§aCimen x64", Material.GRASS_BLOCK, 64, 150, 30, "Bloklar"));
        shopItems.add(new ShopItem("§aLapis Lazuli x64", Material.LAPIS_LAZULI, 64, 300, 150, "Madenler"));
        shopItems.add(new ShopItem("§bElmas x5", Material.DIAMOND, 5, 1000, 400, "Madenler"));
        shopItems.add(new ShopItem("§fDemir Kule x16", Material.IRON_INGOT, 16, 300, 100, "Madenler"));
        shopItems.add(new ShopItem("§eAltin Kule x16", Material.GOLD_INGOT, 16, 600, 200, "Madenler"));
        shopItems.add(new ShopItem("§aZümrüt x5", Material.EMERALD, 5, 800, 300, "Madenler"));
        shopItems.add(new ShopItem("§cBiftek x32", Material.COOKED_BEEF, 32, 120, 20, "Tarim"));
        shopItems.add(new ShopItem("§dElmas Kilic", Material.DIAMOND_SWORD, 1, 1500, 300, "Ekipman"));
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

    public String getTag(Player player) {
        String uuid = player.getUniqueId().toString();
        return getConfig().getString("taglar." + uuid, "§7[Oyuncu]");
    }

    public void setTag(Player player, String tag) {
        String uuid = player.getUniqueId().toString();
        getConfig().set("taglar." + uuid, tag);
        saveConfig();
        player.setDisplayName(tag + " §f" + player.getName());
        player.setPlayerListName(tag + " §f" + player.getName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setTag(player, getTag(player));
        updateScoreboard(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setFormat(getTag(player) + " §f%1$s: %2$s");
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

        obj.getScore("§1 ").setScore(8);
        obj.getScore("§fUnvan: " + getTag(player)).setScore(7);
        obj.getScore("§fOyuncu: §e" + player.getName()).setScore(6);
        obj.getScore("§fBakiye: §a$" + String.format("%.0f", getBalance(player))).setScore(5);
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
            if (hedef != null && hedef.isOnline()) {
                try {
                    double miktar = Double.parseDouble(args[1]);
                    if (miktar > 0 && withdrawBalance(player, miktar)) {
                        addBalance(hedef, miktar);
                        player.sendMessage("§a[Ekonomi] §e" + hedef.getName() + " §fisimli oyuncuya §a$" + miktar + " §fgonderildi.");
                        hedef.sendMessage("§a[Ekonomi] §e" + player.getName() + " §fsize §a$" + miktar + " §fgonderdi.");
                    } else {
                        player.sendMessage("§cYetersiz bakiye!");
                    }
                } catch (NumberFormatException ignored) {}
            }
            return true;
        }

        if (cmd.equals("tag")) {
            if (!player.isOp()) return true;
            if (args.length >= 2) {
                Player hedef = Bukkit.getPlayer(args[0]);
                if (hedef != null) {
                    String yeniTag = args[1].replace("&", "§");
                    setTag(hedef, yeniTag);
                    player.sendMessage("§a[Tag] " + hedef.getName() + " oyuncusuna " + yeniTag + " verildi!");
                }
            }
            return true;
        }

        if (cmd.equals("arena")) {
            player.teleport(new Location(player.getWorld(), 0, 100, 0));
            player.sendMessage("§c[PvP Arena] Arenaya isinlandiniz! Savas basladi!");
            return true;
        }

        switch (cmd) {
            case "klan" -> openKlanMenu(player);
            case "arsa" -> openArsaMenu(player);
            case "kit" -> openKitMenu(player);
            case "shop", "market" -> openMarketKategoriMenu(player);
            case "sat" -> handleQuickSell(player);
            case "rutbe" -> openRutbeMenu(player);
            case "vip" -> openVipMenu(player);
            case "maden" -> openMadenMenu(player);
            case "buyu" -> openBuyuMenu(player);
            case "kasa" -> openKasaMenu(player);
        }

        return true;
    }

    // --- MADEN KAZARKEN KASA DÜŞME OLAYI ---
    @EventHandler
    public void onMine(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();

        // Taş, Lapis veya Cevher kazarken %5 şansla kasa düşer
        if (type == Material.STONE || type == Material.LAPIS_ORE || type == Material.IRON_ORE || type == Material.DIAMOND_ORE || type == Material.EMERALD_ORE) {
            if (random.nextInt(100) < 5) {
                ItemStack kasa = createGuiItem(Material.CHEST, "§6Maden Kasasi", "§7/kasa komutunu kullanarak acabilirsiniz.");
                player.getInventory().addItem(kasa);
                player.sendMessage("§6[Kasa] §aTebrikler! Maden kazarken 1x Maden Kasasi kazandiniz!");
            }
        }
    }

    // --- MADEN MENÜSÜ ---
    public void openMadenMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8=== §bMaden Bolgeleri §8===");

        inv.setItem(10, createGuiItem(Material.LAPIS_BLOCK, "§9Lapis Madeni (Vipsiz)", "§7Boyut: 256x64 Herkese Acik."));
        inv.setItem(12, createGuiItem(Material.IRON_ORE, "§eVIP Madeni", "§7Sadece §eVIP §7ve uzeri girebilir.", "§7Cevher: Demir"));
        inv.setItem(14, createGuiItem(Material.DIAMOND_ORE, "§bVIP+ Madeni", "§7Sadece §bVIP+ §7ve uzeri girebilir.", "§7Cevher: Elmas"));
        inv.setItem(16, createGuiItem(Material.EMERALD_ORE, "§dMVIP Madeni", "§7Sadece §dMVIP §7girebilir.", "§7Cevher: Zümrüt"));

        player.openInventory(inv);
    }

    // --- BÜYÜ MENÜSÜ ---
    public void openBuyuMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8=== §dBuyu Menusu §8===");

        inv.setItem(11, createGuiItem(Material.ENCHANTED_BOOK, "§aKeskinlik V", "§7Elinizdeki kilica basilir.", "§7Fiyat: §a$5.000"));
        inv.setItem(13, createGuiItem(Material.ENCHANTED_BOOK, "§bVerimlilik V", "§7Elinizdeki kazmaya basilir.", "§7Fiyat: §a$5.000"));
        inv.setItem(15, createGuiItem(Material.ENCHANTED_BOOK, "§eKoruma IV", "§7Elinizdeki zirha basilir.", "§7Fiyat: §a$5.000"));

        player.openInventory(inv);
    }

    // --- KASA MENÜSÜ ---
    public void openKasaMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8=== §6Kasa Acma Menusu §8===");

        inv.setItem(13, createGuiItem(Material.CHEST, "§6Maden Kasasini Ac", "§7Envanterinizdeki Maden Kasasini acar.", "§7Rastgele $1.000 - $10.000 verir."));

        player.openInventory(inv);
    }

    // --- KİT MENÜSÜ (HAFTALIK COOLDOWN DESTEKLİ) ---
    public void openKitMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8=== §dKit Menusu §8===");

        inv.setItem(10, createGuiItem(Material.STONE_SWORD, "§fOyuncu Kiti", "§7Süre: 7 Günde 1 Kez"));
        inv.setItem(12, createGuiItem(Material.GOLDEN_SWORD, "§eVIP Kiti", "§7Gereksinim: VIP", "§7Süre: 7 Günde 1 Kez"));
        inv.setItem(14, createGuiItem(Material.DIAMOND_SWORD, "§bVIP+ Kiti", "§7Gereksinim: VIP+", "§7Süre: 7 Günde 1 Kez"));
        inv.setItem(16, createGuiItem(Material.NETHERITE_SWORD, "§dMVIP Kiti", "§7Gereksinim: MVIP", "§7Süre: 7 Günde 1 Kez"));

        player.openInventory(inv);
    }

    private boolean checkKitCooldown(Player player, String kitName) {
        String uuid = player.getUniqueId().toString();
        long lastUsed = getConfig().getLong("cooldowns." + uuid + "." + kitName, 0);
        long weekInMillis = 7L * 24 * 60 * 60 * 1000;

        if (System.currentTimeMillis() - lastUsed < weekInMillis) {
            long remainingHours = (weekInMillis - (System.currentTimeMillis() - lastUsed)) / (1000 * 60 * 60);
            player.sendMessage("§c[Kit] Bu kiti tekrar almak icin §e" + remainingHours + " saat §cbeklemelisiniz!");
            return false;
        }

        getConfig().set("cooldowns." + uuid + "." + kitName, System.currentTimeMillis());
        saveConfig();
        return true;
    }

    // --- DİĞER MENÜLER VE TIKLAMA OLAYLARI ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.contains("Maden Bolgeleri") || title.contains("Buyu Menusu") || title.contains("Kasa Acma") || title.contains("Kit Menusu") || title.contains("Market")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slot = event.getSlot();

            // MADEN
            if (title.contains("Maden Bolgeleri")) {
                player.closeInventory();
                if (slot == 10) {
                    player.teleport(new Location(player.getWorld(), 500, 64, 500));
                    player.sendMessage("§9[Maden] Lapis Madenine (256x64) isinlandiniz!");
                } else if (slot == 12 && (player.hasPermission("vip") || player.isOp())) {
                    player.teleport(new Location(player.getWorld(), 600, 64, 600));
                    player.sendMessage("§e[Maden] VIP Demir Madenine isinlandiniz!");
                } else if (slot == 14 && (player.hasPermission("vip.plus") || player.isOp())) {
                    player.teleport(new Location(player.getWorld(), 700, 64, 700));
                    player.sendMessage("§b[Maden] VIP+ Elmas Madenine isinlandiniz!");
                } else if (slot == 16 && (player.hasPermission("mvip") || player.isOp())) {
                    player.teleport(new Location(player.getWorld(), 800, 64, 800));
                    player.sendMessage("§d[Maden] MVIP Zümrüt Madenine isinlandiniz!");
                } else {
                    player.sendMessage("§cBu madene girmek icin gerekli VIP yetkiniz yok!");
                }
            }

            // BÜYÜ
            else if (title.contains("Buyu Menusu")) {
                player.closeInventory();
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == Material.AIR) {
                    player.sendMessage("§cBuyu basmak icin elinizde bir esya tutmalisiniz!");
                    return;
                }

                if (withdrawBalance(player, 5000)) {
                    if (slot == 11) hand.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
                    else if (slot == 13) hand.addUnsafeEnchantment(Enchantment.DIG_SPEED, 5);
                    else if (slot == 15) hand.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                    player.sendMessage("§a[Buyu] Buyu basarili bir sekilde eşyaniza eklendi! (-$5.000)");
                } else {
                    player.sendMessage("§cYetersiz bakiye! Buyu ucreti: $5.000");
                }
            }

            // KASA
            else if (title.contains("Kasa Acma")) {
                player.closeInventory();
                if (player.getInventory().containsAtLeast(new ItemStack(Material.CHEST), 1)) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == Material.CHEST && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Maden Kasasi")) {
                            item.setAmount(item.getAmount() - 1);
                            double odul = 1000 + random.nextInt(9000);
                            addBalance(player, odul);
                            player.sendMessage("§6[Kasa] Kasa acildi! Icinden §a$" + String.format("%.0f", odul) + " §6cikti!");
                            return;
                        }
                    }
                }
                player.sendMessage("§cEnvanterinizde 'Maden Kasasi' bulunamadi!");
            }

            // KİT
            else if (title.contains("Kit Menusu")) {
                player.closeInventory();
                if (slot == 10 && checkKitCooldown(player, "oyuncu")) {
                    player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                    player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 32));
                    player.sendMessage("§a[Kit] Oyuncu Kiti alindi!");
                } else if (slot == 12 && (player.hasPermission("vip") || player.isOp()) && checkKitCooldown(player, "vip")) {
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
                    player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 8));
                    player.sendMessage("§a[Kit] VIP Kiti alindi!");
                }
            }
        }
    }

    private void openKlanMenu(Player player) {}
    private void openArsaMenu(Player player) {}
    private void openMarketKategoriMenu(Player player) {}
    private void openRutbeMenu(Player player) {}
    private void openVipMenu(Player player) {}
    private void handleQuickSell(Player player) {}
}
