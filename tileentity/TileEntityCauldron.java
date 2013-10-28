package clashsoft.mods.morepotions.tileentity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import clashsoft.brewingapi.BrewingAPI;
import clashsoft.brewingapi.brewing.Brewing;
import clashsoft.brewingapi.brewing.BrewingBase;
import clashsoft.brewingapi.brewing.BrewingList;
import cpw.mods.fml.common.network.PacketDispatcher;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;

public class TileEntityCauldron extends TileEntity
{
	public static final String CHANNEL = "MPMCauldron";
	
	public List<Brewing>	brewings;
	
	public ItemStack		output;
	public int				color;
	
	public TileEntityCauldron()
	{
		brewings = new ArrayList<Brewing>();
	}
	
	/**
	 * Check if the stack is a valid item for this slot. Always true beside for
	 * the armor slots.
	 */
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		if (par1ItemStack != null)
		{
			if (Item.itemsList[par1ItemStack.itemID].isPotionIngredient() || Brewing.getBrewingFromIngredient(par1ItemStack) != null && par1ItemStack.itemID != Item.gunpowder.itemID)
				return true;
		}
		return false;
	}
	
	public String addIngredient(ItemStack ingredient)
	{
		String out = "";
		if (ingredient.getItem() == Item.bucketWater)
		{
			if (!water())
			{
				for (Brewing b : brewings)
				{
					if (b.getEffect() != null)
					{
						b.setEffect(new PotionEffect(b.getEffect().getPotionID(), Math.round(b.getEffect().getDuration() * 0.6F), Math.round(b.getEffect().getAmplifier() * 0.8F)));
					}
				}
				out = I18n.getString("cauldron.effects.durations.decrease");
			}
			else
				out = I18n.getString("cauldron.addwater");
		}
		else if (ingredient.getItem() == Item.glowstone && !water()) // Improving
		{
			for (int var3 = 0; var3 < brewings.size(); var3++)
			{
				Brewing brewing = brewings.get(var3);
				if (brewing != BrewingList.awkward)
				{
					brewings.set(var3, brewing.onImproved());
				}
			}
			out = I18n.getString("cauldron.effects.amplifiers.increase");
		}
		else if (ingredient.getItem() == Item.redstone && !water()) // Extending
		{
			for (int var3 = 0; var3 < brewings.size(); var3++)
			{
				Brewing brewing = brewings.get(var3);
				if (brewing != BrewingList.awkward)
				{
					brewings.set(var3, brewing.onExtended());
				}
			}
			out = I18n.getString("cauldron.effects.durations.increase");
		}
		else if (ingredient.getItem() == Item.fermentedSpiderEye && !water()) // Inverting
		{
			for (int var3 = 0; var3 < brewings.size(); var3++)
			{
				Brewing brewing = brewings.get(var3);
				brewing = brewing.getOpposite() != null ? brewing.getOpposite() : brewing;
				brewing.setOpposite(null);
				brewings.set(var3, brewing);
			}
			out = I18n.getString("cauldron.effects.invert");
		}
		else if (water()) // Other Base Ingredients
		{
			BrewingBase base = BrewingBase.getBrewingBaseFromIngredient(ingredient);
			if (base != null)
			{
				setBaseBrewing(base);
				out = I18n.getStringParams("cauldron.effects.add.base", base.basename);
			}
		}
		else // Normal ingredients
		{
			Brewing b = Brewing.getBrewingFromIngredient(ingredient);
			if (brewings.size() > 0 && b != null)
			{
				boolean contains = brewings.contains(b);
				Brewing stackBase = brewings.get(0);
				BrewingBase requiredBase = b.getBase();
				if (requiredBase != null && stackBase != null && ((BrewingBase) stackBase).basename == requiredBase.basename && !contains)
				{
					brewings.add(b);
					if (b.getEffect() != null)
					{
						out = I18n.getStringParams("cauldron.effects.add", I18n.getString(b.getEffect().getEffectName()));
					}
				}
				else if (contains)
					out = I18n.getString("cauldron.failed.existing");
				else if (requiredBase != null)
					out = I18n.getStringParams("cauldron.failed.wrongbase", requiredBase.basename);
			}
		}
		
		this.updateOutput();
		
		return out;
	}
	
	public void setBaseBrewing(BrewingBase base)
	{
		if (brewings.size() != 0)
		{
			brewings.set(0, base);
		}
		else
		{
			brewings.add(base);
		}
	}
	
	public boolean water()
	{
		return brewings.size() <= 0;
	}
	
	protected void updateOutput()
	{
		this.output = brew(false);
		this.color = output.getItem().getColorFromItemStack(output, 0);
		sync();
	}
	
	public void sync()
	{
		if (!this.worldObj.isRemote)
			PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}
	
	@Override
	public Packet getDescriptionPacket()
	{
		Packet250CustomPayload packet = new Packet250CustomPayload();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try
		{
			dos.writeInt(xCoord);
			dos.writeInt(yCoord);
			dos.writeInt(zCoord);
			dos.writeInt(color);
			Packet.writeItemStack(output, dos);
		}
		catch (IOException ex)
		{
			
		}
		
		packet.channel = CHANNEL;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		
		return packet;
	}
	
	private ItemStack brew(boolean removeDuplicates)
	{
		if (water())
			return new ItemStack(BrewingAPI.potion2, 1, 0);
		
		ItemStack is = new ItemStack(BrewingAPI.potion2, 1, 1);
		this.brewings = removeDuplicates ? (List<Brewing>) Brewing.removeDuplicates(brewings) : brewings;
		
		if (brewings.size() == 1)
			brewings.get(0).addBrewingToItemStack(is);
		else
			for (int i = 1; i < brewings.size(); i++)
				brewings.get(i).addBrewingToItemStack(is);
		
		return is;
	}
	
	public int getColor()
	{
		return color;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		
		if (par1NBTTagCompound.hasKey("Brewing"))
		{
			List var6 = new ArrayList();
			NBTTagList var3 = par1NBTTagCompound.getTagList("Brewing");
			boolean var2 = true;
			
			for (int var4 = 0; var4 < var3.tagCount(); ++var4)
			{
				NBTTagCompound var5 = (NBTTagCompound) var3.tagAt(var4);
				Brewing b = Brewing.readFromNBT(var5);
				var6.add(b);
			}
			brewings = var6;
		}
		
		updateOutput();
	}
	
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{	
		super.writeToNBT(par1NBTTagCompound);
		
		if (!par1NBTTagCompound.hasKey("Brewing"))
		{
			par1NBTTagCompound.setTag("Brewing", new NBTTagList("Brewing"));
		}
		NBTTagList var2 = (NBTTagList) par1NBTTagCompound.getTag("Brewing");
		for (Brewing b : brewings)
		{
			var2.appendTag(b instanceof BrewingBase ? ((BrewingBase) b).createNBT() : b.createNBT());
		}
	}
	
}
