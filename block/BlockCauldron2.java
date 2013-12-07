package clashsoft.mods.morepotions.block;

import clashsoft.mods.morepotions.MorePotionsMod;
import clashsoft.mods.morepotions.client.MPMClientProxy;
import clashsoft.mods.morepotions.tileentity.TileEntityCauldron;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockCauldron2 extends BlockCauldron implements ITileEntityProvider
{
	@SideOnly(Side.CLIENT)
	public Icon	inner;
	@SideOnly(Side.CLIENT)
	public Icon	top;
	@SideOnly(Side.CLIENT)
	public Icon	bottom;
	
	public BlockCauldron2(int blockID)
	{
		super(blockID);
	}
	
	@Override
	public int getRenderType()
	{
		return MPMClientProxy.cauldronRenderType;
	}
	
	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		Block.cauldron.registerIcons(iconRegister);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if (world.isRemote)
			return;
		else if (entity instanceof EntityItem)
		{
			EntityItem item = (EntityItem) entity;
			
			// Makes sure the item is *in* the cauldron
			if (item.posX >= x + 0.125D && item.posX <= x + 0.875D && item.posY >= y + 0.125D && item.posY <= y + 1D && item.posZ >= z + 0.125D && item.posZ <= z + 0.875D)
			{
				if (this.onItemAdded(world, x, y, z, null, item.getEntityItem()))
					item.setDead();
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	
	{
		return onItemAdded(world, x, y, z, player, player.getCurrentEquippedItem());
	}
	
	public boolean onItemAdded(World world, int x, int y, int z, EntityPlayer player, ItemStack stack)
	{
		if (world.isRemote)
			return true;
		else if (world.getBlockTileEntity(x, y, z) instanceof TileEntityCauldron)
		{
			TileEntityCauldron te = (TileEntityCauldron) world.getBlockTileEntity(x, y, z);
			boolean flag = false;
			boolean itemDrop = player == null;
			String message = null;
			
			if (stack == null)
				return false;
			else
			{
				int i1 = world.getBlockMetadata(x, y, z);
				
				if (stack.itemID == Item.bucketWater.itemID && !itemDrop)
				{
					if (i1 < 3)
					{
						if (itemDrop)
						{
							world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.5D, z + 1.5D, new ItemStack(Item.bucketEmpty)));
						}
						else if (!player.capabilities.isCreativeMode)
						{
							player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(Item.bucketEmpty));
						}
						
						world.setBlockMetadataWithNotify(x, y, z, 3, 2);
						
						message = te.addIngredient(stack);
						flag = true;
					}
					else
						flag = false;
				}
				else if (stack.itemID == Item.glassBottle.itemID)
				{
					if (i1 > 0)
					{
						ItemStack itemstack1 = te.output;
						itemstack1.stackSize = 1;
						
						if (itemDrop || !player.inventory.addItemStackToInventory(itemstack1))
						{
							player.dropPlayerItem(itemstack1);
						}
						
						if (player instanceof EntityPlayerMP)
						{
							((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
						}
						
						--stack.stackSize;
						
						if (!itemDrop && stack.stackSize <= 0)
						{
							player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack) null);
						}
						
						--i1;
						
						world.setBlockMetadataWithNotify(x, y, z, i1, 2);
						flag = true;
					}
					
					if (i1 == 0)
						te.potionTypes.clear();
				}
				else if (i1 > 0 && stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).getArmorMaterial() == EnumArmorMaterial.CLOTH)
				{
					ItemArmor itemarmor = (ItemArmor) stack.getItem();
					itemarmor.removeColor(stack);
					world.setBlockMetadataWithNotify(x, y, z, i1 - 1, 2);
					flag = true;
				}
				else
				{
					if (te.isItemValid(stack) && i1 > 0)
					{
						message = te.addIngredient(stack);
						flag = true;
					}
				}
			}
			
			if (flag)
			{				
				world.playSound(x, y, z, "random.pop", 1F, 1F, true);
				if (MorePotionsMod.cauldronInfo && player != null && !world.isRemote && message != null && !message.isEmpty())
					player.addChatMessage(message);
			}
			
			te.sync();
			return flag;
		}
		else
			return false;
	}
	
	/**
	 * currently only used by BlockCauldron to increment metadata during rain
	 */
	@Override
	public void fillWithRain(World world, int x, int y, int z)
	{
		if (world.rand.nextInt(20) == 1)
		{
			int l = world.getBlockMetadata(x, y, z);
			
			if (l < 3)
			{
				world.setBlockMetadataWithNotify(x, y, z, l + 1, 2);
			}
		}
	}
	
	/**
	 * Called when the block receives a BlockEvent - see World.addBlockEvent. By
	 * default, passes it on to the tile entity at this location. Args: world,
	 * x, y, z, blockID, EventID, event parameter
	 */
	@Override
	public boolean onBlockEventReceived(World par1World, int par2, int par3, int par4, int par5, int par6)
	{
		super.onBlockEventReceived(par1World, par2, par3, par4, par5, par6);
		TileEntity tileentity = par1World.getBlockTileEntity(par2, par3, par4);
		return tileentity != null ? tileentity.receiveClientEvent(par5, par6) : false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityCauldron();
	}
}
