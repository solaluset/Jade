package snownee.jade;

import java.text.DecimalFormat;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.jade.util.UsernameCache;

@Mod(Jade.MODID)
public class Jade {
	public static final String MODID = "jade";
	public static final String NAME = "Jade";
	public static DecimalFormat dfCommas = new DecimalFormat("##.##");
	public static final INamedTag<Block> PICK = BlockTags.createOptional(new ResourceLocation(MODID, "pick"));

	public Jade() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JadeCommonConfig.spec);
		FMLJavaModLoadingContext.get().getModEventBus().register(JadeCommonConfig.class);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
	}

	private void init(FMLCommonSetupEvent event) {
		JadeCommonConfig.refresh();

		UsernameCache.load();
		MinecraftForge.EVENT_BUS.register(this);
	}

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {

		final Entity entity = event.getEntity();
		if (entity instanceof PlayerEntity) {
			final PlayerEntity player = (PlayerEntity) entity;
			UsernameCache.setUsername(player.getUniqueID(), player.getName().getString());
		}
	}
}
