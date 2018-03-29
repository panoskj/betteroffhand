package net.panoskj.betteroffhand;

import net.minecraft.item.Item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class BetterOffhand {
    
    @Instance(value = Reference.MODID)
	public static BetterOffhand instance = new BetterOffhand();
    
	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
    
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        
        EntityPlayer player = event.getEntityPlayer();
        
		if (player == null) return;
        
        if (event.getHand() == EnumHand.OFF_HAND) {
            try {
                Class helditemclass = player.getHeldItemMainhand().getItem().getClass();
                Class declaringclass = helditemclass.getMethod("func_180614_a", EntityPlayer.class, World.class, BlockPos.class, EnumHand.class, EnumFacing.class, float.class, float.class, float.class).getDeclaringClass();
                if (!declaringclass.equals(Item.class))
                    event.setUseItem(Event.Result.DENY);
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
        
    }
    
}