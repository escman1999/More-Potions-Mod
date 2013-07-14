package clashsoft.mods.morepotions;

import java.util.Random;

import clashsoft.brewingapi.gui.GuiBrewingStand2;
import clashsoft.brewingapi.inventory.ContainerBrewingStand2;
import clashsoft.brewingapi.item.ItemPotion2;
import clashsoft.brewingapi.tileentity.TileEntityBrewingStand2;
import clashsoft.clashsoftapi.util.CSUtil;
import clashsoft.mods.morepotions.gui.GuiMixer;
import clashsoft.mods.morepotions.gui.GuiUnbrewingStand;
import clashsoft.mods.morepotions.inventory.ContainerMixer;
import clashsoft.mods.morepotions.inventory.ContainerUnbrewingStand;
import clashsoft.mods.morepotions.tileentity.TileEntityMixer;
import clashsoft.mods.morepotions.tileentity.TileEntityUnbrewingStand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
	
    public void registerRenderInformation()
    {

    }

    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == MorePotionsMod.Mixer_TEID)
        {
        	return new GuiMixer(player.inventory, (TileEntityMixer)world.getBlockTileEntity(x, y, z));
        }
        else if (ID == MorePotionsMod.UnbrewingStand_TEID)
        {
        	return new GuiUnbrewingStand(player.inventory, (TileEntityUnbrewingStand)world.getBlockTileEntity(x, y, z));
        }
        return null;
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int X, int Y, int Z)
    {
        TileEntity te = world.getBlockTileEntity(X, Y, Z);
        if (te != null && te instanceof TileEntityBrewingStand2)
        {
            TileEntityBrewingStand2 bs = (TileEntityBrewingStand2) te;
            return new ContainerBrewingStand2(player.inventory, bs);
        }
        else if (te != null && te instanceof TileEntityMixer)
        {
        	TileEntityMixer m = (TileEntityMixer) te;
            return new ContainerMixer(player.inventory, m);
        }
        else if (te != null && te instanceof TileEntityUnbrewingStand)
        {
        	TileEntityUnbrewingStand ubs = (TileEntityUnbrewingStand) te;
        	return new ContainerUnbrewingStand(player.inventory, ubs);
        }
        else
        {
            return null;
        }
    }

    public World getClientWorld()
    {
        return null;
    }
    
    public void registerRenderers() {}

}