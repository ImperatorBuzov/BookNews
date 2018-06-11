package net.lmperatoreq.booknews;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class Command
  implements CommandExecutor
{
  String text;
  
  public Command()
  {
    JsonParser parser = new JsonParser();
    try
    {
      JsonElement response = parser.parse(new InputStreamReader(post("https://api.vk.com/method/wall.get", "count=2&owner_id=-" + Main.instance.getConfig().getString("BookNews.GROUP_ID") + "&access_token=110ff6243a05161b7318f7e8a3bc2cbc173e1e75489c6b0cfe36efd6f87364dbd5ee8a63dbc06545a0845&v=5.76")));
      

      JsonArray items = response.getAsJsonObject().getAsJsonObject("response").getAsJsonArray("items");
      
      for (JsonElement post : items) {
        JsonObject msgObj = post.getAsJsonObject();
        
        if ((!msgObj.has("is_pinned")) || (msgObj.get("is_pinned").getAsInt() != 1)) {
          text = msgObj.get("text").getAsString();
          
          break;
        }
      }
    } catch (IOException e1) {
      e1.printStackTrace();
      
      return;
    }
  }
  
  private InputStream post(String url, String args) throws IOException
  {
    HttpsURLConnection urlConnection = (HttpsURLConnection)new URL(url + "?" + args).openConnection();
    
    return urlConnection.getResponseCode() == 200 ? urlConnection.getInputStream() : urlConnection.getErrorStream();
  }
  
  public boolean onCommand(CommandSender s, org.bukkit.command.Command cmd, String label, String[] args)
  {
    final Player p = (Player)s;
    
    new BukkitRunnable() {
      public void run() {
        org.bukkit.inventory.ItemStack book = Command.newBook("LmperatoreqTOP", "VK", (String[])Splitter.fixedLength(256).splitToList(text).toArray(new String[0]));
        
        Player player = p.getPlayer();
        
        openBook(book, player);
        
        p.sendMessage("§7Вы открыли новости.");
      }
    }.runTaskLater(Main.instance, 1L);
    return false;
  }
  
  public static org.bukkit.inventory.ItemStack newBook(String title, String author, String... pages)
  {
    org.bukkit.inventory.ItemStack is = new org.bukkit.inventory.ItemStack(Material.WRITTEN_BOOK, 1);
    net.minecraft.server.v1_8_R3.ItemStack nmsis = CraftItemStack.asNMSCopy(is);
    NBTTagCompound bd = new NBTTagCompound();
    bd.setString("title", title);
    bd.setString("author", author);
    NBTTagList bp = new NBTTagList();
    for (String text : pages) {
      bp.add(new NBTTagString(text));
    }
    bd.set("pages", bp);
    nmsis.setTag(bd);
    is = CraftItemStack.asBukkitCopy(nmsis);
    return is;
  }
  
  public void openBook(org.bukkit.inventory.ItemStack book, Player p) {
    int slot = p.getInventory().getHeldItemSlot();
    org.bukkit.inventory.ItemStack old = p.getInventory().getItem(slot);
    p.getInventory().setItem(slot, book);
    
    ByteBuf buf = Unpooled.buffer(256);
    buf.setByte(0, 0);
    buf.writerIndex(1);
    
    PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
    getHandleplayerConnection.sendPacket(packet);
    p.getInventory().setItem(slot, old);
  }
}
