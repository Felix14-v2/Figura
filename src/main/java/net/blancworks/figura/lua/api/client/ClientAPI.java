package net.blancworks.figura.lua.api.client;

import net.blancworks.figura.PlayerDataManager;
import net.blancworks.figura.lua.CustomScript;
import net.blancworks.figura.lua.api.ReadOnlyLuaTable;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class ClientAPI {

    public static Identifier getID() {
        return new Identifier("default", "client");
    }

    public static ReadOnlyLuaTable getForScript(CustomScript script) {
        boolean local = script.playerData == PlayerDataManager.localPlayer;
        MinecraftClient client = MinecraftClient.getInstance();

        return new ReadOnlyLuaTable(new LuaTable() {{
            set("getOpenScreen", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    //always return nil when not the local player
                    if (!local || client.currentScreen == null)
                        return NIL;

                    //get the current screen
                    String screenTitle = client.currentScreen.getTitle().getString();
                    if (screenTitle.equals(""))
                        screenTitle = client.currentScreen.getClass().getSimpleName();

                    return LuaValue.valueOf(screenTitle);
                }
            });

            set("getFPS", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.fpsDebugString) : NIL;
                }
            });

            set("isPaused", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.isPaused()) : NIL;
                }
            });

            set("getVersion", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(SharedConstants.getGameVersion().getName()) : NIL;
                }
            });

            set("getVersionType", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.getVersionType()) : NIL;
                }
            });

            set("getServerBrand", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.getServer() == null ? client.player.getServerBrand() : "Integrated") : NIL;
                }
            });

            set("getChunksCount", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.worldRenderer.getChunksDebugString()) : NIL;
                }
            });

            set("getEntityCount", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.worldRenderer.getEntitiesDebugString()) : NIL;
                }
            });

            set("getParticleCount", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.particleManager.getDebugString()) : NIL;
                }
            });

            set("getSoundCount", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.getSoundManager().getDebugString()) : NIL;
                }
            });

            set("getActiveShader", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    if (!local || client.gameRenderer.getShader() == null)
                        return NIL;

                    return LuaValue.valueOf(client.gameRenderer.getShader().getName());
                }
            });

            set("getJavaVersion", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(System.getProperty("java.version")) : NIL;
                }
            });

            set("getMemoryInUse", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    return local ? LuaValue.valueOf(mem) : NIL;
                }
            });

            set("getMaxMemory", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(Runtime.getRuntime().maxMemory()) : NIL;
                }
            });

            set("getAllocatedMemory", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(Runtime.getRuntime().totalMemory()) : NIL;
                }
            });

            set("isWindowFocused", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(client.isWindowFocused()) : NIL;
                }
            });

            set("isHudEnabled", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return local ? LuaValue.valueOf(MinecraftClient.isHudEnabled()) : NIL;
                }
            });

        }});
    }
}
