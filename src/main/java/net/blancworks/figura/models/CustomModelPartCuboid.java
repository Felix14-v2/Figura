package net.blancworks.figura.models;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class CustomModelPartCuboid extends CustomModelPart {

    //Used to store the data for a cuboid, so that we can re-build it later if need be.
    public NbtCompound cuboidProperties = new NbtCompound();

    @Override
    public void rebuild(Vec2f newTexSize) {
        if (newTexSize == null)
            newTexSize = new Vec2f(cuboidProperties.getFloat("tw"), cuboidProperties.getFloat("th"));

        super.rebuild(newTexSize);

        FloatList vertexData = new FloatArrayList();
        int vertexCount = 0;

        float inflate = 0;
        if (cuboidProperties.contains("inf")) inflate = cuboidProperties.getFloat("inf");

        Vec3f from = vec3fFromNbt((NbtList) cuboidProperties.get("f"));
        Vec3f to = vec3fFromNbt((NbtList) cuboidProperties.get("t"));
        Vec3f mid = new Vec3f(
                MathHelper.lerp(0.5f, from.getX(), to.getX()),
                MathHelper.lerp(0.5f, from.getY(), to.getY()),
                MathHelper.lerp(0.5f, from.getZ(), to.getZ())
        );

        from.subtract(mid);
        from.add(-inflate, -inflate, -inflate);
        from.add(mid);

        to.subtract(mid);
        to.add(inflate, inflate, inflate);
        to.add(mid);

        //North
        if (cuboidProperties.contains("n")) {
            NbtCompound faceData = (NbtCompound) cuboidProperties.get("n");

            if (faceData.contains("texture")) {
                Vector4f v = v4fFromNbtList((NbtList) faceData.get("uv"));

                uvData data = UVCustomizations.get(UV.NORTH);
                if (data == null) {
                    data = new uvData();
                    data.uvOffset = new Vec2f(v.getX(), v.getY());
                    data.uvSize = new Vec2f(v.getZ(), v.getW());

                    UVCustomizations.put(UV.NORTH, data);
                } else {
                    v.set(data.uvOffset.x, data.uvOffset.y, v.getZ(), v.getW());
                    v.set(v.getX(), v.getY(), data.uvSize.x, data.uvSize.y);
                }

                float rotation = 0;

                if (faceData.contains("rotation")) {
                    rotation = ((NbtFloat) faceData.get("rotation")).floatValue();
                }

                List<Vec2f> cornerUVs = rotateUV(v, rotation);

                generateFace(
                        new Vec3f(-from.getX(), -from.getY(), from.getZ()),
                        new Vec3f(-to.getX(), -from.getY(), from.getZ()),
                        new Vec3f(-to.getX(), -to.getY(), from.getZ()),
                        new Vec3f(-from.getX(), -to.getY(), from.getZ()),
                        cornerUVs,
                        texSize.x, texSize.y,
                        vertexData
                );
                vertexCount += 4;
            }
        }

        //South
        if (cuboidProperties.contains("s")) {
            NbtCompound faceData = (NbtCompound) cuboidProperties.get("s");

            if (faceData.contains("texture")) {
                Vector4f v = v4fFromNbtList((NbtList) faceData.get("uv"));

                uvData data = UVCustomizations.get(UV.SOUTH);
                if (data == null) {
                    data = new uvData();
                    data.uvOffset = new Vec2f(v.getX(), v.getY());
                    data.uvSize = new Vec2f(v.getZ(), v.getW());

                    UVCustomizations.put(UV.SOUTH, data);
                } else {
                    v.set(data.uvOffset.x, data.uvOffset.y, v.getZ(), v.getW());
                    v.set(v.getX(), v.getY(), data.uvSize.x, data.uvSize.y);
                }

                float rotation = 0;

                if (faceData.contains("rotation")) {
                    rotation = ((NbtFloat) faceData.get("rotation")).floatValue();
                }

                List<Vec2f> cornerUVs = rotateUV(v, rotation);

                generateFace(
                        new Vec3f(-to.getX(), -from.getY(), to.getZ()),
                        new Vec3f(-from.getX(), -from.getY(), to.getZ()),
                        new Vec3f(-from.getX(), -to.getY(), to.getZ()),
                        new Vec3f(-to.getX(), -to.getY(), to.getZ()),
                        cornerUVs,
                        texSize.x, texSize.y,
                        vertexData
                );
                vertexCount += 4;
            }
        }

        //East
        if (cuboidProperties.contains("e")) {
            NbtCompound faceData = (NbtCompound) cuboidProperties.get("e");

            if (faceData.contains("texture")) {
                Vector4f v = v4fFromNbtList((NbtList) faceData.get("uv"));

                uvData data = UVCustomizations.get(UV.EAST);
                if (data == null) {
                    data = new uvData();
                    data.uvOffset = new Vec2f(v.getX(), v.getY());
                    data.uvSize = new Vec2f(v.getZ(), v.getW());

                    UVCustomizations.put(UV.EAST, data);
                } else {
                    v.set(data.uvOffset.x, data.uvOffset.y, v.getZ(), v.getW());
                    v.set(v.getX(), v.getY(), data.uvSize.x, data.uvSize.y);
                }

                float rotation = 0;

                if (faceData.contains("rotation")) {
                    rotation = ((NbtFloat) faceData.get("rotation")).floatValue();
                }

                List<Vec2f> cornerUVs = rotateUV(v, rotation);

                generateFace(
                        new Vec3f(-to.getX(), -from.getY(), from.getZ()),
                        new Vec3f(-to.getX(), -from.getY(), to.getZ()),
                        new Vec3f(-to.getX(), -to.getY(), to.getZ()),
                        new Vec3f(-to.getX(), -to.getY(), from.getZ()),
                        cornerUVs,
                        texSize.x, texSize.y,
                        vertexData
                );
                vertexCount += 4;
            }
        }

        //West
        if (cuboidProperties.contains("w")) {
            NbtCompound faceData = (NbtCompound) cuboidProperties.get("w");

            if (faceData.contains("texture")) {
                Vector4f v = v4fFromNbtList((NbtList) faceData.get("uv"));

                uvData data = UVCustomizations.get(UV.WEST);
                if (data == null) {
                    data = new uvData();
                    data.uvOffset = new Vec2f(v.getX(), v.getY());
                    data.uvSize = new Vec2f(v.getZ(), v.getW());

                    UVCustomizations.put(UV.WEST, data);
                } else {
                    v.set(data.uvOffset.x, data.uvOffset.y, v.getZ(), v.getW());
                    v.set(v.getX(), v.getY(), data.uvSize.x, data.uvSize.y);
                }

                float rotation = 0;

                if (faceData.contains("rotation")) {
                    rotation = ((NbtFloat) faceData.get("rotation")).floatValue();
                }

                List<Vec2f> cornerUVs = rotateUV(v, rotation);

                generateFace(
                        new Vec3f(-from.getX(), -from.getY(), to.getZ()),
                        new Vec3f(-from.getX(), -from.getY(), from.getZ()),
                        new Vec3f(-from.getX(), -to.getY(), from.getZ()),
                        new Vec3f(-from.getX(), -to.getY(), to.getZ()),
                        cornerUVs,
                        texSize.x, texSize.y,
                        vertexData
                );
                vertexCount += 4;
            }
        }

        //Top
        if (cuboidProperties.contains("u")) {
            NbtCompound faceData = (NbtCompound) cuboidProperties.get("u");

            if (faceData.contains("texture")) {
                Vector4f v = v4fFromNbtList((NbtList) faceData.get("uv"));

                uvData data = UVCustomizations.get(UV.UP);
                if (data == null) {
                    data = new uvData();
                    data.uvOffset = new Vec2f(v.getX(), v.getY());
                    data.uvSize = new Vec2f(v.getZ(), v.getW());

                    UVCustomizations.put(UV.UP, data);
                } else {
                    v.set(data.uvOffset.x, data.uvOffset.y, v.getZ(), v.getW());
                    v.set(v.getX(), v.getY(), data.uvSize.x, data.uvSize.y);
                }

                float rotation = 0;

                if (faceData.contains("rotation")) {
                    rotation = ((NbtFloat) faceData.get("rotation")).floatValue();
                }

                List<Vec2f> cornerUVs = rotateUV(v, rotation);

                generateFace(
                        new Vec3f(-to.getX(), -to.getY(), to.getZ()),
                        new Vec3f(-from.getX(), -to.getY(), to.getZ()),
                        new Vec3f(-from.getX(), -to.getY(), from.getZ()),
                        new Vec3f(-to.getX(), -to.getY(), from.getZ()),
                        cornerUVs,
                        texSize.x, texSize.y,
                        vertexData
                );
                vertexCount += 4;
            }
        }

        //Bottom
        if (cuboidProperties.contains("d")) {
            NbtCompound faceData = (NbtCompound) cuboidProperties.get("d");

            if (faceData.contains("texture")) {
                Vector4f v = v4fFromNbtList((NbtList) faceData.get("uv"));

                uvData data = UVCustomizations.get(UV.DOWN);
                if (data == null) {
                    data = new uvData();
                    data.uvOffset = new Vec2f(v.getX(), v.getY());
                    data.uvSize = new Vec2f(v.getZ(), v.getW());

                    UVCustomizations.put(UV.DOWN, data);
                } else {
                    v.set(data.uvOffset.x, data.uvOffset.y, v.getZ(), v.getW());
                    v.set(v.getX(), v.getY(), data.uvSize.x, data.uvSize.y);
                }

                float rotation = 0;

                if (faceData.contains("rotation")) {
                    rotation = ((NbtFloat) faceData.get("rotation")).floatValue();
                }

                List<Vec2f> cornerUVs = rotateUV(v, rotation);

                generateFace(
                        new Vec3f(-to.getX(), -from.getY(), from.getZ()),
                        new Vec3f(-from.getX(), -from.getY(), from.getZ()),
                        new Vec3f(-from.getX(), -from.getY(), to.getZ()),
                        new Vec3f(-to.getX(), -from.getY(), to.getZ()),
                        cornerUVs,
                        texSize.x, texSize.y,
                        vertexData
                );
                vertexCount += 4;
            }
        }

        this.vertexData = vertexData;
        this.vertexCount = vertexCount;
    }

    public void generateFace(Vec3f a, Vec3f b, Vec3f c, Vec3f d, List<Vec2f> uv, float texWidth, float texHeight, FloatList vertexData) {
        Vec3f nA = b.copy();
        nA.subtract(a);
        Vec3f nB = c.copy();
        nB.subtract(a);
        nA.cross(nB);
        nA.normalize();

        addVertex(b, uv.get(0).x / texWidth, uv.get(0).y / texHeight, nA, vertexData);
        addVertex(a, uv.get(1).x / texWidth, uv.get(1).y / texHeight, nA, vertexData);
        addVertex(d, uv.get(2).x / texWidth, uv.get(2).y / texHeight, nA, vertexData);
        addVertex(c, uv.get(3).x / texWidth, uv.get(3).y / texHeight, nA, vertexData);
    }

    @Override
    public void readNbt(NbtCompound partNbt) {
        super.readNbt(partNbt);
        this.cuboidProperties = (NbtCompound) partNbt.get("props");
    }

    @Override
    public PartType getPartType() {
        return PartType.CUBE;
    }

    public static List<Vec2f> rotateUV(Vector4f v, float rotation) {
        List<Vec2f> cornerUVs = new ArrayList<>();
        cornerUVs.add(new Vec2f(v.getX(), v.getW())); //0,1
        cornerUVs.add(new Vec2f(v.getZ(), v.getW())); //1,1
        cornerUVs.add(new Vec2f(v.getZ(), v.getY())); //1,0
        cornerUVs.add(new Vec2f(v.getX(), v.getY())); //0,0

        int rotationAmount = Math.round(rotation / 90.0f);

        for (int i = 0; i < rotationAmount; i++) {
            Vec2f last = cornerUVs.get(0);
            cornerUVs.remove(0);
            cornerUVs.add(last);
        }

        return cornerUVs;
    }
}
