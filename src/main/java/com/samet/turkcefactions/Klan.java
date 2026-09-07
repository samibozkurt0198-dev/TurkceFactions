package com.samet.turkcefactions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;

import java.lang.reflect.Method;
import java.util.*;

public class Klan extends JavaPlugin implements CommandExecutor, Listener {

    public static class ShopItem {
        public String displayName;
        public Material material;
        public int amount;
        public double buyPrice;
        public String category;
        public String iconUrl;

        public ShopItem(String displayName, Material material, int amount, double buyPrice, String category, String iconUrl) {
            this.displayName = displayName;
            this.material = material;
            this.amount = amount;
            this.buyPrice = buyPrice;
            this.category = category;
            this.iconUrl = iconUrl;
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
        getLogger().info("§aTurkce Factions, Arsa, Market & Ekonomi Eklentisi Aktif!");
    }

    private void registerCommand(String name) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(this);
        }
    }

    private void initShopItems() {
        shopItems.add(new ShopItem("Tas x64", Material.STONE, 64, 100, "Bloklar", "https://textures.minecraft.net/texture/a18a287a206a4b159f81f1e8a834e56d43e597c5f3b7933f1f3a22c53a6e3"));
        shopItems.add(new ShopItem("Cimen x64", Material.GRASS_BLOCK, 64, 150, "Bloklar", "https://textures.minecraft.net/texture/1f31f99c85df230a133df1b312384a6c6c7471804c4b5bc7f0bf18df32f8373"));
        shopItems.add(new ShopItem("Mese Odunu x64", Material.OAK_LOG, 64, 200, "Bloklar", "https://textures.minecraft.net/texture/b898a39a3f28cfdf5c68b75960d7d6f5f3e9c71bc458a25c1e5d7718e38d72"));
        shopItems.add(new ShopItem("Obsidyen x16", Material.OBSIDIAN, 16, 500, "Bloklar", "https://textures.minecraft.net/texture/2f8b50e18d6e3c1626f21c27e857850a4d538e1b0c950d8df302e1c9533a1e2"));
        shopItems.add(new ShopItem("Elmas x5", Material.DIAMOND, 5, 1000, "Madenler", "https://textures.minecraft.net/texture/a31a980562e15119bb989a3a91e5e54d632f122e23d7f76371c1f4e1f7d5c7"));
        shopItems.add(new ShopItem("Demir Kule x16", Material.IRON_INGOT, 16, 300, "Madenler", "https://textures.minecraft.net/texture/26197116a41f8615c4d081f2f01f8d8c2c11ee14df1d368d1f8bc63402324e"));
        shopItems.add(new ShopItem("Altin Kule x16", Material.GOLD_INGOT, 16, 600, "Madenler", "https://textures.minecraft.net/texture/d67e0c388274384a123610fb176b6255d6428c943f7a1f6a1c1d0b7e2a9"));
        shopItems.add(new ShopItem("Zümrüt x5", Material.EMERALD, 5, 800, "Madenler", "https://textures.minecraft.net/texture/238a8e1b5f257a3e1a0b5c1c8a14352b2b3a1a9e1d8a1c9e8a7f1a9a8b1c2d3"));
        shopItems.add(new ShopItem("Biftek x32", Material.COOKED_BEEF, 32, 120, "Yemekler", "https://textures.minecraft.net/texture/416323f81504221191060938a168a719c8173400a454d6d1d4d802ed3a2b7e9"));
        shopItems.add(new ShopItem("Altin Elma x2", Material.GOLDEN_APPLE, 2, 800, "Yemekler", "https://textures.minecraft.net/texture/b05688773923ea505745fc7e6168e0e09e3be31d816048556cef289eb4e1d"));
        shopItems.add(new ShopItem("Büyülü Elma x1", Material.ENCHANTED_GOLDEN_APPLE, 1, 5000, "Yemekler", "https://textures.minecraft.net/texture/5f29910e5b0b10b3ef18b375be8540329616d2861e68da08512ccb4415e1926d"));
        shopItems.add(new ShopItem("Elmas Kilic", Material.DIAMOND_SWORD, 1, 1500, "Ekipman", "https://textures.minecraft.net/texture/a18a287a206a4b159f81f1e8a834e56d43e597c5f3b7933f1f3a22c53a6e3"));
        shopItems.add(new ShopItem("Elmas Kazma", Material.DIAMOND_PICKAXE, 1, 1500, "Ekipman", "https://textures.minecraft.net/texture/2f8b50e18d6e3c1626f21c27e857850a4d538e1b0c950d8df302e1c9533a1e2"));
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

    // --- GÜVENLİ FLOODGATE / BEDROCK FORM GÖNDERME METODU ---
    private boolean sendBedrockForm(Player player, Object form) {
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method getInstanceMethod = apiClass.getMethod("getInstance");
            Object apiInstance = getInstanceMethod.invoke(null);
            Method sendFormMethod = apiClass.getMethod("sendForm", UUID.class, Object.class);
            return (boolean) sendFormMethod.invoke(apiInstance, player.getUniqueId(), form);
        } catch (Exception e) {
            player.sendMessage("§c[Hata] Bedrock Arayuzu yuklenemedi. Sunucu destegi pasif olabilir.");
            return false;
        }
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
            case "klan" -> openAnaKlanMenusu(player);
            case "arsa" -> openArsaMenusu(player);
            case "kit" -> openKitMenusu(player);
            case "kc" -> handleKlanChat(player, args);
            case "shop", "market" -> openMarketAnaMenusu(player);
        }

        return true;
    }

    private void openMarketAnaMenusu(Player player) {
        SimpleForm.Builder form = SimpleForm.builder()
                .title("§8=== §aSUNUCU MARKETİ §8===")
                .content("§7Bakiyeniz: §a$" + String.format("%.0f", getBalance(player)) + "\n§fLutfen secim yapin:")
                .button("§e§lUrun Ara (Arama)", FormImage.Type.URL, "https://textures.minecraft.net/texture/e3471018287532353a43f8e58319f39e31d3e23078a0d0a519a4d80a3a78e7f1")
                .button("§b§lBloklar", FormImage.Type.URL, "https://textures.minecraft.net/texture/a18a287a206a4b159f81f1e8a834e56d43e597c5f3b7933f1f3a22c53a6e3")
                .button("§6§lMadenler & Esyalar", FormImage.Type.URL, "https://textures.minecraft.net/texture/a31a980562e15119bb989a3a91e5e54d632f122e23d7f76371c1f4e1f7d5c7")
                .button("§d§lYemekler", FormImage.Type.URL, "https://textures.minecraft.net/texture/416323f81504221191060938a168a719c8173400a454d6d1d4d802ed3a2b7e9")
                .button("§c§lEkipman & Zirh", FormImage.Type.URL, "https://textures.minecraft.net/texture/2f8b50e18d6e3c1626f21c27e857850a4d538e1b0c950d8df302e1c9533a1e2");

        form.validResultHandler(response -> {
            int btn = response.clickedButtonId();
            if (btn == 0) openMarketAramaForm(player);
            else if (btn == 1) openMarketKategoriListesi(player, "Bloklar");
            else if (btn == 2) openMarketKategoriListesi(player, "Madenler");
            else if (btn == 3) openMarketKategoriListesi(player, "Yemekler");
            else if (btn == 4) openMarketKategoriListesi(player, "Ekipman");
        });

        sendBedrockForm(player, form.build());
    }

    private void openMarketAramaForm(Player player) {
        CustomForm form = CustomForm.builder()
                .title("§e§lMarket Arama")
                .input("Aramak istediginiz urunun adini yazin:", "Elmas, Odun, Biftek...")
                .validResultHandler(response -> {
                    String arama = response.asInput(0);
                    if (arama == null || arama.trim().isEmpty()) {
                        player.sendMessage("§cArama kelimesi girmediniz!");
                        return;
                    }
                    showSearchResults(player, arama.trim().toLowerCase());
                })
                .build();

        sendBedrockForm(player, form);
    }

    private void showSearchResults(Player player, String query) {
        List<ShopItem> sonuclar = new ArrayList<>();
        for (ShopItem item : shopItems) {
            if (item.displayName.toLowerCase().contains(query) || item.material.name().toLowerCase().contains(query)) {
                sonuclar.add(item);
            }
        }

        if (sonuclar.isEmpty()) {
            player.sendMessage("§c[Market] '" + query + "' aramasina uygun urun bulunamadi!");
            return;
        }

        SimpleForm.Builder form = SimpleForm.builder()
                .title("§8=== Arama Sonuclari §8===")
                .content("§7Bulunan Urunler:");

        for (ShopItem item : sonuclar) {
            form.button("§f" + item.displayName + "\n§aFiyat: $" + item.buyPrice, FormImage.Type.URL, item.iconUrl);
        }

        form.validResultHandler(response -> {
            int index = response.clickedButtonId();
            if (index >= 0 && index < sonuclar.size()) {
                buyShopItem(player, sonuclar.get(index));
            }
        });

        sendBedrockForm(player, form.build());
    }

    private void openMarketKategoriListesi(Player player, String category) {
        List<ShopItem> katUrunleri = new ArrayList<>();
        for (ShopItem item : shopItems) {
            if (item.category.equalsIgnoreCase(category)) {
                katUrunleri.add(item);
            }
        }

        SimpleForm.Builder form = SimpleForm.builder()
                .title("§8=== " + category + " §8===")
                .content("§7Bakiyeniz: §a$" + String.format("%.0f", getBalance(player)));

        for (ShopItem item : katUrunleri) {
            form.button("§f" + item.displayName + "\n§aFiyat: $" + item.buyPrice, FormImage.Type.URL, item.iconUrl);
        }

        form.validResultHandler(response -> {
            int index = response.clickedButtonId();
            if (index >= 0 && index < katUrunleri.size()) {
                buyShopItem(player, katUrunleri.get(index));
            }
        });

        sendBedrockForm(player, form.build());
    }

    private void buyShopItem(Player player, ShopItem item) {
        double fiyat = item.buyPrice;
        if (withdrawBalance(player, fiyat)) {
            player.getInventory().addItem(new ItemStack(item.material, item.amount));
            player.sendMessage("§a[Market] §e" + item.displayName + " §fbasariyla satin alindi! §a-$" + fiyat);
        } else {
            player.sendMessage("§c[Market] Yetersiz bakiye! Bu urun icin §a$" + fiyat + " §cgerekiyor.");
        }
    }

    private void openAnaKlanMenusu(Player player) { }
    private void handleKlanChat(Player player, String[] args) { }
    private void openArsaMenusu(Player player) { }
    private void openKitMenusu(Player player) { }
}
