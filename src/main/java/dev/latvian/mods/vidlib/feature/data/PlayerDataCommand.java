package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;

public interface PlayerDataCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("player-data", (command, buildContext) -> {
		command.requires(source -> source.hasPermission(2));
		var nbtOps = buildContext.createSerializationContext(NbtOps.INSTANCE);

		var get = Commands.argument("player", EntityArgument.players());
		var set = Commands.argument("player", EntityArgument.players());
		var reset = Commands.argument("player", EntityArgument.players());

		for (var key : DataKey.PLAYER.all.values()) {
			get.then(Commands.literal(key.id())
				.executes(ctx -> {
					for (var player : EntityArgument.getPlayers(ctx, "player")) {
						ctx.getSource().sendSuccess(() -> {
							var value = player.get(key);
							var nbt = key.type().codec().encodeStart(nbtOps, Cast.to(value)).getOrThrow();
							return Component.literal(player.getScoreboardName() + ": ").append(NbtUtils.toPrettyComponent(nbt));
						}, false);
					}

					return 1;
				})
			);

			set.then(Commands.literal(key.id())
				.then(Commands.argument("value", key.command().argument(buildContext))
					.executes(ctx -> {
						var value = key.command().get(ctx, "value");

						for (var player : EntityArgument.getPlayers(ctx, "player")) {
							player.set(key, Cast.to(value));
						}

						return 1;
					})
				)
			);

			reset.then(Commands.literal(key.id())
				.executes(ctx -> {
					for (var player : EntityArgument.getPlayers(ctx, "player")) {
						player.set(key, Cast.to(key.defaultValue()));
					}

					return 1;
				})
			);
		}

		command.then(Commands.literal("get").then(get));
		command.then(Commands.literal("set").then(set));
		command.then(Commands.literal("reset").then(reset));
	});
}
