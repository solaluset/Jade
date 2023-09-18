package snownee.jade.addon.vanilla;

import java.util.UUID;
import java.util.List;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.text.ITextComponent;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IPluginConfig;
import snownee.jade.util.UsernameCache;

public enum AnimalOwnerProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {

	INSTANCE;

	@Override
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		String name;
		if (accessor.getServerData().contains("OwnerName")) {
			name = accessor.getServerData().getString("OwnerName");
		} else {
			final UUID ownerUUID = getOwnerUUID(accessor.getEntity());
			if (ownerUUID == null) {
				return;
			}
			name = UsernameCache.getLastKnownUsername(ownerUUID);
			if (name == null) {
				name = "???";
			}
		}
		tooltip.add(1, new TranslationTextComponent("jade.owner", TextFormatting.YELLOW + name));
	}

	@Override
	public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, Entity entity) {
		final UUID ownerUUID = getOwnerUUID(entity);
		if (ownerUUID != null) {
			String name = UsernameCache.getLastKnownUsername(ownerUUID);
			if (name != null) {
				data.putString("OwnerName", name);
			}
		}
	}

	protected UUID getOwnerUUID(Entity entity) {
		if (entity instanceof TameableEntity) {
			return ((TameableEntity) entity).getOwnerId();
		} else if (entity instanceof AbstractHorseEntity) {
			return ((AbstractHorseEntity) entity).getOwnerUniqueId();
		} else {
			return null;
		}
	}
}
