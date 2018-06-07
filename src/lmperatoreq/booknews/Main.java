package net.lmperatoreq.booknews;

import java.io.File;
import java.sql.Connection;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main
  extends JavaPlugin implements Listener
{
  public static Main instance;
  public File cfg = new File(getDataFolder(), "config.yml");
  private Connection dbConnection;
  
  public Main() {}
  
  public void onEnable() { instance = this;
    if (!cfg.exists()) {
      saveDefaultConfig();
    }
    getServer().getPluginManager().registerEvents(new BookNews(), this);
    getCommand("news").setExecutor(new Command());
  }
}
