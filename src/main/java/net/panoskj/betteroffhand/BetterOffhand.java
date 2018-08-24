package net.panoskj.betteroffhand;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.multiplayer.PlayerControllerMP;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;


import org.lwjgl.input.Mouse;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class BetterOffhand {
    
    @Mod.Instance(value = Reference.MODID)
	public static BetterOffhand instance = new BetterOffhand();
    
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	
	@SideOnly(Side.CLIENT)
	private Object invTweaksInstance = null;
	
	@SideOnly(Side.CLIENT)
	private Method invTweaksOnTickInGame = null;
	
	@SideOnly(Side.CLIENT)
	private void invTweaksIntegration() {
		try {
			if (invTweaksInstance != null && invTweaksOnTickInGame != null)
				invTweaksOnTickInGame.invoke(invTweaksInstance);
		}
		catch (java.lang.Exception e) {
			System.out.println("Failed to execute invtweaks integration method. Reason: " + e);
		}
	}
	
	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
    public void postInit(FMLPostInitializationEvent event) {		
		try {
			
			Field fListenerOwners = EventBus.class.getDeclaredField("listenerOwners");
			
			fListenerOwners.setAccessible(true);
			
			Object listenerOwners = fListenerOwners.get(MinecraftForge.EVENT_BUS);
			
			Set mods = ((Map)listenerOwners).keySet();
			
			for (Object mod : mods)
			{
				if (mod.getClass().getName() == "invtweaks.forge.ClientProxy")
				{
					Field fInstance = mod.getClass().getDeclaredField("instance");
			
					fInstance.setAccessible(true);
			
					invTweaksInstance = fInstance.get(mod);
					
					invTweaksOnTickInGame = invTweaksInstance.getClass().getDeclaredMethod("onTickInGame");
			
					System.out.println("InvTweaks integration set up.");
					
					return;
				}				
			}
			
			System.out.println("InvTweaks is not present.");
		}
		catch (java.lang.Exception e) {
			System.out.println("Failed to integrate with InvTweaks. Reason: " + e);
		}
    }
    
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
		
        EntityPlayer player = event.getEntityPlayer();
        
		if (player == null) return;
        
        if (event.getHand() == EnumHand.OFF_HAND) {
			
			ItemStack mainHeldItemStack = player.getHeldItemMainhand();
			
			if (isItemStackUsable(mainHeldItemStack)) event.setUseItem(Event.Result.DENY);
        }
    }
	
	
	// Use this cache to minimize reflection's and (unlikely) exceptions' overhead. 
	// Note: it is unknown whether the memory overhead is actually worth the gains.
	private HashMap<Item, Boolean> cache = new HashMap<Item, Boolean>();
	
	private boolean isItemStackUsable(ItemStack stack)
	{
		if (stack.isEmpty()) return false;
		
		return isItemUsable(stack.getItem());
	}
	
	private boolean isItemUsable(Item item)
	{		
		Boolean usable = cache.get(item);
		
		if (usable != null) return usable;
		
		try {
			
			Class<?> helditemclass = item.getClass();
			
			Class<?> declaringclass_onItemUse = helditemclass.getMethod("func_180614_a", // onItemUse
					EntityPlayer.class, World.class, BlockPos.class, EnumHand.class,
					EnumFacing.class, float.class, float.class, float.class).getDeclaringClass();
					
			Class<?> declaringclass_onItemUseFinish = helditemclass.getMethod("func_77654_b", // onItemUseFinish
					ItemStack.class, World.class, EntityLivingBase.class).getDeclaringClass();
			
			if (!declaringclass_onItemUse.equals(Item.class) ||
				!declaringclass_onItemUseFinish.equals(Item.class))
			{	
				cache.put(item, true);
				return true;
			}
			
			cache.put(item, false);
		}
		catch (Exception e) {
			// We don't have to cause an exception for this item again.
			cache.put(item, false);
			System.out.println(e);
		}
		
		return false;
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	@SideOnly(Side.CLIENT)
	public void testEvent2(PlayerDestroyItemEvent event) {
		
		if (event.getHand() != EnumHand.MAIN_HAND) return;
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if (event.getEntityPlayer() != mc.player) return;
		
		if (mc.player.isHandActive()) return;
		
		if (mc.playerController.getIsHittingBlock()) return;
		
		if (mc.gameSettings.keyBindAttack.isKeyDown()) return;
		
		if (!isItemStackUsable(event.getOriginal())) return;
		
		invTweaksIntegration();
		
		if (!mc.player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) return;
			
		unpressKeyBind(mc.gameSettings.keyBindUseItem);
		
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	@SideOnly(Side.CLIENT)
	public void testEvent(LivingEntityUseItemEvent.Finish event) {
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if (event.getEntityLiving() != mc.player) return;
		
		if (mc.player.getActiveHand() != EnumHand.MAIN_HAND) return;
		
		ItemStack result = event.getResultStack();
		
		if (result.isEmpty())
		{
			result.grow(1);
			
			Item item = result.getItem();
			
			result.shrink(1);
			
			if (isItemUsable(item))
			{
				//System.out.println("was: "+ item.getUnlocalizedName());
				
				mc.player.setHeldItem(EnumHand.MAIN_HAND, result);
				
				invTweaksIntegration();
				
				//System.out.println("now is: " + mc.player.getHeldItem(EnumHand.MAIN_HAND).getUnlocalizedName());
				
				event.setResultStack(mc.player.getHeldItem(EnumHand.MAIN_HAND));
				
				if (!mc.player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) return;
				
				unpressKeyBind(mc.gameSettings.keyBindUseItem);
			}
			
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void unpressKeyBind(KeyBinding keyBind) {
		while (keyBind.isPressed()) { /* intentional */ }
		KeyBinding.setKeyBindState(keyBind.getKeyCode(), false);
	}
}