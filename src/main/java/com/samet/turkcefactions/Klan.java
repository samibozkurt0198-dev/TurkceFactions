package com.samet.turkcefactions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Klan extends JavaPlugin implements CommandExecutor {

    // Davet hafızası: Davet Edilen UUID -> Klan Adı
    private final Map<UUID, String> klanDavetleri = new HashMap<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Komutları Kaydet
        registerCommand("klan");
        registerCommand("arsa");
        registerCommand("kit");
        registerCommand("kc");

        getLogger().info("§aTurkce Bedrock Factions, Arsa ve Kit Eklentisi Aktif!");
    }

    private void registerCommand(String name) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komutlar sadece oyunda kullanilabilir!");
            return true;
        }

        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            player.sendMessage("§cBu menuyu sadece Bedrock/Mobil oyunculari kullanabilir.");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "klan":
                openAnaKlanMenusu(player);
                break;
            case "arsa":
                openArsaMenusu(player);
                break;
            case "kit":
                openKitMenusu(player);
                break;
            case "kc":
                handleKlanChat(player, args);
                break;
        }

        return true;
    }

    // ==========================================
    // 1. KLAN SİSTEMİ & MENÜLERİ
    // ==========================================

    private void openAnaKlanMenusu(Player player) {
        String uuid = player.getUniqueId().toString();
        boolean klanVarMi = getConfig().contains("oyuncular." + uuid + ".klan");

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("§8=== §bKLAN YÖNETİMİ §8===")
                .content("Yapmak istediğiniz işlemi seçin:");

        if (!klanVarMi) {
            builder.button("§a§lKlan Kur", FormImage.Type.URL, "https://textures.minecraft.net/texture/b05688773923ea505745fc7e6168e0e09e3be31d816048556cef289eb4e1d")
                   .button("§e§lGelen Davetler", FormImage.Type.URL, "https://textures.minecraft.net/texture/d3471018287532353a43f8e58319f39e31d3e23078a0d0a519a4d80a3a78e7f1");

            builder.validResultHandler(response -> {
                int id = response.clickedButtonId();
                if (id == 0) openKlanKurForm(player);
                else if (id == 1) openDavetMenusu(player);
            });
        } else {
            String klanAdi = getConfig().getString("oyuncular." + uuid + ".klan");
            String rol = getConfig().getString("oyuncular." + uuid + ".rol");

            builder.button("§b§lKlan Evine Git (Home)", FormImage.Type.URL, "https://textures.minecraft.net/texture/9e2d1f7c5e8b4e13a29b47e85c2b3f11d123456789a0b1c2d3e4f5a6b7c8d9e")
                   .button("§e§lKlan Bilgileri & Üyeler", FormImage.Type.URL, "https://textures.minecraft.net/texture/d3471018287532353a43f8e58319f39e31d3e23078a0d0a519a4d80a3a78e7f1");

            if ("Lider".equals(rol) || "Subay".equals(rol)) {
                builder.button("§a§lOyuncu Davet Et", FormImage.Type.URL, "https://textures.minecraft.net/texture/b05688773923ea505745fc7e6168e0e09e3be31d816048556cef289eb4e1d")
                       .button("§c§lKlan Evini Belirle (Sethome)", FormImage.Type.URL, "https://textures.minecraft.net/texture/416323f81504221191060938a168a719c8173400a454d6d1d4d802ed3a2b7e9");
            }

            builder.button("§c§lKlandan Ayrıl", FormImage.Type.URL, "https://textures.minecraft.net/texture/5f29910e5b0b10b3ef18b375be8540329616d2861e68da08512ccb4415e1926d");

            builder.validResultHandler(response -> {
                int id = response.clickedButtonId();
                if (id == 0) klanHomeIsinlan(player, klanAdi);
                else if (id == 1) showKlanInfo(player, klanAdi);
                else if (("Lider".equals(rol) || "Subay".equals(rol)) && id == 2) openDavetEtForm(player, klanAdi);
                else if (("Lider".equals(rol) || "Subay".equals(rol)) && id == 3) setKlanHome(player, klanAdi);
                else klandanAyril(player);
            });
        }

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private void openKlanKurForm(Player player) {
        CustomForm klanKurForm = CustomForm.builder()
                .title("§a§lKlan Oluştur")
                .input("Klanınızın Adını Girin:", "Klan İsmi...")
                .validResultHandler(response -> {
                    String klanAdi = response.asInput(0);
                    if (klanAdi == null || klanAdi.trim().isEmpty()) {
                        player.sendMessage("§cGeçersiz bir klan ismi girmediniz!");
                        return;
                    }

                    klanAdi = klanAdi.trim();
                    if (getConfig().contains("klanlar." + klanAdi.toLowerCase())) {
                        player.sendMessage("§cBu isimde bir klan zaten var!");
                        return;
                    }

                    String uuid = player.getUniqueId().toString();
                    getConfig().set("klanlar." + klanAdi.toLowerCase() + ".isim", klanAdi);
                    getConfig().set("klanlar." + klanAdi.toLowerCase() + ".lider", player.getName());

                    getConfig().set("oyuncular." + uuid + ".klan", klanAdi);
                    getConfig().set("oyuncular." + uuid + ".rol", "Lider");

                    saveConfig();
                    player.sendMessage("§a[Klan] §e" + klanAdi + " §fisimli klanınız başarıyla oluşturuldu!");
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), klanKurForm);
    }

    private void setKlanHome(Player player, String klanAdi) {
        Location loc = player.getLocation();
        String path = "klanlar." + klanAdi.toLowerCase() + ".home.";
        getConfig().set(path + "world", loc.getWorld().getName());
        getConfig().set(path + "x", loc.getX());
        getConfig().set(path + "y", loc.getY());
        getConfig().set(path + "z", loc.getZ());
        getConfig().set(path + "yaw", loc.getYaw());
        getConfig().set(path + "pitch", loc.getPitch());
        saveConfig();

        player.sendMessage("§a[Klan] Klan evi bulunduğunuz konuma ayarlandı!");
    }

    private void klanHomeIsinlan(Player player, String klanAdi) {
        String path = "klanlar." + klanAdi.toLowerCase() + ".home";
        if (!getConfig().contains(path)) {
            player.sendMessage("§c[Klan] Klanınız henüz bir klan evi (sethome) belirlememiş!");
            return;
        }

        Location loc = new Location(
                Bukkit.getWorld(getConfig().getString(path + ".world")),
                getConfig().getDouble(path + ".x"),
                getConfig().getDouble(path + ".y"),
                getConfig().getDouble(path + ".z"),
                (float) getConfig().getDouble(path + ".yaw"),
                (float) getConfig().getDouble(path + ".pitch")
        );

        player.teleport(loc);
        player.sendMessage("§a[Klan] Klan evine ışınlandınız!");
    }

    private void openDavetEtForm(Player player, String klanAdi) {
        CustomForm form = CustomForm.builder()
                .title("§a§lKlana Oyuncu Davet Et")
                .input("Davet edilecek oyuncunun adını girin:", "Oyuncu Adı...")
                .validResultHandler(response -> {
                    String hedefIsim = response.asInput(0);
                    Player hedef = Bukkit.getPlayer(hedefIsim);

                    if (hedef == null || !hedef.isOnline()) {
                        player.sendMessage("§cOyuncu bulunamadı veya çevrimdışı!");
                        return;
                    }

                    if (getConfig().contains("oyuncular." + hedef.getUniqueId() + ".klan")) {
                        player.sendMessage("§cBu oyuncu zaten başka bir klanda!");
                        return;
                    }

                    klanDavetleri.put(hedef.getUniqueId(), klanAdi);
                    player.sendMessage("§a[Klan] §e" + hedef.getName() + " §fisimli oyuncuya davet gönderildi.");
                    hedef.sendMessage("§a[Klan] §e" + klanAdi + " §fklanından davet aldınız! §e/klan §fmenüsünden kabul edebilirsiniz.");
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private void openDavetMenusu(Player player) {
        UUID uuid = player.getUniqueId();
        if (!klanDavetleri.containsKey(uuid)) {
            player.sendMessage("§c[Klan] Size gönderilmiş aktif bir klan daveti yok.");
            return;
        }

        String klanAdi = klanDavetleri.get(uuid);

        SimpleForm form = SimpleForm.builder()
                .title("§e§lKlan Daveti")
                .content("§e" + klanAdi + " §fklanına katılmak istiyor musunuz?")
                .button("§a§lKabul Et")
                .button("§c§lReddet")
                .validResultHandler(response -> {
                    if (response.clickedButtonId() == 0) {
                        getConfig().set("oyuncular." + uuid + ".klan", klanAdi);
                        getConfig().set("oyuncular." + uuid + ".rol", "Üye");
                        saveConfig();
                        klanDavetleri.remove(uuid);
                        player.sendMessage("§a[Klan] §e" + klanAdi + " §fklanına başarıyla katıldınız!");
                    } else {
                        klanDavetleri.remove(uuid);
                        player.sendMessage("§c[Klan] Davet reddedildi.");
                    }
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private void showKlanInfo(Player player, String klanAdi) {
        String lider = getConfig().getString("klanlar." + klanAdi.toLowerCase() + ".lider");
        player.sendMessage("§8=== §b" + klanAdi + " Klan Bilgileri §8===");
        player.sendMessage("§7Lider: §e" + lider);
        player.sendMessage("§7Rolünüz: §a" + getConfig().getString("oyuncular." + player.getUniqueId() + ".rol"));
    }

    private void klandanAyril(Player player) {
        String uuid = player.getUniqueId().toString();
        String klan = getConfig().getString("oyuncular." + uuid + ".klan");
        String rol = getConfig().getString("oyuncular." + uuid + ".rol");

        if ("Lider".equals(rol)) {
            player.sendMessage("§cKlan lideri klandan ayrılamaz! Önce klanı devretmeli veya silmelisiniz.");
            return;
        }

        getConfig().set("oyuncular." + uuid, null);
        saveConfig();
        player.sendMessage("§c[Klan] §e" + klan + " §fklanından ayrıldınız.");
    }

    private void handleKlanChat(Player player, String[] args) {
        String uuid = player.getUniqueId().toString();
        if (!getConfig().contains("oyuncular." + uuid + ".klan")) {
            player.sendMessage("§cKlan sohbetini kullanmak için bir klanda olmalısınız!");
            return;
        }

        if (args.length == 0) {
            player.sendMessage("§cKullanım: /kc <mesaj>");
            return;
        }

        String klan = getConfig().getString("oyuncular." + uuid + ".klan");
        String mesaj = String.join(" ", args);

        for (Player p : Bukkit.getOnlinePlayers()) {
            String pKlan = getConfig().getString("oyuncular." + p.getUniqueId() + ".klan");
            if (klan.equalsIgnoreCase(pKlan)) {
                p.sendMessage("§b[KLAN SOHBET] §e" + player.getName() + "§7: §f" + mesaj);
            }
        }
    }

    // ==========================================
    // 2. ARSA SİSTEMİ (64x64 Parseller - 100 Adet)
    // ==========================================

    private void openArsaMenusu(Player player) {
        SimpleForm.Builder form = SimpleForm.builder()
                .title("§8=== §aARSA YÖNETİMİ §8===")
                .content("Satın alınabilir 64x64 arsalar (100 Adet Parsel):");

        for (int i = 1; i <= 100; i++) {
            boolean satildiMi = getConfig().contains("arsalar." + i + ".sahip");
            String durum = satildiMi ? "§c[DOLU] Arsa #" + i : "§a[BOŞ - 5000$] Arsa #" + i;
            form.button(durum);
        }

        form.validResultHandler(response -> {
            int arsaId = response.clickedButtonId() + 1;
            openArsaDetayForm(player, arsaId);
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form.build());
    }

    private void openArsaDetayForm(Player player, int arsaId) {
        boolean satildiMi = getConfig().contains("arsalar." + arsaId + ".sahip");
        String sahip = getConfig().getString("arsalar." + arsaId + ".sahip", "Sahipsiz");

        SimpleForm.Builder form = SimpleForm.builder()
                .title("§8=== Arsa #" + arsaId + " §8===")
                .content("§7Boyut: §e64x64 Block\n§7Durum: " + (satildiMi ? "§cSatıldı" : "§aSatılık (5000$)") + "\n§7Sahibi: §e" + sahip);

        if (!satildiMi) {
            form.button("§a§lSatın Al (5000$)");
        }
        form.button("§b§lArsaya Işınlan");

        form.validResultHandler(response -> {
            int btn = response.clickedButtonId();
            if (!satildiMi && btn == 0) {
                // Arsa Satın Alma
                getConfig().set("arsalar." + arsaId + ".sahip", player.getName());
                getConfig().set("arsalar." + arsaId + ".sahipUUID", player.getUniqueId().toString());
                saveConfig();
                player.sendMessage("§a[Arsa] #" + arsaId + " numaralı 64x64 arsa başarıyla satın alındı!");
            } else {
                // Arsaya Işınlanma (Örnek Koordinat: Her arsa arası 64 blok boşluk)
                int x = (arsaId % 10) * 100;
                int z = (arsaId / 10) * 100;
                Location arsaLoc = new Location(player.getWorld(), x, 65, z);
                player.teleport(arsaLoc);
                player.sendMessage("§a[Arsa] #" + arsaId + " numaralı arsaya ışınlandınız!");
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form.build());
    }

    // ==========================================
    // 3. KIT SİSTEMİ
    // ==========================================

    private void openKitMenusu(Player player) {
        SimpleForm form = SimpleForm.builder()
                .title("§8=== §eOYUNCU KİTLERİ §8===")
                .content("Lütfen almak istediğiniz kiti seçin:")
                .button("§a§lBaşlangıç Kiti", FormImage.Type.URL, "https://textures.minecraft.net/texture/b05688773923ea505745fc7e6168e0e09e3be31d816048556cef289eb4e1d")
                .button("§b§lVIP Kit", FormImage.Type.URL, "https://textures.minecraft.net/texture/d3471018287532353a43f8e58319f39e31d3e23078a0d0a519a4d80a3a78e7f1")
                .validResultHandler(response -> {
                    int id = response.clickedButtonId();
                    if (id == 0) {
                        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_SWORD));
                        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_PICKAXE));
                        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.COOKED_BEEF, 32));
                        player.sendMessage("§a[Kit] Başlangıç kiti alındı!");
                    } else if (id == 1) {
                        if (player.hasPermission("turkcefactions.kit.vip")) {
                            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_SWORD));
                            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_HELMET));
                            player.sendMessage("§a[Kit] VIP kiti alındı!");
                        } else {
                            player.sendMessage("§c[Kit] VIP kitini almak için yetkiniz yok!");
                        }
                    }
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }
}
