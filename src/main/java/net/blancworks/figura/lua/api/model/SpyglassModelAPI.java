package net.blancworks.figura.lua.api.model;

import net.blancworks.figura.lua.CustomScript;
import net.blancworks.figura.lua.api.ReadOnlyLuaTable;
import net.blancworks.figura.lua.api.ScriptLocalAPITable;
import net.blancworks.figura.lua.api.math.LuaVector;
import net.minecraft.util.Identifier;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class SpyglassModelAPI {

    public static final String VANILLA_LEFT_SPYGLASS = "LEFT_SPYGLASS";
    public static final String VANILLA_RIGHT_SPYGLASS = "RIGHT_SPYGLASS";

    public static final Identifier VANILLA_LEFT_SPYGLASS_ID = new Identifier("figura", "left_spyglass");
    public static final Identifier VANILLA_RIGHT_SPYGLASS_ID = new Identifier("figura", "right_spyglass");

    public static Identifier getID() {
        return new Identifier("default", "spyglass_model");
    }

    public static ReadOnlyLuaTable getForScript(CustomScript script) {
        return new ScriptLocalAPITable(script, new LuaTable() {{
            set(VANILLA_LEFT_SPYGLASS, getTableForPart(VANILLA_LEFT_SPYGLASS, script));
            set(VANILLA_RIGHT_SPYGLASS, getTableForPart(VANILLA_RIGHT_SPYGLASS, script));
        }});
    }

    public static ReadOnlyLuaTable getTableForPart(String accessor, CustomScript script) {
        return new SpyglassTable(accessor, script);
    }

    private static class SpyglassTable extends ScriptLocalAPITable {
        String accessor;

        public SpyglassTable(String accessor, CustomScript script) {
            super(script);
            this.accessor = accessor;
            super.setTable(getTable());
        }

        public LuaTable getTable() {
            LuaTable ret = new LuaTable();

            ret.set("getPos", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaVector.of(targetScript.getOrMakePartCustomization(accessor).pos);
                }
            });

            ret.set("setPos", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    VanillaModelPartCustomization customization = targetScript.getOrMakePartCustomization(accessor);
                    customization.pos = LuaVector.checkOrNew(arg1).asV3f();

                    return NIL;
                }
            });

            ret.set("getRot", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaVector.of(targetScript.getOrMakePartCustomization(accessor).rot);
                }
            });

            ret.set("setRot", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    VanillaModelPartCustomization customization = targetScript.getOrMakePartCustomization(accessor);
                    customization.rot = LuaVector.checkOrNew(arg1).asV3f();

                    return NIL;
                }
            });

            ret.set("getScale", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaVector.of(targetScript.getOrMakePartCustomization(accessor).scale);
                }
            });

            ret.set("setScale", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    VanillaModelPartCustomization customization = targetScript.getOrMakePartCustomization(accessor);
                    customization.scale = LuaVector.checkOrNew(arg1).asV3f();
                    return NIL;
                }
            });

            ret.set("getEnabled", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    VanillaModelPartCustomization customization = targetScript.getOrMakePartCustomization(accessor);

                    if (customization != null && customization.visible != null)
                        return LuaBoolean.valueOf(customization.visible);

                    return NIL;
                }
            });

            ret.set("setEnabled", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    VanillaModelPartCustomization customization = targetScript.getOrMakePartCustomization(accessor);

                    if (arg.isnil()) {
                        customization.visible = null;
                        return NIL;
                    }

                    customization.visible = arg.checkboolean();

                    return NIL;
                }
            });

            return ret;
        }
    }

}
