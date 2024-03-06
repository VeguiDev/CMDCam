package team.creative.cmdcam;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import team.creative.cmdcam.client.CMDCamClient;
import team.creative.cmdcam.common.command.argument.CamModeArgument;
import team.creative.cmdcam.common.command.argument.DurationArgument;
import team.creative.cmdcam.common.command.argument.InterpolationArgument;
import team.creative.cmdcam.common.command.argument.InterpolationArgument.AllInterpolationArgument;
import team.creative.cmdcam.common.command.argument.TargetArgument;
import team.creative.cmdcam.common.packet.ConnectPacket;
import team.creative.cmdcam.common.packet.GetPathPacket;
import team.creative.cmdcam.common.packet.SetPathPacket;
import team.creative.cmdcam.common.packet.StartPathPacket;
import team.creative.cmdcam.common.packet.StopPathPacket;
import team.creative.cmdcam.common.util.CamPath;
import team.creative.cmdcam.server.CMDCamServer;
import team.creative.cmdcam.server.CamEventHandler;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.network.CreativePacket;

@Mod(value = CMDCam.MODID)
public class CMDCam {

    public static final String MODID = "cmdcam";

    private static final Logger LOGGER = LogManager.getLogger(CMDCam.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER,
            new ResourceLocation(CMDCam.MODID, "main"));

    public CMDCam() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        DistExecutor.runWhenOn(Dist.CLIENT,
                () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::client));
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
    }

    @OnlyIn(value = Dist.CLIENT)
    private void client(final FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        CMDCamClient.init(event);
    }

    private void init(final FMLCommonSetupEvent event) {
        NETWORK.registerType(ConnectPacket.class);
        NETWORK.registerType(GetPathPacket.class);
        NETWORK.registerType(SetPathPacket.class);
        NETWORK.registerType(StartPathPacket.class);
        NETWORK.registerType(StopPathPacket.class);

        ArgumentTypes.register("duration", DurationArgument.class,
                new ArgumentSerializer<>(() -> DurationArgument.duration()));
        ArgumentTypes.register("cameramode", CamModeArgument.class,
                new ArgumentSerializer<>(() -> CamModeArgument.mode()));
        ArgumentTypes.register("cameratarget", TargetArgument.class,
                new ArgumentSerializer<>(() -> TargetArgument.target()));
        ArgumentTypes.register("interpolation", InterpolationArgument.class,
                new ArgumentSerializer<>(() -> InterpolationArgument.interpolation()));
        ArgumentTypes.register("allinterpolation", AllInterpolationArgument.class,
                new ArgumentSerializer<>(() -> InterpolationArgument.interpolationAll()));

        MinecraftForge.EVENT_BUS.register(new CamEventHandler());

        PermissionAPI.registerNode("cmdcam.cmd.start", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam start command");
        PermissionAPI.registerNode("cmdcam.cmd.add", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam add command");
        PermissionAPI.registerNode("cmdcam.cmd.clear", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam clear command");
        PermissionAPI.registerNode("cmdcam.cmd.stop", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam stop command");
        PermissionAPI.registerNode("cmdcam.cmd.goto", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam goto command");
        PermissionAPI.registerNode("cmdcam.cmd.set", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam set command");
        PermissionAPI.registerNode("cmdcam.cmd.remove", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam remove command");
        PermissionAPI.registerNode("cmdcam.cmd.target", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam target command");
        PermissionAPI.registerNode("cmdcam.cmd.mode", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam mode command");
        PermissionAPI.registerNode("cmdcam.cmd.interpolation", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam interpolation command");
        PermissionAPI.registerNode("cmdcam.cmd.follow-speed", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam follow-speed command");
        PermissionAPI.registerNode("cmdcam.cmd.show", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam show command");
        PermissionAPI.registerNode("cmdcam.cmd.hide", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam hide command");
        PermissionAPI.registerNode("cmdcam.cmd.save", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam save command");
        PermissionAPI.registerNode("cmdcam.cmd.load", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam load command");
        PermissionAPI.registerNode("cmdcam.cmd.list", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam list command");

        PermissionAPI.registerNode("cmdcam.cmd-server.start", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam-server start command");
        PermissionAPI.registerNode("cmdcam.cmd-server.stop", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam-server stop command");
        PermissionAPI.registerNode("cmdcam.cmd-server.list", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam-server list command");
        PermissionAPI.registerNode("cmdcam.cmd-server.remove", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam-server remove command");
        PermissionAPI.registerNode("cmdcam.cmd-server.clear", DefaultPermissionLevel.OP,
                "Allows the usage of the /cam-server clear command");
    }

    private void serverStarting(final FMLServerStartingEvent event) {

        event.getServer().getCommands().getDispatcher().register(Commands.literal("cam-server").executes((x) -> {
            x.getSource()
                    .sendSuccess(new StringTextComponent("" + TextFormatting.BOLD + TextFormatting.YELLOW
                            + "/cam-server start <player> <path> [time|ms|s|m|h|d] [loops (-1 -> endless)] "
                            + TextFormatting.RED + "starts the animation"), false);
            x.getSource()
                    .sendSuccess(
                            new StringTextComponent("" + TextFormatting.BOLD + TextFormatting.YELLOW
                                    + "/cam-server stop <player> " + TextFormatting.RED + "stops the animation"),
                            false);
            x.getSource()
                    .sendSuccess(new StringTextComponent("" + TextFormatting.BOLD + TextFormatting.YELLOW
                            + "/cam-server list " + TextFormatting.RED + "lists all saved paths"), false);
            x.getSource()
                    .sendSuccess(
                            new StringTextComponent("" + TextFormatting.BOLD + TextFormatting.YELLOW
                                    + "/cam-server remove <name> " + TextFormatting.RED + "removes the given path"),
                            false);
            x.getSource()
                    .sendSuccess(new StringTextComponent("" + TextFormatting.BOLD + TextFormatting.YELLOW
                            + "/cam-server clear " + TextFormatting.RED + "clears all saved paths"), false);
            return 0;
        }).then(Commands.literal("start").then(Commands.argument("players", EntityArgument.players())
                .then(Commands.argument("name", StringArgumentType.string()).executes((x) -> {

                    if (x.getSource().getEntity() instanceof ServerPlayerEntity) {
                        ServerPlayerEntity playerEntity = (ServerPlayerEntity) x.getSource().getEntity();
                        if (!PermissionAPI.hasPermission(playerEntity, "cmdcam.cmd-server.start")) {
                            playerEntity.sendMessage(
                                    new StringTextComponent("You don't have the permission to use this command!"),
                                    Util.NIL_UUID);
                            return 0;
                        }
                    }

                    Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(x, "players");
                    if (players.isEmpty())
                        return 0;
                    String pathName = StringArgumentType.getString(x, "name");
                    CamPath path = CMDCamServer.getPath(x.getSource().getLevel(), pathName);
                    if (path != null) {
                        CreativePacket packet = new StartPathPacket(path);
                        for (ServerPlayerEntity player : players)
                            CMDCam.NETWORK.sendToClient(packet, player);
                    } else
                        x.getSource().sendSuccess(
                                new StringTextComponent("Path '" + pathName + "' could not be found!"), true);
                    return 0;
                }).then(Commands.argument("duration", DurationArgument.duration()).executes((x) -> {
                    Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(x, "players");
                    if (players.isEmpty())
                        return 0;

                    if (x.getSource().getEntity() instanceof ServerPlayerEntity) {
                        ServerPlayerEntity playerEntity = (ServerPlayerEntity) x.getSource().getEntity();
                        if (!PermissionAPI.hasPermission(playerEntity, "cmdcam.cmd-server.start")) {
                            playerEntity.sendMessage(
                                    new StringTextComponent("You don't have the permission to use this command!"),
                                    Util.NIL_UUID);
                            return 0;
                        }
                    }

                    String pathName = StringArgumentType.getString(x, "name");
                    CamPath path = CMDCamServer.getPath(x.getSource().getLevel(), pathName);
                    if (path != null) {
                        path = path.copy();
                        long duration = DurationArgument.getDuration(x, "duration");
                        if (duration > 0)
                            path.duration = duration;
                        CreativePacket packet = new StartPathPacket(path);
                        for (ServerPlayerEntity player : players)
                            CMDCam.NETWORK.sendToClient(packet, player);
                    } else
                        x.getSource().sendSuccess(
                                new StringTextComponent("Path '" + pathName + "' could not be found!"), true);
                    return 0;
                }).then(Commands.argument("loop", IntegerArgumentType.integer(-1)).executes((x) -> {
                    Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(x, "players");
                    if (players.isEmpty())
                        return 0;

                    if (x.getSource().getEntity() instanceof ServerPlayerEntity) {
                        ServerPlayerEntity playerEntity = (ServerPlayerEntity) x.getSource().getEntity();
                        if (!PermissionAPI.hasPermission(playerEntity, "cmdcam.cmd-server.start")) {
                            playerEntity.sendMessage(
                                    new StringTextComponent("You don't have the permission to use this command!"),
                                    Util.NIL_UUID);
                            return 0;
                        }
                    }

                    String pathName = StringArgumentType.getString(x, "name");
                    CamPath path = CMDCamServer.getPath(x.getSource().getLevel(), pathName);
                    if (path != null) {
                        path = path.copy();
                        long duration = DurationArgument.getDuration(x, "duration");
                        if (duration > 0)
                            path.duration = duration;

                        path.loop = IntegerArgumentType.getInteger(x, "loop");
                        CreativePacket packet = new StartPathPacket(path);
                        for (ServerPlayerEntity player : players)
                            CMDCam.NETWORK.sendToClient(packet, player);
                    } else
                        x.getSource().sendSuccess(
                                new StringTextComponent("Path '" + pathName + "' could not be found!"), true);
                    return 0;
                })))))).then(Commands.literal("stop")
                        .then(Commands.argument("players", EntityArgument.players()).executes((x) -> {

                            if (x.getSource().getEntity() instanceof ServerPlayerEntity) {
                                ServerPlayerEntity playerEntity = (ServerPlayerEntity) x.getSource().getEntity();
                                if (!PermissionAPI.hasPermission(playerEntity, "cmdcam.cmd-server.stop")) {
                                    playerEntity.sendMessage(
                                            new StringTextComponent(
                                                    "You don't have the permission to use this command!"),
                                            Util.NIL_UUID);
                                    return 0;
                                }
                            }

                            CreativePacket packet = new StopPathPacket();
                            for (ServerPlayerEntity player : EntityArgument.getPlayers(x, "players"))
                                CMDCam.NETWORK.sendToClient(packet, player);
                            return 0;
                        })))
                .then(Commands.literal("list").executes((x) -> {

                    if (x.getSource().getEntity() instanceof ServerPlayerEntity) {
                        ServerPlayerEntity playerEntity = (ServerPlayerEntity) x.getSource().getEntity();
                        if (!PermissionAPI.hasPermission(playerEntity, "cmdcam.cmd-server.list")) {
                            playerEntity.sendMessage(
                                    new StringTextComponent("You don't have the permission to use this command!"),
                                    Util.NIL_UUID);
                            return 0;
                        }
                    }

                    Collection<String> names = CMDCamServer.getSavedPaths(x.getSource().getLevel());
                    String output = "There are " + names.size() + " path(s) in total. ";
                    for (String key : names) {
                        output += key + ", ";
                    }
                    x.getSource().sendSuccess(new StringTextComponent(output), true);
                    return 0;
                })).then(Commands.literal("clear").executes((x) -> {

                    if (x.getSource().getEntity() instanceof ServerPlayerEntity) {
                        ServerPlayerEntity playerEntity = (ServerPlayerEntity) x.getSource().getEntity();
                        if (!PermissionAPI.hasPermission(playerEntity, "cmdcam.cmd-server.clear")) {
                            playerEntity.sendMessage(
                                    new StringTextComponent("You don't have the permission to use this command!"),
                                    Util.NIL_UUID);
                            return 0;
                        }
                    }

                    CMDCamServer.clearPaths(x.getSource().getLevel());
                    x.getSource().sendSuccess(new StringTextComponent("Removed all existing paths (in this world)!"),
                            true);
                    return 0;
                })).then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.string()).executes((x) -> {

                            if (x.getSource().getEntity() instanceof ServerPlayerEntity) {
                                ServerPlayerEntity playerEntity = (ServerPlayerEntity) x.getSource().getEntity();
                                if (!PermissionAPI.hasPermission(playerEntity, "cmdcam.cmd-server.remove")) {
                                    playerEntity.sendMessage(
                                            new StringTextComponent(
                                                    "You don't have the permission to use this command!"),
                                            Util.NIL_UUID);
                                    return 0;
                                }
                            }

                            String path = StringArgumentType.getString(x, "name");
                            if (CMDCamServer.removePath(x.getSource().getLevel(), path))
                                x.getSource().sendSuccess(
                                        new StringTextComponent("Path '" + path + "' has been removed!"), true);
                            else
                                x.getSource().sendSuccess(
                                        new StringTextComponent("Path '" + path + "' could not be found!"), true);
                            return 0;
                        }))));
    }
}
