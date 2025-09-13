package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.math.Line;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.vidlib.feature.canvas.dof.DepthOfField;
import dev.latvian.mods.vidlib.feature.canvas.dof.DepthOfFieldPanel;
import dev.latvian.mods.vidlib.feature.entity.C2SEntityEventPayload;
import dev.latvian.mods.vidlib.feature.entity.EntityData;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.entity.ForceEntityVelocityPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.S2CEntityEventPayload;
import dev.latvian.mods.vidlib.feature.entity.filter.ProfileEntityFilter;
import dev.latvian.mods.vidlib.feature.imgui.EntityExplorerPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.PlayerDataConfigPanel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.PositionType;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VLEntity extends VLLevelContainer, PlayerActionHandler {
	default Entity vl$self() {
		return (Entity) this;
	}

	@Override
	default Level vl$level() {
		return vl$self().level();
	}

	default void vl$setLevel(Level level) {
		throw new NoMixinException(this);
	}

	default boolean vl$isSaving() {
		return false;
	}

	default List<ZoneInstance> getZones() {
		var zones = vl$level().vl$getActiveZones();
		return zones == null ? List.of() : zones.entityZones.getOrDefault((vl$self()).getId(), List.of());
	}

	@Nullable
	default GameType getGameMode() {
		return null;
	}

	default boolean isSpectatorOrCreative() {
		var type = getGameMode();
		return type == GameType.SPECTATOR || type == GameType.CREATIVE;
	}

	default boolean vl$isCreative() {
		return getGameMode() == GameType.CREATIVE;
	}

	default boolean isSurvival() {
		return getGameMode() == GameType.SURVIVAL;
	}

	default boolean isSurvivalLike() {
		var type = getGameMode();
		return type != null && type.isSurvival();
	}

	default boolean isSuspended() {
		return EntityOverride.SUSPENDED.get(this, false);
	}

	default double vl$gravityMod() {
		return EntityOverride.GRAVITY.get(this, 1D);
	}

	default float vl$speedMod() {
		return EntityOverride.SPEED.get(this, 1F);
	}

	default float vl$attackDamageMod() {
		return EntityOverride.ATTACK_DAMAGE.get(this, 1F);
	}

	default Line ray(double distance, float delta) {
		var start = vl$self().getEyePosition(delta);
		var end = start.add(vl$self().getViewVector(delta).scale(distance));
		return new Line(start, end);
	}

	default Line ray(float delta) {
		return ray(4.5D, delta);
	}

	default void teleport(ServerLevel to, Vec3 pos) {
		var entity = vl$self();
		entity.teleport(new TeleportTransition(
			to,
			pos,
			entity.getDeltaMovement(),
			entity.getYRot(),
			entity.getXRot(),
			TeleportTransition.DO_NOTHING
		));
	}

	default void teleport(Vec3 pos) {
		teleport((ServerLevel) vl$level(), pos);
	}

	default void teleport(ServerLevel to, BlockPos pos) {
		teleport(to, new Vec3(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D));
	}

	default void teleport(BlockPos pos) {
		teleport(new Vec3(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D));
	}

	default void teleport(Location location) {
		var entity = vl$self();
		teleport(entity.getServer().getLevel(location.dimension()), location.random(entity.getRandom()).get(entity.level().getGlobalContext()));
	}

	default void forceSetVelocity(Vec3 velocity) {
		var e = vl$self();
		e.setDeltaMovement(velocity);

		if (!e.level().isClientSide()) {
			e.level().s2c(new ForceEntityVelocityPayload(e.getId(), velocity));
		}
	}

	default void forceAddVelocity(Vec3 velocity) {
		forceSetVelocity(vl$self().getDeltaMovement().add(velocity));
	}

	default void playSound(SoundData data, boolean looping, boolean stopImmediately) {
		var e = vl$self();
		e.level().playGlobalSound(new PositionedSoundData(data, e, looping, stopImmediately), KNumberVariables.EMPTY);
	}

	default void playSound(SoundData data) {
		playSound(data, false, true);
	}

	default Vec3 getSoundSource(float delta) {
		return vl$self().getEyePosition(delta);
	}

	default void s2c(EntityData data) {
		vl$level().s2c(new S2CEntityEventPayload(data));
	}

	default void s2cReceived(EntityData event, Player clientPlayer) {
	}

	default void c2s(EntityData data) {
		vl$level().c2s(new C2SEntityEventPayload(data));
	}

	default void c2sReceived(EntityData event, ServerPlayer from) {
	}

	default Vec3 getLookTarget(float delta) {
		var e = vl$self();

		if (delta == 1F) {
			return e.position().add(e.getViewVector(1F));
		} else {
			return e.getPosition(delta).add(e.getViewVector(delta));
		}
	}

	default Vec3 getPosition(PositionType type) {
		var e = vl$self();

		return switch (type) {
			case CENTER -> new Vec3(e.getX(), e.getY() + e.getBbHeight() / 2D, e.getZ());
			case TOP -> new Vec3(e.getX(), e.getY() + e.getBbHeight(), e.getZ());
			case EYES -> e.getEyePosition();
			case LEASH -> e.position().add(e.getLeashOffset(1F));
			case SOUND_SOURCE -> getSoundSource(1F);
			case LOOK_TARGET -> getLookTarget(1F);
			default -> e.position();
		};
	}

	default Vec3 getPosition(PositionType type, float delta) {
		var e = vl$self();

		return switch (type) {
			case CENTER -> e.getPosition(delta).add(0D, e.getBbHeight() / 2D, 0D);
			case TOP -> e.getPosition(delta).add(0D, e.getBbHeight(), 0D);
			case EYES -> e.getEyePosition(delta);
			case LEASH -> e.getPosition(delta).add(e.getLeashOffset(delta));
			case SOUND_SOURCE -> getSoundSource(delta);
			case LOOK_TARGET -> getLookTarget(delta);
			default -> delta == 1F ? e.position() : e.getPosition(delta);
		};
	}

	default float getRelativeHealth(float delta) {
		return 1F;
	}

	default boolean preventDismount(Player passenger) {
		return false;
	}

	default float getVehicleCameraDistance(Player passenger, float original) {
		return original;
	}

	default float getPassengerScale(Entity passenger) {
		return 1F;
	}

	default PlayerInput getPilotInput() {
		return PlayerInput.NONE;
	}

	default void vl$setPilotInput(PlayerInput input) {
		throw new NoMixinException(this);
	}

	@Nullable
	default Boolean forceRenderVehicleCrosshair(Player passenger) {
		return null;
	}

	default Rotation rotation(float delta) {
		var e = vl$self();
		return Rotation.deg(e.getYRot(delta), e.getXRot(delta));
	}

	default Rotation viewRotation(float delta) {
		var e = vl$self();
		return Rotation.deg(e.getViewYRot(delta), e.getViewXRot(delta));
	}

	default boolean isVisible() {
		return !vl$self().isInvisible();
	}

	default boolean isProjectile() {
		return this instanceof Projectile;
	}

	default boolean isItemEntity() {
		return this instanceof ItemEntity;
	}

	default void replaySnapshot(S2CPacketBundleBuilder packets) {
	}

	default void imgui(ImGraphics graphics, float delta) {
		var entity = vl$self();

		if (entity == null) {
			return;
		}

		if (ImGui.smallButton("Copy UUID")) {
			ImGui.setClipboardText(entity.getUUID().toString());
		}

		ImGui.sameLine();

		if (ImGui.smallButton("Copy Network ID")) {
			ImGui.setClipboardText(Integer.toString(entity.getId()));
		}

		if (!graphics.isReplay) {
			if (graphics.smallButton("Kill", ImColorVariant.RED)) {
				graphics.mc.runClientCommand("kill " + entity.getUUID());
			}

			if (entity instanceof Player) {
				ImGui.beginDisabled();
			}

			ImGui.sameLine();

			if (graphics.smallButton("Discard", ImColorVariant.RED)) {
				graphics.mc.runClientCommand("discard " + entity.getUUID());
			}

			if (entity instanceof Player) {
				ImGui.endDisabled();
			}

			ImGui.sameLine();
		}

		if (entity == graphics.mc.player) {
			ImGui.beginDisabled();
		}

		if (graphics.smallButton("TP To", ImColorVariant.DARK_PURPLE)) {
			graphics.mc.runClientCommand("tp " + entity.getUUID());
		}

		ImGui.sameLine();

		if (graphics.smallButton("TP Here", ImColorVariant.DARK_PURPLE)) {
			graphics.mc.runClientCommand("tp " + entity.getUUID() + " @s");
		}

		if (entity == graphics.mc.player) {
			ImGui.endDisabled();
		}

		if (entity instanceof Player player) {
			if (ImGui.button("Edit Player Data###vidlib-edit-player-data", -1F, 0F)) {
				new PlayerDataConfigPanel(player.getScoreboardName(), player.vl$sessionData().dataMap).open();
			}
		}

		if (DepthOfField.OVERRIDE_ENABLED.get() && ImGui.button(ImIcons.APERTURE + " Focus DoF###focus-dof")) {
			if (entity instanceof Player player) {
				DepthOfField.OVERRIDE = DepthOfField.OVERRIDE.withFocus(KVector.following(new ProfileEntityFilter(player.getGameProfile()), PositionType.EYES));
			} else {
				DepthOfField.OVERRIDE = DepthOfField.OVERRIDE.withFocus(KVector.following(entity, PositionType.EYES));
			}

			DepthOfFieldPanel.INSTANCE.builder.set(DepthOfField.OVERRIDE);
		}

		var team = entity.getTeam();

		if (ImGui.button("Team: " + (team == null ? "None" : team.getName()) + "###vidlib-entity-team")) {
			ImGui.openPopup("###vidlib-edit-team-popup");
		}

		if (ImGui.beginPopup("Edit Team###vidlib-edit-team-popup", ImGuiWindowFlags.AlwaysAutoResize)) {
			if (ImGui.beginListBox("###teams", 200F, 120F)) {
				if (ImGui.selectable(ImIcons.CLOSE + " None", team == null)) {
					graphics.mc.runClientCommand("team leave " + entity.getUUID());
					ImGui.closeCurrentPopup();
				}

				for (var teamName : graphics.mc.level.getScoreboard().getTeamNames()) {
					if (ImGui.selectable(teamName, team != null && team.getName().equals(teamName))) {
						graphics.mc.runClientCommand("team join " + teamName + " " + entity.getUUID());
						ImGui.closeCurrentPopup();
					}
				}

				ImGui.endListBox();
			}

			ImGui.endPopup();
		}

		if (entity instanceof Player) {
			ImGui.sameLine();

			var tags = entity.getTags();

			if (ImGui.button(tags.size() + " Tags###vidlib-entity-tags")) {
				ImGui.openPopup("###vidlib-edit-tags-popup");
			}

			if (!tags.isEmpty()) {
				ImGui.sameLine();
				ImGui.alignTextToFramePadding();
				ImGui.text(String.join(", ", tags));
			}

			if (ImGui.beginPopup("Edit Team###vidlib-edit-tags-popup", ImGuiWindowFlags.AlwaysAutoResize)) {
				if (tags.isEmpty()) {
					ImGui.text("No tags");
				}

				for (var tag : tags) {
					ImGui.text(tag);
					ImGui.sameLine();
					graphics.pushStack();
					graphics.setRedButton();

					if (ImGui.smallButton("-###remove-tag-" + tag)) {
						graphics.mc.runClientCommand("tag " + entity.getUUID() + " remove " + tag);
					}

					graphics.popStack();
				}

				ImGui.inputText("###add-tag-input", EntityExplorerPanel.INSTANCE.tagInput);
				boolean finished = ImGui.isItemDeactivatedAfterEdit();

				ImGui.sameLine();

				if (ImGui.button("+###add-tag") || finished) {
					var tag = EntityExplorerPanel.INSTANCE.tagInput.get();
					EntityExplorerPanel.INSTANCE.tagInput.set("");
					graphics.mc.runClientCommand("tag " + entity.getUUID() + " add " + tag);
				}

				ImGui.endPopup();
			}
		}
	}
}
