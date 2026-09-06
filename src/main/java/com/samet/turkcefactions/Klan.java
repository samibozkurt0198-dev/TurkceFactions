package com.samet.turkcefactions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;

public class Klan extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        if (getCommand("klan") != null) {
            getCommand("klan").setExecutor(this);
        }
        getLogger().info("§aTurkce Bedrock Mobil Klan Eklentisi Aktif!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komut sadece oyunda kullanilabilir!");
            return true;
        }

        // Bedrock Ekran Formu (Dokunmatik Menü)
        SimpleForm form = SimpleForm.builder()
                .title("§8=== §bKLAN MENUSU §8===")
                .content("Lutfen yapmak istediginiz islemi secin:")
                .button("§a§lKlan Kur", FormImage.Type.URL, "https://textures.minecraft.net/texture/b05688773923ea505745fc7e6168e0e09e3be31d816048556cef289eb4e1d")
                .button("§e§lKlan Bilgileri", FormImage.Type.URL, "https://textures.minecraft.net/texture/d3471018287532353a43f8e58319f39e31d3e23078a0d0a519a4d80a3a78e7f1")
                .validResultHandler(response -> {
                    int clickedButtonId = response.clickedButtonId();
                    if (clickedButtonId == 0) {
                        player.sendMessage("§a[Klan] §fKlan kurmak icin: §e/klan kur <Isim>");
                    } else if (clickedButtonId == 1) {
                        player.sendMessage("§e[Klan] §fHenuz bir klaniniz yok.");
                    }
                })
                .build();

        player.sendForm(form);
        return true;
    }
}
