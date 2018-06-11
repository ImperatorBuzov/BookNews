package net.lmperatoreq.booknews;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;

public class BookNews implements Listener {
	
	private Main instance;
	private String text;

	public BookNews(Main instance) {
		this.instance = instance;
		this.text = "";
	}

	public BookNews() {
		JsonParser parser = new JsonParser();
		try {
			JsonElement response = parser.parse(new InputStreamReader(this.post("https://api.vk.com/method/wall.get",
					"count=2&owner_id=-" + Main.instance.getConfig().getString("BookNews.GROUP_ID")
							+ "&access_token=110ff6243a05161b7318f7e8a3bc2cbc173e1e75489c6b0cfe36efd6f87364dbd5ee8a63dbc06545a0845&v=5.76")));

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
		}
	}

	private InputStream post(String url, String args) throws IOException {
		HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(url + "?" + args).openConnection();

		return urlConnection.getResponseCode() == 200 ? urlConnection.getInputStream() : urlConnection.getErrorStream();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(final PlayerJoinEvent e) {
		if (!e.getPlayer().hasPlayedBefore()) {
			new BukkitRunnable() {
				public void run() {
					org.bukkit.inventory.ItemStack book = BookNews.newBook("LmperatoreqTOP", "VK",
							(String[]) Splitter.fixedLength(256).splitToList(text).toArray(new String[0]));

					Player player = e.getPlayer();

					BookNews.this.openBook(book, player);
				}
			}.runTaskLater(Main.instance, 1L);
		}
	}

	private static ItemStack newBook(String title, String author, String... pages) {
		ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta) is.getItemMeta();
		meta.setAuthor(author);
		meta.setTitle(title);
		meta.setPages(pages);
		is.setItemMeta(meta);
		return is;
	}

	private void openBook(ItemStack book, Player p) {
		int slot = p.getInventory().getHeldItemSlot();
		org.bukkit.inventory.ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, book);

		ByteBuf buf = Unpooled.buffer(256);
		buf.setByte(0, 0);
		buf.writerIndex(1);

		PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		p.getInventory().setItem(slot, old);
	}
}
