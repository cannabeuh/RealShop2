package fr.crafter.tickleman.realshop2.shop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.block.Block;

import fr.crafter.tickleman.realplugin.RealPlugin;
import fr.crafter.tickleman.realplugin.RealLocation;

//######################################################################################## ShopList
public class ShopList
{

	private final String fileName;

	private final RealPlugin plugin;

	/** Shops list : "x;y;z;world" => Shop */
	private Map<String, Shop> shops = new HashMap<String, Shop>();

	//-------------------------------------------------------------------------------------- ShopList
	public ShopList(final RealPlugin plugin)
	{
		this(plugin, "shops");
	}

	//-------------------------------------------------------------------------------------- ShopList
	public ShopList(final RealPlugin plugin, String fileName)
	{
		this.plugin = plugin;
		this.fileName = plugin.getDataFolder().getPath() + "/" + fileName + ".txt";
	}

	//----------------------------------------------------------------------------------------- clear
	public void clear()
	{
		shops.clear();
	}

	//---------------------------------------------------------------------------------------- delete
	public void delete(Shop shop)
	{
		shops.remove(shop.getId());
	}

	//--------------------------------------------------------------------------- getSortedByDistance
	public Map<Double, Shop> getSortedByDistance(Location location)
	{
		Map<Double, Shop> sortedShops = new TreeMap<Double, Shop>();
		for (Shop shop : shops.values()) {
			if (location.getWorld().equals(shop.getLocation().getWorld())) {
				double distance = Math.sqrt(
					Math.pow(Math.abs(shop.getLocation().getX() - location.getX()), 2)
					+ Math.pow(Math.abs(shop.getLocation().getZ() - location.getZ()), 2)
				);
				sortedShops.put(distance, shop);
			}
		}
		return sortedShops;
	}

	//---------------------------------------------------------------------------------------- isShop
	public boolean isShop(Block block)
	{
		return isShop(block.getLocation());
	}

	//---------------------------------------------------------------------------------------- isShop
	public boolean isShop(Location location)
	{
		return (shopAt(location) != null);
	}

	//------------------------------------------------------------------------------------------ load
	public ShopList load()
	{
		plugin.getLog().debug("ShopList.load()");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String buffer;
			while ((buffer = reader.readLine()) != null) {
				if ((buffer.length() > 0) && (buffer.charAt(0) != '#') && (buffer.split(";").length > 4)) {
					Shop shop = Shop.parseShop(plugin.getServer(), buffer);
					if (shop != null) {
						plugin.getLog().debug("shop load " + shop.toString());
						put(shop);
					}
				}
			}
		} catch (Exception e) {
			plugin.getLog().warning("File read error " + fileName + " (will create one)");
			save();
		}
		try { reader.close(); } catch (Exception e) {}
		return this;
	}

	//------------------------------------------------------------------------------------ loadFromV0
	public boolean loadFromV0()
	{
		String fileName = this.fileName.replace("RealShop2/", "RealShop/");
		plugin.getLog().debug("ShopList.loadFromV0(" + fileName + ")");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String buffer;
			while ((buffer = reader.readLine()) != null) {
				if ((buffer.length() > 0) && (buffer.charAt(0) != '#') && (buffer.split(";").length > 4)) {
					Shop shop = Shop.parseShopV0(plugin.getServer(), buffer);
					if (shop != null) {
						plugin.getLog().debug("shop load " + shop.toString());
						put(shop);
					}
				}
			}
		} catch (Exception e) {
			plugin.getLog().warning("File read error " + fileName + " (loadFromV0)");
		}
		return true;
	}

	//------------------------------------------------------------------------------------------- put
	public void put(Shop shop)
	{
		shops.put(shop.getId(), shop);
	}

	//------------------------------------------------------------------------------------------ save
	public void save()
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(
				"#world;x;y;z;x2;y2;z2;owner;name;buyOnly;sellOnly;buyExclude;sellExclude;opened;"
				+ "infiniteBuy;infiniteSell;marketItemsOnly;damagedItems\n"
			);
			for (Shop shop : shops.values()) {
				writer.write(shop.toString() + "\n");
			}
			writer.flush();
		} catch (Exception e) {
			plugin.getLog().severe("File save error " + fileName);
		}
		try { writer.close(); } catch (Exception e) {}
	}

	//---------------------------------------------------------------------------------------- shopAt
	/**
	 * Return Shop at location
	 * If location contains a big chest, both locations will be tested to be sure we get the shop
	 * even if it is on one of the two chest blocks
	 */
	public Shop shopAt(Location location)
	{
		Shop shop = shops.get(RealLocation.getId(location));
		if (shop == null) {
			location = RealLocation.neighbor(location);
			if (location != null) {
				shop = shops.get(RealLocation.getId(location));
			}
		}
		return shop;
	}

}
