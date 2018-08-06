package net.panoskj.betteroffhand;

import java.util.HashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
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
			
			Item helditem = player.getHeldItemMainhand().getItem();
			
			Boolean canbeused = cache.get(helditem);
			
			if (canbeused != null)
			{
				if (canbeused) event.setUseItem(Event.Result.DENY);
			}
			else
			{
				try {
					
					Class helditemclass = player.getHeldItemMainhand().getItem().getClass();
					
					Class declaringclass_onItemUse = helditemclass.getMethod("func_180614_a",
							EntityPlayer.class, World.class, BlockPos.class, EnumHand.class,
							EnumFacing.class, float.class, float.class, float.class).getDeclaringClass();
							
					Class declaringclass_onItemUseFinish = helditemclass.getMethod("func_77654_b",
							ItemStack.class, World.class, EntityLivingBase.class).getDeclaringClass();
					
					if (!declaringclass_onItemUse.equals(Item.class) ||
						!declaringclass_onItemUseFinish.equals(Item.class))
					{	
						cache.put(helditem, true);
						event.setUseItem(Event.Result.DENY);
					}
					else cache.put(helditem, false);
				}
				catch (Exception e) {
					// We don't have to cause an exception for this item again.
					cache.put(helditem, false);
					System.out.println(e);
				}
			}
        }
        
    }
	
	// Use this cache to minimize reflection's and (unlikely) exceptions' overhead. 
	// Note: it is unknown whether the memory overhead is actually worth it.
	private HashMap<Item, Boolean> cache = new HashMap<Item, Boolean>();
}