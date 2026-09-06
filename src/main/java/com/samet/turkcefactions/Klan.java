package com.samet.turkcefactions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

public class Klan extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getCommand("klan") != null) {
            getCommand("klan").setExecutor(this);
        }
        getLogger().info("§aTurkce Bedrock Klan Eklentisi Aktif!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komut sadece oyunda kullanilabilir!");
            return true;
        }

        // Oyuncu Bedrock (mobil) cihazdan mı girmiş kontrol et
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            player.sendMessage("§cBu menuyu sadece Bedrock/Mobil oyunculari kullanabilir.");
            return true;
        }

        // Ana Mobil Menü
        SimpleForm form = SimpleForm.builder()
                .title("§8=== §bKLAN MENUSU §8===")
                .content("Lutfen yapmak istediginiz islemi secin:")
                .button("§a§lKlan Kur", FormImage.Type.URL, "https://textures.minecraft.net/texture/b05688773923ea505745fc7e6168e0e09e3be31d816048556cef289eb4e1d")
                .button("§e§lKlan Bilgileri", FormImage.Type.URL, "https://textures.minecraft.net/texture/d3471018287532353a43f8e58319f39e31d3e23078a0d0a519a4d80a3a78e7f1")
                .validResultHandler(response -> {
                    int clickedButtonId = response.clickedButtonId();
                    if (clickedButtonId == 0) {
                        openKlanKurForm(player);
                    } else if (clickedButtonId == 1) {
                        showKlanInfo(player);
                    }
                })
                .build();

        // Formu Floodgate API ile oyuncuya gönder
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
        return true;
    }

    private void openKlanKurForm(Player player) {
        if (getConfig().contains("oyuncular." + player.getUniqueId())) {
            String mevcutKlan = getConfig().getString("oyuncular." + player.getUniqueId() + ".klan");
            player.sendMessage("§cZaten §e" + mevcutKlan + " §cadli bir klandasiniz!");
            return;
        }

        CustomForm klanKurForm = CustomForm.builder()
                .title("§a§lKlan Olustur")
                .input("Klaninizin Adini Girin:", "Klan Ismi...")
                .validResultHandler(response -> {
                    String klanAdi = response.asInput(0);

                    if (klanAdi == null || klanAdi.trim().isEmpty()) {
                        player.sendMessage("§cGecerli bir klan ismi girmediniz!");
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
                    getConfig().set("klanlar." + klanAdi.toLowerCase() + ".liderUUID", uuid);

                    getConfig().set("oyuncular." + uuid + ".klan", klanAdi);
                    getConfig().set("oyuncular." + uuid + ".rol", "Lider");

                    saveConfig();

                    player.sendMessage("§a[Klan] §e" + klanAdi + " §fisimli klaniniz basariyla olusturuldu!");
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), klanKurForm);
    }

    private void showKlanInfo(Player player) {
        String uuid = player.getUniqueId().toString();

        if (!getConfig().contains("oyuncular." + uuid)) {
            player.sendMessage("§e[Klan] §fHenuz bir klaniniz yok.");
            return;
        }

        String klanAdi = getConfig().getString("oyuncular." + uuid + ".klan");
        String rol = getConfig().getString("oyuncular." + uuid + ".rol");
        String lider = getConfig().getString("klanlar." + klanAdi.toLowerCase() + ".lider");

        player.sendMessage("§8=== §b" + klanAdi + " Klan Bilgileri §8===");
        player.sendMessage("§7Klan Lideri: §e" + lider);
        player.sendMessage("§7Klandaki Rolunuz: §a" + rol);
    }
}
