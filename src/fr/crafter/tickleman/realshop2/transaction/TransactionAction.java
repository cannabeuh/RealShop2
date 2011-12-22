package fr.crafter.tickleman.realshop2.transaction;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.crafter.tickleman.realplugin.RealEnchantment;
import fr.crafter.tickleman.realplugin.RealItemType;
import fr.crafter.tickleman.realplugin.RealColor;
import fr.crafter.tickleman.realplugin.RealItemStack;
import fr.crafter.tickleman.realshop2.RealShop2Plugin;
import fr.crafter.tickleman.realshop2.price.ItemPriceList;
import fr.crafter.tickleman.realshop2.price.Price;
import fr.crafter.tickleman.realshop2.shop.Shop;

//############################################################################### TransactionAction
public class TransactionAction
{

	private RealShop2Plugin plugin;

	//----------------------------------------------------------------------------- TransactionAction
	public TransactionAction(RealShop2Plugin plugin)
	{
		this.plugin = plugin;
	}

	//------------------------------------------------------------------------------------------- buy
	public int buy(Player player, Shop shop, ItemStack itemStack)
	{
		plugin.getLog().debug("buy " + RealItemStack.create(itemStack).toString());
		if (canPay(player, shop, itemStack, null)) {
			Price price = calculatePrice(shop, itemStack);
			if (price != null) {
				plugin.getEconomy().transfer(
					player.getName(), shop.getPlayerName(), price.getBuyPrice(itemStack.getAmount())
				);
				sendMessage(
					player, shop, itemStack,
					price.getBuyPrice(), price.getBuyPrice(itemStack.getAmount()),
					"Purchased", "purchased"
				);
				return itemStack.getAmount();
			}
		}
		return 0;
	}

	//--------------------------------------------------------------------- broadcastPlayersBuyOrSell
	private void broadcastNearbyPlayersBuyOrSell(
			ItemStack itemStack, RealItemType itemType, Player player, Shop shop, String side,
			double amount, double price
	) {
		for (Entity entity : player.getNearbyEntities(5.0d, 5.0d, 5.0d)) {
			if (entity instanceof Player) {
				Player nearbyPlayer = (Player)entity;
				nearbyPlayer.sendMessage(
					RealColor.text
					+ plugin.tr("+client " + side + " +item x+quantity (+linePrice)")
					.replace("+client", RealColor.player + player.getName() + RealColor.text)
					.replace("+item", RealColor.item + plugin.tr(itemType.getName()) + RealColor.text)
					.replace("+linePrice", "" + RealColor.price + amount + RealColor.text)
					.replace("+name", RealColor.shop + shop.getName() + RealColor.text)
					.replace("+owner", RealColor.player + shop.getPlayerName() + RealColor.text)
					.replace("+price", "" + RealColor.price + price + RealColor.text)
					.replace("+quantity", "" + RealColor.quantity + itemStack.getAmount() + RealColor.text)
					.replace("  ", " ").replace(" ]", "]").replace("[ ", "[")
				);
			}
		}
	}

  //----------------------------------------------------------------------------------------- price
	public Price calculatePrice(Shop shop, ItemStack itemStack)
	{
		return calculatePrice(shop, new RealItemStack(itemStack));
	}

	//----------------------------------------------------------------------------------------- price
	public Price calculatePrice(Shop shop, RealItemStack itemStack)
	{
		RealItemType itemType = itemStack.getItemType();
		ItemPriceList prices = new ItemPriceList(plugin, shop.getPlayerName()).load();
		Price price = prices.getPrice(itemType, itemStack.getDamage(), plugin.getMarketPrices());
		return price;
	}

	//---------------------------------------------------------------------------------------- canPay
	/**
	 * Return true if player can buy buyStack and sell sellStack into the shop
	 */
	public boolean canPay(Player player, Shop shop, ItemStack buyStack, ItemStack sellStack)
	{
		if((sellStack != null)){
			if(!RealEnchantment.enchantable(sellStack.getTypeId()).equals("NADA")){
				if(sellStack.getEnchantments()!=null){
					return false;
				}
				ItemStack testDuration = new ItemStack(sellStack.getTypeId());
				if(sellStack.getDurability()<testDuration.getDurability()){
					return false;
				}
			}
		}
		if((buyStack != null)){
			if(!RealEnchantment.enchantable(buyStack.getTypeId()).equals("NADA")){
				if(buyStack.getEnchantments()!=null){
					return false;
				}
				ItemStack testDuration = new ItemStack(buyStack.getTypeId());
				if(buyStack.getDurability()<testDuration.getDurability()){
					return false;
				}
			}
		}
		if ((buyStack != null) && !shop.canBuyItem(plugin, new RealItemStack(buyStack))) {
			plugin.getLog().debug("can not buy item");
			return false;
		}
		if ((sellStack != null) && !shop.canSellItem(plugin, new RealItemStack(sellStack))) {
			plugin.getLog().debug("can not sell item");
			return false;
		}
		Price buyPrice = (buyStack == null) ? null : calculatePrice(shop, buyStack);
		Price sellPrice = (sellStack == null) ? null : calculatePrice(shop, sellStack);
		if (
			((buyStack != null) && (buyStack.getAmount() > 0) && (buyPrice == null))
			|| ((sellStack != null) && (sellStack.getAmount() > 0) && (sellPrice == null))
		) {
			if ((buyStack != null) && (buyStack.getAmount() > 0) && (buyPrice == null)) {
				plugin.getLog().debug("canPay price not found for buy " + RealItemStack.create(buyStack));
			}
			if ((buyStack != null) && (buyStack.getAmount() > 0) && (buyPrice == null)) {
				plugin.getLog().debug("canPay sell price not found for sell " + RealItemStack.create(sellStack));
			}
			plugin.getLog().debug("Can not pay as an item has a null price");
			// can't pay if any item has a null price ("price not found")
			return false;
		}
		double diffAmount
			= ((sellPrice == null) ? 0 : sellPrice.getSellPrice(sellStack.getAmount()))
			- ((buyPrice == null) ? 0 : buyPrice.getBuyPrice(buyStack.getAmount()));
		if (diffAmount > 0) {
			plugin.getLog().debug(
				"canPay check if " + plugin.getEconomy().getBalance(shop.getPlayerName())
				+ " >= " + diffAmount
			);
			// sell more than buy : can pay if shop's owner has enough money
			return plugin.getEconomy().getBalance(shop.getPlayerName()) >= diffAmount;
		} else {
			plugin.getLog().debug(
				"canPay check if " + plugin.getEconomy().getBalance(player.getName())
				+ " >= " + (-diffAmount)
			);
			// buy more than sell : can pay if client player has enough money
			return plugin.getEconomy().getBalance(player.getName()) >= -diffAmount;
		}
	}

	//------------------------------------------------------------------------------------------ sell
	public int sell(Player player, Shop shop, ItemStack itemStack)
	{
		plugin.getLog().debug("sell " + RealItemStack.create(itemStack).toString());
		if (canPay(player, shop, null, itemStack)) {
			Price price = calculatePrice(shop, itemStack);
			if (price != null) {
				plugin.getEconomy().transfer(
					shop.getPlayerName(), player.getName(), price.getSellPrice(itemStack.getAmount())
				);
				sendMessage(
					player, shop, itemStack,
					price.getSellPrice(), price.getSellPrice(itemStack.getAmount()),
					"Sold", "sold"
				);
				return itemStack.getAmount();
			}
		}
		return 0;
	}

	//----------------------------------------------------------------------------------- sendMessage
	private void sendMessage(
		Player player, Shop shop, ItemStack itemStack,
		double price, double amount,
		String side, String shopSide
	) {
		RealItemType itemType = new RealItemType(itemStack);
		player.sendMessage(
			RealColor.text
			+ plugin.tr(side + " +item x+quantity (+linePrice)")
			.replace("+client", RealColor.player + player.getName() + RealColor.text)
			.replace("+item", RealColor.item + plugin.tr(itemType.getName()) + RealColor.text)
			.replace("+linePrice", "" + RealColor.price + amount + RealColor.text)
			.replace("+name", RealColor.shop + shop.getName() + RealColor.text)
			.replace("+owner", RealColor.player + shop.getPlayerName() + RealColor.text)
			.replace("+price", "" + RealColor.price + price + RealColor.text)
			.replace("+quantity", "" + RealColor.quantity + itemStack.getAmount() + RealColor.text)
			.replace("  ", " ").replace(" ]", "]").replace("[ ", "[")
		);
		Player shopPlayer = plugin.getServer().getPlayer(shop.getPlayerName());
		if (shopPlayer != null) {
			shopPlayer.sendMessage(
				RealColor.text
				+ plugin.tr("+client " + shopSide + " +item x+quantity (+linePrice)")
				.replace("+client", RealColor.player + player.getName() + RealColor.text)
				.replace("+item", RealColor.item + plugin.tr(itemType.getName()) + RealColor.text)
				.replace("+linePrice", "" + RealColor.price + amount + RealColor.text)
				.replace("+name", RealColor.shop + shop.getName() + RealColor.text)
				.replace("+owner", RealColor.player + shop.getPlayerName() + RealColor.text)
				.replace("+price", "" + RealColor.price + price + RealColor.text)
				.replace("+quantity", "" + RealColor.quantity + itemStack.getAmount() + RealColor.text)
				.replace("  ", " ").replace(" ]", "]").replace("[ ", "[")
			);
		}
		broadcastNearbyPlayersBuyOrSell(itemStack, itemType, player, shop, shopSide, amount, price);
	}

}
