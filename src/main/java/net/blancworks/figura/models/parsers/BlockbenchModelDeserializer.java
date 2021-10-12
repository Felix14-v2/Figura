package net.blancworks.figura.models.parsers;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.blancworks.figura.LocalPlayerData;
import net.blancworks.figura.models.CustomModel;
import net.blancworks.figura.models.CustomModelPart;
import net.blancworks.figura.models.CustomModelPartCuboid;
import net.blancworks.figura.models.CustomModelPartMesh;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.nbt.*;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class BlockbenchModelDeserializer implements JsonDeserializer<CustomModel> {

    public static boolean overrideAsPlayerModel = false;

    private static final Map<String, CustomModelPart.ParentType> NAME_PARENT_TYPE_TAGS =
            new ImmutableMap.Builder<String, CustomModelPart.ParentType>()
                    .put("HEAD", CustomModelPart.ParentType.Head)
                    .put("TORSO", CustomModelPart.ParentType.Torso)
                    .put("LEFT_ARM", CustomModelPart.ParentType.LeftArm)
                    .put("RIGHT_ARM", CustomModelPart.ParentType.RightArm)
                    .put("LEFT_LEG", CustomModelPart.ParentType.LeftLeg)
                    .put("RIGHT_LEG", CustomModelPart.ParentType.RightLeg)
                    .put("NO_PARENT", CustomModelPart.ParentType.WORLD)
                    .put("LEFT_HELD_ITEM", CustomModelPart.ParentType.LeftItemOrigin)
                    .put("RIGHT_HELD_ITEM", CustomModelPart.ParentType.RightItemOrigin)
                    .put("LEFT_ELYTRA_ORIGIN", CustomModelPart.ParentType.LeftElytraOrigin)
                    .put("RIGHT_ELYTRA_ORIGIN", CustomModelPart.ParentType.RightElytraOrigin)
                    .put("LEFT_PARROT", CustomModelPart.ParentType.LeftParrotOrigin)
                    .put("RIGHT_PARROT", CustomModelPart.ParentType.RightParrotOrigin)
                    .put("LEFT_ELYTRA", CustomModelPart.ParentType.LeftElytra)
                    .put("RIGHT_ELYTRA", CustomModelPart.ParentType.RightElytra)
                    .put("LEFT_SPYGLASS", CustomModelPart.ParentType.LeftSpyglass)
                    .put("RIGHT_SPYGLASS", CustomModelPart.ParentType.RightSpyglass)
                    .put("CAMERA", CustomModelPart.ParentType.Camera)
                    .put("SKULL", CustomModelPart.ParentType.Skull)
                    .build();

    private static final Map<String, CustomModelPart.ParentType> NAME_MIMIC_TYPE_TAGS =
            new ImmutableMap.Builder<String, CustomModelPart.ParentType>()
                    .put("MIMIC_HEAD", CustomModelPart.ParentType.Head)
                    .put("MIMIC_TORSO", CustomModelPart.ParentType.Torso)
                    .put("MIMIC_LEFT_ARM", CustomModelPart.ParentType.LeftArm)
                    .put("MIMIC_RIGHT_ARM", CustomModelPart.ParentType.RightArm)
                    .put("MIMIC_LEFT_LEG", CustomModelPart.ParentType.LeftLeg)
                    .put("MIMIC_RIGHT_LEG", CustomModelPart.ParentType.RightLeg)
                    .build();

    static class PlayerSkinRemap {
        public CustomModelPart.ParentType parentType;
        public Vec3f offset;

        public PlayerSkinRemap(CustomModelPart.ParentType parentType, Vec3f offset) {
            this.parentType = parentType;
            this.offset = offset;
        }
    }

    private static final Map<String, PlayerSkinRemap> PLAYER_SKIN_REMAPS =
            new ImmutableMap.Builder<String, PlayerSkinRemap>()
                    .put("Head", new PlayerSkinRemap(CustomModelPart.ParentType.Head, new Vec3f(0, -24, 0)))
                    .put("Body", new PlayerSkinRemap(CustomModelPart.ParentType.Torso, new Vec3f(0, -24, 0)))
                    .put("RightArm", new PlayerSkinRemap(CustomModelPart.ParentType.RightArm, new Vec3f(-5, -22, 0)))
                    .put("LeftArm", new PlayerSkinRemap(CustomModelPart.ParentType.LeftArm, new Vec3f(5, -22, 0)))
                    .put("RightLeg", new PlayerSkinRemap(CustomModelPart.ParentType.RightLeg, new Vec3f(-2, -12, 0)))
                    .put("LeftLeg", new PlayerSkinRemap(CustomModelPart.ParentType.LeftLeg, new Vec3f(2, -12, 0)))
                    .build();
    
    @Override
    public CustomModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        CustomModel retModel = new CustomModel();

        JsonObject root = json.getAsJsonObject();
        JsonObject meta = root.get("meta").getAsJsonObject();
        String name = root.get("name").getAsString();
        JsonObject resolution = root.get("resolution").getAsJsonObject();
        JsonArray elements = root.get("elements").getAsJsonArray();
        JsonArray outliner = root.get("outliner").getAsJsonArray();
        JsonArray textures = root.get("textures").getAsJsonArray();

        if(meta.has("model_format") && meta.get("model_format").getAsString().equals("skin"))
            overrideAsPlayerModel = true;

        retModel.defaultTextureSize = new Vec2f(resolution.get("width").getAsFloat(), resolution.get("height").getAsFloat());

        Map<UUID, JsonObject> elementsByUuid = sortElements(elements);
        Map<UUID, CustomModelPart> parsedParts = new Object2ObjectOpenHashMap<>();

        //Parse out custom model parts from json objects.
        for (Map.Entry<UUID, JsonObject> entry : elementsByUuid.entrySet()) {
            UUID id = entry.getKey();
            JsonObject obj = entry.getValue();

            parsedParts.put(id, parseElement(obj, retModel));
        }

        for (JsonElement element : outliner) {
            if (element.isJsonObject()) {
                //If the element is a json object, it's a group, so parse the group.
                buildGroup(element.getAsJsonObject(), retModel, parsedParts, null, new Vec3f());
            } else {
                //If the element is a string, it's an element, so just add it to the children.
                String s = element.getAsString();

                if (s != null)
                    retModel.allParts.add(parsedParts.get(UUID.fromString(s)));
            }
        }

        //Reset this value.
        overrideAsPlayerModel = false;
        retModel.sortAllParts();
        return retModel;
    }

    //Builds out a group from a JsonObject that specifies the group in the outline.
    public void buildGroup(JsonObject group, CustomModel target, Map<UUID, CustomModelPart> allParts, CustomModelPart parent, Vec3f playerModelOffset) {
        CustomModelPart groupPart = new CustomModelPart();
        
        if (group.has("name")) {
            groupPart.name = group.get("name").getAsString();

            if (groupPart.name.startsWith("MESH_")) {
                Path meshFilePath = LocalPlayerData.getContentDirectory().resolve(groupPart.name.substring(5) + ".obj");

                if (Files.exists(meshFilePath)) {
                    groupPart = CustomModelPartMesh.loadFromObj(meshFilePath);
                    groupPart.name = group.get("name").getAsString();
                }
            }

            groupPart.parentType = CustomModelPart.ParentType.Model;
            //Find parent type.

            for (Map.Entry<String, CustomModelPart.ParentType> entry : NAME_MIMIC_TYPE_TAGS.entrySet()) {
                if (groupPart.name.contains(entry.getKey())) {
                    groupPart.isMimicMode = true;
                    groupPart.parentType = entry.getValue();
                    break;
                }
            }

            //Only set group parent if not mimicing. We can't mimic and be parented.
            if (!groupPart.isMimicMode) {
                //Check for parent parts
                for (Map.Entry<String, CustomModelPart.ParentType> entry : NAME_PARENT_TYPE_TAGS.entrySet()) {
                    if (groupPart.name.contains(entry.getKey())) {
                        groupPart.parentType = entry.getValue();
                        break;
                    }
                }
                //Check for player model parts.
                if(overrideAsPlayerModel){
                    for (Map.Entry<String, PlayerSkinRemap> entry : PLAYER_SKIN_REMAPS.entrySet()) {
                        if (groupPart.name.contains(entry.getKey())) {
                            groupPart.parentType = entry.getValue().parentType;
                            playerModelOffset = entry.getValue().offset.copy();
                            break;
                        }
                    }
                }
            }
        }
        if (group.has("visibility")) groupPart.isHidden = !group.get("visibility").getAsBoolean();
        if (group.has("origin")) {
            Vec3f corrected = v3fFromJArray(group.get("origin").getAsJsonArray());
            corrected.set(corrected.getX(), corrected.getY(), -corrected.getZ());
            groupPart.pivot = playerModelOffset.copy();
            groupPart.pivot.add(corrected);
        }
        if (group.has("rotation")) groupPart.rot = v3fFromJArray(group.get("rotation").getAsJsonArray());

        JsonArray children = group.get("children").getAsJsonArray();

        for (JsonElement child : children) {
            if (child.isJsonObject()) {
                //If the element is a json object, it's a group, so parse the group.
                buildGroup(child.getAsJsonObject(), target, allParts, groupPart, playerModelOffset.copy());
            } else {
                //If the element is a string, it's an element, so just add it to the children.
                String s = child.getAsString();

                if (s != null) {
                    CustomModelPart part = allParts.get(UUID.fromString(s));
                    groupPart.children.add(part);
                    
                    if(part != null){
                        part.applyTrueOffset(playerModelOffset.copy());
                    }
                }
            }
        }

        //Add part.
        if (parent == null)
            target.allParts.add(groupPart);
        else {
            parent.children.add(groupPart);
        }
    }

    public CustomModelPart parseElement(JsonObject elementObject, CustomModel target) {
        CustomModelPartCuboid elementPart = new CustomModelPartCuboid();

        if (elementObject.has("name")) {
            elementPart.name = elementObject.get("name").getAsString();
        }
        if (elementObject.has("visibility")) elementPart.isHidden = !elementObject.get("visibility").getAsBoolean();

        Vec3f from = v3fFromJArray(elementObject.get("from").getAsJsonArray());
        Vec3f to = v3fFromJArray(elementObject.get("to").getAsJsonArray());
        if (elementObject.has("origin")) {
            Vec3f corrected = v3fFromJArray(elementObject.get("origin").getAsJsonArray());
            corrected.set(corrected.getX(), corrected.getY(), -corrected.getZ());
            elementPart.pivot = corrected;
        }
        if (elementObject.has("rotation")) {
            Vec3f corrected = v3fFromJArray(elementObject.get("rotation").getAsJsonArray());
            corrected.set(corrected.getX(), corrected.getY(), corrected.getZ());

            elementPart.rot = corrected;
        }

        elementPart.texSize = target.defaultTextureSize;

        JsonObject facesObject = elementObject.get("faces").getAsJsonObject();

        NbtCompound cuboidPropertiesTag = new NbtCompound();

        if (elementObject.has("inflate"))
            cuboidPropertiesTag.put("inf", NbtFloat.of(elementObject.get("inflate").getAsFloat()));

        cuboidPropertiesTag.put("f", new NbtList() {{
            add(NbtFloat.of(from.getX()));
            add(NbtFloat.of(from.getY()));
            add(NbtFloat.of(from.getZ()));
        }});

        cuboidPropertiesTag.put("t", new NbtList() {{
            add(NbtFloat.of(to.getX()));
            add(NbtFloat.of(to.getY()));
            add(NbtFloat.of(to.getZ()));
        }});

        cuboidPropertiesTag.put("tw", NbtFloat.of(target.defaultTextureSize.x));
        cuboidPropertiesTag.put("th", NbtFloat.of(target.defaultTextureSize.y));

        cuboidPropertiesTag.put("n", getNbtElementFromJsonElement(facesObject.get("north")));
        cuboidPropertiesTag.put("s", getNbtElementFromJsonElement(facesObject.get("south")));
        cuboidPropertiesTag.put("e", getNbtElementFromJsonElement(facesObject.get("east")));
        cuboidPropertiesTag.put("w", getNbtElementFromJsonElement(facesObject.get("west")));
        cuboidPropertiesTag.put("u", getNbtElementFromJsonElement(facesObject.get("up")));
        cuboidPropertiesTag.put("d", getNbtElementFromJsonElement(facesObject.get("down")));

        elementPart.cuboidProperties = cuboidPropertiesTag;
        elementPart.rebuild();

        return elementPart;
    }

    public Vec3f v3fFromJArray(JsonArray array) {
        return new Vec3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    public Vector4f v4fFromJArray(JsonArray array) {
        return new Vector4f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat());
    }

    public NbtCompound jsonObjectToNbt(JsonObject obj) {
        return new NbtCompound() {{
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                JsonElement element = entry.getValue();

                if (element.isJsonNull())
                    continue;

                String key = entry.getKey();
                put(key, getNbtElementFromJsonElement(element));
            }
        }};
    }

    public NbtList jsonArrayToNbtList(JsonArray array) {
        return new NbtList() {{
            for (JsonElement element : array) {
                add(getNbtElementFromJsonElement(element));
            }
        }};
    }

    public NbtElement getNbtElementFromJsonElement(JsonElement element) {
        if (element instanceof JsonArray)
            return this.jsonArrayToNbtList(element.getAsJsonArray());
        else if (element instanceof JsonObject)
            return this.jsonObjectToNbt(element.getAsJsonObject());
        else if (element instanceof JsonPrimitive) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean())
                return NbtByte.of(primitive.getAsBoolean());
            if (primitive.isNumber())
                return NbtFloat.of(primitive.getAsNumber().floatValue());
            if (primitive.isString())
                return NbtString.of(primitive.getAsString());
        }
        return null;
    }

    /**
     * Sorts out all the things in a json array out by UUID.
     */
    public Map<UUID, JsonObject> sortElements(JsonArray elementContainer) {
        Map<UUID, JsonObject> objects = new Object2ObjectOpenHashMap<>();
        for (JsonElement jsonElement : elementContainer) {
            if (!jsonElement.isJsonObject())
                continue;
            JsonObject obj = jsonElement.getAsJsonObject();

            if (!obj.has("uuid"))
                continue;
            objects.put(UUID.fromString(obj.get("uuid").getAsString()), obj);

            if (obj.has("children")) {
                JsonElement children = obj.get("children");
                if (children.isJsonArray()) {
                    JsonArray childrenArray = children.getAsJsonArray();
                    objects.putAll(sortElements(childrenArray));
                }
            }
        }
        return objects;
    }
}
